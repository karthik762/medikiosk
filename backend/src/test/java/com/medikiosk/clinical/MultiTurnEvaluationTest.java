package com.medikiosk.clinical;

import com.medikiosk.clinical.adaptive.AIQuestionPlanner;
import com.medikiosk.clinical.adaptive.IntakeCompletionPolicy;
import com.medikiosk.clinical.adaptive.QuestionHistory;
import com.medikiosk.clinical.adaptive.QuestionSafetyValidator;
import com.medikiosk.clinical.conversation.ConversationContext;
import com.medikiosk.clinical.conversation.ConversationTurn;
import com.medikiosk.clinical.reasoning.MockClinicalReasoningProvider;
import com.medikiosk.clinical.state.ClinicalConflict;
import com.medikiosk.clinical.state.ClinicalFact;
import com.medikiosk.clinical.state.ClinicalFactProvenance;
import com.medikiosk.clinical.state.ClinicalState;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Phase 27 — Multi-Turn Evaluation Scenarios & Quality Tests")
class MultiTurnEvaluationTest {

    private MockClinicalReasoningProvider reasoningProvider;
    private QuestionSafetyValidator safetyValidator;
    private IntakeCompletionPolicy completionPolicy;
    private AIQuestionPlanner planner;

    @BeforeEach
    void setUp() {
        reasoningProvider = new MockClinicalReasoningProvider();
        safetyValidator = new QuestionSafetyValidator();
        completionPolicy = new IntakeCompletionPolicy(20);
        planner = new AIQuestionPlanner(reasoningProvider, safetyValidator, completionPolicy);
    }

    @Test
    @DisplayName("Scenario 1 — Chest Pain Multi-Turn Context Accumulation")
    void testScenario1ChestPainMultiTurn() {
        ClinicalState state = new ClinicalState(UUID.randomUUID(), UUID.randomUUID(), "PAT-101");
        state.setChiefComplaint("I've had chest pain since yesterday.");

        ConversationContext context = new ConversationContext();
        context.addTurn(ConversationTurn.patient("I've had chest pain since yesterday."));
        QuestionHistory history = new QuestionHistory();

        // Turn 1
        AIQuestionPlanner.QuestionPlan q1 = planner.planNextQuestion(state, context, history, "en");
        assertNotNull(q1);
        assertFalse(q1.isCompleted());
        assertEquals("CHEST_PAIN_CHARACTER", q1.getIntentCode());
        assertTrue(state.hasAffirmedConcept("CHEST_PAIN"));
        assertTrue(state.getFact("DURATION").isPresent());

        // Turn 2: Patient reports pressure
        context.addTurn(ConversationTurn.system(q1.getQuestionText()));
        context.addTurn(ConversationTurn.patient("It's more like pressure."));
        history.recordQuestion(q1.getIntentCode(), q1.getQuestionText(), "It's more like pressure.", q1.getIntentCode());

        AIQuestionPlanner.QuestionPlan q2 = planner.planNextQuestion(state, context, history, "en");
        assertNotNull(q2);
        assertFalse(q2.isCompleted());
        assertEquals("CHEST_PAIN_AGGRAVATING", q2.getIntentCode());
        assertTrue(q2.getQuestionText().contains("pressure"), "Question wording should reflect accumulated 'pressure' context");

        // Turn 3: Patient reports exertional aggravation (walking)
        context.addTurn(ConversationTurn.system(q2.getQuestionText()));
        context.addTurn(ConversationTurn.patient("It gets worse when I walk."));
        history.recordQuestion(q2.getIntentCode(), q2.getQuestionText(), "It gets worse when I walk.", q2.getIntentCode());

        AIQuestionPlanner.QuestionPlan q3 = planner.planNextQuestion(state, context, history, "en");
        assertNotNull(q3);
        assertFalse(q3.isCompleted());
        assertEquals("CHEST_PAIN_RADIATION", q3.getIntentCode());
        assertTrue(q3.isSafetyCritical());
        assertTrue(q3.getQuestionText().contains("pressure"), "Radiation question should maintain conversational context 'pressure'");
    }

    @Test
    @DisplayName("Scenario 2 — Abdominal Pain Multi-Turn Progression")
    void testScenario2AbdominalPainMultiTurn() {
        ClinicalState state = new ClinicalState(UUID.randomUUID(), UUID.randomUUID(), "PAT-102");
        state.setChiefComplaint("My stomach has been hurting.");

        ConversationContext context = new ConversationContext();
        context.addTurn(ConversationTurn.patient("My stomach has been hurting."));
        QuestionHistory history = new QuestionHistory();

        // Turn 1
        AIQuestionPlanner.QuestionPlan q1 = planner.planNextQuestion(state, context, history, "en");
        assertNotNull(q1);
        assertFalse(q1.isCompleted());
        assertEquals("ABDOMINAL_LOCATION", q1.getIntentCode());

        // Turn 2: Patient reports location (right side)
        context.addTurn(ConversationTurn.patient("Mostly on the right side."));
        history.recordQuestion(q1.getIntentCode(), q1.getQuestionText(), "Mostly on the right side.", q1.getIntentCode());

        AIQuestionPlanner.QuestionPlan q2 = planner.planNextQuestion(state, context, history, "en");
        assertNotNull(q2);
        assertFalse(q2.isCompleted());
        assertEquals("ABDOMINAL_VOMITING", q2.getIntentCode());
    }

    @Test
    @DisplayName("Scenario 3 — Negation Handling (Cough for 3 days, no fever)")
    void testScenario3NegationHandling() {
        ClinicalState state = new ClinicalState(UUID.randomUUID(), UUID.randomUUID(), "PAT-103");
        state.setChiefComplaint("I've been coughing for three days, but I don't have fever.");

        ConversationContext context = new ConversationContext();
        context.addTurn(ConversationTurn.patient("I've been coughing for three days, but I don't have fever."));
        QuestionHistory history = new QuestionHistory();

        AIQuestionPlanner.QuestionPlan plan = planner.planNextQuestion(state, context, history, "en");

        assertNotNull(plan);
        assertTrue(state.hasAffirmedConcept("COUGH"));
        assertTrue(state.getFact("DURATION").isPresent());
        assertTrue(state.hasNegatedConcept("FEVER"), "FEVER must be negated in ClinicalState");
        assertFalse(state.hasAffirmedConcept("FEVER"), "FEVER must NOT be affirmed");

        // System must not ask fever follow-ups
        assertNotEquals("FEVER_DURATION", plan.getIntentCode());
        assertNotEquals("FEVER_CHILLS", plan.getIntentCode());
    }

    @Test
    @DisplayName("Scenario 4 — Document + Conversation Gating & Provenance")
    void testScenario4DocumentAndConversation() {
        ClinicalState state = new ClinicalState(UUID.randomUUID(), UUID.randomUUID(), "PAT-104");
        state.setChiefComplaint("I take medication for blood pressure.");
        state.addFact(new ClinicalFact("DOCUMENT_MEDICATION", "Prescription Amlodipine 5mg", "AFFIRMED", ClinicalFactProvenance.DOCUMENT_OCR, 0.94));

        ConversationContext context = new ConversationContext();
        context.addTurn(ConversationTurn.patient("I take medication for blood pressure."));
        QuestionHistory history = new QuestionHistory();

        AIQuestionPlanner.QuestionPlan plan = planner.planNextQuestion(state, context, history, "en");

        assertNotNull(plan);
        // Duplicate medication confirmation should be skipped due to high-confidence OCR document presence
        assertNotEquals("MEDICATION_CONFIRMATION", plan.getIntentCode());
        assertEquals(ClinicalFactProvenance.DOCUMENT_OCR, state.getFact("DOCUMENT_MEDICATION").get().getProvenance());
    }

    @Test
    @DisplayName("Scenario 5 — Document vs Patient Report Conflict Resolution")
    void testScenario5ConflictResolution() {
        ClinicalState state = new ClinicalState(UUID.randomUUID(), UUID.randomUUID(), "PAT-105");
        state.setChiefComplaint("Chest pain");
        state.recordAnswer("Q_KNOWN_ALLERGIES", "No known allergies");
        state.addConflict(new ClinicalConflict(
                "ALLERGY",
                "Possible drug sensitivity mentioned in uploaded medical document",
                "PATIENT_REPORT",
                "No known allergies",
                "DOCUMENT_OCR",
                "Mention of Penicillin in record"
        ));

        ConversationContext context = new ConversationContext();
        QuestionHistory history = new QuestionHistory();

        AIQuestionPlanner.QuestionPlan plan = planner.planNextQuestion(state, context, history, "en");

        assertNotNull(plan);
        assertFalse(plan.isCompleted());
        assertTrue(plan.getIntentCode().startsWith("CONFLICT_CLARIFICATION"));
        assertTrue(plan.getQuestionText().contains("penicillin allergy"), "Clarification must neutrally ask about penicillin allergy");
        assertFalse(plan.getQuestionText().contains("confidence"), "Question must NEVER expose internal confidence scores to patient");
    }

    @Test
    @DisplayName("Natural Language Severity Extraction ('around seven, especially when walking')")
    void testNaturalLanguageSeverityAndExertion() {
        ClinicalState state = new ClinicalState(UUID.randomUUID(), UUID.randomUUID(), "PAT-106");
        state.setChiefComplaint("Chest pain");

        ConversationContext context = new ConversationContext();
        context.addTurn(ConversationTurn.patient("Honestly, it's probably around seven, especially when I'm walking."));

        com.medikiosk.clinical.reasoning.ClinicalContextRequest req = new com.medikiosk.clinical.reasoning.ClinicalContextRequest(
                state.getSessionId(), "Chest pain", context.toFormattedHistory(8), "en"
        );

        com.medikiosk.clinical.reasoning.ClinicalContextResult res = reasoningProvider.analyzeContext(req);

        assertTrue(res.getExtractedFacts().stream().anyMatch(f -> "SEVERITY".equals(f.getConceptCode()) && "7".equals(f.getValue())));
        assertTrue(res.getExtractedFacts().stream().anyMatch(f -> "EXERTIONAL".equals(f.getConceptCode())));
    }
}
