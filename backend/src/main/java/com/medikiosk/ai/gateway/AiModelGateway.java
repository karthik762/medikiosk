package com.medikiosk.ai.gateway;

/**
 * Generic AI Model Gateway supporting pluggable LLM routing (Mock, OpenAI, OpenRouter, OmniRoute)
 * with tier-based fast and strong model execution.
 */
public interface AiModelGateway {

    /**
     * Executes structured JSON generation with schema validation and fallback handling.
     */
    <T> T generateStructured(String systemPrompt, String userPrompt, Class<T> responseType);

    /**
     * Generates a patient-friendly conversational question for a given clinical intent and context.
     */
    String generateQuestion(String intent, String context, String language);

    /**
     * Extracts raw clinical context JSON from patient input and conversation turns.
     */
    String extractClinicalContext(String patientText, String history);

    /**
     * Returns provider name (e.g. "MockGateway", "OpenRouterGateway").
     */
    String getProviderName();

    /**
     * Returns operational status: MOCK, CONFIGURED, CONNECTED, UNAVAILABLE, ERROR.
     */
    String getProviderStatus();

    /**
     * Whether this gateway is operating offline in mock mode.
     */
    boolean isMock();
}
