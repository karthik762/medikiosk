package com.medikiosk.clinical.adaptive;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Tracks the history of questions presented to the patient during an intake session,
 * preventing repeated questions or duplicate intents.
 */
public class QuestionHistory {

    public static class QuestionRecord {
        private String intentCode;
        private String questionText;
        private String answerText;
        private Instant askedAt;
        private String generatedBy;

        public QuestionRecord(String intentCode, String questionText, String generatedBy) {
            this.intentCode = intentCode;
            this.questionText = questionText;
            this.generatedBy = generatedBy;
            this.askedAt = Instant.now();
        }

        public String getIntentCode() {
            return intentCode;
        }

        public String getQuestionText() {
            return questionText;
        }

        public String getAnswerText() {
            return answerText;
        }

        public void setAnswerText(String answerText) {
            this.answerText = answerText;
        }

        public Instant getAskedAt() {
            return askedAt;
        }

        public String getGeneratedBy() {
            return generatedBy;
        }
    }

    private static final java.util.Map<String, String> CODE_ALIASES = new java.util.HashMap<>();
    static {
        CODE_ALIASES.put("Q_HEADACHE_VISION", "HEADACHE_RED_FLAGS");
        CODE_ALIASES.put("HEADACHE_VISION", "HEADACHE_RED_FLAGS");
        CODE_ALIASES.put("HEADACHE_RED_FLAGS", "Q_HEADACHE_VISION");
        CODE_ALIASES.put("NEURO_RED_FLAGS", "HEADACHE_RED_FLAGS");
        CODE_ALIASES.put("Q_HEADACHE_SEVERITY", "HEADACHE_SEVERITY");

        CODE_ALIASES.put("Q_KNOWN_ALLERGIES", "ALLERGY_CONFIRMATION");
        CODE_ALIASES.put("KNOWN_ALLERGIES", "ALLERGY_CONFIRMATION");
        CODE_ALIASES.put("ALLERGY_CONFIRMATION", "Q_KNOWN_ALLERGIES");

        CODE_ALIASES.put("Q_CURRENT_MEDS", "MEDICATION_CONFIRMATION");
        CODE_ALIASES.put("CURRENT_MEDS", "MEDICATION_CONFIRMATION");
        CODE_ALIASES.put("MEDICATION_CONFIRMATION", "Q_CURRENT_MEDS");

        CODE_ALIASES.put("Q_CP_SEVERITY_SCALE", "CHEST_PAIN_SEVERITY");
        CODE_ALIASES.put("CP_SEVERITY_SCALE", "CHEST_PAIN_SEVERITY");
        CODE_ALIASES.put("CHEST_PAIN_SEVERITY", "Q_CP_SEVERITY_SCALE");

        CODE_ALIASES.put("Q_CHEST_PAIN_BREATHING", "BREATHING_DIFFICULTY");
        CODE_ALIASES.put("CHEST_PAIN_BREATHING", "BREATHING_DIFFICULTY");
        CODE_ALIASES.put("BREATHING_DIFFICULTY", "Q_CHEST_PAIN_BREATHING");

        CODE_ALIASES.put("Q_SOB_REST", "BREATHING_DIFFICULTY_REST");
        CODE_ALIASES.put("SOB_REST", "BREATHING_DIFFICULTY_REST");

        CODE_ALIASES.put("Q_ABDOMINAL_LOCATION", "ABDOMINAL_LOCATION");
        CODE_ALIASES.put("Q_ABDOMINAL_VOMITING", "ABDOMINAL_VOMITING");
        CODE_ALIASES.put("Q_CHEST_PAIN_AGGRAVATING", "CHEST_PAIN_AGGRAVATING");
        CODE_ALIASES.put("Q_CHEST_PAIN_RADIATION", "CHEST_PAIN_RADIATION");
        CODE_ALIASES.put("Q_CHEST_PAIN_CHARACTER", "CHEST_PAIN_CHARACTER");
        CODE_ALIASES.put("Q_CHEST_PAIN_DURATION", "CHEST_PAIN_DURATION");
        CODE_ALIASES.put("Q_FEVER_DURATION", "FEVER_DURATION");
        CODE_ALIASES.put("Q_FEVER_CHILLS", "FEVER_CHILLS");
        CODE_ALIASES.put("Q_FEVER_TEMPERATURE", "FEVER_TEMPERATURE");
        CODE_ALIASES.put("Q_COUGH_DURATION", "COUGH_DURATION");
        CODE_ALIASES.put("Q_COUGH_BLOOD", "COUGH_HEMOPTYSIS");
        CODE_ALIASES.put("COUGH_BLOOD", "COUGH_HEMOPTYSIS");
    }

    private final List<QuestionRecord> records = new ArrayList<>();
    private final Set<String> askedIntents = new HashSet<>();

    public synchronized void recordQuestion(String intentCode, String questionText, String generatedBy) {
        if (intentCode != null) {
            String norm = intentCode.toUpperCase();
            askedIntents.add(norm);
            if (norm.startsWith("Q_")) {
                askedIntents.add(norm.substring(2));
            }
            String alias = CODE_ALIASES.get(norm);
            if (alias != null) {
                askedIntents.add(alias);
            }
        }
        records.add(new QuestionRecord(intentCode, questionText, generatedBy));
    }

    public synchronized void recordQuestion(String intentCode, String questionText, String answerText, String generatedBy) {
        if (intentCode != null) {
            String norm = intentCode.toUpperCase();
            askedIntents.add(norm);
            if (norm.startsWith("Q_")) {
                askedIntents.add(norm.substring(2));
            }
            String alias = CODE_ALIASES.get(norm);
            if (alias != null) {
                askedIntents.add(alias);
            }
        }
        QuestionRecord record = new QuestionRecord(intentCode, questionText, generatedBy);
        record.setAnswerText(answerText);
        records.add(record);
    }

    public int getAskedCount() {
        return records.size();
    }

    public synchronized void recordAnswer(String intentCode, String answerText) {
        for (int i = records.size() - 1; i >= 0; i--) {
            QuestionRecord r = records.get(i);
            if (intentCode != null && (intentCode.equalsIgnoreCase(r.getIntentCode()) || intentCode.equalsIgnoreCase("Q_" + r.getIntentCode()))) {
                r.setAnswerText(answerText);
                break;
            }
        }
    }

    public boolean isIntentAsked(String intentCode) {
        if (intentCode == null) return false;
        String norm = intentCode.toUpperCase();
        if (askedIntents.contains(norm)) return true;
        if (norm.startsWith("Q_") && askedIntents.contains(norm.substring(2))) return true;
        if (askedIntents.contains("Q_" + norm)) return true;
        String alias = CODE_ALIASES.get(norm);
        if (alias != null && askedIntents.contains(alias)) return true;
        return false;
    }

    public boolean isQuestionTextSimilar(String candidateText) {
        if (candidateText == null) return false;
        String normalized = candidateText.toLowerCase().replaceAll("[^a-z0-9]", "");
        for (QuestionRecord r : records) {
            String existingNorm = r.getQuestionText().toLowerCase().replaceAll("[^a-z0-9]", "");
            if (normalized.equals(existingNorm) || normalized.contains(existingNorm) || existingNorm.contains(normalized)) {
                return true;
            }
        }
        return false;
    }

    public List<QuestionRecord> getRecords() {
        return Collections.unmodifiableList(records);
    }

    public int size() {
        return records.size();
    }
}
