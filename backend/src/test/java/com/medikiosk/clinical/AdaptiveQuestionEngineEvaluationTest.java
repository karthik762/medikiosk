package com.medikiosk.clinical;

import com.medikiosk.clinical.adaptive.AdaptiveQuestionEngine;
import com.medikiosk.clinical.adaptive.NextQuestionResponse;
import com.medikiosk.clinical.nlp.ClinicalExtractionRequest;
import com.medikiosk.clinical.nlp.ClinicalExtractionResult;
import com.medikiosk.clinical.nlp.MockClinicalNlpProvider;
import com.medikiosk.clinical.state.ClinicalConflict;
import com.medikiosk.clinical.state.ClinicalFact;
import com.medikiosk.clinical.state.ClinicalFactProvenance;
import com.medikiosk.clinical.state.ClinicalState;
import com.medikiosk.concept.ConceptRegistry;
import com.medikiosk.entity.Question;
import com.medikiosk.repository.QuestionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

class AdaptiveQuestionEngineEvaluationTest {

    private ConceptRegistry conceptRegistry;
    private MockClinicalNlpProvider nlpProvider;
    private QuestionRepository questionRepository;
    private AdaptiveQuestionEngine adaptiveEngine;

    private Question qChiefComplaint;
    private Question qChestPainSeverity;
    private Question qChestPainRadiation;
    private Question qFeverDuration;
    private Question qDizzinessSyncope;
    private Question qAllergies;

    @BeforeEach
    void setUp() {
        conceptRegistry = new ConceptRegistry();
        nlpProvider = new MockClinicalNlpProvider(conceptRegistry);
        questionRepository = Mockito.mock(QuestionRepository.class);
        adaptiveEngine = new AdaptiveQuestionEngine(questionRepository);

        qChiefComplaint = new Question("Q_PRIMARY_COMPLAINT", "What brings you in?", "TEXT", "CHIEF_COMPLAINT");
        qChiefComplaint.setId(UUID.randomUUID());
        qChiefComplaint.setPriority(10);

        qChestPainSeverity = new Question("Q_CHEST_PAIN_SEVERITY", "How severe is your chest pain (0-10)?", "SCALE_0_10", "SYMPTOM_SEVERITY");
        qChestPainSeverity.setId(UUID.randomUUID());
        qChestPainSeverity.setClinicalConceptCode("CHEST_PAIN");
        qChestPainSeverity.setSafetyCritical(true);
        qChestPainSeverity.setPriority(1);

        qChestPainRadiation = new Question("Q_CHEST_PAIN_RADIATION", "Does chest pain radiate to left arm/jaw?", "YES_NO", "RED_FLAG_SCREENING");
        qChestPainRadiation.setId(UUID.randomUUID());
        qChestPainRadiation.setClinicalConceptCode("CHEST_PAIN");
        qChestPainRadiation.setSafetyCritical(true);
        qChestPainRadiation.setPriority(3);

        qFeverDuration = new Question("Q_FEVER_DURATION", "How many days have you had fever?", "TEXT", "SYMPTOM_DURATION");
        qFeverDuration.setId(UUID.randomUUID());
        qFeverDuration.setClinicalConceptCode("FEVER");
        qFeverDuration.setSafetyCritical(false);
        qFeverDuration.setPriority(15);

        qDizzinessSyncope = new Question("Q_DIZZINESS_SYNCOPE", "Have you passed out or fainted?", "YES_NO", "RED_FLAG_SCREENING");
        qDizzinessSyncope.setId(UUID.randomUUID());
        qDizzinessSyncope.setClinicalConceptCode("DIZZINESS");
        qDizzinessSyncope.setSafetyCritical(true);
        qDizzinessSyncope.setPriority(8);

        qAllergies = new Question("Q_KNOWN_ALLERGIES", "Do you have any allergies?", "YES_NO", "ALLERGIES");
        qAllergies.setId(UUID.randomUUID());
        qAllergies.setClinicalConceptCode("ALLERGY");
        qAllergies.setSafetyCritical(true);
        qAllergies.setPriority(70);

        when(questionRepository.findByActiveTrue()).thenReturn(Arrays.asList(
                qChiefComplaint, qChestPainSeverity, qChestPainRadiation, qFeverDuration, qDizzinessSyncope, qAllergies
        ));
    }

    @Test
    @DisplayName("NLP correctly affirms positive symptoms and negates absent symptoms")
    void testNlpExtractionAndNegation() {
        String input = "I have severe chest pain since yesterday, but no fever and not allergic to penicillin.";
        ClinicalExtractionResult result = nlpProvider.extractEntities(new ClinicalExtractionRequest(input));

        assertTrue(result.getAffirmedConcepts().contains("CHEST_PAIN"), "Chest pain must be affirmed");
        assertTrue(result.getNegatedConcepts().contains("FEVER"), "Fever must be marked negated");
        assertTrue(result.getNegatedConcepts().contains("ALLERGY"), "Allergy must be marked negated");
        assertFalse(result.getAffirmedConcepts().contains("FEVER"), "Negated fever must not be in affirmed concepts");
    }

    @Test
    @DisplayName("Adaptive engine prevents duplicate questions and prioritizes safety-critical red flags")
    void testAdaptiveRankingSafetyCritical() {
        ClinicalState state = new ClinicalState(UUID.randomUUID(), UUID.randomUUID(), "MK-PAT-001");
        state.setChiefComplaint("I have severe chest pain since yesterday.");
        state.addFact(new ClinicalFact("CHEST_PAIN", "chest pain", "AFFIRMED", ClinicalFactProvenance.PATIENT_TEXT, 1.0));
        state.addFact(new ClinicalFact("DURATION", "since yesterday", "AFFIRMED", ClinicalFactProvenance.PATIENT_TEXT, 1.0));

        NextQuestionResponse response = adaptiveEngine.determineNextQuestion(state);

        assertNotNull(response);
        assertFalse(response.isCompleted());
        // Must NOT ask for primary complaint again
        assertNotEquals("Q_PRIMARY_COMPLAINT", response.getQuestionCode());
        // Must prioritize chest pain follow-up
        assertTrue(response.isSafetyCritical(), "Next question for acute chest pain must be safety critical");
        assertEquals("CHEST_PAIN", response.getClinicalConceptCode());
    }

    @Test
    @DisplayName("Adaptive engine skips questions for negated symptoms")
    void testAdaptiveEngineSkipsNegatedConcepts() {
        ClinicalState state = new ClinicalState(UUID.randomUUID(), UUID.randomUUID(), "MK-PAT-002");
        state.setChiefComplaint("Mild fatigue, no chest pain.");
        state.addFact(new ClinicalFact("CHEST_PAIN", "chest pain", "NEGATED", ClinicalFactProvenance.PATIENT_TEXT, 1.0));

        NextQuestionResponse response = adaptiveEngine.determineNextQuestion(state);

        assertNotNull(response);
        assertNotEquals("Q_CHEST_PAIN_SEVERITY", response.getQuestionCode());
        assertNotEquals("Q_CHEST_PAIN_RADIATION", response.getQuestionCode());
    }

    @Test
    @DisplayName("Clinical conflict is properly retained when patient report contradicts document extraction")
    void testClinicalConflictTracking() {
        ClinicalState state = new ClinicalState(UUID.randomUUID(), UUID.randomUUID(), "MK-PAT-003");
        state.recordAnswer("Q_KNOWN_ALLERGIES", "NO");

        ClinicalConflict conflict = new ClinicalConflict(
                "ALLERGY",
                "Patient denied allergies, but document OCR indicates penicillin allergy",
                "PATIENT_REPORT",
                "NO",
                "DOCUMENT_OCR",
                "Penicillin 500mg - ALLERGIC"
        );
        state.addConflict(conflict);

        assertEquals(1, state.getConflicts().size());
        assertEquals("ALLERGY", state.getConflicts().get(0).getConcept());
        assertEquals("DOCUMENT_OCR", state.getConflicts().get(0).getConflictingSource());
    }
}
