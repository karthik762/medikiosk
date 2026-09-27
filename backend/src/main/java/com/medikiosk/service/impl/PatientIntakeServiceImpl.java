package com.medikiosk.service.impl;

import com.medikiosk.dto.intake.AllergyRequest;
import com.medikiosk.dto.intake.AllergyResponse;
import com.medikiosk.dto.intake.AnswerRequest;
import com.medikiosk.dto.intake.AnswerResponse;
import com.medikiosk.dto.intake.ChiefComplaintRequest;
import com.medikiosk.dto.intake.ChiefComplaintResponse;
import com.medikiosk.dto.intake.ConsentRequest;
import com.medikiosk.dto.intake.ConsentResponse;
import com.medikiosk.dto.intake.IntakeSessionRequest;
import com.medikiosk.dto.intake.IntakeSessionResponse;
import com.medikiosk.dto.intake.MedicalHistoryRequest;
import com.medikiosk.dto.intake.MedicalHistoryResponse;
import com.medikiosk.dto.intake.MedicationRequest;
import com.medikiosk.dto.intake.MedicationResponse;
import com.medikiosk.dto.intake.QuestionResponse;
import com.medikiosk.dto.intake.SymptomRequest;
import com.medikiosk.dto.intake.SymptomResponse;
import com.medikiosk.entity.Allergy;
import com.medikiosk.entity.Answer;
import com.medikiosk.entity.Consent;
import com.medikiosk.entity.IntakeSession;
import com.medikiosk.entity.MedicalHistory;
import com.medikiosk.entity.Medication;
import com.medikiosk.entity.Patient;
import com.medikiosk.entity.Question;
import com.medikiosk.entity.QuestionCondition;
import com.medikiosk.entity.Symptom;
import com.medikiosk.entity.User;
import com.medikiosk.entity.enums.ConsentStatus;
import com.medikiosk.entity.enums.ConsentType;
import com.medikiosk.entity.enums.IntakeSessionStatus;
import com.medikiosk.entity.enums.UserRole;
import com.medikiosk.exception.ApiException;
import com.medikiosk.exception.ResourceNotFoundException;
import com.medikiosk.repository.AllergyRepository;
import com.medikiosk.repository.AnswerRepository;
import com.medikiosk.repository.ConsentRepository;
import com.medikiosk.repository.IntakeSessionRepository;
import com.medikiosk.repository.MedicalHistoryRepository;
import com.medikiosk.repository.MedicationRepository;
import com.medikiosk.repository.PatientRepository;
import com.medikiosk.repository.QuestionConditionRepository;
import com.medikiosk.repository.QuestionRepository;
import com.medikiosk.repository.SymptomRepository;
import com.medikiosk.service.PatientIntakeService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class PatientIntakeServiceImpl implements PatientIntakeService {

    private static final Logger log = LoggerFactory.getLogger(PatientIntakeServiceImpl.class);

    private final PatientRepository patientRepository;
    private final IntakeSessionRepository intakeSessionRepository;
    private final ConsentRepository consentRepository;
    private final QuestionRepository questionRepository;
    private final QuestionConditionRepository questionConditionRepository;
    private final AnswerRepository answerRepository;
    private final SymptomRepository symptomRepository;
    private final MedicalHistoryRepository medicalHistoryRepository;
    private final MedicationRepository medicationRepository;
    private final AllergyRepository allergyRepository;

    public PatientIntakeServiceImpl(
            PatientRepository patientRepository,
            IntakeSessionRepository intakeSessionRepository,
            ConsentRepository consentRepository,
            QuestionRepository questionRepository,
            QuestionConditionRepository questionConditionRepository,
            AnswerRepository answerRepository,
            SymptomRepository symptomRepository,
            MedicalHistoryRepository medicalHistoryRepository,
            MedicationRepository medicationRepository,
            AllergyRepository allergyRepository) {
        this.patientRepository = patientRepository;
        this.intakeSessionRepository = intakeSessionRepository;
        this.consentRepository = consentRepository;
        this.questionRepository = questionRepository;
        this.questionConditionRepository = questionConditionRepository;
        this.answerRepository = answerRepository;
        this.symptomRepository = symptomRepository;
        this.medicalHistoryRepository = medicalHistoryRepository;
        this.medicationRepository = medicationRepository;
        this.allergyRepository = allergyRepository;
    }

    @Override
    @Transactional
    public IntakeSessionResponse createSession(IntakeSessionRequest request, User authenticatedUser) {
        Patient patient = patientRepository.findById(request.getPatientId())
                .orElseThrow(() -> new ResourceNotFoundException("Patient not found with id: " + request.getPatientId()));

        validatePatientOwnership(patient, authenticatedUser);

        IntakeSession session = new IntakeSession(patient);
        if (StringUtils.hasText(request.getLanguage())) {
            session.setLanguage(request.getLanguage().trim());
        }
        session.setExpiresAt(Instant.now().plus(24, ChronoUnit.HOURS));

        IntakeSession saved = intakeSessionRepository.save(session);
        log.info("Created intake session {} for patient {}", saved.getId(), patient.getPatientIdentifier());

        return mapSessionToResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public IntakeSessionResponse getSession(UUID sessionId, User authenticatedUser) {
        IntakeSession session = getSessionOrThrow(sessionId);
        validateSessionAccess(session, authenticatedUser);
        return mapSessionToResponse(session);
    }

    @Override
    @Transactional
    public ConsentResponse recordConsent(UUID sessionId, ConsentRequest request, User authenticatedUser) {
        IntakeSession session = getSessionOrThrow(sessionId);
        validateSessionAccess(session, authenticatedUser);
        verifySessionModifiable(session);

        String consentType = request.getConsentType() != null ? request.getConsentType() : ConsentType.GENERAL_INTAKE;
        Consent consent = consentRepository
                .findFirstBySessionIdAndConsentTypeOrderByConsentedAtDesc(sessionId, consentType)
                .orElse(new Consent(session.getPatient(), session, request.getConsentStatus(),
                        consentType, request.getConsentVersion()));

        consent.setConsentStatus(request.getConsentStatus());
        consent.setConsentType(consentType);
        consent.setConsentVersion(request.getConsentVersion());
        consent.setExternalReference(request.getExternalReference());
        consent.setConsentedAt(Instant.now());

        Consent savedConsent = consentRepository.save(consent);

        // Only GENERAL_INTAKE acceptance advances the clinical intake session state.
        if (ConsentType.GENERAL_INTAKE.equals(consentType)
                && request.getConsentStatus() == ConsentStatus.ACCEPTED) {
            session.setStatus(IntakeSessionStatus.IN_PROGRESS);
            intakeSessionRepository.save(session);
            log.info("Consent accepted for session {}. Session status transitioned to IN_PROGRESS", sessionId);
        } else if (ConsentType.GENERAL_INTAKE.equals(consentType)) {
            log.warn("Consent declined for session {}. Further clinical intake is blocked.", sessionId);
        }

        return new ConsentResponse(
                savedConsent.getId(),
                session.getPatient().getId(),
                sessionId,
                savedConsent.getConsentStatus(),
                savedConsent.getConsentType(),
                savedConsent.getConsentVersion(),
                savedConsent.getExternalReference(),
                savedConsent.getConsentedAt()
        );
    }

    @Override
    @Transactional
    public ChiefComplaintResponse recordChiefComplaint(UUID sessionId, ChiefComplaintRequest request, User authenticatedUser) {
        IntakeSession session = getSessionOrThrow(sessionId);
        validateSessionAccess(session, authenticatedUser);
        verifySessionModifiable(session);
        verifyConsentGiven(sessionId);

        Question complaintQuestion = questionRepository.findByQuestionCode("Q_PRIMARY_COMPLAINT")
                .or(() -> questionRepository.findByQuestionCode("Q_CHIEF_COMPLAINT"))
                .orElseGet(() -> {
                    Question q = new Question("Q_PRIMARY_COMPLAINT", "What is the primary reason for your visit today?", "TEXT", "CHIEF_COMPLAINT");
                    return questionRepository.save(q);
                });

        Answer answer = new Answer(session, complaintQuestion, request.getComplaintText().trim(), request.getInputMethod());
        answer.setOriginalTranscription(request.getOriginalTranscription());
        answer.setConfidenceScore(request.getConfidenceScore());

        Answer savedAnswer = answerRepository.save(answer);
        log.info("Recorded chief complaint for intake session {}", sessionId);

        return new ChiefComplaintResponse(
                savedAnswer.getId(),
                sessionId,
                savedAnswer.getAnswerText(),
                savedAnswer.getInputMethod(),
                savedAnswer.getAnsweredAt()
        );
    }

    @Override
    @Transactional
    public SymptomResponse recordSymptom(UUID sessionId, SymptomRequest request, User authenticatedUser) {
        IntakeSession session = getSessionOrThrow(sessionId);
        validateSessionAccess(session, authenticatedUser);
        verifySessionModifiable(session);
        verifyConsentGiven(sessionId);

        Symptom symptom = new Symptom(session, request.getName().trim(), request.getSeverity(), request.getDuration());
        symptom.setLocation(request.getLocation());
        symptom.setFrequency(request.getFrequency());
        symptom.setOnset(request.getOnset());
        if (StringUtils.hasText(request.getSource())) {
            symptom.setSource(request.getSource().trim());
        }

        Symptom saved = symptomRepository.save(symptom);
        log.info("Recorded symptom '{}' for session {}", saved.getName(), sessionId);

        return new SymptomResponse(
                saved.getId(),
                sessionId,
                saved.getName(),
                saved.getLocation(),
                saved.getDuration(),
                saved.getSeverity(),
                saved.getFrequency(),
                saved.getOnset(),
                saved.getSource(),
                saved.getVerified(),
                saved.getCreatedAt()
        );
    }

    @Override
    @Transactional
    public MedicalHistoryResponse recordMedicalHistory(UUID sessionId, MedicalHistoryRequest request, User authenticatedUser) {
        IntakeSession session = getSessionOrThrow(sessionId);
        validateSessionAccess(session, authenticatedUser);
        verifySessionModifiable(session);
        verifyConsentGiven(sessionId);

        MedicalHistory history = new MedicalHistory(session.getPatient(), request.getCategory().trim(), request.getDescription().trim(), request.getSource());
        history.setSession(session);

        MedicalHistory saved = medicalHistoryRepository.save(history);
        log.info("Recorded medical history [category: {}] for session {}", saved.getCategory(), sessionId);

        return new MedicalHistoryResponse(
                saved.getId(),
                session.getPatient().getId(),
                sessionId,
                saved.getCategory(),
                saved.getDescription(),
                saved.getSource(),
                saved.getVerified(),
                saved.getCreatedAt()
        );
    }

    @Override
    @Transactional
    public MedicationResponse recordMedication(UUID sessionId, MedicationRequest request, User authenticatedUser) {
        IntakeSession session = getSessionOrThrow(sessionId);
        validateSessionAccess(session, authenticatedUser);
        verifySessionModifiable(session);
        verifyConsentGiven(sessionId);

        Medication medication = new Medication(session.getPatient(), request.getMedicationName().trim(), request.getDosage(), request.getFrequency());
        medication.setSession(session);
        medication.setDuration(request.getDuration());
        if (StringUtils.hasText(request.getSource())) {
            medication.setSource(request.getSource().trim());
        }

        Medication saved = medicationRepository.save(medication);
        log.info("Recorded medication '{}' for session {}", saved.getMedicationName(), sessionId);

        return new MedicationResponse(
                saved.getId(),
                session.getPatient().getId(),
                sessionId,
                saved.getMedicationName(),
                saved.getDosage(),
                saved.getFrequency(),
                saved.getDuration(),
                saved.getSource(),
                saved.getVerified(),
                saved.getCreatedAt()
        );
    }

    @Override
    @Transactional
    public AllergyResponse recordAllergy(UUID sessionId, AllergyRequest request, User authenticatedUser) {
        IntakeSession session = getSessionOrThrow(sessionId);
        validateSessionAccess(session, authenticatedUser);
        verifySessionModifiable(session);
        verifyConsentGiven(sessionId);

        Allergy allergy = new Allergy(session.getPatient(), request.getAllergen().trim(), request.getReaction(), request.getSeverity());
        allergy.setSession(session);
        if (StringUtils.hasText(request.getSource())) {
            allergy.setSource(request.getSource().trim());
        }

        Allergy saved = allergyRepository.save(allergy);
        log.info("Recorded allergy to '{}' for session {}", saved.getAllergen(), sessionId);

        return new AllergyResponse(
                saved.getId(),
                session.getPatient().getId(),
                sessionId,
                saved.getAllergen(),
                saved.getReaction(),
                saved.getSeverity(),
                saved.getSource(),
                saved.getVerified(),
                saved.getCreatedAt()
        );
    }

    @Override
    @Transactional(readOnly = true)
    public List<QuestionResponse> getApplicableQuestions(UUID sessionId, User authenticatedUser) {
        IntakeSession session = getSessionOrThrow(sessionId);
        validateSessionAccess(session, authenticatedUser);
        verifyConsentGiven(sessionId);

        List<Question> activeQuestions = questionRepository.findByActiveTrue();
        List<Answer> existingAnswers = answerRepository.findBySessionId(sessionId);

        Map<UUID, String> answeredMap = existingAnswers.stream()
                .collect(Collectors.toMap(
                        a -> a.getQuestion().getId(),
                        a -> a.getAnswerText() != null ? a.getAnswerText().trim().toUpperCase() : "",
                        (first, second) -> second
                ));

        List<QuestionResponse> applicable = new ArrayList<>();
        for (Question question : activeQuestions) {
            List<QuestionCondition> conditions = questionConditionRepository.findByDependentQuestionId(question.getId());

            boolean shouldInclude = true;
            for (QuestionCondition condition : conditions) {
                UUID parentId = condition.getQuestion().getId();
                String parentAnswer = answeredMap.get(parentId);
                String expectedValue = condition.getConditionValue() != null ? condition.getConditionValue().trim().toUpperCase() : "";

                if ("SHOW".equalsIgnoreCase(condition.getAction())) {
                    // Only show if parent has been answered and matches condition_value
                    if (parentAnswer == null || !parentAnswer.equals(expectedValue)) {
                        shouldInclude = false;
                        break;
                    }
                } else if ("SKIP".equalsIgnoreCase(condition.getAction())) {
                    // Skip if parent answer matches condition_value
                    if (parentAnswer != null && parentAnswer.equals(expectedValue)) {
                        shouldInclude = false;
                        break;
                    }
                }
            }

            if (shouldInclude) {
                applicable.add(new QuestionResponse(
                        question.getId(),
                        question.getQuestionCode(),
                        question.getQuestionText(),
                        question.getQuestionType(),
                        question.getCategory(),
                        question.getLanguage(),
                        question.getRequired()
                ));
            }
        }

        return applicable;
    }

    @Override
    @Transactional
    public AnswerResponse recordAnswer(UUID sessionId, AnswerRequest request, User authenticatedUser) {
        IntakeSession session = getSessionOrThrow(sessionId);
        validateSessionAccess(session, authenticatedUser);
        verifySessionModifiable(session);
        verifyConsentGiven(sessionId);

        Question question = questionRepository.findById(request.getQuestionId())
                .orElseThrow(() -> new ResourceNotFoundException("Question not found with id: " + request.getQuestionId()));

        if (Boolean.FALSE.equals(question.getActive())) {
            throw new ApiException("Question is inactive", HttpStatus.BAD_REQUEST);
        }

        String inputMethod = request.getInputMethod() != null ? request.getInputMethod().toUpperCase() : "TEXT";

        // Voice input method requires explicit patient confirmation before final answer storage
        if ("VOICE".equalsIgnoreCase(inputMethod)) {
            if (request.getConfirmedByPatient() == null || Boolean.FALSE.equals(request.getConfirmedByPatient())) {
                log.warn("Rejected unconfirmed voice answer for question {} in session {}", question.getQuestionCode(), sessionId);
                throw new ApiException("Voice response must be confirmed by the patient before saving", HttpStatus.BAD_REQUEST);
            }
        }

        Answer answer = new Answer(session, question, request.getAnswerText().trim(), inputMethod);
        answer.setOriginalTranscription(request.getOriginalTranscription());
        answer.setConfidenceScore(request.getConfidenceScore());
        answer.setConfirmedByPatient(request.getConfirmedByPatient() != null ? request.getConfirmedByPatient() : true);

        Answer saved = answerRepository.save(answer);
        log.info("Saved answer for question {} in session {} (inputMethod: {}, confirmed: {})",
                question.getQuestionCode(), sessionId, inputMethod, saved.getConfirmedByPatient());

        return new AnswerResponse(
                saved.getId(),
                sessionId,
                question.getId(),
                question.getQuestionCode(),
                saved.getAnswerText(),
                saved.getInputMethod(),
                saved.getOriginalTranscription(),
                saved.getConfidenceScore(),
                saved.getConfirmedByPatient(),
                saved.getAnsweredAt()
        );
    }

    @Override
    @Transactional
    public IntakeSessionResponse completeSession(UUID sessionId, User authenticatedUser) {
        IntakeSession session = getSessionOrThrow(sessionId);
        validateSessionAccess(session, authenticatedUser);

        if (session.getStatus() == IntakeSessionStatus.COMPLETED) {
            log.warn("Session {} completion rejected: Already completed", sessionId);
            throw new ApiException("Session is already completed", HttpStatus.CONFLICT);
        }

        verifyConsentGiven(sessionId);

        // Verify that some clinical intake data has been submitted
        boolean hasAnswers = !answerRepository.findBySessionId(sessionId).isEmpty();
        boolean hasSymptoms = !symptomRepository.findBySessionId(sessionId).isEmpty();
        boolean hasHistory = !medicalHistoryRepository.findBySessionId(sessionId).isEmpty();

        if (!hasAnswers && !hasSymptoms && !hasHistory) {
            throw new ApiException("Cannot complete intake session: No clinical intake data has been provided", HttpStatus.BAD_REQUEST);
        }

        session.setStatus(IntakeSessionStatus.COMPLETED);
        session.setCompletedAt(Instant.now());

        IntakeSession completedSession = intakeSessionRepository.save(session);
        log.info("Successfully finalized and completed intake session {}", sessionId);

        return mapSessionToResponse(completedSession);
    }

    private IntakeSession getSessionOrThrow(UUID sessionId) {
        return intakeSessionRepository.findById(sessionId)
                .orElseThrow(() -> new ResourceNotFoundException("Intake session not found with id: " + sessionId));
    }

    private void validatePatientOwnership(Patient patient, User user) {
        if (user == null) {
            throw new ApiException("Authentication required", HttpStatus.UNAUTHORIZED);
        }
        if (user.getRole() == UserRole.PATIENT) {
            if (patient.getUserId() == null || !patient.getUserId().equals(user.getId())) {
                log.warn("Unauthorized attempt: Patient user {} tried creating session for patient {}", user.getId(), patient.getId());
                throw new ApiException("Access denied: You do not own this patient record", HttpStatus.FORBIDDEN);
            }
        }
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
            log.warn("Intake operation blocked: Valid accepted GENERAL_INTAKE consent missing for session {}", sessionId);
            throw new ApiException("Clinical intake cannot proceed without valid consent", HttpStatus.BAD_REQUEST);
        }
    }

    private IntakeSessionResponse mapSessionToResponse(IntakeSession session) {
        return new IntakeSessionResponse(
                session.getId(),
                session.getPatient().getId(),
                session.getPatient().getPatientIdentifier(),
                session.getStatus(),
                session.getLanguage(),
                session.getStartedAt(),
                session.getCompletedAt(),
                session.getExpiresAt()
        );
    }
}
