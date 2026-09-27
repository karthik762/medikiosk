package com.medikiosk.ai.gateway;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.medikiosk.clinical.adaptive.QuestionIntent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Deterministic offline implementation of AiModelGateway.
 * Provides reliable, instantaneous responses for testing, evaluation, and local development.
 */
@Component
public class MockAiModelGateway implements AiModelGateway {

    private static final Logger log = LoggerFactory.getLogger(MockAiModelGateway.class);

    private final ObjectMapper objectMapper;

    public MockAiModelGateway() {
        this(new ObjectMapper());
    }

    public MockAiModelGateway(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public <T> T generateStructured(String systemPrompt, String userPrompt, Class<T> responseType) {
        log.debug("MockAiModelGateway generating structured response for type: {}", responseType.getSimpleName());
        try {
            // Provide default instance if possible
            return responseType.getDeclaredConstructor().newInstance();
        } catch (Exception e) {
            log.warn("Could not instantiate {}: {}", responseType.getSimpleName(), e.getMessage());
            return null;
        }
    }

    @Override
    public String generateQuestion(String intent, String context, String language) {
        QuestionIntent qi = QuestionIntent.fromCode(intent);
        if (qi != null) {
            return qi.getDefaultApprovedTemplate();
        }
        return "Could you provide more details regarding your symptoms?";
    }

    @Override
    public String extractClinicalContext(String patientText, String history) {
        return "{\"status\": \"mock_extracted\", \"confidence\": 0.98}";
    }

    @Override
    public String getProviderName() {
        return "MockAiModelGateway";
    }

    @Override
    public String getProviderStatus() {
        return "MOCK";
    }

    @Override
    public boolean isMock() {
        return true;
    }
}
