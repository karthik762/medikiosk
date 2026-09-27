package com.medikiosk.clinical.adaptive;

import com.medikiosk.clinical.nlp.ClinicalEntity;
import com.medikiosk.clinical.nlp.ClinicalExtractionRequest;
import com.medikiosk.clinical.nlp.ClinicalExtractionResult;
import com.medikiosk.clinical.nlp.ClinicalNlpProvider;
import com.medikiosk.clinical.state.ClinicalConflict;
import com.medikiosk.clinical.state.ClinicalFact;
import com.medikiosk.clinical.state.ClinicalFactProvenance;
import com.medikiosk.clinical.state.ClinicalState;
import com.medikiosk.entity.Allergy;
import com.medikiosk.entity.Answer;
import com.medikiosk.entity.DocumentExtraction;
import com.medikiosk.entity.IntakeSession;
import com.medikiosk.entity.MedicalHistory;
import com.medikiosk.entity.Medication;
import com.medikiosk.entity.Patient;
import com.medikiosk.entity.Symptom;
import com.medikiosk.entity.TriageResult;
import com.medikiosk.entity.User;
import com.medikiosk.entity.enums.UserRole;
import com.medikiosk.exception.ApiException;
import com.medikiosk.exception.ResourceNotFoundException;
import com.medikiosk.repository.AllergyRepository;
import com.medikiosk.repository.AnswerRepository;
import com.medikiosk.repository.AyurvedaHistoryRepository;
import com.medikiosk.repository.DocumentExtractionRepository;
import com.medikiosk.repository.IntakeSessionRepository;
import com.medikiosk.repository.MedicalHistoryRepository;
import com.medikiosk.repository.MedicationRepository;
import com.medikiosk.repository.SymptomRepository;
import com.medikiosk.repository.TriageResultRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.medikiosk.clinical.conversation.ConversationContext;
import com.medikiosk.clinical.conversation.ConversationTurn;
import com.medikiosk.entity.Question;
import com.medikiosk.repository.QuestionRepository;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class AdaptiveQuestionServiceImpl implements AdaptiveQuestionService {

    private static final Logger log = LoggerFactory.getLogger(AdaptiveQuestionServiceImpl.class);

    private final IntakeSessionRepository intakeSessionRepository;
    private final AnswerRepository answerRepository;
    private final SymptomRepository symptomRepository;
    private final MedicalHistoryRepository medicalHistoryRepository;
    private final MedicationRepository medicationRepository;
    private final AllergyRepository allergyRepository;
    private final AyurvedaHistoryRepository ayurvedaHistoryRepository;
    private final DocumentExtractionRepository documentExtractionRepository;
    private final TriageResultRepository triageResultRepository;
    private final ClinicalNlpProvider clinicalNlpProvider;
    private final AdaptiveQuestionEngine adaptiveQuestionEngine;
    private final AIQuestionPlanner aiQuestionPlanner;
    private final QuestionRepository questionRepository;

    public AdaptiveQuestionServiceImpl(
            IntakeSessionRepository intakeSessionRepository,
            AnswerRepository answerRepository,
            SymptomRepository symptomRepository,
            MedicalHistoryRepository medicalHistoryRepository,
            MedicationRepository medicationRepository,
            AllergyRepository allergyRepository,
            AyurvedaHistoryRepository ayurvedaHistoryRepository,
            DocumentExtractionRepository documentExtractionRepository,
            TriageResultRepository triageResultRepository,
            ClinicalNlpProvider clinicalNlpProvider,
            AdaptiveQuestionEngine adaptiveQuestionEngine,
            AIQuestionPlanner aiQuestionPlanner,
            QuestionRepository questionRepository) {
        this.intakeSessionRepository = intakeSessionRepository;
        this.answerRepository = answerRepository;
        this.symptomRepository = symptomRepository;
        this.medicalHistoryRepository = medicalHistoryRepository;
        this.medicationRepository = medicationRepository;
        this.allergyRepository = allergyRepository;
        this.ayurvedaHistoryRepository = ayurvedaHistoryRepository;
        this.documentExtractionRepository = documentExtractionRepository;
        this.triageResultRepository = triageResultRepository;
        this.clinicalNlpProvider = clinicalNlpProvider;
        this.adaptiveQuestionEngine = adaptiveQuestionEngine;
        this.aiQuestionPlanner = aiQuestionPlanner;
        this.questionRepository = questionRepository;
    }

    @Override
    @Transactional
    public NextQuestionResponse getNextQuestion(UUID sessionId, User authenticatedUser) {
        IntakeSession session = intakeSessionRepository.findById(sessionId)
                .orElseThrow(() -> new ResourceNotFoundException("Intake session not found with id: " + sessionId));
        validateAccess(session, authenticatedUser);

        ClinicalState state = compileClinicalState(sessionId, authenticatedUser);

        // Build ConversationContext and QuestionHistory from recorded answers
        List<Answer> answers = answerRepository.findBySessionId(sessionId);
        ConversationContext context = new ConversationContext();
        QuestionHistory history = new QuestionHistory();

        boolean complaintRecordedInTurns = false;
        if (state.getChiefComplaint() != null && !state.getChiefComplaint().trim().isEmpty()) {
            context.addTurn(ConversationTurn.patient(state.getChiefComplaint()));
            complaintRecordedInTurns = true;
        }

        for (Answer ans : answers) {
            String qCode = ans.getQuestion().getQuestionCode();
            String qText = ans.getQuestion().getQuestionText();
            String aText = ans.getAnswerText();
            if (!("Q_PRIMARY_COMPLAINT".equals(qCode) && complaintRecordedInTurns)) {
                context.addTurn(ConversationTurn.system(qText));
                context.addTurn(ConversationTurn.patient(aText));
            }
            history.recordQuestion(qCode, qText, aText, null);
        }

        // 1. Try AI Question Planning
        try {
            AIQuestionPlanner.QuestionPlan plan = aiQuestionPlanner.planNextQuestion(
                    state, context, history, session.getLanguage());

            if (plan != null) {
                if (plan.isCompleted()) {
                    return NextQuestionResponse.completed(plan.getReason());
                }

                Question matchedQuestion = resolveQuestionForPlan(plan);
                if (matchedQuestion != null) {
                    NextQuestionResponse resp = new NextQuestionResponse();
                    resp.setQuestionId(matchedQuestion.getId());
                    resp.setQuestionCode(matchedQuestion.getQuestionCode());
                    resp.setQuestionText(plan.getQuestionText() != null ? plan.getQuestionText() : matchedQuestion.getQuestionText());
                    resp.setQuestionType(plan.getAnswerType() != null ? plan.getAnswerType() : matchedQuestion.getQuestionType());
                    resp.setAnswerType(plan.getAnswerType() != null ? plan.getAnswerType() : matchedQuestion.getQuestionType());
                    resp.setCategory(matchedQuestion.getCategory());
                    resp.setClinicalConceptCode(matchedQuestion.getClinicalConceptCode());
                    resp.setSafetyCritical(plan.isSafetyCritical() || Boolean.TRUE.equals(matchedQuestion.getSafetyCritical()));
                    resp.setPriority(plan.getPriority());
                    resp.setCompleted(false);
                    resp.setProgressStage("Step " + (history.getAskedCount() + 1) + " of clinical intake");

                    resp.setIntent(plan.getIntentCode());
                    resp.setGenerated(!"ApprovedClinicalTemplateFallback".equals(plan.getGeneratedBy()));
                    resp.setProvider(plan.getGeneratedBy() != null ? plan.getGeneratedBy() : "MOCK");
                    resp.setSafetyLevel("SAFE");
                    resp.setRequiresConfirmation(false);
                    resp.setReason(plan.getReason());

                    if (plan.getOptions() != null && !plan.getOptions().isEmpty()) {
                        resp.setOptions(plan.getOptions());
                    } else {
                        resp.setOptions(getDefaultOptions(matchedQuestion.getQuestionType(), matchedQuestion.getQuestionCode()));
                    }

                    if (matchedQuestion.getClinicalConceptCode() != null) {
                        resp.getTriggeringConcepts().add(matchedQuestion.getClinicalConceptCode());
                    }

                    return resp;
                }
            }
        } catch (Exception ex) {
            log.warn("AI Question Planner encountered exception: {}. Falling back to deterministic engine.", ex.getMessage());
        }

        // 2. Deterministic Fallback
        return adaptiveQuestionEngine.determineNextQuestion(state);
    }

    private Question resolveQuestionForPlan(AIQuestionPlanner.QuestionPlan plan) {
        if (plan == null || plan.getIntentCode() == null) {
            return null;
        }
        String intent = plan.getIntentCode();
        String candidateCode = null;
        switch (intent) {
            case "CHEST_PAIN_CHARACTER":
                candidateCode = "Q_CHEST_PAIN_CHARACTER";
                break;
            case "CHEST_PAIN_AGGRAVATING":
                candidateCode = "Q_CHEST_PAIN_AGGRAVATING";
                break;
            case "CHEST_PAIN_SEVERITY":
                candidateCode = "Q_CP_SEVERITY_SCALE";
                break;
            case "CHEST_PAIN_RADIATION":
                candidateCode = "Q_CHEST_PAIN_RADIATION";
                break;
            case "CHEST_PAIN_DURATION":
                candidateCode = "Q_CHEST_PAIN_DURATION";
                break;
            case "BREATHING_DIFFICULTY":
                candidateCode = "Q_CHEST_PAIN_BREATHING";
                break;
            case "BREATHING_DIFFICULTY_REST":
                candidateCode = "Q_SOB_REST";
                break;
            case "FEVER_DURATION":
                candidateCode = "Q_FEVER_DURATION";
                break;
            case "FEVER_CHILLS":
                candidateCode = "Q_FEVER_CHILLS";
                break;
            case "FEVER_TEMPERATURE":
                candidateCode = "Q_FEVER_TEMPERATURE";
                break;
            case "HEADACHE_SEVERITY":
                candidateCode = "Q_HEADACHE_SEVERITY";
                break;
            case "HEADACHE_RED_FLAGS":
                candidateCode = "Q_HEADACHE_VISION";
                break;
            case "DIZZINESS_SYNCOPE":
                candidateCode = "Q_DIZZINESS_SYNCOPE";
                break;
            case "ABDOMINAL_LOCATION":
                candidateCode = "Q_ABDOMINAL_LOCATION";
                break;
            case "ABDOMINAL_VOMITING":
                candidateCode = "Q_ABDOMINAL_VOMITING";
                break;
            case "COUGH_DURATION":
                candidateCode = "Q_COUGH_DURATION";
                break;
            case "COUGH_HEMOPTYSIS":
                candidateCode = "Q_COUGH_BLOOD";
                break;
            case "ALLERGY_CONFIRMATION":
                candidateCode = "Q_KNOWN_ALLERGIES";
                break;
            case "MEDICATION_CONFIRMATION":
                candidateCode = "Q_CURRENT_MEDS";
                break;
            default:
                candidateCode = "Q_" + intent;
        }

        if (candidateCode != null) {
            Optional<Question> qOpt = questionRepository.findByQuestionCode(candidateCode);
            if (qOpt.isPresent() && Boolean.TRUE.equals(qOpt.get().getActive())) {
                return qOpt.get();
            }
        }

        QuestionIntent qi = QuestionIntent.fromCode(intent);

        // Dynamically persist Question entity so subsequent answer records have valid foreign key
        Question newQ = new Question();
        newQ.setQuestionCode(candidateCode != null ? candidateCode : "Q_" + intent);
        newQ.setQuestionText(plan.getQuestionText() != null ? plan.getQuestionText() : (qi != null ? qi.getDefaultApprovedTemplate() : "Please describe your symptoms."));
        newQ.setQuestionType(plan.getAnswerType() != null ? plan.getAnswerType() : (qi != null ? qi.getAnswerType() : "TEXT"));
        newQ.setCategory("ADAPTIVE_AI");
        newQ.setClinicalConceptCode(qi != null ? qi.getClinicalConcept() : "GENERAL");
        newQ.setSafetyCritical(plan.isSafetyCritical() || (qi != null && qi.isSafetyCritical()));
        newQ.setPriority(plan.getPriority() > 0 ? plan.getPriority() : (qi != null ? qi.getPriority() : 50));
        newQ.setActive(true);
        newQ.setRequired(true);
        return questionRepository.save(newQ);
    }

    private List<String> getDefaultOptions(String questionType, String questionCode) {
        if ("SCALE_0_10".equalsIgnoreCase(questionType)) {
            return Arrays.asList("0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "10");
        } else if ("YES_NO".equalsIgnoreCase(questionType)) {
            return Arrays.asList("YES", "NO");
        } else if ("CHOICE".equalsIgnoreCase(questionType)) {
            if ("Q_SOB_ONSET".equals(questionCode)) {
                return Arrays.asList("Suddenly (Minutes/Hours)", "Gradually (Days/Weeks)");
            }
        }
        return new java.util.ArrayList<>();
    }

    @Override
    @Transactional(readOnly = true)
    public ClinicalState getClinicalState(UUID sessionId, User authenticatedUser) {
        return compileClinicalState(sessionId, authenticatedUser);
    }

    private ClinicalState compileClinicalState(UUID sessionId, User authenticatedUser) {
        IntakeSession session = intakeSessionRepository.findById(sessionId)
                .orElseThrow(() -> new ResourceNotFoundException("Intake session not found with id: " + sessionId));

        validateAccess(session, authenticatedUser);

        Patient patient = session.getPatient();
        ClinicalState state = new ClinicalState(sessionId, patient.getId(), patient.getPatientIdentifier());
        state.setPatientName(patient.getFirstName() + " " + patient.getLastName());
        state.setLanguage(session.getLanguage());

        // 1. Gather answers
        List<Answer> answers = answerRepository.findBySessionId(sessionId);
        for (Answer ans : answers) {
            String qCode = ans.getQuestion().getQuestionCode();
            state.recordAnswer(qCode, ans.getAnswerText());

            if ("Q_PRIMARY_COMPLAINT".equals(qCode)) {
                state.setChiefComplaint(ans.getAnswerText());
            }
        }

        // 2. Gather symptoms
        List<Symptom> symptoms = symptomRepository.findBySessionId(sessionId);
        for (Symptom s : symptoms) {
            state.getSymptoms().add(s.getName());
            ClinicalFact fact = new ClinicalFact(
                    s.getName().toUpperCase().replace(" ", "_"),
                    s.getName(),
                    "AFFIRMED",
                    ClinicalFactProvenance.PATIENT_SELECTED,
                    1.0
            );
            if (s.getSeverity() != null) {
                fact.setSeverity(String.valueOf(s.getSeverity()));
            }
            if (s.getDuration() != null) {
                fact.setDuration(s.getDuration());
            }
            state.addFact(fact);
        }

        // 3. Process NLP on Chief Complaint if present
        if (state.getChiefComplaint() != null && !state.getChiefComplaint().trim().isEmpty()) {
            ClinicalExtractionRequest nlpRequest = new ClinicalExtractionRequest(
                    state.getChiefComplaint(),
                    session.getLanguage(),
                    ClinicalFactProvenance.PATIENT_TEXT
            );
            ClinicalExtractionResult nlpResult = clinicalNlpProvider.extractEntities(nlpRequest);

            for (ClinicalEntity entity : nlpResult.getEntities()) {
                ClinicalFact fact = new ClinicalFact(
                        entity.getConceptCode(),
                        entity.getText(),
                        entity.getAssertion(),
                        ClinicalFactProvenance.PATIENT_TEXT,
                        entity.getConfidence()
                );
                state.addFact(fact);
            }
        }

        // 4. Gather histories
        List<MedicalHistory> histories = medicalHistoryRepository.findBySessionId(sessionId);
        for (MedicalHistory h : histories) {
            state.getMedicalHistories().add(h.getDescription());
        }

        // 5. Gather medications
        List<Medication> meds = medicationRepository.findBySessionId(sessionId);
        for (Medication m : meds) {
            state.getMedications().add(m.getMedicationName());
        }

        // 6. Gather allergies
        List<Allergy> allergies = allergyRepository.findBySessionId(sessionId);
        for (Allergy a : allergies) {
            state.getAllergies().add(a.getAllergen());
        }

        // 7. Check document extractions for confidence gating and conflict detection
        List<DocumentExtraction> extractions = documentExtractionRepository.findAll();
        for (DocumentExtraction de : extractions) {
            if (de.getDocument() != null && de.getDocument().getSession() != null
                    && de.getDocument().getSession().getId().equals(sessionId)) {
                state.setDocumentCount(state.getDocumentCount() + 1);
                String extractedText = de.getExtractedText() != null ? de.getExtractedText().toLowerCase() : "";
                java.math.BigDecimal confScore = de.getConfidenceScore();
                double confidence = confScore != null ? confScore.doubleValue() : 0.90;

                if (confidence >= 0.85) {
                    // High confidence: contribute to ClinicalState facts with DOCUMENT_OCR provenance
                    if (extractedText.contains("penicillin") || extractedText.contains("amoxicillin")) {
                        state.addFact(new com.medikiosk.clinical.state.ClinicalFact(
                                "DOCUMENT_PENICILLIN_ALLERGY", "Penicillin Allergy", "AFFIRMED",
                                ClinicalFactProvenance.DOCUMENT_OCR, confidence
                        ));
                    }
                    if (extractedText.contains("lisinopril") || extractedText.contains("amlodipine") || extractedText.contains("metformin")) {
                        state.addFact(new com.medikiosk.clinical.state.ClinicalFact(
                                "DOCUMENT_MEDICATION", "Prescription Medication", "AFFIRMED",
                                ClinicalFactProvenance.DOCUMENT_OCR, confidence
                        ));
                    }
                } else {
                    // Low confidence (< 0.85): do NOT treat as established fact; flag for confirmation
                    state.addFact(new com.medikiosk.clinical.state.ClinicalFact(
                            "LOW_CONFIDENCE_OCR_MEDICATION", "Uncertain Document Text", "UNCERTAIN",
                            ClinicalFactProvenance.DOCUMENT_OCR, confidence
                    ));
                }

                // Conflict 1: Patient claims no allergies vs document shows penicillin/amoxicillin
                boolean patientDeniedAllergies = "NO".equalsIgnoreCase(state.getAnswerText("Q_KNOWN_ALLERGIES"))
                        || (state.isQuestionAnswered("Q_KNOWN_ALLERGIES") && state.getAllergies().isEmpty());
                if ((state.getAllergies().isEmpty() || patientDeniedAllergies)
                        && (extractedText.contains("penicillin") || extractedText.contains("amoxicillin"))) {
                    state.addConflict(new ClinicalConflict(
                            "ALLERGY",
                            "Possible drug sensitivity mentioned in uploaded medical document",
                            "PATIENT_REPORT",
                            "No known allergies",
                            "DOCUMENT_OCR",
                            "Mention of Penicillin in record"
                    ));
                }

                // Conflict 2: Patient claims no medication vs document mentions medication
                boolean patientDeniedMeds = "NO".equalsIgnoreCase(state.getAnswerText("Q_CURRENT_MEDS"))
                        || (state.isQuestionAnswered("Q_CURRENT_MEDS") && state.getMedications().isEmpty());
                if (patientDeniedMeds && (extractedText.contains("lisinopril") || extractedText.contains("amlodipine") || extractedText.contains("metformin"))) {
                    state.addConflict(new ClinicalConflict(
                            "MEDICATION",
                            "Active prescription mentioned in uploaded document vs patient reported no medications",
                            "PATIENT_REPORT",
                            "No active medications",
                            "DOCUMENT_OCR",
                            "Prescription details found in document"
                    ));
                }
            }
        }

        // 8. Triage Priority
        Optional<TriageResult> triageOpt = triageResultRepository.findBySessionId(sessionId);
        triageOpt.ifPresent(tr -> state.setTriagePriority(tr.getPriority().name()));

        // 9. Ayurveda completion check
        state.setAyurvedaCompleted(ayurvedaHistoryRepository.findBySessionId(sessionId).isPresent());

        return state;
    }

    private void validateAccess(IntakeSession session, User user) {
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
}
