package com.medikiosk.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.medikiosk.dto.triage.TriageEvaluateRequest;
import com.medikiosk.dto.triage.TriageResponse;
import com.medikiosk.dto.triage.TriggeredRuleDto;
import com.medikiosk.entity.Answer;
import com.medikiosk.entity.AuditLog;
import com.medikiosk.entity.Consent;
import com.medikiosk.entity.IntakeSession;
import com.medikiosk.entity.MedicalHistory;
import com.medikiosk.entity.Patient;
import com.medikiosk.entity.Symptom;
import com.medikiosk.entity.TriageResult;
import com.medikiosk.entity.User;
import com.medikiosk.entity.enums.ConsentStatus;
import com.medikiosk.entity.enums.ConsentType;
import com.medikiosk.entity.enums.IntakeSessionStatus;
import com.medikiosk.entity.enums.TriagePriority;
import com.medikiosk.entity.enums.UserRole;
import com.medikiosk.exception.ApiException;
import com.medikiosk.exception.ResourceNotFoundException;
import com.medikiosk.repository.AnswerRepository;
import com.medikiosk.repository.AuditLogRepository;
import com.medikiosk.repository.ConsentRepository;
import com.medikiosk.repository.IntakeSessionRepository;
import com.medikiosk.repository.MedicalHistoryRepository;
import com.medikiosk.repository.SymptomRepository;
import com.medikiosk.repository.TriageResultRepository;
import com.medikiosk.service.TriageService;
import com.medikiosk.triage.TriageDecision;
import com.medikiosk.triage.TriageEngine;
import com.medikiosk.triage.TriageInputContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class TriageServiceImpl implements TriageService {

    private static final Logger log = LoggerFactory.getLogger(TriageServiceImpl.class);

    private final IntakeSessionRepository intakeSessionRepository;
    private final ConsentRepository consentRepository;
    private final SymptomRepository symptomRepository;
    private final AnswerRepository answerRepository;
    private final MedicalHistoryRepository medicalHistoryRepository;
    private final TriageResultRepository triageResultRepository;
    private final AuditLogRepository auditLogRepository;
    private final TriageEngine triageEngine;
    private final ObjectMapper objectMapper;

    public TriageServiceImpl(IntakeSessionRepository intakeSessionRepository,
                             ConsentRepository consentRepository,
                             SymptomRepository symptomRepository,
                             AnswerRepository answerRepository,
                             MedicalHistoryRepository medicalHistoryRepository,
                             TriageResultRepository triageResultRepository,
                             AuditLogRepository auditLogRepository,
                             TriageEngine triageEngine,
                             ObjectMapper objectMapper) {
        this.intakeSessionRepository = intakeSessionRepository;
        this.consentRepository = consentRepository;
        this.symptomRepository = symptomRepository;
        this.answerRepository = answerRepository;
        this.medicalHistoryRepository = medicalHistoryRepository;
        this.triageResultRepository = triageResultRepository;
        this.auditLogRepository = auditLogRepository;
        this.triageEngine = triageEngine;
        this.objectMapper = objectMapper != null ? objectMapper : new ObjectMapper();
    }

    @Override
    @Transactional
    public TriageResponse evaluateSessionTriage(UUID sessionId, TriageEvaluateRequest request, User currentUser) {
        IntakeSession session = getSessionOrThrow(sessionId);
        validateSessionAccess(session, currentUser);
        verifySessionModifiable(session);
        verifyConsentGiven(sessionId);

        List<Symptom> symptoms = symptomRepository.findBySessionId(sessionId);
        List<Answer> answers = answerRepository.findBySessionId(sessionId);
        List<MedicalHistory> histories = medicalHistoryRepository.findBySessionId(sessionId);

        if (symptoms.isEmpty() && answers.isEmpty()) {
            throw new ApiException("Cannot evaluate triage: No structured intake data recorded for session", HttpStatus.BAD_REQUEST);
        }

        TriageInputContext context = new TriageInputContext(
                session,
                symptoms,
                answers,
                histories,
                request != null ? request.getParameters() : Collections.emptyMap()
        );

        TriageDecision decision = triageEngine.evaluate(context);

        // Idempotent persistence: update existing or create new
        Optional<TriageResult> existingOpt = triageResultRepository.findBySessionId(sessionId);
        TriageResult triageResult;
        String triggeredRulesJson;
        try {
            triggeredRulesJson = objectMapper.writeValueAsString(decision.getTriggeredRules());
        } catch (Exception e) {
            log.error("Failed to serialize triggered rules JSON", e);
            triggeredRulesJson = "[]";
        }

        if (existingOpt.isPresent()) {
            triageResult = existingOpt.get();
            triageResult.setPriority(decision.getPriority());
            triageResult.setTriggeredRules(triggeredRulesJson);
            triageResult.setExplanation(decision.getExplanation());
        } else {
            triageResult = new TriageResult(session, decision.getPriority(), decision.getExplanation());
            triageResult.setTriggeredRules(triggeredRulesJson);
        }

        triageResult = triageResultRepository.save(triageResult);

        // Audit log
        try {
            AuditLog auditLog = new AuditLog(
                    currentUser != null ? currentUser.getId() : null,
                    "TRIAGE_EVALUATED",
                    "IntakeSession",
                    sessionId,
                    "{\"priority\":\"" + decision.getPriority().name() + "\"}"
            );
            auditLogRepository.save(auditLog);
        } catch (Exception e) {
            log.warn("Failed to write audit log for triage evaluation: {}", e.getMessage());
        }

        return formatResponseForUser(triageResult, decision.getTriggeredRules(), decision.getReasons(),
                decision.getExplanation(), decision.getPatientInstruction(), currentUser);
    }

    @Override
    @Transactional(readOnly = true)
    public TriageResponse getSessionTriage(UUID sessionId, User currentUser) {
        IntakeSession session = getSessionOrThrow(sessionId);
        validateSessionAccess(session, currentUser);

        TriageResult triageResult = triageResultRepository.findBySessionId(sessionId)
                .orElseThrow(() -> new ResourceNotFoundException("Triage result not found for session: " + sessionId));

        List<TriggeredRuleDto> triggeredRules = new ArrayList<>();
        List<String> reasons = new ArrayList<>();
        if (triageResult.getTriggeredRules() != null && !triageResult.getTriggeredRules().isBlank()) {
            try {
                triggeredRules = objectMapper.readValue(triageResult.getTriggeredRules(), new TypeReference<List<TriggeredRuleDto>>() {});
                for (TriggeredRuleDto r : triggeredRules) {
                    if (r.getReason() != null) {
                        reasons.add(r.getReason());
                    }
                }
            } catch (Exception e) {
                log.warn("Failed to deserialize triggered rules for session {}: {}", sessionId, e.getMessage());
            }
        }

        String patientInstruction;
        if (triageResult.getPriority() == TriagePriority.URGENT) {
            patientInstruction = "Your responses indicate that a healthcare professional should review your information promptly.";
        } else if (triageResult.getPriority() == TriagePriority.PRIORITY) {
            patientInstruction = "Your responses have been recorded and marked for priority clinical review.";
        } else {
            patientInstruction = "Your intake responses have been recorded for doctor review.";
        }

        return formatResponseForUser(triageResult, triggeredRules, reasons,
                triageResult.getExplanation(), patientInstruction, currentUser);
    }

    private TriageResponse formatResponseForUser(TriageResult triageResult,
                                                List<TriggeredRuleDto> triggeredRules,
                                                List<String> reasons,
                                                String explanation,
                                                String patientInstruction,
                                                User user) {
        UUID sessionId = triageResult.getSession().getId();
        Instant evaluatedAt = triageResult.getCreatedAt();

        if (user != null && user.getRole() == UserRole.PATIENT) {
            // Patient view: safe routing instructions, no internal technical rule IDs exposed
            return new TriageResponse(
                    sessionId,
                    triageResult.getPriority(),
                    "COMPLETED",
                    Collections.emptyList(),
                    Collections.singletonList(patientInstruction),
                    explanation,
                    patientInstruction,
                    evaluatedAt
            );
        }

        // Clinician / Doctor / Admin view: full audit trace and rule explanations
        return new TriageResponse(
                sessionId,
                triageResult.getPriority(),
                "COMPLETED",
                triggeredRules,
                reasons,
                explanation,
                patientInstruction,
                evaluatedAt
        );
    }

    private IntakeSession getSessionOrThrow(UUID sessionId) {
        return intakeSessionRepository.findById(sessionId)
                .orElseThrow(() -> new ResourceNotFoundException("Intake session not found with id: " + sessionId));
    }

    private void validateSessionAccess(IntakeSession session, User user) {
        if (user == null) {
            throw new ApiException("Authentication required", HttpStatus.UNAUTHORIZED);
        }
        if (user.getRole() == UserRole.PATIENT) {
            Patient patient = session.getPatient();
            if (patient.getUserId() == null || !patient.getUserId().equals(user.getId())) {
                log.warn("Cross-patient access denied: User {} tried accessing session {}", user.getId(), session.getId());
                throw new ApiException("Access denied: You do not own this intake session", HttpStatus.FORBIDDEN);
            }
        }
    }

    private void verifySessionModifiable(IntakeSession session) {
        if (session.getStatus() == IntakeSessionStatus.COMPLETED) {
            throw new ApiException("Intake session is already completed and cannot be modified", HttpStatus.CONFLICT);
        }
        if (session.getStatus() == IntakeSessionStatus.EXPIRED) {
            throw new ApiException("Intake session is expired", HttpStatus.CONFLICT);
        }
    }

    private void verifyConsentGiven(UUID sessionId) {
        Optional<Consent> consentOpt = consentRepository
                .findFirstBySessionIdAndConsentTypeOrderByConsentedAtDesc(sessionId, ConsentType.GENERAL_INTAKE);
        if (consentOpt.isEmpty() || consentOpt.get().getConsentStatus() != ConsentStatus.ACCEPTED) {
            log.warn("Triage blocked: Valid accepted GENERAL_INTAKE consent missing for session {}", sessionId);
            throw new ApiException("Clinical intake cannot proceed without valid consent", HttpStatus.BAD_REQUEST);
        }
    }
}
