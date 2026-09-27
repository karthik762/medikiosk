package com.medikiosk.healthlake.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.medikiosk.abdm.dto.IntegrationRecordDto;
import com.medikiosk.dto.fhir.FhirValidationResponseDto;
import com.medikiosk.entity.AuditLog;
import com.medikiosk.entity.ClinicalSummary;
import com.medikiosk.entity.IntakeSession;
import com.medikiosk.entity.IntegrationRecord;
import com.medikiosk.entity.Patient;
import com.medikiosk.entity.User;
import com.medikiosk.entity.enums.ClinicalSummaryStatus;
import com.medikiosk.entity.enums.IntegrationStatus;
import com.medikiosk.entity.enums.IntegrationType;
import com.medikiosk.entity.enums.UserRole;
import com.medikiosk.exception.ApiException;
import com.medikiosk.fhir.FhirMappingService;
import com.medikiosk.healthlake.config.HealthLakeProperties;
import com.medikiosk.healthlake.dto.HealthLakeExportRequestDto;
import com.medikiosk.healthlake.dto.HealthLakeExportResponseDto;
import com.medikiosk.healthlake.dto.HealthLakeStatusDto;
import com.medikiosk.healthlake.provider.HealthLakeExportResult;
import com.medikiosk.healthlake.provider.HealthLakeIntegrationProvider;
import com.medikiosk.healthlake.service.HealthLakeService;
import com.medikiosk.repository.AuditLogRepository;
import com.medikiosk.repository.ClinicalSummaryRepository;
import com.medikiosk.repository.IntakeSessionRepository;
import com.medikiosk.repository.IntegrationRecordRepository;
import com.medikiosk.repository.PatientRepository;
import com.medikiosk.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Implementation of AWS HealthLake FHIR integration workflows.
 * Enforces doctor-verification gating, patient reference separation,
 * FHIR bundle validation, deterministic idempotency, and comprehensive audit logging.
 * Conforms to docs/HEALTHLAKE_SPEC.md, docs/SECURITY.md, and Phase 14 requirements.
 */
@Service
public class HealthLakeServiceImpl implements HealthLakeService {

    private static final Logger log = LoggerFactory.getLogger(HealthLakeServiceImpl.class);

    private final HealthLakeProperties properties;
    private final HealthLakeIntegrationProvider mockProvider;
    private final HealthLakeIntegrationProvider awsProvider;
    private final PatientRepository patientRepository;
    private final IntakeSessionRepository intakeSessionRepository;
    private final ClinicalSummaryRepository clinicalSummaryRepository;
    private final IntegrationRecordRepository integrationRecordRepository;
    private final FhirMappingService fhirMappingService;
    private final AuditLogRepository auditLogRepository;
    private final UserRepository userRepository;
    private final ObjectMapper objectMapper;

    public HealthLakeServiceImpl(
            HealthLakeProperties properties,
            @Qualifier("mockHealthLakeProvider") HealthLakeIntegrationProvider mockProvider,
            @Qualifier("awsHealthLakeProvider") HealthLakeIntegrationProvider awsProvider,
            PatientRepository patientRepository,
            IntakeSessionRepository intakeSessionRepository,
            ClinicalSummaryRepository clinicalSummaryRepository,
            IntegrationRecordRepository integrationRecordRepository,
            FhirMappingService fhirMappingService,
            AuditLogRepository auditLogRepository,
            UserRepository userRepository,
            ObjectMapper objectMapper) {
        this.properties = properties;
        this.mockProvider = mockProvider;
        this.awsProvider = awsProvider;
        this.patientRepository = patientRepository;
        this.intakeSessionRepository = intakeSessionRepository;
        this.clinicalSummaryRepository = clinicalSummaryRepository;
        this.integrationRecordRepository = integrationRecordRepository;
        this.fhirMappingService = fhirMappingService;
        this.auditLogRepository = auditLogRepository;
        this.userRepository = userRepository;
        this.objectMapper = objectMapper;
    }

    private HealthLakeIntegrationProvider getActiveProvider() {
        if (!properties.isMock() && properties.hasCredentials()) {
            return awsProvider;
        }
        return mockProvider;
    }

    @Override
    public HealthLakeStatusDto getStatus() {
        return getActiveProvider().getStatus();
    }

    @Override
    @Transactional
    public HealthLakeExportResponseDto exportClinicalData(UUID sessionId, HealthLakeExportRequestDto request, UUID authenticatedDoctorId) {
        User doctor = getAuthenticatedUser(authenticatedDoctorId);
        if (doctor.getRole() != UserRole.DOCTOR && doctor.getRole() != UserRole.ADMIN) {
            throw new ApiException("AWS HealthLake clinical export requires DOCTOR or ADMIN role", HttpStatus.FORBIDDEN);
        }

        IntakeSession session = intakeSessionRepository.findById(sessionId)
                .orElseThrow(() -> new ApiException("Intake session not found", HttpStatus.NOT_FOUND));
        Patient patient = session.getPatient();

        // 1. DOCTOR VERIFICATION BOUNDARY CHECK (CRITICAL INVARIANT)
        ClinicalSummary summary = clinicalSummaryRepository.findBySessionId(sessionId).orElse(null);
        if (summary == null || summary.getStatus() != ClinicalSummaryStatus.VERIFIED) {
            log.warn("Blocked AWS HealthLake export: Session {} summary missing or status is {}", sessionId, summary != null ? summary.getStatus() : "NONE");
            return HealthLakeExportResponseDto.builder()
                    .sessionId(sessionId)
                    .patientId(patient.getId())
                    .status(IntegrationStatus.FAILED)
                    .errorMessage("HealthLake export rejected: Clinical summary must be verified by a licensed doctor prior to cloud transmission")
                    .message("HealthLake export rejected: Clinical summary must be verified by a licensed doctor prior to cloud transmission")
                    .build();
        }

        // 2. FHIR BUNDLE VALIDATION CHECK
        FhirValidationResponseDto validation = fhirMappingService.validateBundleForSession(sessionId);
        if (!validation.isValid()) {
            log.warn("Blocked AWS HealthLake export: Session {} FHIR validation failed: {}", sessionId, validation.getErrors());
            return HealthLakeExportResponseDto.builder()
                    .sessionId(sessionId)
                    .patientId(patient.getId())
                    .status(IntegrationStatus.FAILED)
                    .errorMessage("HealthLake export rejected: FHIR bundle validation failed: " + String.join("; ", validation.getErrors()))
                    .message("HealthLake export rejected: FHIR bundle validation failed: " + String.join("; ", validation.getErrors()))
                    .build();
        }

        // 3. IDEMPOTENCY CHECK
        String idempotencyKey = (request != null && request.getIdempotencyKey() != null && !request.getIdempotencyKey().isBlank())
                ? request.getIdempotencyKey().trim()
                : "HEALTHLAKE-EXPORT-" + sessionId;

        Optional<IntegrationRecord> existingRecord = integrationRecordRepository.findByIdempotencyKey(idempotencyKey);
        if (existingRecord.isPresent() && existingRecord.get().getStatus() == IntegrationStatus.SUCCESS) {
            IntegrationRecord rec = existingRecord.get();
            log.info("Idempotent replay for AWS HealthLake export key {}", idempotencyKey);
            return HealthLakeExportResponseDto.builder()
                    .integrationRecordId(rec.getId())
                    .sessionId(sessionId)
                    .patientId(patient.getId())
                    .status(rec.getStatus())
                    .healthlakeRef(patient.getHealthlakeRef())
                    .bundleId("Bundle-" + sessionId)
                    .datastoreId(properties.getDatastoreId())
                    .idempotencyKey(idempotencyKey)
                    .isMock(getActiveProvider().getStatus().isMock())
                    .attemptedAt(rec.getAttemptedAt())
                    .completedAt(rec.getCompletedAt())
                    .errorMessage(null)
                    .message("Idempotent response: Record already successfully transmitted to AWS HealthLake")
                    .build();
        }

        // 4. PREPARE TRANSMISSION
        String datastoreId = (request != null && request.getDatastoreId() != null && !request.getDatastoreId().isBlank())
                ? request.getDatastoreId().trim()
                : properties.getDatastoreId();

        String effectiveHealthLakeRef = patient.getHealthlakeRef();
        if (request != null && request.getHealthlakeRef() != null && !request.getHealthlakeRef().isBlank()) {
            effectiveHealthLakeRef = request.getHealthlakeRef().trim();
            if (patient.getHealthlakeRef() == null) {
                patient.setHealthlakeRef(effectiveHealthLakeRef);
                patientRepository.save(patient);
            }
        }

        String fhirBundleJson = fhirMappingService.toFhirClinicalBundle(sessionId);
        HealthLakeIntegrationProvider provider = getActiveProvider();

        IntegrationRecord record = existingRecord.orElse(
                new IntegrationRecord(IntegrationType.HEALTHLAKE, "HEALTHLAKE_CLINICAL_EXPORT", patient, session)
        );
        record.setIdempotencyKey(idempotencyKey);
        record.setStatus(IntegrationStatus.PENDING);
        record.setAttemptedAt(Instant.now());
        record.setAttemptCount(record.getAttemptCount() != null ? record.getAttemptCount() + 1 : 1);
        record = integrationRecordRepository.save(record);

        logAuditEvent(authenticatedDoctorId, "HEALTHLAKE_REQUESTED", "IntakeSession", sessionId.toString(),
                Map.of("idempotencyKey", idempotencyKey, "bundleId", "Bundle-" + sessionId, "datastoreId", datastoreId != null ? datastoreId : "DEFAULT"));

        HealthLakeExportResult result = provider.exportClinicalBundle(
                fhirBundleJson,
                effectiveHealthLakeRef,
                datastoreId,
                idempotencyKey
        );

        if (result.success()) {
            record.setStatus(IntegrationStatus.SUCCESS);
            record.setExternalReference(result.externalReference());
            record.setResponsePayload(result.responsePayload());
            record.setCompletedAt(Instant.now());
            record.setErrorMessage(null);
            integrationRecordRepository.save(record);

            // If patient had no HealthLake reference, assign deterministic reference
            if (patient.getHealthlakeRef() == null) {
                String assignedRef = (effectiveHealthLakeRef != null) ? effectiveHealthLakeRef : "HL-PAT-" + patient.getId().toString().substring(0, 8);
                patient.setHealthlakeRef(assignedRef);
                patientRepository.save(patient);
                effectiveHealthLakeRef = assignedRef;
            }

            logAuditEvent(authenticatedDoctorId, "HEALTHLAKE_SUCCEEDED", "IntakeSession", sessionId.toString(),
                    Map.of("externalReference", result.externalReference(), "isMock", provider.getStatus().isMock(), "datastoreId", datastoreId != null ? datastoreId : "DEFAULT"));

            return HealthLakeExportResponseDto.builder()
                    .integrationRecordId(record.getId())
                    .sessionId(sessionId)
                    .patientId(patient.getId())
                    .status(IntegrationStatus.SUCCESS)
                    .healthlakeRef(effectiveHealthLakeRef)
                    .bundleId("Bundle-" + sessionId)
                    .datastoreId(datastoreId)
                    .idempotencyKey(idempotencyKey)
                    .isMock(provider.getStatus().isMock())
                    .attemptedAt(record.getAttemptedAt())
                    .completedAt(record.getCompletedAt())
                    .errorMessage(null)
                    .message("Verified clinical summary and FHIR bundle exported to AWS HealthLake successfully")
                    .build();
        } else {
            record.setStatus(IntegrationStatus.FAILED);
            record.setErrorMessage(result.errorMessage());
            record.setCompletedAt(Instant.now());
            integrationRecordRepository.save(record);

            logAuditEvent(authenticatedDoctorId, "HEALTHLAKE_FAILED", "IntakeSession", sessionId.toString(),
                    Map.of("error", result.errorMessage(), "isMock", provider.getStatus().isMock()));

            return HealthLakeExportResponseDto.builder()
                    .integrationRecordId(record.getId())
                    .sessionId(sessionId)
                    .patientId(patient.getId())
                    .status(IntegrationStatus.FAILED)
                    .healthlakeRef(effectiveHealthLakeRef)
                    .bundleId("Bundle-" + sessionId)
                    .datastoreId(datastoreId)
                    .idempotencyKey(idempotencyKey)
                    .isMock(provider.getStatus().isMock())
                    .attemptedAt(record.getAttemptedAt())
                    .completedAt(record.getCompletedAt())
                    .errorMessage(result.errorMessage())
                    .message("AWS HealthLake export failed: " + result.errorMessage())
                    .build();
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<IntegrationRecordDto> getSessionIntegrationHistory(UUID sessionId, UUID authenticatedUserId) {
        IntakeSession session = intakeSessionRepository.findById(sessionId)
                .orElseThrow(() -> new ApiException("Intake session not found", HttpStatus.NOT_FOUND));

        User user = getAuthenticatedUser(authenticatedUserId);
        validatePatientAccess(session.getPatient(), user);

        List<IntegrationRecord> records = integrationRecordRepository.findBySessionIdAndIntegrationType(sessionId, IntegrationType.HEALTHLAKE);
        return records.stream()
                .map(IntegrationRecordDto::fromEntity)
                .collect(Collectors.toList());
    }

    private User getAuthenticatedUser(UUID userId) {
        if (userId == null) {
            throw new ApiException("Authentication required", HttpStatus.UNAUTHORIZED);
        }
        return userRepository.findById(userId)
                .orElseThrow(() -> new ApiException("Authenticated user not found", HttpStatus.UNAUTHORIZED));
    }

    private void validatePatientAccess(Patient patient, User user) {
        if (user.getRole() == UserRole.ADMIN || user.getRole() == UserRole.DOCTOR) {
            return;
        }
        if (user.getRole() == UserRole.PATIENT) {
            if (patient.getUserId() == null || !patient.getUserId().equals(user.getId())) {
                throw new ApiException("Access restricted to own patient records", HttpStatus.FORBIDDEN);
            }
            return;
        }
        throw new ApiException("Access restricted", HttpStatus.FORBIDDEN);
    }

    private void logAuditEvent(UUID userId, String action, String entityType, String entityId, Map<String, Object> metadata) {
        try {
            String metaJson = metadata != null ? objectMapper.writeValueAsString(metadata) : "{}";
            UUID entUuid = null;
            try {
                if (entityId != null) {
                    entUuid = UUID.fromString(entityId);
                }
            } catch (Exception ignored) {
            }
            AuditLog auditLog = new AuditLog(userId, action, entityType, entUuid, metaJson);
            auditLogRepository.save(auditLog);
        } catch (Exception e) {
            log.warn("Failed to write HealthLake audit log for action {}: {}", action, e.getMessage());
        }
    }
}
