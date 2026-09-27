package com.medikiosk.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.medikiosk.ai.provider.AiProvider;
import com.medikiosk.ai.provider.AiStructuringContext;
import com.medikiosk.ai.provider.AiStructuringResult;
import com.medikiosk.ai.safety.AiSafetyValidator;
import com.medikiosk.ai.safety.AiSafetyViolationException;
import com.medikiosk.audit.AuditService;
import com.medikiosk.dto.clinical.ClinicalSummaryDto;
import com.medikiosk.entity.Allergy;
import com.medikiosk.entity.Answer;
import com.medikiosk.entity.AyurvedaHistory;
import com.medikiosk.entity.ClinicalSummary;
import com.medikiosk.entity.Consent;
import com.medikiosk.entity.DocumentExtraction;
import com.medikiosk.entity.IntakeSession;
import com.medikiosk.entity.MedicalDocument;
import com.medikiosk.entity.MedicalHistory;
import com.medikiosk.entity.Medication;
import com.medikiosk.entity.Patient;
import com.medikiosk.entity.Symptom;
import com.medikiosk.entity.TriageResult;
import com.medikiosk.entity.User;
import com.medikiosk.entity.enums.ClinicalSummaryStatus;
import com.medikiosk.entity.enums.ConsentStatus;
import com.medikiosk.entity.enums.ConsentType;
import com.medikiosk.entity.enums.UserRole;
import com.medikiosk.exception.ApiException;
import com.medikiosk.exception.ResourceNotFoundException;
import com.medikiosk.repository.AllergyRepository;
import com.medikiosk.repository.AnswerRepository;
import com.medikiosk.repository.AuditLogRepository;
import com.medikiosk.repository.AyurvedaHistoryRepository;
import com.medikiosk.repository.ClinicalSummaryRepository;
import com.medikiosk.repository.ConsentRepository;
import com.medikiosk.repository.DocumentExtractionRepository;
import com.medikiosk.repository.IntakeSessionRepository;
import com.medikiosk.repository.MedicalDocumentRepository;
import com.medikiosk.repository.MedicalHistoryRepository;
import com.medikiosk.repository.MedicationRepository;
import com.medikiosk.repository.SymptomRepository;
import com.medikiosk.repository.TriageResultRepository;
import com.medikiosk.service.ClinicalSummaryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Implementation of ClinicalSummaryService orchestrating the complete pre-consult structuring pipeline.
 */
@Service
public class ClinicalSummaryServiceImpl implements ClinicalSummaryService {

    private static final Logger log = LoggerFactory.getLogger(ClinicalSummaryServiceImpl.class);

    private final ClinicalSummaryRepository clinicalSummaryRepository;
    private final IntakeSessionRepository intakeSessionRepository;
    private final ConsentRepository consentRepository;
    private final AnswerRepository answerRepository;
    private final SymptomRepository symptomRepository;
    private final MedicalHistoryRepository medicalHistoryRepository;
    private final MedicationRepository medicationRepository;
    private final AllergyRepository allergyRepository;
    private final AyurvedaHistoryRepository ayurvedaHistoryRepository;
    private final TriageResultRepository triageResultRepository;
    private final MedicalDocumentRepository medicalDocumentRepository;
    private final DocumentExtractionRepository documentExtractionRepository;

    private final AiProvider aiProvider;
    private final AiSafetyValidator aiSafetyValidator;
    private final AuditLogRepository auditLogRepository;
    private final ObjectMapper objectMapper;

    public ClinicalSummaryServiceImpl(
            ClinicalSummaryRepository clinicalSummaryRepository,
            IntakeSessionRepository intakeSessionRepository,
            ConsentRepository consentRepository,
            AnswerRepository answerRepository,
            SymptomRepository symptomRepository,
            MedicalHistoryRepository medicalHistoryRepository,
            MedicationRepository medicationRepository,
            AllergyRepository allergyRepository,
            AyurvedaHistoryRepository ayurvedaHistoryRepository,
            TriageResultRepository triageResultRepository,
            MedicalDocumentRepository medicalDocumentRepository,
            DocumentExtractionRepository documentExtractionRepository,
            AiProvider aiProvider,
            AiSafetyValidator aiSafetyValidator,
            AuditLogRepository auditLogRepository,
            ObjectMapper objectMapper) {
        this.clinicalSummaryRepository = clinicalSummaryRepository;
        this.intakeSessionRepository = intakeSessionRepository;
        this.consentRepository = consentRepository;
        this.answerRepository = answerRepository;
        this.symptomRepository = symptomRepository;
        this.medicalHistoryRepository = medicalHistoryRepository;
        this.medicationRepository = medicationRepository;
        this.allergyRepository = allergyRepository;
        this.ayurvedaHistoryRepository = ayurvedaHistoryRepository;
        this.triageResultRepository = triageResultRepository;
        this.medicalDocumentRepository = medicalDocumentRepository;
        this.documentExtractionRepository = documentExtractionRepository;
        this.aiProvider = aiProvider;
        this.aiSafetyValidator = aiSafetyValidator;
        this.auditLogRepository = auditLogRepository;
        this.objectMapper = objectMapper;
    }

    @Override
    @Transactional
    public ClinicalSummaryDto generateSummary(UUID sessionId, User currentUser) {
        IntakeSession session = intakeSessionRepository.findById(sessionId)
                .orElseThrow(() -> new ResourceNotFoundException("IntakeSession", "id", sessionId));

        validateAccess(session, currentUser);
        validateConsent(session);

        UUID userId = currentUser != null ? currentUser.getId() : null;
        logAuditEvent(userId, "AI_SUMMARY_GENERATION_STARTED", "IntakeSession", session.getId().toString(),
                Map.of("sessionId", sessionId.toString(), "provider", aiProvider.getProviderName()));

        // Check if an already verified summary exists - do not overwrite doctor-verified summary
        Optional<ClinicalSummary> existingOpt = clinicalSummaryRepository.findBySessionId(sessionId);
        if (existingOpt.isPresent() && existingOpt.get().getStatus() == ClinicalSummaryStatus.VERIFIED) {
            throw new ApiException("Cannot regenerate summary: A doctor has already verified the clinical summary for this session.", HttpStatus.CONFLICT);
        }

        // 1. Assemble context from intake and documents
        AiStructuringContext context = buildContext(session);

        // 2. Invoke AI Provider
        AiStructuringResult result = aiProvider.generateClinicalSummary(context);
        if (!result.isSuccess()) {
            logAuditEvent(userId, "AI_SUMMARY_GENERATION_FAILED", "IntakeSession", session.getId().toString(),
                    Map.of("reason", result.getErrorMessage(), "provider", aiProvider.getProviderName()));
            throw new ApiException("AI clinical structuring failed: " + result.getErrorMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }

        ClinicalSummaryDto summaryDto = result.getSummaryDto();

        // 3. Application-Layer Deterministic Safety & Hallucination Filter
        try {
            aiSafetyValidator.validate(summaryDto, context);
        } catch (AiSafetyViolationException ex) {
            log.warn("AI safety filter triggered for session {}: {} [{}]", sessionId, ex.getMessage(), ex.getViolationCategory());
            logAuditEvent(userId, "AI_SUMMARY_REJECTED", "IntakeSession", session.getId().toString(),
                    Map.of("reason", ex.getMessage(), "category", ex.getViolationCategory(), "provider", aiProvider.getProviderName()));
            throw new ApiException("AI output rejected by clinical safety filter: " + ex.getMessage(), HttpStatus.UNPROCESSABLE_ENTITY);
        }

        // 4. Persist or Update ClinicalSummary entity (Idempotent per session)
        ClinicalSummary summary = existingOpt.orElseGet(() -> {
            ClinicalSummary newSummary = new ClinicalSummary();
            newSummary.setPatient(session.getPatient());
            newSummary.setSession(session);
            newSummary.setVersion(1);
            return newSummary;
        });

        if (existingOpt.isPresent()) {
            summary.setVersion(summary.getVersion() + 1);
        }

        try {
            summary.setSummaryData(objectMapper.writeValueAsString(summaryDto));
        } catch (Exception e) {
            summary.setSummaryData("{}");
        }
        summary.setSummaryText(summaryDto.getHumanReadableSummary());
        summary.setGeneratedBy(aiProvider.getProviderName());
        summary.setStatus(ClinicalSummaryStatus.AI_GENERATED);
        summary.setModelIdentifier(aiProvider.getModelIdentifier());
        summary.setIsMock(aiProvider.isMock());

        ClinicalSummary saved = clinicalSummaryRepository.save(summary);

        summaryDto.setId(saved.getId());
        summaryDto.setVersion(saved.getVersion());
        summaryDto.setCreatedAt(saved.getCreatedAt());
        summaryDto.setUpdatedAt(saved.getUpdatedAt());

        logAuditEvent(userId, "AI_SUMMARY_GENERATED", "ClinicalSummary", saved.getId().toString(),
                Map.of("sessionId", sessionId.toString(), "version", saved.getVersion(), "isMock", saved.getIsMock()));

        return summaryDto;
    }

    @Override
    @Transactional(readOnly = true)
    public ClinicalSummaryDto getSummaryBySessionId(UUID sessionId, User currentUser) {
        IntakeSession session = intakeSessionRepository.findById(sessionId)
                .orElseThrow(() -> new ResourceNotFoundException("IntakeSession", "id", sessionId));

        validateAccess(session, currentUser);

        ClinicalSummary summary = clinicalSummaryRepository.findBySessionId(sessionId)
                .orElseThrow(() -> new ResourceNotFoundException("ClinicalSummary", "sessionId", sessionId));

        return toDto(summary);
    }

    @Override
    @Transactional(readOnly = true)
    public ClinicalSummaryDto getSummaryById(UUID summaryId, User currentUser) {
        ClinicalSummary summary = clinicalSummaryRepository.findById(summaryId)
                .orElseThrow(() -> new ResourceNotFoundException("ClinicalSummary", "id", summaryId));

        validateAccess(summary.getSession(), currentUser);

        return toDto(summary);
    }

    @Override
    @Transactional(readOnly = true)
    public ClinicalSummaryDto getSummaryByPatientId(UUID patientId, User currentUser) {
        List<ClinicalSummary> summaries = clinicalSummaryRepository.findByPatientId(patientId);
        if (summaries == null || summaries.isEmpty()) {
            throw new ResourceNotFoundException("ClinicalSummary", "patientId", patientId);
        }

        summaries.sort((a, b) -> b.getCreatedAt().compareTo(a.getCreatedAt()));
        ClinicalSummary latest = summaries.get(0);

        validateAccess(latest.getSession(), currentUser);

        return toDto(latest);
    }

    private AiStructuringContext buildContext(IntakeSession session) {
        AiStructuringContext context = new AiStructuringContext();
        context.setSession(session);
        context.setPatient(session.getPatient());

        context.setAnswers(answerRepository.findBySessionId(session.getId()));
        context.setSymptoms(symptomRepository.findBySessionId(session.getId()));
        List<MedicalHistory> histories = new ArrayList<>(medicalHistoryRepository.findBySessionId(session.getId()));
        if (session.getPatient() != null) {
            Set<UUID> existingIds = histories.stream().map(MedicalHistory::getId).collect(Collectors.toSet());
            for (MedicalHistory ph : medicalHistoryRepository.findByPatientId(session.getPatient().getId())) {
                if (ph.getId() != null && !existingIds.contains(ph.getId())) {
                    histories.add(ph);
                }
            }
        }
        context.setMedicalHistories(histories);

        List<Medication> medications = new ArrayList<>(medicationRepository.findBySessionId(session.getId()));
        if (session.getPatient() != null) {
            Set<UUID> existingIds = medications.stream().map(Medication::getId).collect(Collectors.toSet());
            for (Medication pm : medicationRepository.findByPatientId(session.getPatient().getId())) {
                if (pm.getId() != null && !existingIds.contains(pm.getId())) {
                    medications.add(pm);
                }
            }
        }
        context.setMedications(medications);

        List<Allergy> allergies = new ArrayList<>(allergyRepository.findBySessionId(session.getId()));
        if (session.getPatient() != null) {
            Set<UUID> existingIds = allergies.stream().map(Allergy::getId).collect(Collectors.toSet());
            for (Allergy pa : allergyRepository.findByPatientId(session.getPatient().getId())) {
                if (pa.getId() != null && !existingIds.contains(pa.getId())) {
                    allergies.add(pa);
                }
            }
        }
        context.setAllergies(allergies);
        context.setAyurvedaHistory(ayurvedaHistoryRepository.findBySessionId(session.getId()).orElse(null));
        context.setTriageResult(triageResultRepository.findBySessionId(session.getId()).orElse(null));

        List<MedicalDocument> documents = medicalDocumentRepository.findBySessionId(session.getId());
        context.setDocuments(documents);

        Map<MedicalDocument, DocumentExtraction> extractions = new HashMap<>();
        for (MedicalDocument doc : documents) {
            List<DocumentExtraction> docExts = documentExtractionRepository.findByDocumentId(doc.getId());
            if (!docExts.isEmpty()) {
                extractions.put(doc, docExts.get(0));
            }
        }
        context.setExtractions(extractions);

        return context;
    }

    private ClinicalSummaryDto toDto(ClinicalSummary summary) {
        try {
            if (summary.getSummaryData() != null && !summary.getSummaryData().isBlank()) {
                ClinicalSummaryDto dto = objectMapper.readValue(summary.getSummaryData(), ClinicalSummaryDto.class);
                dto.setId(summary.getId());
                dto.setPatientId(summary.getPatient().getId());
                dto.setSessionId(summary.getSession().getId());
                dto.setStatus(summary.getStatus());
                dto.setVersion(summary.getVersion());
                dto.setHumanReadableSummary(summary.getSummaryText());
                dto.setCreatedAt(summary.getCreatedAt());
                dto.setUpdatedAt(summary.getUpdatedAt());
                return dto;
            }
        } catch (Exception ex) {
            log.warn("Failed to parse summary_data JSON for summary ID {}", summary.getId(), ex);
        }

        ClinicalSummaryDto fallback = new ClinicalSummaryDto();
        fallback.setId(summary.getId());
        fallback.setPatientId(summary.getPatient().getId());
        fallback.setSessionId(summary.getSession().getId());
        fallback.setStatus(summary.getStatus());
        fallback.setVersion(summary.getVersion());
        fallback.setHumanReadableSummary(summary.getSummaryText());
        fallback.setCreatedAt(summary.getCreatedAt());
        fallback.setUpdatedAt(summary.getUpdatedAt());
        return fallback;
    }

    private void validateAccess(IntakeSession session, User currentUser) {
        if (currentUser == null) {
            throw new ApiException("Authentication required to access clinical summary", HttpStatus.UNAUTHORIZED);
        }

        if (currentUser.getRole() == UserRole.DOCTOR || currentUser.getRole() == UserRole.ADMIN) {
            return;
        }

        Patient patient = session.getPatient();
        if (patient.getUserId() != null && !patient.getUserId().equals(currentUser.getId())) {
            throw new ApiException("Access denied: You do not own this intake session", HttpStatus.FORBIDDEN);
        }
    }

    private void validateConsent(IntakeSession session) {
        Optional<Consent> consentOpt = consentRepository
                .findFirstBySessionIdAndConsentTypeOrderByConsentedAtDesc(session.getId(), ConsentType.GENERAL_INTAKE);
        if (consentOpt.isEmpty() || consentOpt.get().getConsentStatus() != ConsentStatus.ACCEPTED) {
            throw new ApiException("Clinical summary generation cannot proceed without valid patient consent", HttpStatus.BAD_REQUEST);
        }
    }

    private void logAuditEvent(UUID userId, String action, String entityType, String entityId, Map<String, Object> metadata) {
        try {
            String metadataJson = metadata != null ? objectMapper.writeValueAsString(metadata) : "{}";
            UUID entUuid = null;
            try {
                if (entityId != null) entUuid = UUID.fromString(entityId);
            } catch (Exception ignored) {
            }
            com.medikiosk.entity.AuditLog logEntry = new com.medikiosk.entity.AuditLog(
                    userId,
                    action,
                    entityType,
                    entUuid,
                    metadataJson
            );
            auditLogRepository.save(logEntry);
        } catch (Exception e) {
            log.warn("Failed to record audit event: {}", action, e);
        }
    }
}
