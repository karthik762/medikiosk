package com.medikiosk.clinical;

import com.medikiosk.clinical.adaptive.AIQuestionPlanner;
import com.medikiosk.clinical.adaptive.IntakeCompletionPolicy;
import com.medikiosk.clinical.adaptive.QuestionHistory;
import com.medikiosk.clinical.adaptive.QuestionSafetyValidator;
import com.medikiosk.clinical.conversation.ConversationContext;
import com.medikiosk.clinical.conversation.ConversationTurn;
import com.medikiosk.clinical.reasoning.ClinicalContextRequest;
import com.medikiosk.clinical.reasoning.ClinicalContextResult;
import com.medikiosk.clinical.reasoning.MockClinicalReasoningProvider;
import com.medikiosk.clinical.state.ClinicalState;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class ClinicalContextReasoningTest {

    private MockClinicalReasoningProvider reasoningProvider;
    private QuestionSafetyValidator safetyValidator;
    private IntakeCompletionPolicy completionPolicy;
    private AIQuestionPlanner planner;

    @BeforeEach
    void setUp() {
        reasoningProvider = new MockClinicalReasoningProvider();
        safetyValidator = new QuestionSafetyValidator();
        completionPolicy = new IntakeCompletionPolicy();
        planner = new AIQuestionPlanner(reasoningProvider, safetyValidator, completionPolicy);
    }

    @Test
    @DisplayName("Extracts symptoms, duration, and exertional pattern from natural language")
    void testNaturalLanguageExtraction() {
        String input = "I've had chest pain since yesterday and it gets worse when I walk.";
        ClinicalContextRequest request = new ClinicalContextRequest(UUID.randomUUID(), input, null, "en");

        ClinicalContextResult result = reasoningProvider.analyzeContext(request);

        assertNotNull(result);
        assertTrue(result.getExtractedFacts().stream().anyMatch(f -> "CHEST_PAIN".equals(f.getConceptCode()) && f.isAffirmed()));
        assertTrue(result.getExtractedFacts().stream().anyMatch(f -> "DURATION".equals(f.getConceptCode()) && "1 DAY".equals(f.getValue())));
        assertTrue(result.getExtractedFacts().stream().anyMatch(f -> "EXERTIONAL".equals(f.getConceptCode()) && f.isAffirmed()));
    }

    @Test
    @DisplayName("Negation handling: 'I don't have fever' marks fever as NEGATED, never affirmed")
    void testNegationHandling() {
        String input = "I have stomach pain for 2 days, but I don't have fever and no vomiting.";
        ClinicalContextRequest request = new ClinicalContextRequest(UUID.randomUUID(), input, null, "en");

        ClinicalContextResult result = reasoningProvider.analyzeContext(request);

        assertNotNull(result);
        assertTrue(result.getExtractedFacts().stream().anyMatch(f -> "ABDOMINAL_PAIN".equals(f.getConceptCode()) && f.isAffirmed()));
        assertTrue(result.getExtractedFacts().stream().anyMatch(f -> "FEVER".equals(f.getConceptCode()) && f.isNegated()));
        assertFalse(result.getExtractedFacts().stream().anyMatch(f -> "FEVER".equals(f.getConceptCode()) && f.isAffirmed()));
    }

    @Test
    @DisplayName("Severity extraction: 'severe' or 'extreme' pain")
    void testSeverityExtraction() {
        String input = "I have extreme severe headache since this morning.";
        ClinicalContextRequest request = new ClinicalContextRequest(UUID.randomUUID(), input, null, "en");

        ClinicalContextResult result = reasoningProvider.analyzeContext(request);

        assertNotNull(result);
        assertTrue(result.getExtractedFacts().stream().anyMatch(f -> "HEADACHE".equals(f.getConceptCode()) && f.isAffirmed()));
        assertTrue(result.getExtractedFacts().stream().anyMatch(f -> "SEVERITY".equals(f.getConceptCode()) && "SEVERE".equals(f.getValue())));
    }

    @Test
    @DisplayName("Multi-turn context: AI deduces pressure characterization across conversational turns")
    void testMultiTurnContextProgression() {
        ClinicalState state = new ClinicalState(UUID.randomUUID(), UUID.randomUUID(), "PAT-401");
        state.setChiefComplaint("I have chest discomfort since yesterday.");

        ConversationContext context = new ConversationContext();
        context.addTurn(ConversationTurn.patient("I have chest discomfort since yesterday."));
        context.addTurn(ConversationTurn.system("Can you describe what the chest pain feels like?"));
        context.addTurn(ConversationTurn.patient("It feels like pressure and squeezing."));

        QuestionHistory history = new QuestionHistory();
        history.recordQuestion("Q_CHEST_PAIN_CHARACTER", "Can you describe what the chest pain feels like?", "It feels like pressure and squeezing.", "CHEST_PAIN_CHARACTER");

        AIQuestionPlanner.QuestionPlan plan = planner.planNextQuestion(state, context, history, "en");

        assertNotNull(plan);
        // Next intent should be aggravating factors, worded with 'pressure'
        assertEquals("CHEST_PAIN_AGGRAVATING", plan.getIntentCode());
        assertTrue(plan.getQuestionText().contains("pressure"));
    }

    @Test
    @DisplayName("Safety validator rejects diagnosis, treatments, and false reassurance")
    void testSafetyValidatorRejections() {
        QuestionHistory history = new QuestionHistory();

        // Diagnostic suggestion
        QuestionSafetyValidator.ValidationResult r1 = safetyValidator.validate("Do you think you are having a heart attack?", "CHEST_PAIN", history);
        assertFalse(r1.isValid());
        assertTrue(r1.getRejectionReason().contains("Diagnostic"));

        // Treatment recommendation
        QuestionSafetyValidator.ValidationResult r2 = safetyValidator.validate("Should you take aspirin to relieve the discomfort?", "CHEST_PAIN", history);
        assertFalse(r2.isValid());
        assertTrue(r2.getRejectionReason().contains("Treatment"));

        // False reassurance
        QuestionSafetyValidator.ValidationResult r3 = safetyValidator.validate("Don't worry, you are completely safe.", "CHEST_PAIN", history);
        assertFalse(r3.isValid());
        assertTrue(r3.getRejectionReason().contains("reassurance"));

        // Safe question passes
        QuestionSafetyValidator.ValidationResult r4 = safetyValidator.validate("Does anything make the pressure better or worse?", "CHEST_PAIN_AGGRAVATING", history);
        assertTrue(r4.isValid());
    }

    @Test
    @DisplayName("Question deduplication: Cannot ask same intent or identical question twice")
    void testQuestionDeduplication() {
        QuestionHistory history = new QuestionHistory();
        history.recordQuestion("Q_CHEST_PAIN_CHARACTER", "Can you describe what the chest pain feels like?", "Pressure", "CHEST_PAIN_CHARACTER");

        // Attempting to ask CHEST_PAIN_CHARACTER again
        QuestionSafetyValidator.ValidationResult r = safetyValidator.validate(
                "Can you describe what the chest pain feels like?",
                "CHEST_PAIN_CHARACTER",
                history
        );

        assertFalse(r.isValid());
        assertTrue(r.getRejectionReason().contains("already explored") || r.getRejectionReason().contains("Duplicate"));
    }
}
