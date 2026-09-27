package com.medikiosk.clinical;

import com.medikiosk.clinical.reasoning.ClinicalContextRequest;
import com.medikiosk.clinical.reasoning.ClinicalContextResult;
import com.medikiosk.clinical.reasoning.MockClinicalReasoningProvider;
import com.medikiosk.clinical.state.ClinicalFact;
import com.medikiosk.clinical.state.ClinicalFactProvenance;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class MultilingualVoiceIntegrationTest {

    private MockClinicalReasoningProvider reasoningProvider;

    @BeforeEach
    void setUp() {
        reasoningProvider = new MockClinicalReasoningProvider();
    }

    @Test
    @DisplayName("Verify Hindi speech/text maps to language-independent ClinicalState concepts")
    void testHindiExtraction() {
        ClinicalContextRequest req = new ClinicalContextRequest();
        req.setPatientStatement("Mujhe pet me dard hai aur kal se bukhar nahi hai.");
        req.setExistingFacts(Collections.emptyList());

        ClinicalContextResult res = reasoningProvider.analyzeContext(req);
        assertNotNull(res);

        assertTrue(res.getExtractedFacts().stream().anyMatch(f -> "ABDOMINAL_PAIN".equalsIgnoreCase(f.getConceptCode()) && f.isAffirmed()));
        assertTrue(res.getExtractedFacts().stream().anyMatch(f -> "FEVER".equalsIgnoreCase(f.getConceptCode()) && f.isNegated()));
        assertTrue(res.getExtractedFacts().stream().anyMatch(f -> "DURATION".equalsIgnoreCase(f.getConceptCode()) && "1 DAY".equalsIgnoreCase(f.getValue())));
    }

    @Test
    @DisplayName("Verify Telugu speech/text maps to language-independent ClinicalState concepts")
    void testTeluguExtraction() {
        ClinicalContextRequest req = new ClinicalContextRequest();
        req.setPatientStatement("Kaduppu noppi undi, jwaram ledu.");
        req.setExistingFacts(Collections.emptyList());

        ClinicalContextResult res = reasoningProvider.analyzeContext(req);
        assertNotNull(res);

        assertTrue(res.getExtractedFacts().stream().anyMatch(f -> "ABDOMINAL_PAIN".equalsIgnoreCase(f.getConceptCode()) && f.isAffirmed()));
        assertTrue(res.getExtractedFacts().stream().anyMatch(f -> "FEVER".equalsIgnoreCase(f.getConceptCode()) && f.isNegated()));
    }

    @Test
    @DisplayName("Verify contradiction detection when patient statement conflicts with prior recorded facts")
    void testContradictionDetection() {
        ClinicalContextRequest req = new ClinicalContextRequest();
        req.setPatientStatement("Actually I do have fever now.");
        req.setExistingFacts(List.of(
                new ClinicalFact("FEVER", "Fever", "ABSENT", ClinicalFactProvenance.PATIENT_TEXT, 0.98)
        ));

        ClinicalContextResult res = reasoningProvider.analyzeContext(req);
        assertNotNull(res);
        assertTrue(res.getUncertainties().stream().anyMatch(u -> u.getConceptCode().startsWith("CONTRADICTION")));
    }

    @Test
    @DisplayName("Verify volunteered medical history extractions (diabetes, hypertension)")
    void testVolunteeredInfoExtraction() {
        ClinicalContextRequest req = new ClinicalContextRequest();
        req.setPatientStatement("I've had chest pain since yesterday and I am diabetic with high blood pressure.");
        req.setExistingFacts(Collections.emptyList());

        ClinicalContextResult res = reasoningProvider.analyzeContext(req);
        assertNotNull(res);
        assertTrue(res.getExtractedFacts().stream().anyMatch(f -> "PAST_HISTORY_DIABETES".equalsIgnoreCase(f.getConceptCode())));
        assertTrue(res.getExtractedFacts().stream().anyMatch(f -> "PAST_HISTORY_HYPERTENSION".equalsIgnoreCase(f.getConceptCode())));
    }
}
