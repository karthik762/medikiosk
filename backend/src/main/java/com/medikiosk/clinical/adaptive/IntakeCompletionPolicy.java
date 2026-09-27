package com.medikiosk.clinical.adaptive;

import com.medikiosk.clinical.state.ClinicalState;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Evaluates whether an intake session has gathered sufficient clinical information
 * or reached safe conversational completion boundaries.
 * 
 * NOTE: The runaway safety ceiling (MAX_AI_QUESTIONS) is purely a protection against
 * infinite conversational loops, NOT the clinical completion criterion. Clinical completion
 * is driven by symptom characterization, safety screening, conflict resolution, and patient intent.
 */
@Component
public class IntakeCompletionPolicy {

    private final int maxAiQuestionsCeiling;

    public IntakeCompletionPolicy(
            @Value("${medikiosk.adaptive.max-questions:20}") int maxAiQuestionsCeiling) {
        this.maxAiQuestionsCeiling = maxAiQuestionsCeiling;
    }

    public IntakeCompletionPolicy() {
        this(20);
    }

    public static class CompletionStatus {
        private final boolean complete;
        private final String reason;

        public CompletionStatus(boolean complete, String reason) {
            this.complete = complete;
            this.reason = reason;
        }

        public boolean isComplete() {
            return complete;
        }

        public String getReason() {
            return reason;
        }
    }

    public CompletionStatus evaluateCompletion(ClinicalState state, QuestionHistory history) {
        if (state == null) {
            return new CompletionStatus(true, "No active clinical state");
        }

        int turnsAsked = history != null ? history.size() : state.getAnsweredQuestionCodes().size();

        // 1. Runaway Safety Ceiling Protection (Ceiling protection, NOT completion criterion)
        if (turnsAsked >= maxAiQuestionsCeiling) {
            return new CompletionStatus(true, "Runaway safety ceiling limit (" + maxAiQuestionsCeiling + " questions) reached");
        }

        // 2. Require chief complaint
        if (state.getChiefComplaint() == null || state.getChiefComplaint().trim().isEmpty()) {
            return new CompletionStatus(false, "Chief complaint required");
        }

        // 3. Patient Termination Intent ("That's all", "Nothing else", "I want to finish")
        boolean patientWantsToFinish = state.hasAffirmedConcept("PATIENT_TERMINATION_INTENT");

        // 4. Safety-Critical Information Checks
        boolean missingSafetyCritical = false;

        if (state.hasAffirmedConcept("CHEST_PAIN") && !state.hasNegatedConcept("CHEST_PAIN")) {
            boolean hasRadiation = state.hasAffirmedConcept("CHEST_PAIN_RADIATION") || state.hasNegatedConcept("CHEST_PAIN_RADIATION") || (history != null && history.isIntentAsked("CHEST_PAIN_RADIATION"));
            boolean hasSeverity = state.getFact("SEVERITY").isPresent() || (history != null && history.isIntentAsked("CHEST_PAIN_SEVERITY"));
            boolean hasCharacter = state.getFact("CHEST_PAIN_CHARACTER").isPresent() || (history != null && history.isIntentAsked("CHEST_PAIN_CHARACTER"));
            if (!hasRadiation || (!hasSeverity && !hasCharacter)) {
                missingSafetyCritical = true;
                if (!patientWantsToFinish) {
                    return new CompletionStatus(false, "Safety-critical cardiac characterization pending");
                }
            }
        }

        if (state.hasAffirmedConcept("HEADACHE") && !state.hasNegatedConcept("HEADACHE")) {
            boolean hasRedFlags = state.hasAffirmedConcept("NEURO_RED_FLAGS") || state.hasNegatedConcept("NEURO_RED_FLAGS") || (history != null && history.isIntentAsked("HEADACHE_RED_FLAGS"));
            if (!hasRedFlags) {
                missingSafetyCritical = true;
                if (!patientWantsToFinish) {
                    return new CompletionStatus(false, "Safety-critical neurological screening pending");
                }
            }
        }

        if (state.hasAffirmedConcept("ABDOMINAL_PAIN") && !state.hasNegatedConcept("ABDOMINAL_PAIN")) {
            boolean hasLocation = state.getFact("ABDOMINAL_LOCATION").isPresent() || (history != null && history.isIntentAsked("ABDOMINAL_LOCATION"));
            boolean hasVomiting = state.hasAffirmedConcept("VOMITING") || state.hasNegatedConcept("VOMITING") || (history != null && history.isIntentAsked("ABDOMINAL_VOMITING"));
            if (!hasLocation || !hasVomiting) {
                missingSafetyCritical = true;
                if (!patientWantsToFinish) {
                    return new CompletionStatus(false, "Safety-critical acute abdomen screening pending");
                }
            }
        }

        // 5. If patient expressed explicit desire to finish and minimum safety items addressed
        if (patientWantsToFinish) {
            return new CompletionStatus(true, "Patient requested intake completion");
        }

        // 6. Intelligent Symptom Completion Criteria:
        // Complete if chief complaint is characterized and all safety-critical checks are satisfied
        boolean hasDuration = state.getFact("DURATION").isPresent();
        boolean hasSeverity = state.getFact("SEVERITY").isPresent();

        if (!missingSafetyCritical && turnsAsked >= 1) {
            // HEADACHE: Red flags + (Severity or Duration or 2 turns)
            if (state.hasAffirmedConcept("HEADACHE") && !state.hasNegatedConcept("HEADACHE")) {
                boolean hasRedFlags = state.hasAffirmedConcept("NEURO_RED_FLAGS") || state.hasNegatedConcept("NEURO_RED_FLAGS") || (history != null && history.isIntentAsked("HEADACHE_RED_FLAGS"));
                if (hasRedFlags && (hasSeverity || hasDuration || turnsAsked >= 2)) {
                    return new CompletionStatus(true, "Headache clinical intake characterization complete");
                }
            }

            // CHEST PAIN: Radiation + (Aggravating factor or 2 turns) + (Character or Severity)
            if (state.hasAffirmedConcept("CHEST_PAIN") && !state.hasNegatedConcept("CHEST_PAIN")) {
                boolean hasRadiation = state.hasAffirmedConcept("CHEST_PAIN_RADIATION") || state.hasNegatedConcept("CHEST_PAIN_RADIATION") || (history != null && history.isIntentAsked("CHEST_PAIN_RADIATION"));
                boolean hasAggravating = state.getFact("EXERTIONAL").isPresent() || state.getFact("CHEST_PAIN_AGGRAVATING").isPresent() || (history != null && history.isIntentAsked("CHEST_PAIN_AGGRAVATING"));
                boolean hasCharacter = state.getFact("CHEST_PAIN_CHARACTER").isPresent() || (history != null && history.isIntentAsked("CHEST_PAIN_CHARACTER"));
                if (hasRadiation && (hasAggravating || turnsAsked >= 2) && (hasCharacter || hasSeverity || turnsAsked >= 2)) {
                    return new CompletionStatus(true, "Chest pain clinical characterization fully complete");
                }
            }

            // ABDOMINAL PAIN: Location + Vomiting check + (Food relation or 2 turns)
            if (state.hasAffirmedConcept("ABDOMINAL_PAIN") && !state.hasNegatedConcept("ABDOMINAL_PAIN")) {
                boolean hasLocation = state.getFact("ABDOMINAL_LOCATION").isPresent() || (history != null && history.isIntentAsked("ABDOMINAL_LOCATION"));
                boolean hasVomiting = state.hasAffirmedConcept("VOMITING") || state.hasNegatedConcept("VOMITING") || (history != null && history.isIntentAsked("ABDOMINAL_VOMITING"));
                if (hasLocation && hasVomiting && turnsAsked >= 2) {
                    return new CompletionStatus(true, "Abdominal pain clinical characterization fully complete");
                }
            }

            // FEVER: Duration or Chills check
            if (state.hasAffirmedConcept("FEVER") && !state.hasNegatedConcept("FEVER")) {
                boolean hasChills = state.hasAffirmedConcept("CHILLS") || state.hasNegatedConcept("CHILLS") || (history != null && history.isIntentAsked("FEVER_CHILLS"));
                if ((hasDuration || hasChills || hasSeverity) && turnsAsked >= 2) {
                    return new CompletionStatus(true, "Fever clinical intake characterization complete");
                }
            }

            // RESPIRATORY / COUGH
            if (state.hasAffirmedConcept("COUGH") && !state.hasNegatedConcept("COUGH")) {
                boolean hasFeverCheck = state.hasAffirmedConcept("FEVER") || state.hasNegatedConcept("FEVER");
                if ((hasDuration || hasFeverCheck) && turnsAsked >= 2) {
                    return new CompletionStatus(true, "Respiratory intake characterization complete");
                }
            }

            // GENERAL COMPLETION CEILING FOR OTHER COMPLAINTS
            if (turnsAsked >= 2) {
                return new CompletionStatus(true, "Clinical intake characterization complete");
            }
        }

        return new CompletionStatus(false, "Intake in progress");
    }
}

