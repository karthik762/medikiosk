package com.medikiosk.ai.gateway;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;

class ExternalAiGatewayIntegrationTest {

    private OpenAiCompatibleAiModelGateway gateway;
    private MockAiModelGateway mockGateway;

    @BeforeEach
    void setUp() {
        ObjectMapper mapper = new ObjectMapper();
        mockGateway = new MockAiModelGateway(mapper);
        gateway = new OpenAiCompatibleAiModelGateway(mapper, mockGateway);
    }

    @Test
    @DisplayName("Verify provider defaults to MOCK mode when AI_PROVIDER is mock or API key is missing")
    void testDefaultMockMode() {
        ReflectionTestUtils.setField(gateway, "providerType", "mock");
        ReflectionTestUtils.setField(gateway, "apiKey", "");

        assertTrue(gateway.isMock());
        assertEquals("MOCK", gateway.getProviderStatus());
        assertTrue(gateway.getProviderName().contains("Mock"));

        String question = gateway.generateQuestion("CHEST_PAIN_CHARACTER", "Patient reports chest pain", "en");
        assertNotNull(question);
        assertFalse(question.isEmpty());
    }

    @Test
    @DisplayName("Verify external provider falls back safely to mock response on network error")
    void testSafeFallbackOnNetworkError() {
        ReflectionTestUtils.setField(gateway, "providerType", "openrouter");
        ReflectionTestUtils.setField(gateway, "baseUrl", "https://invalid.openrouter.endpoint.test/v1");
        ReflectionTestUtils.setField(gateway, "apiKey", "sk-or-dummy-test-key");
        ReflectionTestUtils.setField(gateway, "fastModel", "gpt-4o-mini");

        assertFalse(gateway.isMock());
        assertEquals("CONFIGURED", gateway.getProviderStatus());

        String question = gateway.generateQuestion("CHEST_PAIN_CHARACTER", "Patient reports chest pain", "en");
        assertNotNull(question);
        assertEquals("UNAVAILABLE", gateway.getProviderStatus());
    }

    @Test
    @DisplayName("Verify AI_EXTERNAL_TESTS flag controls external test execution")
    void testExternalTestsFlagDefaultDisabled() {
        ReflectionTestUtils.setField(gateway, "externalTestsEnabled", false);
        assertFalse(gateway.isExternalTestsEnabled());
    }
}
