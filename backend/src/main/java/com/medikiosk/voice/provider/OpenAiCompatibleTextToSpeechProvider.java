package com.medikiosk.voice.provider;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

/**
 * Real OpenAI-compatible Text-To-Speech provider implementation.
 * Performs real TTS audio synthesis over HTTP when configured with valid credentials,
 * with automatic fallback to MockTextToSpeechProvider when unavailable or unconfigured.
 */
public class OpenAiCompatibleTextToSpeechProvider implements TextToSpeechProvider {

    private static final Logger log = LoggerFactory.getLogger(OpenAiCompatibleTextToSpeechProvider.class);
    public static final String PROVIDER_NAME = "OPENAI_TTS_PROVIDER";

    private final MockTextToSpeechProvider fallbackProvider;
    private final ObjectMapper objectMapper;
    private final HttpClient httpClient;

    @Value("${app.voice.provider:mock}")
    private String providerType;

    @Value("${app.voice.tts.base-url:${medikiosk.ai.base-url:https://api.openai.com/v1}}")
    private String baseUrl;

    @Value("${app.voice.tts.api-key:${medikiosk.ai.api-key:}}")
    private String apiKey;

    @Value("${app.voice.tts.model:tts-1}")
    private String model;

    @Value("${app.voice.tts.voice:alloy}")
    private String voice;

    public OpenAiCompatibleTextToSpeechProvider(MockTextToSpeechProvider fallbackProvider, ObjectMapper objectMapper) {
        this.fallbackProvider = fallbackProvider;
        this.objectMapper = objectMapper;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(5))
                .build();
    }

    @Override
    public AudioSynthesisResult synthesize(String text, String language, Map<String, Object> metadata) {
        if (isMock() || text == null || text.isBlank()) {
            return fallbackProvider.synthesize(text, language, metadata);
        }

        try {
            Map<String, Object> payload = new HashMap<>();
            payload.put("model", model);
            payload.put("input", text);
            payload.put("voice", voice);
            payload.put("response_format", "mp3");

            String jsonPayload = objectMapper.writeValueAsString(payload);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(baseUrl + "/audio/speech"))
                    .header("Authorization", "Bearer " + apiKey)
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(jsonPayload))
                    .timeout(Duration.ofSeconds(8))
                    .build();

            HttpResponse<byte[]> response = httpClient.send(request, HttpResponse.BodyHandlers.ofByteArray());

            if (response.statusCode() >= 200 && response.statusCode() < 300) {
                byte[] audioBytes = response.body();
                if (audioBytes != null && audioBytes.length > 0) {
                    return AudioSynthesisResult.success(audioBytes, "audio/mp3", language != null ? language : "en", false);
                }
            }
            log.warn("Real TTS provider returned status {}; falling back to mock provider.", response.statusCode());
        } catch (Exception e) {
            log.warn("Real TTS provider call failed ({}); falling back to local PCM speech synthesis.", e.getMessage());
        }

        return fallbackProvider.synthesize(text, language, metadata);
    }

    @Override
    public String getProviderName() {
        return isMock() ? fallbackProvider.getProviderName() : PROVIDER_NAME;
    }

    @Override
    public boolean isAvailable() {
        return true;
    }

    private boolean isMock() {
        return "mock".equalsIgnoreCase(providerType) || apiKey == null || apiKey.trim().isEmpty();
    }
}
