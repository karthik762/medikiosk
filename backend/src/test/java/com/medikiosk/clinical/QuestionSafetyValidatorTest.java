package com.medikiosk.clinical;

import com.medikiosk.clinical.adaptive.QuestionHistory;
import com.medikiosk.clinical.adaptive.QuestionSafetyValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class QuestionSafetyValidatorTest {

    private QuestionSafetyValidator validator;
    private QuestionHistory history;

    @BeforeEach
    void setUp() {
        validator = new QuestionSafetyValidator();
        history = new QuestionHistory();
    }

    @Test
    @DisplayName("Validator accepts safe clinical intake questions")
    void testAcceptsSafeQuestions() {
        String question = "Does the chest pain spread or radiate to your left arm or jaw?";
        QuestionSafetyValidator.ValidationResult result = validator.validate(question, "CHEST_PAIN_RADIATION", history);

        assertTrue(result.isValid(), "Safe question must be validated as valid");
        assertNull(result.getRejectionReason());
    }

    @Test
    @DisplayName("Validator rejects diagnostic statements and hypotheses")
    void testRejectsDiagnosticStatements() {
        String question = "It seems you might have a myocardial infarction. Does your chest hurt?";
        QuestionSafetyValidator.ValidationResult result = validator.validate(question, "CHEST_PAIN_RADIATION", history);

        assertFalse(result.isValid(), "Diagnostic statements must be rejected");
        assertTrue(result.getRejectionReason().contains("Diagnostic suggestion"));
    }

    @Test
    @DisplayName("Validator rejects prescribing and medication recommendations")
    void testRejectsMedicationRecommendations() {
        String question = "You should take an aspirin right away before seeing the doctor.";
        QuestionSafetyValidator.ValidationResult result = validator.validate(question, "CHEST_PAIN_SEVERITY", history);

        assertFalse(result.isValid(), "Prescription and treatment advice must be rejected");
        assertTrue(result.getRejectionReason().contains("Treatment or prescribing advice"));
    }

    @Test
    @DisplayName("Validator rejects false reassurance")
    void testRejectsFalseReassurance() {
        String question = "Don't worry, you are fine. What is your pain score?";
        QuestionSafetyValidator.ValidationResult result = validator.validate(question, "CHEST_PAIN_SEVERITY", history);

        assertFalse(result.isValid(), "False reassurance must be rejected");
        assertTrue(result.getRejectionReason().contains("reassurance"));
    }

    @Test
    @DisplayName("Validator rejects repetitive questions asked in recent history")
    void testRejectsRepetitiveQuestions() {
        history.recordQuestion("CHEST_PAIN_SEVERITY", "How severe is your chest pain from 0 to 10?", "8", "MOCK");

        String similarQuestion = "How severe is your chest pain from 0 to 10?";
        QuestionSafetyValidator.ValidationResult result = validator.validate(similarQuestion, "CHEST_PAIN_SEVERITY", history);

        assertFalse(result.isValid(), "Repetitive question must be rejected");
        assertTrue(result.getRejectionReason().contains("already explored"));
    }
}
