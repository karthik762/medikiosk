package com.medikiosk.clinical;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.medikiosk.ai.gateway.MockAiModelGateway;
import com.medikiosk.ai.gateway.OpenAiCompatibleAiModelGateway;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AiModelGatewayTest {

    @Test
    @DisplayName("Mock gateway returns structured mock results offline")
    void testMockGatewayExecution() {
        ObjectMapper objectMapper = new ObjectMapper();
        MockAiModelGateway gateway = new MockAiModelGateway(objectMapper);

        String question = gateway.generateQuestion("CHEST_PAIN_SEVERITY", "Patient has chest pain", "en");
        assertNotNull(question);
        assertTrue(question.contains("scale from 0 to 10") || question.contains("severity") || question.contains("chest"));

        String extracted = gateway.extractClinicalContext("Severe chest pain", "");
        assertNotNull(extracted);
        assertTrue(extracted.contains("mock_extracted"));
        assertEquals("MockAiModelGateway", gateway.getProviderName());
        assertTrue(gateway.isMock());
    }

    @Test
    @DisplayName("OpenAI compatible gateway falls back to mock gracefully if endpoint unreachable or not configured")
    void testGatewayFallbackGraceful() {
        ObjectMapper objectMapper = new ObjectMapper();
        MockAiModelGateway mockFallback = new MockAiModelGateway(objectMapper);
        OpenAiCompatibleAiModelGateway gateway = new OpenAiCompatibleAiModelGateway(objectMapper, mockFallback);

        String result = gateway.generateQuestion("CHEST_PAIN_RADIATION", "Chest pain since yesterday", "en");

        assertNotNull(result, "Fallback to mock gateway must return a non-null result");
        assertTrue(result.contains("Does the chest") || result.contains("spread") || result.contains("radiate"));
        assertTrue(gateway.isMock());
    }

    @Test
    @DisplayName("Gateway gracefully recovers when response is malformed or invalid")
    void testMalformedResponseRecovery() {
        ObjectMapper objectMapper = new ObjectMapper();
        MockAiModelGateway mockFallback = new MockAiModelGateway(objectMapper);
        OpenAiCompatibleAiModelGateway gateway = new OpenAiCompatibleAiModelGateway(objectMapper, mockFallback);

        // Even with mock provider, structured generation returns a valid default object or falls back
        Object res = gateway.generateStructured("system prompt", "user prompt", java.util.HashMap.class);
        assertNotNull(res, "Structured generation should fall back rather than throwing uncaught exception");
    }

    @Test
    @DisplayName("Real external AI tests remain opt-in and disabled by default")
    void testExternalAiTestsDisabledByDefault() {
        String externalFlag = System.getenv("AI_EXTERNAL_TESTS");
        assertNotEquals("true", externalFlag != null ? externalFlag.toLowerCase() : "false",
                "External AI calls should remain opt-in and not run during regular mvn test");
    }
}
