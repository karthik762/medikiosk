package com.medikiosk.clinical.adaptive;

import com.medikiosk.clinical.conversation.ConversationContext;
import com.medikiosk.clinical.reasoning.ClinicalContextRequest;
import com.medikiosk.clinical.reasoning.ClinicalContextResult;
import com.medikiosk.clinical.reasoning.ClinicalFactExtraction;
import com.medikiosk.clinical.reasoning.ClinicalQuestionIntent;
import com.medikiosk.clinical.reasoning.ClinicalReasoningProvider;
import com.medikiosk.clinical.state.ClinicalState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

/**
 * AI Question Planner that selects controlled clinical question intents based on
 * cumulative dialogue state and generates patient-friendly phrasing under strict safety validation.
 */
@Component
public class AIQuestionPlanner {

    private static final Logger log = LoggerFactory.getLogger(AIQuestionPlanner.class);

    private final ClinicalReasoningProvider reasoningProvider;
    private final QuestionSafetyValidator safetyValidator;
    private final IntakeCompletionPolicy completionPolicy;
    private final com.medikiosk.ai.gateway.AiModelGateway aiModelGateway;

    public static class QuestionPlan {
        private String intentCode;
        private String questionText;
        private String answerType;
        private String reason;
        private int priority;
        private boolean safetyCritical;
        private boolean completed;
        private List<String> options = new ArrayList<>();
        private String generatedBy;

        public static QuestionPlan completed(String reason) {
            QuestionPlan qp = new QuestionPlan();
            qp.completed = true;
            qp.reason = reason;
            qp.questionText = "Clinical intake questionnaire complete. Ready for review.";
            return qp;
        }

        // Getters and Setters
        public String getIntentCode() { return intentCode; }
        public void setIntentCode(String intentCode) { this.intentCode = intentCode; }
        public String getQuestionText() { return questionText; }
        public void setQuestionText(String questionText) { this.questionText = questionText; }
        public String getAnswerType() { return answerType; }
        public void setAnswerType(String answerType) { this.answerType = answerType; }
        public String getReason() { return reason; }
        public void setReason(String reason) { this.reason = reason; }
        public int getPriority() { return priority; }
        public void setPriority(int priority) { this.priority = priority; }
        public boolean isSafetyCritical() { return safetyCritical; }
        public void setSafetyCritical(boolean safetyCritical) { this.safetyCritical = safetyCritical; }
        public boolean isCompleted() { return completed; }
        public void setCompleted(boolean completed) { this.completed = completed; }
        public List<String> getOptions() { return options; }
        public void setOptions(List<String> options) { this.options = options != null ? options : new ArrayList<>(); }
        public String getGeneratedBy() { return generatedBy; }
        public void setGeneratedBy(String generatedBy) { this.generatedBy = generatedBy; }
    }

    @org.springframework.beans.factory.annotation.Autowired
    public AIQuestionPlanner(
            ClinicalReasoningProvider reasoningProvider,
            QuestionSafetyValidator safetyValidator,
            IntakeCompletionPolicy completionPolicy,
            @org.springframework.beans.factory.annotation.Autowired(required = false) com.medikiosk.ai.gateway.AiModelGateway aiModelGateway) {
        this.reasoningProvider = reasoningProvider;
        this.safetyValidator = safetyValidator;
        this.completionPolicy = completionPolicy;
        this.aiModelGateway = aiModelGateway;
    }

    public AIQuestionPlanner(
            ClinicalReasoningProvider reasoningProvider,
            QuestionSafetyValidator safetyValidator,
            IntakeCompletionPolicy completionPolicy) {
        this(reasoningProvider, safetyValidator, completionPolicy, null);
    }

    /**
     * Plans the next question based on the accumulated multi-turn dialogue and clinical state.
     */
    public QuestionPlan planNextQuestion(
            ClinicalState state,
            ConversationContext context,
            QuestionHistory history,
            String language) {

        // 1. Perform clinical context extraction on latest conversation first so ClinicalState is updated
        ClinicalContextRequest req = new ClinicalContextRequest(
                state.getSessionId(),
                state.getChiefComplaint(),
                context != null ? context.toFormattedHistory(8) : null,
                language != null ? language : "en"
        );
        req.setExistingFacts(state.getFacts());

        ClinicalContextResult analysis = reasoningProvider.analyzeContext(req);

        // Update state with newly extracted facts preserving open-world semantic properties and correction handling
        for (ClinicalFactExtraction fe : analysis.getExtractedFacts()) {
            state.addFactWithCorrection(fe.toClinicalFact());
        }

        // 2. Evaluate intake completion policy on updated ClinicalState
        IntakeCompletionPolicy.CompletionStatus compStatus = completionPolicy.evaluateCompletion(state, history);
        if (compStatus.isComplete()) {
            return QuestionPlan.completed(compStatus.getReason());
        }

        // 3. Priority Check: Active Document Conflicts (Phase 25.2)
        if (state.getConflicts() != null && !state.getConflicts().isEmpty()) {
            for (com.medikiosk.clinical.state.ClinicalConflict conflict : state.getConflicts()) {
                String conflictIntentCode = "CONFLICT_CLARIFICATION_" + conflict.getCategory();
                if (history == null || !history.isIntentAsked(conflictIntentCode)) {
                    QuestionPlan conflictPlan = new QuestionPlan();
                    conflictPlan.setIntentCode(conflictIntentCode);
                    conflictPlan.setQuestionText("Your responses and one of your uploaded records contain different information about a " +
                            (conflict.getCategory().equalsIgnoreCase("ALLERGY") ? "penicillin allergy" : "your current medications") +
                            ". Which information is current?");
                    conflictPlan.setAnswerType("CHOICE");
                    conflictPlan.setPriority(98);
                    conflictPlan.setSafetyCritical(true);
                    conflictPlan.setCompleted(false);
                    conflictPlan.setGeneratedBy(reasoningProvider.getProviderName());
                    conflictPlan.setReason("Neutral conflict resolution for " + conflict.getCategory());
                    conflictPlan.setOptions(Arrays.asList("Patient response is current", "Document record is current", "Not sure / Discuss with doctor"));
                    return conflictPlan;
                }
            }
        }

        // 4. Priority Check: Low Confidence OCR Confirmation (Phase 25.1)
        if (state.hasFact("LOW_CONFIDENCE_OCR_MEDICATION") && (history == null || !history.isIntentAsked("OCR_MEDICATION_CONFIRMATION"))) {
            QuestionPlan ocrPlan = new QuestionPlan();
            ocrPlan.setIntentCode("OCR_MEDICATION_CONFIRMATION");
            ocrPlan.setQuestionText("We found a medication name in your document. Can you confirm whether you currently take it?");
            ocrPlan.setAnswerType("YES_NO");
            ocrPlan.setPriority(88);
            ocrPlan.setSafetyCritical(false);
            ocrPlan.setCompleted(false);
            ocrPlan.setGeneratedBy(reasoningProvider.getProviderName());
            ocrPlan.setReason("Confirmation of low-confidence OCR medication extraction");
            ocrPlan.setOptions(Arrays.asList("YES", "NO"));
            return ocrPlan;
        }

        // 5. Find candidate intents from analysis
        QuestionIntent selectedIntent = selectBestIntent(analysis, state, history);

        if (selectedIntent == null) {
            // Check default safety fallback intents
            selectedIntent = selectDefaultFallbackIntent(state, history);
        }

        if (selectedIntent == null) {
            return QuestionPlan.completed("All relevant clinical intents for current complaint addressed");
        }

        // 6. Generate wording with conversational awareness or external AI model gateway
        String candidateWording = null;
        String providerName = reasoningProvider.getProviderName();

        if (aiModelGateway != null && !aiModelGateway.isMock()) {
            try {
                String ctxText = context != null ? context.toFormattedHistory(8) : state.getChiefComplaint();
                candidateWording = aiModelGateway.generateQuestion(selectedIntent.getCode(), ctxText, language);
                providerName = aiModelGateway.getProviderName();
            } catch (Exception ex) {
                log.warn("External AI model gateway question generation failed ({}); falling back to local phrasing.", ex.getMessage());
            }
        }

        if (candidateWording == null || candidateWording.trim().isEmpty()) {
            candidateWording = generateConversationalWording(selectedIntent, context, language);
        }

        // 7. Run safety validator
        QuestionSafetyValidator.ValidationResult valResult = safetyValidator.validate(candidateWording, selectedIntent.getCode(), history);

        String finalWording;
        String generatedBy;

        if (valResult.isValid()) {
            finalWording = candidateWording;
            generatedBy = providerName;
        } else {
            log.warn("Generated question failed safety: '{}'. Falling back to approved template.", valResult.getRejectionReason());
            finalWording = "te".equalsIgnoreCase(language) ? getTeluguWording(selectedIntent, context) : selectedIntent.getDefaultApprovedTemplate();
            generatedBy = "ApprovedClinicalTemplateFallback";
        }

        // 6. Build question plan
        QuestionPlan plan = new QuestionPlan();
        plan.setIntentCode(selectedIntent.getCode());
        plan.setQuestionText(finalWording);
        plan.setAnswerType(selectedIntent.getAnswerType());
        plan.setPriority(selectedIntent.getPriority());
        plan.setSafetyCritical(selectedIntent.isSafetyCritical());
        plan.setCompleted(false);
        plan.setGeneratedBy(generatedBy);
        plan.setReason("Information intent: " + selectedIntent.getCode() + " for " + selectedIntent.getClinicalConcept());

        // Assign standard options based on answer type and language
        if ("te".equalsIgnoreCase(language)) {
            if ("YES_NO".equalsIgnoreCase(selectedIntent.getAnswerType())) {
                plan.setOptions(Arrays.asList("అవును", "లేదు", "సమాచారం ఖచ్చితంగా తెలియదు"));
            } else if ("SCALE_0_10".equalsIgnoreCase(selectedIntent.getAnswerType())) {
                plan.setOptions(Arrays.asList("0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "10"));
            } else if ("CHOICE".equalsIgnoreCase(selectedIntent.getAnswerType())) {
                if ("CHEST_PAIN_CHARACTER".equalsIgnoreCase(selectedIntent.getCode())) {
                    plan.setOptions(Arrays.asList("ఒత్తిడి / బరువుగా", "చురుక్కున పొడిచినట్లు", "మంటగా", "మందకొడి నొప్పి", "ఖచ్చితంగా తెలియదు"));
                } else {
                    plan.setOptions(Arrays.asList("అవును", "లేదు", "ఖచ్చితంగా తెలియదు"));
                }
            }
        } else {
            if ("SCALE_0_10".equalsIgnoreCase(selectedIntent.getAnswerType())) {
                plan.setOptions(Arrays.asList("0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "10"));
            } else if ("YES_NO".equalsIgnoreCase(selectedIntent.getAnswerType())) {
                plan.setOptions(Arrays.asList("YES", "NO", "Not Sure"));
            } else if ("CHOICE".equalsIgnoreCase(selectedIntent.getAnswerType())) {
                if ("CHEST_PAIN_CHARACTER".equalsIgnoreCase(selectedIntent.getCode())) {
                    plan.setOptions(Arrays.asList("Pressure / Squeezing", "Sharp / Stabbing", "Burning", "Dull Ache", "Not Sure"));
                } else {
                    plan.setOptions(Arrays.asList("YES", "NO", "Not Sure"));
                }
            }
        }

        return plan;
    }

    private QuestionIntent selectBestIntent(ClinicalContextResult analysis, ClinicalState state, QuestionHistory history) {
        for (ClinicalQuestionIntent cqi : analysis.getRecommendedIntents()) {
            QuestionIntent intent = QuestionIntent.fromCode(cqi.getIntentCode());
            if (intent != null && isIntentCandidate(intent, state, history)) {
                return intent;
            }
        }
        return null;
    }

    private QuestionIntent selectDefaultFallbackIntent(ClinicalState state, QuestionHistory history) {
        for (QuestionIntent intent : QuestionIntent.values()) {
            if (isIntentCandidate(intent, state, history)) {
                // Must be relevant to established, affirmed concepts in current state
                String concept = intent.getClinicalConcept();
                if (state.hasAffirmedConcept(concept) && !state.hasNegatedConcept(concept)) {
                    return intent;
                }
            }
        }
        return null;
    }

    private boolean isIntentCandidate(QuestionIntent intent, ClinicalState state, QuestionHistory history) {
        if (intent == null) return false;

        // Check if already asked in history
        if (history != null && history.isIntentAsked(intent.getCode())) {
            return false;
        }

        // Check if already answered in state
        if (state.isQuestionAnswered(intent.getCode())) {
            return false;
        }

        // Avoid duplicate question when high-confidence OCR document already supplied medication/allergy info
        if ("MEDICATION_CONFIRMATION".equals(intent.getCode()) && state.getFact("DOCUMENT_MEDICATION").isPresent()) {
            return false;
        }
        if ("ALLERGY_CONFIRMATION".equals(intent.getCode()) && state.getFact("DOCUMENT_PENICILLIN_ALLERGY").isPresent()) {
            return false;
        }

        // Check if fact already firmly established
        if ("CHEST_PAIN_CHARACTER".equals(intent.getCode()) && state.getFact("CHEST_PAIN_CHARACTER").isPresent()) {
            return false;
        }
        if ("CHEST_PAIN_AGGRAVATING".equals(intent.getCode()) && (state.getFact("EXERTIONAL").isPresent() || state.getFact("CHEST_PAIN_AGGRAVATING").isPresent())) {
            return false;
        }
        if ("CHEST_PAIN_SEVERITY".equals(intent.getCode()) && state.getFact("SEVERITY").isPresent()) {
            return false;
        }
        if ("CHEST_PAIN_DURATION".equals(intent.getCode()) && state.getFact("DURATION").isPresent()) {
            return false;
        }
        if ("FEVER_DURATION".equals(intent.getCode()) && state.getFact("DURATION").isPresent()) {
            return false;
        }
        if ("CHEST_PAIN_RADIATION".equals(intent.getCode()) && state.getFact("CHEST_PAIN_RADIATION").isPresent()) {
            return false;
        }

        // If symptom is negated, do not ask follow-ups
        if (state.hasNegatedConcept(intent.getClinicalConcept())) {
            return false;
        }

        return true;
    }

    private String generateConversationalWording(QuestionIntent intent, ConversationContext context, String language) {
        if (language != null && language.toLowerCase().startsWith("te")) {
            return getTeluguWording(intent, context);
        }
        // Translate or adapt wording with accumulated conversational context
        if (context != null && context.size() > 0) {
            String history = context.toFormattedHistory(8).toLowerCase(java.util.Locale.ROOT);
            boolean hasPressure = history.contains("pressure") || history.contains("heavy") || history.contains("tightness");

            if ("CHEST_PAIN_CHARACTER".equals(intent.getCode())) {
                return "Can you describe what the chest pain feels like?";
            }
            if ("CHEST_PAIN_AGGRAVATING".equals(intent.getCode())) {
                if (hasPressure) {
                    return "Does anything make the pressure better or worse?";
                }
                return "Does anything make the chest pain better or worse?";
            }
            if ("CHEST_PAIN_RADIATION".equals(intent.getCode())) {
                if (hasPressure) {
                    return "Does the pressure spread or radiate anywhere, such as to your left arm, jaw, neck, or back?";
                }
                return "Does the chest pain spread or radiate anywhere, such as to your left arm, jaw, neck, or back?";
            }
            if ("CHEST_PAIN_SEVERITY".equals(intent.getCode())) {
                if (hasPressure) {
                    return "On a scale from 0 to 10, how severe is the pressure in your chest right now?";
                }
                return "On a scale from 0 to 10, how severe is your chest discomfort right now?";
            }
            if ("BREATHING_DIFFICULTY".equals(intent.getCode())) {
                if (hasPressure) {
                    return "Are you also experiencing any shortness of breath along with the pressure?";
                }
                return "Are you experiencing any shortness of breath or difficulty breathing?";
            }
        }
        return intent.getDefaultApprovedTemplate();
    }

    private String getTeluguWording(QuestionIntent intent, ConversationContext context) {
        if (intent == null) return "దయచేసి మీ ఆరోగ్య వివరాలను తెలపండి.";
        switch (intent.getCode()) {
            case "CHEST_PAIN_CHARACTER":
                return "ఛాతీలో నొప్పి లేదా అసౌకర్యం ఎలా అనిపిస్తుందో వివరంగా చెప్పగలరా?";
            case "CHEST_PAIN_RADIATION":
                return "ఈ నొప్పి మీ ఎడమ చేయి, దవడ, మెడ లేదా వీపు వైపునకు ప్రాకుతోందా?";
            case "CHEST_PAIN_SEVERITY":
                return "0 నుండి 10 స్కేలులో, మీ ఛాతీ నొప్పి ఎంత తీవ్రంగా ఉందో చెప్పగలరా?";
            case "CHEST_PAIN_DURATION":
                return "ఈ ఛాతీ నొప్పి ఎప్పుడు ప్రారంభమైంది లేదా ఎంత సేపటి నుండి ఉంది?";
            case "CHEST_PAIN_AGGRAVATING":
                return "నడవటం, మెట్లు ఎక్కటం లేదా లోతుగా ఊపిరి పీల్చడం ద్వారా నొప్పి పెరుగుతోందా?";
            case "BREATHING_DIFFICULTY":
                return "మీకు ఆయాసం లేదా ఊపిరి తీసుకోవడంలో ఏమైనా ఇబ్బంది ఉందా?";
            case "BREATHING_DIFFICULTY_REST":
                return "విశ్రాంతిగా ఉన్నప్పుడు లేదా పడుకున్నప్పుడు కూడా ఆయాసంగా ఉందా?";
            case "FEVER_DURATION":
                return "జ్వరం ఎన్ని రోజుల నుండి ఉందో చెప్పగలరా?";
            case "FEVER_CHILLS":
                return "జ్వరంతో పాటు ఒళ్లు వణకడం లేదా చలిగా అనిపిస్తోందా?";
            case "FEVER_TEMPERATURE":
                return "ఇంట్లో థర్మామీటర్‌తో ఉష్ణోగ్రత చూసుకున్నారా, ఎంత వచ్చింది?";
            case "HEADACHE_SEVERITY":
                return "0 నుండి 10 స్కేలులో, మీ తలనొప్పి ఎంత తీవ్రంగా ఉందో తెలపండి?";
            case "HEADACHE_RED_FLAGS":
                return "తలనొప్పితో పాటు మెడ పట్టుకుపోవడం, కంటి చూపు మసకబారడం లేదా వెలుతురు చూడలేకపోవడం ఉందా?";
            case "DIZZINESS_SYNCOPE":
                return "మీకు కళ్లు తిరగడం లేదా స్పృహ తప్పి పడిపోవడం జరిగిందా?";
            case "ABDOMINAL_LOCATION":
                return "కడుపు నొప్పి ఏ భాగంలో ఉందో తెలపండి (పై భాగం, కుడి వైపు, ఎడమ వైపు లేదా పొత్తికడుపు)?";
            case "ABDOMINAL_VOMITING":
                return "మీకు వరుసగా వాంతులు అవ్వడం లేదా వాంతులయ్యే భావన ఉందా?";
            case "COUGH_DURATION":
                return "ఈ దగ్గు ఎన్ని రోజుల నుండి వస్తోంది?";
            case "COUGH_HEMOPTYSIS":
                return "దగ్గినప్పుడు కఫంలో రక్తం పడటం గమనించారా?";
            case "ALLERGY_CONFIRMATION":
                return "మీకు పెన్సిలిన్ వంటి ఏవైనా మందుల వల్ల అలర్జీ ఉందా?";
            case "MEDICATION_CONFIRMATION":
                return "మీరు ప్రస్తుతం క్రమం తప్పకుండా ఏదైనా బిపి, షుగర్ లేదా ఇతర మందులు వాడుతున్నారా?";
            default:
                return intent.getDefaultApprovedTemplate();
        }
    }
}
