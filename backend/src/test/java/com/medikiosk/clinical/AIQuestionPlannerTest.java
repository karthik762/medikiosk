package com.medikiosk.clinical;

import com.medikiosk.clinical.adaptive.AIQuestionPlanner;
import com.medikiosk.clinical.adaptive.IntakeCompletionPolicy;
import com.medikiosk.clinical.adaptive.QuestionHistory;
import com.medikiosk.clinical.adaptive.QuestionSafetyValidator;
import com.medikiosk.clinical.conversation.ConversationContext;
import com.medikiosk.clinical.conversation.ConversationTurn;
import com.medikiosk.clinical.reasoning.MockClinicalReasoningProvider;
import com.medikiosk.clinical.state.ClinicalFact;
import com.medikiosk.clinical.state.ClinicalFactProvenance;
import com.medikiosk.clinical.state.ClinicalState;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class AIQuestionPlannerTest {

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
    @DisplayName("Turn 1: Planner selects characterization for initial chest pain complaint")
    void testInitialChestPainComplaint() {
        ClinicalState state = new ClinicalState(UUID.randomUUID(), UUID.randomUUID(), "PAT-001");
        state.setChiefComplaint("I've had chest pain since yesterday.");

        ConversationContext context = new ConversationContext();
        context.addTurn(ConversationTurn.patient("I've had chest pain since yesterday."));

        QuestionHistory history = new QuestionHistory();

        AIQuestionPlanner.QuestionPlan plan = planner.planNextQuestion(state, context, history, "en");

        assertNotNull(plan);
        assertFalse(plan.isCompleted());
        assertEquals("CHEST_PAIN_CHARACTER", plan.getIntentCode());
        assertEquals("Can you describe what the chest pain feels like?", plan.getQuestionText());
        assertFalse(plan.getOptions().isEmpty());
    }

    @Test
    @DisplayName("Turn 2: Planner uses accumulated context (pressure) to ask about aggravating factors")
    void testTurn2PressureContextAggravating() {
        ClinicalState state = new ClinicalState(UUID.randomUUID(), UUID.randomUUID(), "PAT-001");
        state.setChiefComplaint("I've had chest pain since yesterday.");

        ConversationContext context = new ConversationContext();
        context.addTurn(ConversationTurn.patient("I've had chest pain since yesterday."));
        context.addTurn(ConversationTurn.system("Can you describe what the chest pain feels like?"));
        context.addTurn(ConversationTurn.patient("It's more like pressure."));

        QuestionHistory history = new QuestionHistory();
        history.recordQuestion("Q_CHEST_PAIN_CHARACTER", "Can you describe what the chest pain feels like?", "It's more like pressure.", "CHEST_PAIN_CHARACTER");

        AIQuestionPlanner.QuestionPlan plan = planner.planNextQuestion(state, context, history, "en");

        assertNotNull(plan);
        assertFalse(plan.isCompleted());
        assertEquals("CHEST_PAIN_AGGRAVATING", plan.getIntentCode());
        assertEquals("Does anything make the pressure better or worse?", plan.getQuestionText());
    }

    @Test
    @DisplayName("Turn 3: Planner uses accumulated exertion context to advance to radiation screening")
    void testTurn3ExertionalContextRadiation() {
        ClinicalState state = new ClinicalState(UUID.randomUUID(), UUID.randomUUID(), "PAT-001");
        state.setChiefComplaint("I've had chest pain since yesterday.");

        ConversationContext context = new ConversationContext();
        context.addTurn(ConversationTurn.patient("I've had chest pain since yesterday."));
        context.addTurn(ConversationTurn.system("Can you describe what the chest pain feels like?"));
        context.addTurn(ConversationTurn.patient("It's more like pressure."));
        context.addTurn(ConversationTurn.system("Does anything make the pressure better or worse?"));
        context.addTurn(ConversationTurn.patient("It gets worse when I walk."));

        QuestionHistory history = new QuestionHistory();
        history.recordQuestion("Q_CHEST_PAIN_CHARACTER", "Can you describe what the chest pain feels like?", "It's more like pressure.", "CHEST_PAIN_CHARACTER");
        history.recordQuestion("Q_CHEST_PAIN_AGGRAVATING", "Does anything make the pressure better or worse?", "It gets worse when I walk.", "CHEST_PAIN_AGGRAVATING");

        AIQuestionPlanner.QuestionPlan plan = planner.planNextQuestion(state, context, history, "en");

        assertNotNull(plan);
        assertFalse(plan.isCompleted());
        assertEquals("CHEST_PAIN_RADIATION", plan.getIntentCode());
        assertEquals("Does the pressure spread or radiate anywhere, such as to your left arm, jaw, neck, or back?", plan.getQuestionText());
        assertTrue(plan.isSafetyCritical());
    }

    @Test
    @DisplayName("Planner skips fever questions if fever is negated ('I don't have fever')")
    void testSkipsNegatedFever() {
        ClinicalState state = new ClinicalState(UUID.randomUUID(), UUID.randomUUID(), "PAT-002");
        state.setChiefComplaint("I have headache for two days, but I don't have fever.");

        ConversationContext context = new ConversationContext();
        context.addTurn(ConversationTurn.patient("I have headache for two days, but I don't have fever."));

        QuestionHistory history = new QuestionHistory();

        AIQuestionPlanner.QuestionPlan plan = planner.planNextQuestion(state, context, history, "en");

        assertNotNull(plan);
        assertFalse(plan.isCompleted());
        // Must NOT plan fever questions
        assertNotEquals("FEVER_DURATION", plan.getIntentCode());
        assertNotEquals("FEVER_CHILLS", plan.getIntentCode());
        // Must address headache
        assertTrue(plan.getIntentCode().startsWith("HEADACHE"));
    }

    @Test
    @DisplayName("Planner respects runaway ceiling policy and completes questionnaire")
    void testIntakeCompletionPolicyMaxTurns() {
        ClinicalState state = new ClinicalState(UUID.randomUUID(), UUID.randomUUID(), "PAT-003");
        state.setChiefComplaint("Headache");

        ConversationContext context = new ConversationContext();
        QuestionHistory history = new QuestionHistory();

        // Simulate reaching runaway limit (20 turns)
        for (int i = 1; i <= 20; i++) {
            history.recordQuestion("Q_" + i, "Question " + i, "Answer " + i, "INTENT_" + i);
        }

        AIQuestionPlanner.QuestionPlan plan = planner.planNextQuestion(state, context, history, "en");

        assertNotNull(plan);
        assertTrue(plan.isCompleted());
        assertTrue(plan.getReason().contains("Runaway safety ceiling limit"));
    }
}
