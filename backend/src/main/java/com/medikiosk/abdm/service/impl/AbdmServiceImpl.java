package com.medikiosk.abdm.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.medikiosk.abdm.config.AbdmProperties;
import com.medikiosk.abdm.dto.AbdmConsentRequestDto;
import com.medikiosk.abdm.dto.AbdmConsentResponseDto;
import com.medikiosk.abdm.dto.AbdmExchangeRequestDto;
import com.medikiosk.abdm.dto.AbdmExchangeResponseDto;
import com.medikiosk.abdm.dto.AbdmStatusDto;
import com.medikiosk.abdm.dto.AbhaVerificationResponseDto;
import com.medikiosk.abdm.provider.AbdmExchangeResult;
import com.medikiosk.abdm.provider.AbdmIntegrationProvider;
import com.medikiosk.abdm.service.AbdmService;
import com.medikiosk.dto.fhir.FhirValidationResponseDto;
import com.medikiosk.entity.AuditLog;
import com.medikiosk.entity.ClinicalSummary;
import com.medikiosk.entity.Consent;
import com.medikiosk.entity.IntakeSession;
import com.medikiosk.entity.IntegrationRecord;
import com.medikiosk.entity.Patient;
import com.medikiosk.entity.User;
import com.medikiosk.entity.enums.AbhaStatus;
import com.medikiosk.entity.enums.ClinicalSummaryStatus;
import com.medikiosk.entity.enums.ConsentStatus;
import com.medikiosk.entity.enums.IntegrationStatus;
import com.medikiosk.entity.enums.IntegrationType;
import com.medikiosk.entity.enums.UserRole;
import com.medikiosk.exception.ApiException;
import com.medikiosk.fhir.FhirMappingService;
import com.medikiosk.repository.AuditLogRepository;
import com.medikiosk.repository.ClinicalSummaryRepository;
import com.medikiosk.repository.ConsentRepository;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * Implementation of ABDM and ABHA integration workflows.
 * Enforces doctor-verification gating, separate ABDM consent, standards-compliant
 * FHIR bundle validation, deterministic idempotency, and thorough audit logging.
 * Conforms to docs/ABDM_SPEC.md, docs/SECURITY.md, and Phase 12 requirements.
 */
@Service
public class AbdmServiceImpl implements AbdmService {

    private static final Logger log = LoggerFactory.getLogger(AbdmServiceImpl.class);
    private static final String CONSENT_TYPE_ABDM = "ABDM_DATA_SHARE";

    private final AbdmProperties properties;
    private final AbdmIntegrationProvider mockProvider;
    private final AbdmIntegrationProvider sandboxProvider;
    private final PatientRepository patientRepository;
    private final IntakeSessionRepository intakeSessionRepository;
    private final ConsentRepository consentRepository;
    private final IntegrationRecordRepository integrationRecordRepository;
    private final ClinicalSummaryRepository clinicalSummaryRepository;
    private final FhirMappingService fhirMappingService;
    private final AuditLogRepository auditLogRepository;
    private final UserRepository userRepository;
    private final ObjectMapper objectMapper;

    public AbdmServiceImpl(
            AbdmProperties properties,
            @Qualifier("mockAbdmProvider") AbdmIntegrationProvider mockProvider,
            @Qualifier("sandboxAbdmProvider") AbdmIntegrationProvider sandboxProvider,
            PatientRepository patientRepository,
            IntakeSessionRepository intakeSessionRepository,
            ConsentRepository consentRepository,
            IntegrationRecordRepository integrationRecordRepository,
            ClinicalSummaryRepository clinicalSummaryRepository,
            FhirMappingService fhirMappingService,
            AuditLogRepository auditLogRepository,
            UserRepository userRepository,
            ObjectMapper objectMapper) {
        this.properties = properties;
        this.mockProvider = mockProvider;
        this.sandboxProvider = sandboxProvider;
        this.patientRepository = patientRepository;
        this.intakeSessionRepository = intakeSessionRepository;
        this.consentRepository = consentRepository;
        this.integrationRecordRepository = integrationRecordRepository;
        this.clinicalSummaryRepository = clinicalSummaryRepository;
        this.fhirMappingService = fhirMappingService;
        this.auditLogRepository = auditLogRepository;
        this.userRepository = userRepository;
        this.objectMapper = objectMapper;
    }

    private AbdmIntegrationProvider getActiveProvider() {
        if (!properties.isMock() && properties.hasCredentials()) {
            return sandboxProvider;
        }
        return mockProvider;
    }

    @Override
    public AbdmStatusDto getStatus() {
        return getActiveProvider().getStatus();
    }

    @Override
    @Transactional
    public AbhaVerificationResponseDto verifyPatientAbha(UUID patientId, String abhaId, UUID authenticatedUserId) {
        Patient patient = patientRepository.findById(patientId)
                .orElseThrow(() -> new ApiException("Patient not found", HttpStatus.NOT_FOUND));

        // RBAC / Patient Ownership check
        User authUser = getAuthenticatedUser(authenticatedUserId);
        validatePatientAccess(patient, authUser);

        logAuditEvent(authenticatedUserId, "ABHA_VERIFICATION_REQUESTED", "Patient", patientId.toString(),
                Map.of("abhaProvided", abhaId != null));

        AbdmIntegrationProvider provider = getActiveProvider();
        AbhaVerificationResponseDto result = provider.verifyAbha(abhaId);

        // Record IntegrationRecord for ABHA verification attempt
        IntegrationRecord record = new IntegrationRecord(IntegrationType.ABDM, "ABHA_VERIFICATION", patient, null);
        record.setStatus(result.isVerified() ? IntegrationStatus.SUCCESS : IntegrationStatus.FAILED);
        record.setExternalReference(result.getMaskedAbha());
        record.setCompletedAt(Instant.now());
        if (!result.isVerified()) {
            record.setErrorMessage(result.getMessage());
        }
        integrationRecordRepository.save(record);

        if (result.isVerified()) {
            patient.setAbhaId(abhaId.trim());
            patient.setAbhaStatus(AbhaStatus.VERIFIED);
            patient.setAbhaVerifiedAt(result.getVerifiedAt() != null ? result.getVerifiedAt() : Instant.now());
            patientRepository.save(patient);

            logAuditEvent(authenticatedUserId, "ABHA_VERIFICATION_SUCCEEDED", "Patient", patientId.toString(),
                    Map.of("maskedAbha", result.getMaskedAbha(), "isMock", result.isMock()));
        } else {
            // Keep status UNVERIFIED or NOT_PROVIDED, do not falsely verify
            if (abhaId != null && !abhaId.isBlank()) {
                patient.setAbhaId(abhaId.trim());
                patient.setAbhaStatus(AbhaStatus.UNVERIFIED);
            } else {
                patient.setAbhaStatus(AbhaStatus.NOT_PROVIDED);
            }
            patient.setAbhaVerifiedAt(null);
            patientRepository.save(patient);

            logAuditEvent(authenticatedUserId, "ABHA_VERIFICATION_FAILED", "Patient", patientId.toString(),
                    Map.of("reason", result.getMessage()));
        }

        return result;
    }

    @Override
    @Transactional
    public AbdmConsentResponseDto grantConsent(UUID sessionId, AbdmConsentRequestDto request, UUID authenticatedUserId) {
        IntakeSession session = intakeSessionRepository.findById(sessionId)
                .orElseThrow(() -> new ApiException("Intake session not found", HttpStatus.NOT_FOUND));
        Patient patient = session.getPatient();

        User authUser = getAuthenticatedUser(authenticatedUserId);
        validatePatientAccess(patient, authUser);

        String consentType = (request != null && request.getConsentType() != null)
                ? request.getConsentType() : CONSENT_TYPE_ABDM;
        int validMonths = (request != null && request.getValidMonths() != null)
                ? request.getValidMonths() : 12;
        String purpose = (request != null && request.getPurpose() != null)
                ? request.getPurpose() : "Clinical care coordination and record exchange";

        AbdmIntegrationProvider provider = getActiveProvider();
        String artefactId = provider.generateConsentArtefact(patient.getAbhaId(), purpose, validMonths);

        // Find existing consent or create new one
        Consent consent = consentRepository.findFirstBySessionIdAndConsentTypeOrderByConsentedAtDesc(sessionId, consentType)
                .orElse(new Consent(patient, session, ConsentStatus.ACCEPTED, consentType, "v1.0"));

        consent.setConsentStatus(ConsentStatus.ACCEPTED);
        consent.setConsentType(consentType);
        consent.setExternalReference(artefactId);
        consent.setConsentedAt(Instant.now());
        consent.setRevokedAt(null);
        consent = consentRepository.save(consent);

        // Record IntegrationRecord
        IntegrationRecord record = new IntegrationRecord(IntegrationType.ABDM, "ABDM_CONSENT_GRANTED", patient, session);
        record.setStatus(IntegrationStatus.SUCCESS);
        record.setExternalReference(artefactId);
        record.setCompletedAt(Instant.now());
        integrationRecordRepository.save(record);

        logAuditEvent(authenticatedUserId, "ABDM_CONSENT_GRANTED", "Consent", consent.getId().toString(),
                Map.of("sessionId", sessionId.toString(), "artefactId", artefactId));

        return new AbdmConsentResponseDto(
                consent.getId(),
                patient.getId(),
                session.getId(),
                ConsentStatus.ACCEPTED,
                consentType,
                artefactId,
                consent.getConsentedAt(),
                null,
                provider.getStatus().isMock(),
                "ABDM data-sharing consent granted successfully"
        );
    }

    @Override
    @Transactional
    public AbdmConsentResponseDto revokeConsent(UUID sessionId, UUID authenticatedUserId) {
        IntakeSession session = intakeSessionRepository.findById(sessionId)
                .orElseThrow(() -> new ApiException("Intake session not found", HttpStatus.NOT_FOUND));
        Patient patient = session.getPatient();

        User authUser = getAuthenticatedUser(authenticatedUserId);
        validatePatientAccess(patient, authUser);

        Consent consent = consentRepository.findFirstBySessionIdAndConsentTypeOrderByConsentedAtDesc(sessionId, CONSENT_TYPE_ABDM)
                .orElseThrow(() -> new ApiException("Active ABDM consent not found for this session", HttpStatus.NOT_FOUND));

        consent.setConsentStatus(ConsentStatus.REVOKED);
        consent.setRevokedAt(Instant.now());
        consent = consentRepository.save(consent);

        AbdmIntegrationProvider provider = getActiveProvider();
        provider.revokeConsentArtefact(consent.getExternalReference());

        // Record IntegrationRecord
        IntegrationRecord record = new IntegrationRecord(IntegrationType.ABDM, "ABDM_CONSENT_REVOKED", patient, session);
        record.setStatus(IntegrationStatus.SUCCESS);
        record.setExternalReference(consent.getExternalReference());
        record.setCompletedAt(Instant.now());
        integrationRecordRepository.save(record);

        logAuditEvent(authenticatedUserId, "ABDM_CONSENT_REVOKED", "Consent", consent.getId().toString(),
                Map.of("sessionId", sessionId.toString()));

        return new AbdmConsentResponseDto(
                consent.getId(),
                patient.getId(),
                session.getId(),
                ConsentStatus.REVOKED,
                consent.getConsentType(),
                consent.getExternalReference(),
                consent.getConsentedAt(),
                consent.getRevokedAt(),
                provider.getStatus().isMock(),
                "ABDM data-sharing consent revoked successfully"
        );
    }

    @Override
    @Transactional
    public AbdmExchangeResponseDto exchangeClinicalData(UUID sessionId, AbdmExchangeRequestDto request, UUID authenticatedDoctorId) {
        User doctor = getAuthenticatedUser(authenticatedDoctorId);
        if (doctor.getRole() != UserRole.DOCTOR && doctor.getRole() != UserRole.ADMIN) {
            throw new ApiException("ABDM clinical exchange requires DOCTOR or ADMIN role", HttpStatus.FORBIDDEN);
        }

        IntakeSession session = intakeSessionRepository.findById(sessionId)
                .orElseThrow(() -> new ApiException("Intake session not found", HttpStatus.NOT_FOUND));
        Patient patient = session.getPatient();

        // 1. DOCTOR VERIFICATION BOUNDARY CHECK
        ClinicalSummary summary = clinicalSummaryRepository.findBySessionId(sessionId)
                .orElse(null);

        if (summary == null || summary.getStatus() != ClinicalSummaryStatus.VERIFIED) {
            log.warn("Blocked ABDM exchange: Session {} summary missing or status is {}", sessionId, summary != null ? summary.getStatus() : "NONE");
            return new AbdmExchangeResponseDto(
                    null, sessionId, patient.getId(), IntegrationStatus.FAILED, null, "Bundle-" + sessionId,
                    "ABDM-EXCHANGE-" + sessionId, getActiveProvider().getStatus().isMock(), Instant.now(), null,
                    "ABDM clinical exchange rejected: Clinical summary must be verified by a licensed doctor prior to external exchange",
                    "ABDM clinical exchange rejected: Clinical summary must be verified by a licensed doctor prior to external exchange"
            );
        }

        // 2. PATIENT CONSENT CHECK
        Optional<Consent> consentOpt = consentRepository.findFirstBySessionIdAndConsentTypeOrderByConsentedAtDesc(sessionId, CONSENT_TYPE_ABDM);
        if (consentOpt.isEmpty() || consentOpt.get().getConsentStatus() != ConsentStatus.ACCEPTED || consentOpt.get().getRevokedAt() != null) {
            log.warn("Blocked ABDM exchange: Session {} missing active ABDM data-sharing consent", sessionId);
            return new AbdmExchangeResponseDto(
                    null, sessionId, patient.getId(), IntegrationStatus.FAILED, null, "Bundle-" + sessionId,
                    "ABDM-EXCHANGE-" + sessionId, getActiveProvider().getStatus().isMock(), Instant.now(), null,
                    "ABDM clinical exchange rejected: Active patient consent for ABDM data sharing is required",
                    "ABDM clinical exchange rejected: Active patient consent for ABDM data sharing is required"
            );
        }
        Consent consent = consentOpt.get();

        // 3. FHIR BUNDLE VALIDATION CHECK
        FhirValidationResponseDto validation = fhirMappingService.validateBundleForSession(sessionId);
        if (!validation.isValid()) {
            log.warn("Blocked ABDM exchange: Session {} FHIR validation failed: {}", sessionId, validation.getErrors());
            throw new ApiException("ABDM clinical exchange rejected: FHIR bundle validation failed: " + String.join("; ", validation.getErrors()), HttpStatus.UNPROCESSABLE_ENTITY);
        }

        // 4. IDEMPOTENCY CHECK
        String idempotencyKey = (request != null && request.getIdempotencyKey() != null && !request.getIdempotencyKey().isBlank())
                ? request.getIdempotencyKey().trim()
                : "ABDM-EXCHANGE-" + sessionId;

        Optional<IntegrationRecord> existingRecord = integrationRecordRepository.findByIdempotencyKey(idempotencyKey);
        if (existingRecord.isPresent() && existingRecord.get().getStatus() == IntegrationStatus.SUCCESS) {
            IntegrationRecord rec = existingRecord.get();
            log.info("Idempotent replay for ABDM exchange key {}", idempotencyKey);
            return new AbdmExchangeResponseDto(
                    rec.getId(),
                    sessionId,
                    patient.getId(),
                    rec.getStatus(),
                    rec.getExternalReference(),
                    "Bundle-" + sessionId,
                    idempotencyKey,
                    getActiveProvider().getStatus().isMock(),
                    rec.getAttemptedAt(),
                    rec.getCompletedAt(),
                    null,
                    "Idempotent response: Record already successfully transmitted to ABDM"
            );
        }

        // 5. INITIATE TRANSMISSION
        String fhirBundleJson = fhirMappingService.toFhirClinicalBundle(sessionId);
        AbdmIntegrationProvider provider = getActiveProvider();

        IntegrationRecord record = existingRecord.orElse(
                new IntegrationRecord(IntegrationType.ABDM, "ABDM_CLINICAL_EXCHANGE", patient, session)
        );
        record.setIdempotencyKey(idempotencyKey);
        record.setStatus(IntegrationStatus.PENDING);
        record.setAttemptedAt(Instant.now());
        record.setAttemptCount(record.getAttemptCount() != null ? record.getAttemptCount() + 1 : 1);
        record = integrationRecordRepository.save(record);

        logAuditEvent(authenticatedDoctorId, "ABDM_REQUESTED", "IntakeSession", sessionId.toString(),
                Map.of("idempotencyKey", idempotencyKey, "bundleId", "Bundle-" + sessionId));

        AbdmExchangeResult result = provider.transmitClinicalBundle(
                fhirBundleJson,
                patient.getAbhaId(),
                consent.getExternalReference(),
                idempotencyKey
        );

        if (result.success()) {
            record.setStatus(IntegrationStatus.SUCCESS);
            record.setExternalReference(result.transactionId());
            record.setResponsePayload(result.responsePayload());
            record.setCompletedAt(Instant.now());
            record.setErrorMessage(null);
            integrationRecordRepository.save(record);

            logAuditEvent(authenticatedDoctorId, "ABDM_SUCCEEDED", "IntakeSession", sessionId.toString(),
                    Map.of("transactionId", result.transactionId(), "isMock", provider.getStatus().isMock()));

            return new AbdmExchangeResponseDto(
                    record.getId(),
                    sessionId,
                    patient.getId(),
                    IntegrationStatus.SUCCESS,
                    result.transactionId(),
                    "Bundle-" + sessionId,
                    idempotencyKey,
                    provider.getStatus().isMock(),
                    record.getAttemptedAt(),
                    record.getCompletedAt(),
                    null,
                    "Verified clinical bundle exchanged with ABDM successfully"
            );
        } else {
            record.setStatus(IntegrationStatus.FAILED);
            record.setErrorMessage(result.errorMessage());
            record.setCompletedAt(Instant.now());
            integrationRecordRepository.save(record);

            logAuditEvent(authenticatedDoctorId, "ABDM_FAILED", "IntakeSession", sessionId.toString(),
                    Map.of("error", result.errorMessage() != null ? result.errorMessage() : "Unknown"));

            return new AbdmExchangeResponseDto(
                    record.getId(),
                    sessionId,
                    patient.getId(),
                    IntegrationStatus.FAILED,
                    null,
                    "Bundle-" + sessionId,
                    idempotencyKey,
                    provider.getStatus().isMock(),
                    record.getAttemptedAt(),
                    record.getCompletedAt(),
                    result.errorMessage(),
                    "ABDM clinical exchange failed: " + result.errorMessage()
            );
        }
    }

    @Override
    public List<com.medikiosk.abdm.dto.IntegrationRecordDto> getSessionIntegrationHistory(UUID sessionId, UUID authenticatedUserId) {
        IntakeSession session = intakeSessionRepository.findById(sessionId)
                .orElseThrow(() -> new ApiException("Intake session not found", HttpStatus.NOT_FOUND));

        User user = getAuthenticatedUser(authenticatedUserId);
        validatePatientAccess(session.getPatient(), user);

        return integrationRecordRepository.findBySessionIdAndIntegrationType(sessionId, IntegrationType.ABDM)
                .stream()
                .map(com.medikiosk.abdm.dto.IntegrationRecordDto::fromEntity)
                .toList();
    }

    private User getAuthenticatedUser(UUID userId) {
        if (userId == null) {
            throw new ApiException("Authentication required", HttpStatus.UNAUTHORIZED);
        }
        return userRepository.findById(userId)
                .orElseThrow(() -> new ApiException("User not found", HttpStatus.UNAUTHORIZED));
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
                entUuid = UUID.fromString(entityId);
            } catch (Exception ignored) {
            }
            AuditLog auditLog = new AuditLog(userId, action, entityType, entUuid, metaJson);
            auditLogRepository.save(auditLog);
        } catch (Exception e) {
            log.warn("Failed to write audit log for action {}: {}", action, e.getMessage());
        }
    }
}
