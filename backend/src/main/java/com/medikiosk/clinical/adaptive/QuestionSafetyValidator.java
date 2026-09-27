package com.medikiosk.clinical.adaptive;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Locale;
import java.util.regex.Pattern;

/**
 * Validates AI-generated patient questions against clinical governance and safety boundaries.
 * Rejects diagnosis, treatment recommendations, leading clinical suggestions, and repeated questions.
 */
@Component
public class QuestionSafetyValidator {

    private static final Logger log = LoggerFactory.getLogger(QuestionSafetyValidator.class);

    // Diagnostic assertion or inquiry
    private static final Pattern DIAGNOSIS_PATTERN = Pattern.compile(
            "\\b(you have|you are suffering from|diagnosis is|diagnosed with|heart attack|myocardial infarction|stroke|pneumonia|covid|angina|pulmonary embolism|do you think you have|could this be)\\b",
            Pattern.CASE_INSENSITIVE
    );

    // Treatment or prescribing recommendations
    private static final Pattern TREATMENT_PATTERN = Pattern.compile(
            "\\b(take aspirin|take paracetamol|take .*mg|prescribe|prescription|medication recommendation|should take|start taking|increase dose|stop taking|treatment plan|remedy|paracetamol)\\b",
            Pattern.CASE_INSENSITIVE
    );

    // False reassurance or safety assertions
    private static final Pattern FALSE_REASSURANCE_PATTERN = Pattern.compile(
            "\\b(don't worry|you are completely safe|nothing to worry about|it is harmless|it is nothing serious|you are fine)\\b",
            Pattern.CASE_INSENSITIVE
    );

    // Inappropriate or unsafe phrasing
    private static final Pattern INAPPROPRIATE_PATTERN = Pattern.compile(
            "\\b(are you going to die|fatal|is it terminal|do you fear death|sexual history|credit card|bank account)\\b",
            Pattern.CASE_INSENSITIVE
    );

    public static class ValidationResult {
        private final boolean valid;
        private final String rejectionReason;

        private ValidationResult(boolean valid, String rejectionReason) {
            this.valid = valid;
            this.rejectionReason = rejectionReason;
        }

        public static ValidationResult pass() {
            return new ValidationResult(true, null);
        }

        public static ValidationResult fail(String reason) {
            return new ValidationResult(false, reason);
        }

        public boolean isValid() {
            return valid;
        }

        public String getRejectionReason() {
            return rejectionReason;
        }
    }

    /**
     * Validates candidate question wording against safety boundaries and history.
     */
    public ValidationResult validate(String questionText, String intentCode, QuestionHistory history) {
        if (questionText == null || questionText.trim().isEmpty()) {
            return ValidationResult.fail("Question text is empty");
        }

        String trimmed = questionText.trim();
        String lower = trimmed.toLowerCase(Locale.ROOT);

        // 1. Length check
        if (trimmed.length() < 5 || trimmed.length() > 300) {
            return ValidationResult.fail("Question length outside acceptable boundary (5-300 chars)");
        }

        // 2. Reject diagnostic language
        if (DIAGNOSIS_PATTERN.matcher(lower).find()) {
            log.warn("Safety rejection: diagnostic suggestion detected in: '{}'", trimmed);
            return ValidationResult.fail("Diagnostic suggestion or naming detected");
        }

        // 3. Reject treatment or prescribing recommendations
        if (TREATMENT_PATTERN.matcher(lower).find()) {
            log.warn("Safety rejection: treatment advice detected in: '{}'", trimmed);
            return ValidationResult.fail("Treatment or prescribing advice detected");
        }

        // 4. Reject false reassurance
        if (FALSE_REASSURANCE_PATTERN.matcher(lower).find()) {
            log.warn("Safety rejection: false reassurance detected in: '{}'", trimmed);
            return ValidationResult.fail("Unsafe reassurance assertion detected");
        }

        // 5. Reject inappropriate language
        if (INAPPROPRIATE_PATTERN.matcher(lower).find()) {
            log.warn("Safety rejection: inappropriate question detected in: '{}'", trimmed);
            return ValidationResult.fail("Inappropriate questioning detected");
        }

        // 6. Check for repetition against session question history
        if (history != null) {
            if (intentCode != null && history.isIntentAsked(intentCode)) {
                log.warn("Safety rejection: intent '{}' was already explored", intentCode);
                return ValidationResult.fail("Intent already explored in session history");
            }
            if (history.isQuestionTextSimilar(trimmed)) {
                log.warn("Safety rejection: question text similar to previous turn");
                return ValidationResult.fail("Duplicate or highly similar question wording");
            }
        }

        return ValidationResult.pass();
    }
}
