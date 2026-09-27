package com.medikiosk.ai.gateway;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * OpenAI / OpenRouter-compatible model gateway supporting tier-based fast and strong model execution,
 * strict response schema validation, and instantaneous fallback to MockAiModelGateway on timeout or failure.
 */
@org.springframework.context.annotation.Primary
@Component
public class OpenAiCompatibleAiModelGateway implements AiModelGateway {

    private static final Logger log = LoggerFactory.getLogger(OpenAiCompatibleAiModelGateway.class);

    private final ObjectMapper objectMapper;
    private final MockAiModelGateway fallbackGateway;
    private final HttpClient httpClient;

    @Value("${medikiosk.ai.provider:mock}")
    private String providerType;

    @Value("${medikiosk.ai.base-url:https://openrouter.ai/api/v1}")
    private String baseUrl;

    @Value("${medikiosk.ai.api-key:}")
    private String apiKey;

    @Value("${medikiosk.ai.model:gpt-4o-mini}")
    private String defaultModel;

    @Value("${medikiosk.ai.fast-model:gpt-4o-mini}")
    private String fastModel;

    @Value("${medikiosk.ai.strong-model:gpt-4o}")
    private String strongModel;

    @Value("${medikiosk.ai.external-tests:false}")
    private boolean externalTestsEnabled;

    private volatile String lastStatus = "CONFIGURED";

    public OpenAiCompatibleAiModelGateway(ObjectMapper objectMapper, MockAiModelGateway fallbackGateway) {
        this.objectMapper = objectMapper;
        this.fallbackGateway = fallbackGateway;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(5))
                .build();
    }

    @Override
    public <T> T generateStructured(String systemPrompt, String userPrompt, Class<T> responseType) {
        if (isMock()) {
            return fallbackGateway.generateStructured(systemPrompt, userPrompt, responseType);
        }

        try {
            String rawJson = callChatCompletion(systemPrompt, userPrompt, strongModel);
            if (rawJson != null) {
                lastStatus = "CONNECTED";
                return objectMapper.readValue(rawJson, responseType);
            }
        } catch (Exception e) {
            lastStatus = "UNAVAILABLE";
            log.warn("External AI call failed ({}); falling back to local reasoning gateway.", e.getMessage());
        }
        return fallbackGateway.generateStructured(systemPrompt, userPrompt, responseType);
    }

    @Override
    public String generateQuestion(String intent, String context, String language) {
        if (isMock()) {
            return fallbackGateway.generateQuestion(intent, context, language);
        }

        try {
            String userPrompt = String.format("Intent: %s\nContext: %s\nLanguage: %s\nGenerate a single patient-facing question.",
                    intent, context, language != null ? language : "en");
            String result = callChatCompletion("You are a clinical intake question generator. Return only the question wording.", userPrompt, fastModel);
            if (result != null && !result.trim().isEmpty()) {
                lastStatus = "CONNECTED";
                return result.trim().replace("\"", "");
            }
        } catch (Exception e) {
            lastStatus = "UNAVAILABLE";
            log.warn("External AI question generation failed ({}); falling back to pre-approved template.", e.getMessage());
        }
        return fallbackGateway.generateQuestion(intent, context, language);
    }

    @Override
    public String extractClinicalContext(String patientText, String history) {
        if (isMock()) {
            return fallbackGateway.extractClinicalContext(patientText, history);
        }

        try {
            String userPrompt = String.format("Patient statement: %s\nPrior conversation: %s", patientText, history);
            String result = callChatCompletion("Extract clinical facts in JSON format.", userPrompt, fastModel);
            if (result != null) {
                lastStatus = "CONNECTED";
                return result;
            }
        } catch (Exception e) {
            lastStatus = "UNAVAILABLE";
            log.warn("External clinical extraction failed ({}); falling back to local extraction.", e.getMessage());
        }
        return fallbackGateway.extractClinicalContext(patientText, history);
    }

    private String callChatCompletion(String systemPrompt, String userPrompt, String model) throws Exception {
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", model);
        requestBody.put("messages", List.of(
                Map.of("role", "system", "content", systemPrompt),
                Map.of("role", "user", "content", userPrompt)
        ));
        requestBody.put("temperature", 0.2);

        String jsonPayload = objectMapper.writeValueAsString(requestBody);

        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/chat/completions"))
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + apiKey)
                .POST(HttpRequest.BodyPublishers.ofString(jsonPayload))
                .timeout(Duration.ofSeconds(6))
                .build();

        HttpResponse<String> response = httpClient.send(req, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() >= 200 && response.statusCode() < 300) {
            JsonNode root = objectMapper.readTree(response.body());
            JsonNode choices = root.path("choices");
            if (choices.isArray() && !choices.isEmpty()) {
                return choices.get(0).path("message").path("content").asText();
            }
        }
        throw new RuntimeException("API returned HTTP " + response.statusCode());
    }

    @Override
    public String getProviderName() {
        return isMock() ? "MockAiModelGateway" : "OpenAiCompatibleAiModelGateway (" + providerType + ")";
    }

    @Override
    public String getProviderStatus() {
        if (isMock()) return "MOCK";
        return lastStatus;
    }

    public boolean isExternalTestsEnabled() {
        return externalTestsEnabled;
    }

    @Override
    public boolean isMock() {
        return "mock".equalsIgnoreCase(providerType) || apiKey == null || apiKey.trim().isEmpty();
    }
}
