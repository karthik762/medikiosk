package com.medikiosk.clinical.adaptive;

import com.medikiosk.clinical.state.ClinicalState;
import com.medikiosk.entity.Question;
import com.medikiosk.repository.QuestionRepository;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Deterministic adaptive question engine selecting the next best clinical question
 * based on clinical state, safety-critical priority, relevance, and dependency satisfaction.
 */
@Component
public class AdaptiveQuestionEngine {

    private final QuestionRepository questionRepository;

    public AdaptiveQuestionEngine(QuestionRepository questionRepository) {
        this.questionRepository = questionRepository;
    }

    public NextQuestionResponse determineNextQuestion(ClinicalState state) {
        if (state == null) {
            return NextQuestionResponse.completed("Ready for review");
        }

        List<Question> allActive = questionRepository.findByActiveTrue();

        // 1. Filter out already answered questions
        List<Question> candidates = allActive.stream()
                .filter(q -> !state.isQuestionAnswered(q.getQuestionCode()))
                .collect(Collectors.toList());

        // 2. Filter out questions whose target info is already known from NLP extraction
        candidates = candidates.stream()
                .filter(q -> !isConceptAlreadyKnown(q, state))
                .filter(q -> evaluateDependencies(q, state))
                .collect(Collectors.toList());

        if (candidates.isEmpty()) {
            return NextQuestionResponse.completed("Intake questions complete");
        }

        // 3. Rank candidates
        // Score calculation:
        // - Safety critical directly related to affirmed symptom: +1000
        // - Directly related to affirmed symptom: +500
        // - Missing essential baseline: +100
        // - Lower priority integer in database breaks ties
        Question bestQuestion = candidates.stream()
                .min(Comparator.comparingInt(q -> calculateRankingScore(q, state)))
                .orElse(null);

        if (bestQuestion == null) {
            return NextQuestionResponse.completed("Intake questions complete");
        }

        return mapToResponse(bestQuestion, state);
    }

    private boolean isConceptAlreadyKnown(Question q, ClinicalState state) {
        String code = q.getQuestionCode();
        // Never re-ask chief complaint if already entered
        if ("Q_PRIMARY_COMPLAINT".equals(code) && state.getChiefComplaint() != null && !state.getChiefComplaint().trim().isEmpty()) {
            return true;
        }
        // If duration is already extracted by NLP from complaint, don't ask duration again
        if (code.contains("DURATION") && state.getFact("DURATION").isPresent()) {
            return true;
        }
        // If severity is already extracted by NLP, don't ask severity again
        if (code.contains("SEVERITY") && state.getFact("SEVERITY").isPresent()) {
            return true;
        }
        // If specific concept is negated (e.g. patient said "no chest pain"), skip chest pain follow-ups
        if (q.getClinicalConceptCode() != null && state.hasNegatedConcept(q.getClinicalConceptCode())) {
            return true;
        }
        return false;
    }

    private boolean evaluateDependencies(Question q, ClinicalState state) {
        String code = q.getQuestionCode();
        if ("Q_PAST_CONDITIONS_DETAIL".equals(code)) {
            String pastAnswer = state.getAnswer("Q_PAST_CONDITIONS");
            return "YES".equalsIgnoreCase(pastAnswer) || "TRUE".equalsIgnoreCase(pastAnswer);
        }
        if ("Q_KNOWN_ALLERGIES_DETAIL".equals(code)) {
            String allergyAnswer = state.getAnswer("Q_KNOWN_ALLERGIES");
            return "YES".equalsIgnoreCase(allergyAnswer) || "TRUE".equalsIgnoreCase(allergyAnswer);
        }
        // Symptom-specific questions only apply if the concept is affirmed in chief complaint or symptoms
        String concept = q.getClinicalConceptCode();
        if (concept != null && !concept.equals("CHIEF_COMPLAINT") && !concept.equals("PAST_HISTORY") && !concept.equals("MEDICATION") && !concept.equals("ALLERGY")) {
            return state.hasAffirmedConcept(concept);
        }
        return true;
    }

    private int calculateRankingScore(Question q, ClinicalState state) {
        int base = q.getPriority() != null ? q.getPriority() : 50;
        String concept = q.getClinicalConceptCode();

        boolean isAffirmedSymptom = concept != null && state.hasAffirmedConcept(concept);

        if (q.getSafetyCritical() && isAffirmedSymptom) {
            return base - 500; // Highest urgency (e.g. chest pain radiation/dyspnea)
        }
        if (isAffirmedSymptom) {
            return base - 200; // Directly relevant to complaint
        }
        if (q.getSafetyCritical()) {
            return base - 100; // General safety critical (e.g. allergies)
        }
        return base;
    }

    private NextQuestionResponse mapToResponse(Question q, ClinicalState state) {
        NextQuestionResponse resp = new NextQuestionResponse();
        resp.setQuestionId(q.getId());
        resp.setQuestionCode(q.getQuestionCode());
        resp.setQuestionText(q.getQuestionText());
        resp.setQuestionType(q.getQuestionType());
        resp.setCategory(q.getCategory());
        resp.setClinicalConceptCode(q.getClinicalConceptCode());
        resp.setSafetyCritical(q.getSafetyCritical());
        resp.setPriority(q.getPriority());
        resp.setCompleted(false);

        int answeredCount = state.getAnsweredQuestionCodes().size();
        resp.setProgressStage("Step " + (answeredCount + 1) + " of clinical intake");

        // Set standard options based on question type
        if ("SCALE_0_10".equalsIgnoreCase(q.getQuestionType())) {
            resp.setOptions(Arrays.asList("0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "10"));
        } else if ("YES_NO".equalsIgnoreCase(q.getQuestionType())) {
            resp.setOptions(Arrays.asList("YES", "NO"));
        } else if ("CHOICE".equalsIgnoreCase(q.getQuestionType())) {
            if ("Q_SOB_ONSET".equals(q.getQuestionCode())) {
                resp.setOptions(Arrays.asList("Suddenly (Minutes/Hours)", "Gradually (Days/Weeks)"));
            }
        }

        // Explanation / Debug Rationale
        if (q.getSafetyCritical()) {
            resp.setReason("Safety-critical red flag screening for " + (q.getClinicalConceptCode() != null ? q.getClinicalConceptCode() : "patient safety"));
        } else if (q.getClinicalConceptCode() != null && state.hasAffirmedConcept(q.getClinicalConceptCode())) {
            resp.setReason("Follow-up characterization for reported " + q.getClinicalConceptCode());
        } else {
            resp.setReason("Standard clinical history and safety baseline");
        }

        if (q.getClinicalConceptCode() != null) {
            resp.getTriggeringConcepts().add(q.getClinicalConceptCode());
        }

        return resp;
    }
}
