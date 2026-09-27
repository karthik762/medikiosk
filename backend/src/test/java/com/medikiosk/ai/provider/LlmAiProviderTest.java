package com.medikiosk.ai.provider;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.medikiosk.ai.gateway.AiModelGateway;
import com.medikiosk.dto.clinical.ClinicalSummaryDto;
import com.medikiosk.dto.clinical.DoctorVerificationDto;
import com.medikiosk.dto.clinical.StructuredSymptomDto;
import com.medikiosk.entity.IntakeSession;
import com.medikiosk.entity.Patient;
import com.medikiosk.entity.Symptom;
import com.medikiosk.entity.TriageResult;
import com.medikiosk.entity.enums.TriagePriority;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

class LlmAiProviderTest {

    private AiModelGateway aiModelGateway;
    private ObjectMapper objectMapper;
    private LlmAiProvider llmAiProvider;

    @BeforeEach
    void setUp() {
        aiModelGateway = Mockito.mock(AiModelGateway.class);
        objectMapper = new ObjectMapper();
        llmAiProvider = new LlmAiProvider(aiModelGateway, objectMapper);
    }

    @Test
    @DisplayName("LlmAiProvider identifies as non-mock provider")
    void testProviderProperties() {
        assertFalse(llmAiProvider.isMock());
        assertEquals("REAL_LLM_PROVIDER", llmAiProvider.getProviderName());
        assertNotNull(llmAiProvider.getModelIdentifier());
    }

    @Test
    @DisplayName("Generate clinical summary via real LLM gateway successfully")
    void testGenerateClinicalSummarySuccess() {
        AiStructuringContext context = createTestContext();

        ClinicalSummaryDto mockLlmOutput = new ClinicalSummaryDto();
        mockLlmOutput.setChiefComplaint("Chest pain");
        mockLlmOutput.setSymptoms(List.of(new StructuredSymptomDto("Chest tightness", "2 hours", 8, "Chest", "Sudden", "Constant", "PATIENT_RESPONSE")));
        mockLlmOutput.setHumanReadableSummary("Patient presents with acute chest tightness of 2 hours duration.");

        when(aiModelGateway.generateStructured(anyString(), anyString(), eq(ClinicalSummaryDto.class)))
                .thenReturn(mockLlmOutput);

        AiStructuringResult result = llmAiProvider.generateClinicalSummary(context);

        assertTrue(result.isSuccess(), "Summary generation failed: " + result.getErrorMessage());
        assertFalse(result.isMock());
        assertNotNull(result.getSummaryDto());
        assertEquals("Chest pain", result.getSummaryDto().getChiefComplaint());
        assertEquals(context.getPatient().getId(), result.getSummaryDto().getPatientId());
        assertEquals(context.getSession().getId(), result.getSummaryDto().getSessionId());
        
        // Doctor verification must be forced to PENDING
        assertNotNull(result.getSummaryDto().getDoctorVerification());
        assertEquals("PENDING", result.getSummaryDto().getDoctorVerification().getStatus());
        
        // Deterministic triage priority must be preserved from ground truth
        assertNotNull(result.getSummaryDto().getTriage());
        assertEquals(TriagePriority.URGENT, result.getSummaryDto().getTriage().getPriority());
    }

    @Test
    @DisplayName("Handle gateway failure gracefully without crashing")
    void testGenerateClinicalSummaryGatewayFailure() {
        AiStructuringContext context = createTestContext();

        when(aiModelGateway.generateStructured(anyString(), anyString(), eq(ClinicalSummaryDto.class)))
                .thenThrow(new RuntimeException("LLM Gateway timeout (5000ms exceeded)"));

        AiStructuringResult result = llmAiProvider.generateClinicalSummary(context);

        assertFalse(result.isSuccess());
        assertFalse(result.isMock());
        assertTrue(result.getErrorMessage().contains("timeout"));
    }

    private AiStructuringContext createTestContext() {
        Patient patient = new Patient();
        patient.setId(UUID.randomUUID());
        patient.setFirstName("John");
        patient.setLastName("Doe");
        patient.setGender("MALE");

        IntakeSession session = new IntakeSession();
        session.setId(UUID.randomUUID());
        session.setPatient(patient);

        Symptom symptom = new Symptom();
        symptom.setName("Chest tightness");
        symptom.setDuration("2 hours");
        symptom.setSeverity(8);

        TriageResult triageResult = new TriageResult();
        triageResult.setPriority(TriagePriority.URGENT);
        triageResult.setExplanation("Chest pain triggered emergency triage rule");

        AiStructuringContext context = new AiStructuringContext();
        context.setPatient(patient);
        context.setSession(session);
        context.setSymptoms(List.of(symptom));
        context.setTriageResult(triageResult);

        return context;
    }
}
