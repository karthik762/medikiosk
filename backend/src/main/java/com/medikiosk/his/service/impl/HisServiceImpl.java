package com.medikiosk.his.service.impl;

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
import com.medikiosk.his.config.HisProperties;
import com.medikiosk.his.dto.HisExportRequestDto;
import com.medikiosk.his.dto.HisExportResponseDto;
import com.medikiosk.his.dto.HisLinkPatientRequestDto;
import com.medikiosk.his.dto.HisLinkPatientResponseDto;
import com.medikiosk.his.dto.HisPatientCandidateDto;
import com.medikiosk.his.dto.HisPatientSearchResponseDto;
import com.medikiosk.his.dto.HisStatusDto;
import com.medikiosk.his.provider.HisExportResult;
import com.medikiosk.his.provider.HisIntegrationProvider;
import com.medikiosk.his.service.HisService;
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
 * Implementation of Hospital Information System (HIS) integration workflows.
 * Enforces doctor-verification gating, patient identifier separation, standards-compliant
 * FHIR bundle validation, deterministic idempotency, and comprehensive audit logging.
 * Conforms to docs/HIS_SPEC.md, docs/SECURITY.md, and Phase 13 requirements.
 */
@Service
public class HisServiceImpl implements HisService {

    private static final Logger log = LoggerFactory.getLogger(HisServiceImpl.class);

    private final HisProperties properties;
    private final HisIntegrationProvider mockProvider;
    private final HisIntegrationProvider hospitalProvider;
    private final PatientRepository patientRepository;
    private final IntakeSessionRepository intakeSessionRepository;
    private final ClinicalSummaryRepository clinicalSummaryRepository;
    private final IntegrationRecordRepository integrationRecordRepository;
    private final FhirMappingService fhirMappingService;
    private final AuditLogRepository auditLogRepository;
    private final UserRepository userRepository;
    private final ObjectMapper objectMapper;

    public HisServiceImpl(
            HisProperties properties,
            @Qualifier("mockHisProvider") HisIntegrationProvider mockProvider,
            @Qualifier("hospitalHisProvider") HisIntegrationProvider hospitalProvider,
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
        this.hospitalProvider = hospitalProvider;
        this.patientRepository = patientRepository;
        this.intakeSessionRepository = intakeSessionRepository;
        this.clinicalSummaryRepository = clinicalSummaryRepository;
        this.integrationRecordRepository = integrationRecordRepository;
        this.fhirMappingService = fhirMappingService;
        this.auditLogRepository = auditLogRepository;
        this.userRepository = userRepository;
        this.objectMapper = objectMapper;
    }

    private HisIntegrationProvider getActiveProvider() {
        if (!properties.isMock() && properties.hasCredentials()) {
            return hospitalProvider;
        }
        return mockProvider;
    }

    @Override
    public HisStatusDto getStatus() {
        return getActiveProvider().getStatus();
    }

    @Override
    @Transactional
    public HisExportResponseDto exportClinicalData(UUID sessionId, HisExportRequestDto request, UUID authenticatedDoctorId) {
        User doctor = getAuthenticatedUser(authenticatedDoctorId);
        if (doctor.getRole() != UserRole.DOCTOR && doctor.getRole() != UserRole.ADMIN) {
            throw new ApiException("HIS clinical export requires DOCTOR or ADMIN role", HttpStatus.FORBIDDEN);
        }

        IntakeSession session = intakeSessionRepository.findById(sessionId)
                .orElseThrow(() -> new ApiException("Intake session not found", HttpStatus.NOT_FOUND));
        Patient patient = session.getPatient();

        // 1. DOCTOR VERIFICATION BOUNDARY CHECK (CRITICAL INVARIANT)
        ClinicalSummary summary = clinicalSummaryRepository.findBySessionId(sessionId)
                .orElseThrow(() -> new ApiException("Clinical summary not found for session", HttpStatus.NOT_FOUND));

        if (summary == null || summary.getStatus() != ClinicalSummaryStatus.VERIFIED) {
            log.warn("Blocked HIS export: Session {} summary missing or status is {}", sessionId, summary != null ? summary.getStatus() : "NONE");
            return HisExportResponseDto.builder()
                    .sessionId(sessionId)
                    .patientId(patient.getId())
                    .status(IntegrationStatus.FAILED)
                    .errorMessage("HIS export rejected: Clinical summary must be verified by a licensed doctor prior to hospital transmission")
                    .message("HIS export rejected: Clinical summary must be verified by a licensed doctor prior to hospital transmission")
                    .build();
        }

        // 2. FHIR BUNDLE VALIDATION CHECK
        FhirValidationResponseDto validation = fhirMappingService.validateBundleForSession(sessionId);
        if (!validation.isValid()) {
            log.warn("Blocked HIS export: Session {} FHIR validation failed: {}", sessionId, validation.getErrors());
            return HisExportResponseDto.builder()
                    .sessionId(sessionId)
                    .patientId(patient.getId())
                    .status(IntegrationStatus.FAILED)
                    .errorMessage("HIS export rejected: FHIR bundle validation failed: " + String.join("; ", validation.getErrors()))
                    .message("HIS export rejected: FHIR bundle validation failed: " + String.join("; ", validation.getErrors()))
                    .build();
        }

        // 3. IDEMPOTENCY CHECK
        String idempotencyKey = (request != null && request.getIdempotencyKey() != null && !request.getIdempotencyKey().isBlank())
                ? request.getIdempotencyKey().trim()
                : "HIS-EXPORT-" + sessionId;

        Optional<IntegrationRecord> existingRecord = integrationRecordRepository.findByIdempotencyKey(idempotencyKey);
        if (existingRecord.isPresent() && existingRecord.get().getStatus() == IntegrationStatus.SUCCESS) {
            IntegrationRecord rec = existingRecord.get();
            log.info("Idempotent replay for HIS export key {}", idempotencyKey);
            return HisExportResponseDto.builder()
                    .integrationRecordId(rec.getId())
                    .sessionId(sessionId)
                    .patientId(patient.getId())
                    .status(rec.getStatus())
                    .encounterReference(rec.getExternalReference())
                    .bundleId("Bundle-" + sessionId)
                    .hisId(patient.getHisId())
                    .intakeQueue(request != null && request.getIntakeQueue() != null ? request.getIntakeQueue() : "OPD_INTAKE")
                    .idempotencyKey(idempotencyKey)
                    .isMock(getActiveProvider().getStatus().isMock())
                    .attemptedAt(rec.getAttemptedAt())
                    .completedAt(rec.getCompletedAt())
                    .errorMessage(null)
                    .message("Idempotent response: Record already successfully transmitted to HIS")
                    .build();
        }

        // 4. PREPARE TRANSMISSION
        String intakeQueue = (request != null && request.getIntakeQueue() != null && !request.getIntakeQueue().isBlank())
                ? request.getIntakeQueue().trim()
                : "OPD_INTAKE";

        String effectiveHisId = patient.getHisId();
        if (request != null && request.getHisId() != null && !request.getHisId().isBlank()) {
            effectiveHisId = request.getHisId().trim();
            // If patient had no HIS ID, associate it safely
            if (patient.getHisId() == null) {
                patient.setHisId(effectiveHisId);
                patientRepository.save(patient);
            }
        }

        String fhirBundleJson = fhirMappingService.toFhirClinicalBundle(sessionId);
        HisIntegrationProvider provider = getActiveProvider();

        IntegrationRecord record = existingRecord.orElse(
                new IntegrationRecord(IntegrationType.HIS, "HIS_CLINICAL_EXPORT", patient, session)
        );
        record.setIdempotencyKey(idempotencyKey);
        record.setStatus(IntegrationStatus.PENDING);
        record.setAttemptedAt(Instant.now());
        record.setAttemptCount(record.getAttemptCount() != null ? record.getAttemptCount() + 1 : 1);
        record = integrationRecordRepository.save(record);

        logAuditEvent(authenticatedDoctorId, "HIS_REQUESTED", "IntakeSession", sessionId.toString(),
                Map.of("idempotencyKey", idempotencyKey, "bundleId", "Bundle-" + sessionId, "queue", intakeQueue));

        HisExportResult result = provider.exportClinicalBundle(
                fhirBundleJson,
                effectiveHisId,
                intakeQueue,
                idempotencyKey
        );

        if (result.success()) {
            record.setStatus(IntegrationStatus.SUCCESS);
            record.setExternalReference(result.externalReference());
            record.setResponsePayload(result.responsePayload());
            record.setCompletedAt(Instant.now());
            record.setErrorMessage(null);
            integrationRecordRepository.save(record);

            logAuditEvent(authenticatedDoctorId, "HIS_SUCCEEDED", "IntakeSession", sessionId.toString(),
                    Map.of("externalReference", result.externalReference(), "isMock", provider.getStatus().isMock(), "queue", intakeQueue));

            return HisExportResponseDto.builder()
                    .integrationRecordId(record.getId())
                    .sessionId(sessionId)
                    .patientId(patient.getId())
                    .status(IntegrationStatus.SUCCESS)
                    .encounterReference(result.externalReference())
                    .bundleId("Bundle-" + sessionId)
                    .hisId(effectiveHisId)
                    .intakeQueue(intakeQueue)
                    .idempotencyKey(idempotencyKey)
                    .isMock(provider.getStatus().isMock())
                    .attemptedAt(record.getAttemptedAt())
                    .completedAt(record.getCompletedAt())
                    .errorMessage(null)
                    .message("Verified clinical summary transmitted to HIS intake queue successfully")
                    .build();
        } else {
            record.setStatus(IntegrationStatus.FAILED);
            record.setErrorMessage(result.errorMessage());
            record.setCompletedAt(Instant.now());
            integrationRecordRepository.save(record);

            logAuditEvent(authenticatedDoctorId, "HIS_FAILED", "IntakeSession", sessionId.toString(),
                    Map.of("error", result.errorMessage(), "isMock", provider.getStatus().isMock()));

            return HisExportResponseDto.builder()
                    .integrationRecordId(record.getId())
                    .sessionId(sessionId)
                    .patientId(patient.getId())
                    .status(IntegrationStatus.FAILED)
                    .encounterReference(null)
                    .bundleId("Bundle-" + sessionId)
                    .hisId(effectiveHisId)
                    .intakeQueue(intakeQueue)
                    .idempotencyKey(idempotencyKey)
                    .isMock(provider.getStatus().isMock())
                    .attemptedAt(record.getAttemptedAt())
                    .completedAt(record.getCompletedAt())
                    .errorMessage(result.errorMessage())
                    .message("HIS transmission failed: " + result.errorMessage())
                    .build();
        }
    }

    @Override
    @Transactional(readOnly = true)
    public HisPatientSearchResponseDto searchHospitalPatients(String query, String searchType, UUID authenticatedUserId) {
        User user = getAuthenticatedUser(authenticatedUserId);
        if (user.getRole() != UserRole.DOCTOR && user.getRole() != UserRole.ADMIN) {
            throw new ApiException("HIS patient search requires DOCTOR or ADMIN role", HttpStatus.FORBIDDEN);
        }

        HisIntegrationProvider provider = getActiveProvider();
        List<HisPatientCandidateDto> candidates = provider.searchPatients(query, searchType);

        logAuditEvent(authenticatedUserId, "HIS_PATIENT_SEARCH", "Patient", query != null ? query : "ALL",
                Map.of("query", query != null ? query : "", "searchType", searchType != null ? searchType : "ALL", "count", candidates.size()));

        return HisPatientSearchResponseDto.builder()
                .candidates(candidates)
                .totalFound(candidates.size())
                .query(query)
                .searchType(searchType)
                .isMock(provider.getStatus().isMock())
                .message(candidates.isEmpty() ? "No matching hospital patient records found" : "Matched " + candidates.size() + " hospital record(s)")
                .build();
    }

    @Override
    @Transactional
    public HisLinkPatientResponseDto linkPatientHisId(UUID patientId, HisLinkPatientRequestDto request, UUID authenticatedUserId) {
        User user = getAuthenticatedUser(authenticatedUserId);
        if (user.getRole() != UserRole.DOCTOR && user.getRole() != UserRole.ADMIN) {
            throw new ApiException("Linking hospital patient ID requires DOCTOR or ADMIN role", HttpStatus.FORBIDDEN);
        }

        if (request == null || request.getHisId() == null || request.getHisId().isBlank()) {
            throw new ApiException("Hospital HIS identifier cannot be blank", HttpStatus.BAD_REQUEST);
        }

        Patient patient = patientRepository.findById(patientId)
                .orElseThrow(() -> new ApiException("Patient not found", HttpStatus.NOT_FOUND));

        String cleanHisId = request.getHisId().trim();
        patient.setHisId(cleanHisId);
        patient = patientRepository.save(patient);

        // Record IntegrationRecord for audit and tracking
        IntegrationRecord record = new IntegrationRecord(IntegrationType.HIS, "HIS_PATIENT_LINK", patient, null);
        record.setStatus(IntegrationStatus.SUCCESS);
        record.setExternalReference(cleanHisId);
        record.setCompletedAt(Instant.now());
        record.setIdempotencyKey("HIS-LINK-" + patient.getId() + "-" + cleanHisId);
        integrationRecordRepository.save(record);

        logAuditEvent(authenticatedUserId, "HIS_PATIENT_LINK", "Patient", patient.getId().toString(),
                Map.of("hisId", cleanHisId, "patientIdentifier", patient.getPatientIdentifier()));

        return HisLinkPatientResponseDto.builder()
                .patientId(patient.getId())
                .patientIdentifier(patient.getPatientIdentifier())
                .hisId(cleanHisId)
                .linkedAt(Instant.now())
                .message("Patient successfully associated with hospital HIS ID: " + cleanHisId)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public List<IntegrationRecordDto> getSessionIntegrationHistory(UUID sessionId, UUID authenticatedUserId) {
        IntakeSession session = intakeSessionRepository.findById(sessionId)
                .orElseThrow(() -> new ApiException("Intake session not found", HttpStatus.NOT_FOUND));

        User user = getAuthenticatedUser(authenticatedUserId);
        validatePatientAccess(session.getPatient(), user);

        List<IntegrationRecord> records = integrationRecordRepository.findBySessionIdAndIntegrationType(sessionId, IntegrationType.HIS);
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
            log.warn("Failed to write HIS audit log for action {}: {}", action, e.getMessage());
        }
    }
}
