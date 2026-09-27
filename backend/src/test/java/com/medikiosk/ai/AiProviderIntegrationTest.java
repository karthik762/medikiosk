package com.medikiosk.ai;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.medikiosk.ai.config.AiConfig;
import com.medikiosk.ai.gateway.AiModelGateway;
import com.medikiosk.ai.gateway.MockAiModelGateway;
import com.medikiosk.ai.provider.AiProvider;
import com.medikiosk.ai.provider.AiStructuringContext;
import com.medikiosk.ai.provider.LlmAiProvider;
import com.medikiosk.ai.provider.MockAiProvider;
import com.medikiosk.ai.safety.AiSafetyValidator;
import com.medikiosk.ai.safety.AiSafetyViolationException;
import com.medikiosk.dto.clinical.ClinicalSummaryDto;
import com.medikiosk.dto.clinical.StructuredMedicationDto;
import com.medikiosk.entity.IntakeSession;
import com.medikiosk.entity.Medication;
import com.medikiosk.entity.Patient;
import com.medikiosk.entity.TriageResult;
import com.medikiosk.entity.enums.TriagePriority;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class AiProviderIntegrationTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    @DisplayName("Verify AiConfig instantiates MockAiProvider when medikiosk.ai.provider is mock")
    void testMockAiProviderInstantiation() {
        AiConfig config = new AiConfig();
        AiProvider provider = config.mockAiProvider(objectMapper);

        assertNotNull(provider);
        assertTrue(provider.isMock());
        assertEquals("MOCK_AI_PROVIDER", provider.getProviderName());
    }

    @Test
    @DisplayName("Verify AiConfig instantiates LlmAiProvider when medikiosk.ai.provider is openai")
    void testOpenAiProviderInstantiation() {
        AiConfig config = new AiConfig();
        MockAiModelGateway mockGateway = new MockAiModelGateway(objectMapper);
        AiProvider provider = config.openAiProvider(mockGateway, objectMapper);

        assertNotNull(provider);
        assertFalse(provider.isMock());
        assertEquals("REAL_LLM_PROVIDER", provider.getProviderName());
    }

    @Test
    @DisplayName("Verify AiSafetyValidator catches ungrounded medication hallucination from LLM provider")
    void testSafetyValidatorCatchesHallucination() {
        AiSafetyValidator validator = new AiSafetyValidator(objectMapper);

        Patient patient = new Patient();
        patient.setId(UUID.randomUUID());
        IntakeSession session = new IntakeSession();
        session.setId(UUID.randomUUID());

        Medication groundMed = new Medication();
        groundMed.setMedicationName("Aspirin");

        AiStructuringContext context = new AiStructuringContext();
        context.setPatient(patient);
        context.setSession(session);
        context.setMedications(List.of(groundMed));

        // LLM generates summary containing ungrounded medication "Oxycodone"
        ClinicalSummaryDto summaryDto = new ClinicalSummaryDto();
        summaryDto.setChiefComplaint("Pain");
        summaryDto.setMedications(List.of(
                new StructuredMedicationDto("Aspirin", "100mg", "Once daily", null, "PATIENT_RESPONSE", false, null),
                new StructuredMedicationDto("Oxycodone", "20mg", "As needed", null, "PATIENT_RESPONSE", false, null)
        ));

        AiSafetyViolationException ex = assertThrows(AiSafetyViolationException.class, () ->
                validator.validate(summaryDto, context)
        );

        assertEquals("HALLUCINATED_ENTITY", ex.getViolationCategory());
        assertTrue(ex.getMessage().contains("Oxycodone"));
    }

    @Test
    @DisplayName("Verify AiSafetyValidator prevents LLM from overriding deterministic triage priority")
    void testSafetyValidatorPreventsTriageOverride() {
        AiSafetyValidator validator = new AiSafetyValidator(objectMapper);

        TriageResult groundTruthTriage = new TriageResult();
        groundTruthTriage.setPriority(TriagePriority.URGENT);
        groundTruthTriage.setExplanation("Chest pain");

        AiStructuringContext context = new AiStructuringContext();
        context.setTriageResult(groundTruthTriage);

        // LLM attempts to demote triage priority to STABLE
        ClinicalSummaryDto summaryDto = new ClinicalSummaryDto();
        summaryDto.setTriage(new com.medikiosk.dto.clinical.TriageSummaryDto(TriagePriority.STABLE, "Demoted by LLM", List.of()));

        AiSafetyViolationException ex = assertThrows(AiSafetyViolationException.class, () ->
                validator.validate(summaryDto, context)
        );

        assertEquals("TRIAGE_OVERRIDE_PROHIBITED", ex.getViolationCategory());
    }
}
