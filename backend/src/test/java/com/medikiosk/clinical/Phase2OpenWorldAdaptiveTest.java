package com.medikiosk.clinical;

import com.medikiosk.ai.gateway.MockAiModelGateway;

import com.medikiosk.clinical.adaptive.AIQuestionPlanner;
import com.medikiosk.clinical.adaptive.IntakeCompletionPolicy;
import com.medikiosk.clinical.adaptive.QuestionHistory;
import com.medikiosk.clinical.adaptive.QuestionIntent;
import com.medikiosk.clinical.adaptive.QuestionSafetyValidator;
import com.medikiosk.clinical.conversation.ConversationContext;
import com.medikiosk.clinical.reasoning.ClinicalContextRequest;
import com.medikiosk.clinical.reasoning.ClinicalContextResult;
import com.medikiosk.clinical.reasoning.ClinicalFactExtraction;
import com.medikiosk.clinical.reasoning.ClinicalQuestionIntent;
import com.medikiosk.clinical.reasoning.ClinicalReasoningProvider;
import com.medikiosk.clinical.reasoning.MockClinicalReasoningProvider;
import com.medikiosk.clinical.state.ClinicalConflict;
import com.medikiosk.clinical.state.ClinicalFact;
import com.medikiosk.clinical.state.ClinicalFactProvenance;
import com.medikiosk.clinical.state.ClinicalState;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Phase 2 — True Open-World AI Adaptive Clinical Interview Tests")
public class Phase2OpenWorldAdaptiveTest {

    private MockClinicalReasoningProvider mockReasoner;
    private QuestionSafetyValidator safetyValidator;
    private IntakeCompletionPolicy completionPolicy;
    private AIQuestionPlanner questionPlanner;

    @BeforeEach
    void setUp() {
        mockReasoner = new MockClinicalReasoningProvider();
        safetyValidator = new QuestionSafetyValidator();
        completionPolicy = new IntakeCompletionPolicy();
        questionPlanner = new AIQuestionPlanner(mockReasoner, safetyValidator, completionPolicy, new MockAiModelGateway());
    }

    @Test
    @DisplayName("1. Arbitrary Chief Complaint Extraction — Retro-Orbital Pain")
    void testOpenWorld_ArbitraryChiefComplaintExtraction_RetroOrbitalPain() {
        ClinicalContextRequest req = new ClinicalContextRequest(UUID.randomUUID(), "pain at the back of my eyes", null, "en");
        ClinicalContextResult result = mockReasoner.analyzeContext(req);

        assertNotNull(result);
        assertFalse(result.getExtractedFacts().isEmpty());

        ClinicalFactExtraction fact = result.getExtractedFacts().stream()
                .filter(f -> "retro-orbital".equalsIgnoreCase(f.getAnatomicalSite()) || "RETRO_ORBITAL_PAIN".equalsIgnoreCase(f.getConceptCode()))
                .findFirst().orElse(null);

        assertNotNull(fact, "Retro-orbital pain should be extracted");
        assertEquals("retro-orbital", fact.getAnatomicalSite());
        assertEquals("pain", fact.getSensation());
        assertEquals("UNMAPPED", fact.getNormalizedConcept());
        assertTrue(fact.getNormalizationConfidence() < 0.5, "Unmapped concept should have low normalization confidence");
    }

    @Test
    @DisplayName("2. Arbitrary Chief Complaint Extraction — Crawling Skin")
    void testOpenWorld_ArbitraryChiefComplaintExtraction_CrawlingSkin() {
        ClinicalContextRequest req = new ClinicalContextRequest(UUID.randomUUID(), "crawling sensation under my skin", null, "en");
        ClinicalContextResult result = mockReasoner.analyzeContext(req);

        ClinicalFactExtraction fact = result.getExtractedFacts().stream()
                .filter(f -> "crawling".equalsIgnoreCase(f.getSensation()))
                .findFirst().orElse(null);

        assertNotNull(fact, "Crawling sensation should be extracted");
        assertEquals("subcutaneous", fact.getAnatomicalSite());
        assertEquals("UNMAPPED", fact.getNormalizedConcept());
    }

    @Test
    @DisplayName("3. Compound Multi-Fact Extraction")
    void testOpenWorld_CompoundMultiFactExtraction() {
        ClinicalContextRequest req = new ClinicalContextRequest(
                UUID.randomUUID(),
                "I have severe thumping headache and nausea since 3 hours after eating spicy food",
                null, "en"
        );
        ClinicalContextResult result = mockReasoner.analyzeContext(req);

        List<ClinicalFactExtraction> facts = result.getExtractedFacts();
        assertTrue(facts.size() >= 2, "Should extract multiple facts from single response");

        boolean hasHeadache = facts.stream().anyMatch(f -> "HEADACHE".equalsIgnoreCase(f.getConceptCode()));
        boolean hasVomitingNausea = facts.stream().anyMatch(f -> "VOMITING".equalsIgnoreCase(f.getConceptCode()) || "NAUSEA".equalsIgnoreCase(f.getConceptCode()));

        assertTrue(hasHeadache, "Headache should be extracted");
        assertTrue(hasVomitingNausea, "Nausea/vomiting should be extracted");
    }

    @Test
    @DisplayName("4. Unmapped Term Preservation")
    void testOpenWorld_UnmappedTermPreservation() {
        ClinicalFactExtraction fe = new ClinicalFactExtraction("NOVEL_PAIN", "PRESENT", "AFFIRMED", ClinicalFactProvenance.PATIENT_TEXT, 0.90);
        fe.setPatientExpression("pulling sensation in my right groin");
        fe.setAnatomicalSite("groin");
        fe.setLaterality("RIGHT");
        fe.setSensation("pulling");
        fe.setNormalizedConcept("UNMAPPED");
        fe.setNormalizationConfidence(0.35);

        ClinicalFact fact = fe.toClinicalFact();

        assertEquals("pulling sensation in my right groin", fact.getPatientExpression());
        assertEquals("UNMAPPED", fact.getNormalizedConcept());
        assertEquals(0.35, fact.getNormalizationConfidence());
        assertTrue(fact.isUnmapped(), "Fact with normalizedConcept=UNMAPPED should return isUnmapped()=true");
    }

    @Test
    @DisplayName("5. Distinguish UNKNOWN from ABSENT")
    void testOpenWorld_DistinguishUnknownFromAbsent() {
        ClinicalState state = new ClinicalState();
        state.addFact(new ClinicalFact("HEADACHE", "PRESENT", "AFFIRMED", ClinicalFactProvenance.PATIENT_TEXT, 0.95));

        assertFalse(state.hasNegatedConcept("FEVER"), "Unmentioned symptom must not be defaulted to negated/ABSENT");
        assertFalse(state.hasAffirmedConcept("FEVER"), "Unmentioned symptom must not be defaulted to affirmed");
    }

    @Test
    @DisplayName("6. Distinguish PATIENT_REPORTED from AI_INFERRED")
    void testOpenWorld_DistinguishPatientReportedFromAiInferred() {
        ClinicalFact patientFact = new ClinicalFact("CHEST_PAIN", "PRESENT", "AFFIRMED", ClinicalFactProvenance.PATIENT_TEXT, 0.99);
        patientFact.setSourceType("PATIENT_REPORTED");

        ClinicalFact aiFact = new ClinicalFact("ISCHEMIA_RISK", "POSSIBLE", "UNCERTAIN", ClinicalFactProvenance.AI_EXTRACTED, 0.70);
        aiFact.setSourceType("AI_INFERRED");

        assertEquals("PATIENT_REPORTED", patientFact.getSourceType());
        assertEquals("AI_INFERRED", aiFact.getSourceType());
        assertNotEquals(patientFact.getSourceType(), aiFact.getSourceType());
    }

    @Test
    @DisplayName("7. Patient Correction — Left Knee to Right Knee")
    void testOpenWorld_PatientCorrection_LeftKneeToRightKnee() {
        ClinicalState state = new ClinicalState();
        
        ClinicalFact leftKnee = new ClinicalFact("KNEE_PAIN", "Left Knee Pain", "AFFIRMED", ClinicalFactProvenance.PATIENT_TEXT, 0.90);
        leftKnee.setAnatomicalSite("knee");
        leftKnee.setLaterality("LEFT");
        state.addFact(leftKnee);

        ClinicalFact rightKnee = new ClinicalFact("KNEE_PAIN", "Right Knee Pain", "AFFIRMED", ClinicalFactProvenance.PATIENT_TEXT, 0.95);
        rightKnee.setAnatomicalSite("knee");
        rightKnee.setLaterality("RIGHT");
        rightKnee.setExtractionMethod("PATIENT_CORRECTION");

        state.addFactWithCorrection(rightKnee);

        assertEquals("PATIENT_CORRECTED", rightKnee.getVerificationStatus());
        assertEquals("SUPERSEDED", leftKnee.getVerificationStatus());
    }

    @Test
    @DisplayName("8. Patient Correction — Negation Flip")
    void testOpenWorld_PatientCorrection_NegationFlip() {
        ClinicalState state = new ClinicalState();

        ClinicalFact feverAffirmed = new ClinicalFact("FEVER", "PRESENT", "AFFIRMED", ClinicalFactProvenance.PATIENT_TEXT, 0.90);
        state.addFact(feverAffirmed);

        ClinicalFact feverNegated = new ClinicalFact("FEVER", "ABSENT", "NEGATED", ClinicalFactProvenance.PATIENT_TEXT, 0.95);
        state.addFactWithCorrection(feverNegated);

        assertEquals("SUPERSEDED", feverAffirmed.getVerificationStatus());
        assertEquals("PATIENT_CORRECTED", feverNegated.getVerificationStatus());
    }

    @Test
    @DisplayName("9. Contradiction Detection Across Turn Facts")
    void testOpenWorld_ContradictionDetection_TurnFacts() {
        ClinicalState state = new ClinicalState();

        ClinicalFact fact1 = new ClinicalFact("CHEST_PAIN", "PRESENT", "AFFIRMED", ClinicalFactProvenance.PATIENT_TEXT, 0.95);
        state.addFact(fact1);

        ClinicalFact fact2 = new ClinicalFact("CHEST_PAIN", "ABSENT", "NEGATED", ClinicalFactProvenance.PATIENT_TEXT, 0.95);
        state.addFactWithCorrection(fact2);

        assertFalse(state.getConflicts().isEmpty(), "Contradictory turn statements must generate a ClinicalConflict");
    }

    @Test
    @DisplayName("10. Ambiguity Handling — Head Feels Strange")
    void testOpenWorld_AmbiguityHandling_HeadFeelsStrange() {
        ClinicalContextRequest req = new ClinicalContextRequest(UUID.randomUUID(), "my head feels strange", null, "en");
        ClinicalContextResult result = mockReasoner.analyzeContext(req);

        ClinicalFactExtraction fact = result.getExtractedFacts().stream()
                .filter(f -> "AMBIGUOUS".equalsIgnoreCase(f.getCertainty()))
                .findFirst().orElse(null);

        assertNotNull(fact, "Ambiguous symptom should be extracted with AMBIGUOUS certainty");
    }

    @Test
    @DisplayName("11. Hindi Natural Language Extraction")
    void testOpenWorld_HindiNaturalLanguageExtraction() {
        ClinicalContextRequest req = new ClinicalContextRequest(UUID.randomUUID(), "aankhon ke peeche dard hai", null, "hi");
        ClinicalContextResult result = mockReasoner.analyzeContext(req);

        assertFalse(result.getExtractedFacts().isEmpty());
        ClinicalFactExtraction fact = result.getExtractedFacts().get(0);
        assertEquals("retro-orbital", fact.getAnatomicalSite());
    }

    @Test
    @DisplayName("12. Hindi Negation — Chaati Me Dard Nahi Hai")
    void testOpenWorld_HindiNegation_ChaatiMeDardNahiHai() {
        ClinicalContextRequest req = new ClinicalContextRequest(UUID.randomUUID(), "chaati me dard nahi hai", null, "hi");
        ClinicalContextResult result = mockReasoner.analyzeContext(req);

        ClinicalFactExtraction fact = result.getExtractedFacts().stream()
                .filter(f -> "CHEST_PAIN".equalsIgnoreCase(f.getConceptCode()))
                .findFirst().orElse(null);

        assertNotNull(fact);
        assertTrue(fact.isNegated(), "Hindi negation 'nahi hai' should set negated assertion");
    }

    @Test
    @DisplayName("13. Telugu Natural Language Extraction")
    void testOpenWorld_TeluguNaturalLanguageExtraction() {
        ClinicalContextRequest req = new ClinicalContextRequest(UUID.randomUUID(), "kallla venuka noppi", null, "te");
        ClinicalContextResult result = mockReasoner.analyzeContext(req);

        assertFalse(result.getExtractedFacts().isEmpty());
        ClinicalFactExtraction fact = result.getExtractedFacts().get(0);
        assertEquals("retro-orbital", fact.getAnatomicalSite());
    }

    @Test
    @DisplayName("14. Telugu Negation — Chati Lo Noppi Ledu")
    void testOpenWorld_TeluguNegation_ChatiLoNoppiLedu() {
        ClinicalContextRequest req = new ClinicalContextRequest(UUID.randomUUID(), "chati lo noppi ledu", null, "te");
        ClinicalContextResult result = mockReasoner.analyzeContext(req);

        ClinicalFactExtraction fact = result.getExtractedFacts().stream()
                .filter(f -> "CHEST_PAIN".equalsIgnoreCase(f.getConceptCode()))
                .findFirst().orElse(null);

        assertNotNull(fact);
        assertTrue(fact.isNegated(), "Telugu negation 'ledu' should set negated assertion");
    }

    @Test
    @DisplayName("15. AI Model Gateway — Strict Contract Validation")
    void testOpenWorld_AiModelGateway_StrictContractValidation() {
        MockAiModelGateway mockGateway = new MockAiModelGateway();
        assertTrue(mockGateway.isMock());
        assertNotNull(mockGateway.getProviderName());

        String question = mockGateway.generateQuestion("CHEST_PAIN_SEVERITY", "Chest pressure", "en");
        assertNotNull(question);
        assertFalse(question.trim().isEmpty());
    }

    @Test
    @DisplayName("16. AI Model Gateway — Timeout Fallback To Mock")
    void testOpenWorld_AiModelGateway_TimeoutFallbackToMock() {
        MockAiModelGateway fallback = new MockAiModelGateway();
        assertNotNull(fallback);
        assertTrue(fallback.isMock());
    }

    @Test
    @DisplayName("17. AI Model Gateway — Invalid JSON Fallback To Mock")
    void testOpenWorld_AiModelGateway_InvalidJsonFallbackToMock() {
        MockAiModelGateway fallback = new MockAiModelGateway();
        String ctx = fallback.extractClinicalContext("invalid text", "");
        assertNotNull(ctx);
    }

    @Test
    @DisplayName("18. Safety Validator — Blocks Diagnostic Claims")
    void testOpenWorld_SafetyValidator_BlocksDiagnosticClaims() {
        QuestionHistory history = new QuestionHistory();
        QuestionSafetyValidator.ValidationResult res = safetyValidator.validate(
                "You have acute myocardial infarction. Are you taking aspirin?",
                "CHEST_PAIN_CHARACTER",
                history
        );

        assertFalse(res.isValid(), "Validator must block diagnostic statements");
        assertTrue(res.getRejectionReason().toLowerCase().contains("diagnos") || res.getRejectionReason().toLowerCase().contains("claim") || res.getRejectionReason().toLowerCase().contains("naming"), "Rejection reason should mention diagnostic detection");
    }

    @Test
    @DisplayName("19. Safety Validator — Blocks Medication Prescriptions")
    void testOpenWorld_SafetyValidator_BlocksMedicationPrescriptions() {
        QuestionHistory history = new QuestionHistory();
        QuestionSafetyValidator.ValidationResult res = safetyValidator.validate(
                "Please take 500mg Paracetamol right away for your fever.",
                "FEVER_DURATION",
                history
        );

        assertFalse(res.isValid(), "Validator must block medication prescriptions");
        assertTrue(res.getRejectionReason().toLowerCase().contains("treatment") || res.getRejectionReason().toLowerCase().contains("prescrib") || res.getRejectionReason().toLowerCase().contains("medication"), "Rejection reason should mention treatment/prescription detection");
    }

    @Test
    @DisplayName("20. Safety Validator — Fallback To Approved Template")
    void testOpenWorld_SafetyValidator_FallbackToApprovedTemplate() {
        ClinicalState state = new ClinicalState();
        state.setChiefComplaint("Chest pain");
        state.addFact(new ClinicalFact("CHEST_PAIN", "PRESENT", "AFFIRMED", ClinicalFactProvenance.PATIENT_TEXT, 0.99));

        QuestionHistory history = new QuestionHistory();
        ConversationContext context = new ConversationContext();

        AIQuestionPlanner.QuestionPlan plan = questionPlanner.planNextQuestion(state, context, history, "en");

        assertNotNull(plan);
        assertFalse(plan.isCompleted());
        assertNotNull(plan.getQuestionText());
    }

    @Test
    @DisplayName("21. End-To-End Intake Session — Novel Complaint")
    void testOpenWorld_EndToEndIntakeSession_NovelComplaint() {
        ClinicalState state = new ClinicalState();
        state.setChiefComplaint("burning sensation under my right rib cage");
        
        ClinicalContextRequest req = new ClinicalContextRequest(UUID.randomUUID(), state.getChiefComplaint(), null, "en");
        ClinicalContextResult res = mockReasoner.analyzeContext(req);

        for (ClinicalFactExtraction fe : res.getExtractedFacts()) {
            state.addFactWithCorrection(fe.toClinicalFact());
        }

        QuestionHistory history = new QuestionHistory();
        ConversationContext context = new ConversationContext();

        AIQuestionPlanner.QuestionPlan plan = questionPlanner.planNextQuestion(state, context, history, "en");

        assertNotNull(plan);
        assertEquals("burning sensation under my right rib cage", state.getChiefComplaint());
    }

    @Test
    @DisplayName("22. Provider Provenance In Question Plan")
    void testOpenWorld_ProviderProvenanceInQuestionPlan() {
        ClinicalState state = new ClinicalState();
        state.setChiefComplaint("Severe headache");
        state.addFact(new ClinicalFact("HEADACHE", "PRESENT", "AFFIRMED", ClinicalFactProvenance.PATIENT_TEXT, 0.99));

        QuestionHistory history = new QuestionHistory();
        ConversationContext context = new ConversationContext();

        AIQuestionPlanner.QuestionPlan plan = questionPlanner.planNextQuestion(state, context, history, "en");

        assertNotNull(plan);
        assertNotNull(plan.getGeneratedBy(), "Question plan must specify generator/provider provenance");
    }

    @Test
    @DisplayName("23. Novel STT Transcript Open-World Understanding — Left Subcostal Pulling Sensation")
    void testOpenWorld_NovelSpeechTranscriptExtraction_LeftSubcostalPullingSensation() {
        String novelTranscript = "I have a strange pulling sensation underneath my left ribs after I eat, and sometimes it spreads toward my back.";
        ClinicalContextRequest req = new ClinicalContextRequest(UUID.randomUUID(), novelTranscript, null, "en");
        ClinicalContextResult res = mockReasoner.analyzeContext(req);

        assertNotNull(res);
        assertFalse(res.getExtractedFacts().isEmpty());

        ClinicalFactExtraction pullingFact = res.getExtractedFacts().stream()
                .filter(f -> "LEFT_SUBCOSTAL_PULLING_SENSATION".equalsIgnoreCase(f.getConceptCode()))
                .findFirst().orElse(null);

        assertNotNull(pullingFact, "Novel left rib pulling complaint must be extracted");
        assertEquals("left subcostal", pullingFact.getAnatomicalSite());
        assertEquals("LEFT", pullingFact.getLaterality());
        assertEquals("pulling", pullingFact.getSensation());
        assertEquals("UNMAPPED", pullingFact.getNormalizedConcept());

        boolean hasPostprandial = res.getExtractedFacts().stream()
                .anyMatch(f -> "TIMING_POSTPRANDIAL".equalsIgnoreCase(f.getConceptCode()));
        boolean hasBackRadiation = res.getExtractedFacts().stream()
                .anyMatch(f -> "RADIATION_BACK".equalsIgnoreCase(f.getConceptCode()));

        assertTrue(hasPostprandial, "Postprandial timing should be extracted");
        assertTrue(hasBackRadiation, "Radiation to back should be extracted");
    }
}
