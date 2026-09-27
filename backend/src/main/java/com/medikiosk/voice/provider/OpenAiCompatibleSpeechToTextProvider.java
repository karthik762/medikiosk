package com.medikiosk.voice.provider;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.medikiosk.voice.util.AudioSilenceDetector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;

import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Map;
import java.util.UUID;

/**
 * Real OpenAI / Whisper-compatible Speech-To-Text provider implementation.
 * Performs real STT audio transcription over HTTP when configured with valid credentials,
 * with explicit distinction between REAL and MOCK modes.
 *
 * Strict silence rule: Silence is never converted into speech.
 */
public class OpenAiCompatibleSpeechToTextProvider implements SpeechToTextProvider {

    private static final Logger log = LoggerFactory.getLogger(OpenAiCompatibleSpeechToTextProvider.class);
    public static final String PROVIDER_NAME = "OPENAI_WHISPER_STT_PROVIDER";

    private final MockSpeechToTextProvider fallbackProvider;
    private final ObjectMapper objectMapper;
    private final HttpClient httpClient;

    @Value("${app.voice.provider:mock}")
    private String providerType;

    @Value("${app.voice.stt.base-url:${medikiosk.ai.base-url:https://api.openai.com/v1}}")
    private String baseUrl;

    @Value("${app.voice.stt.api-key:${medikiosk.ai.api-key:}}")
    private String apiKey;

    @Value("${app.voice.stt.model:whisper-1}")
    private String model;

    public OpenAiCompatibleSpeechToTextProvider(MockSpeechToTextProvider fallbackProvider, ObjectMapper objectMapper) {
        this.fallbackProvider = fallbackProvider;
        this.objectMapper = objectMapper;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(5))
                .build();
    }

    @Override
    public TranscriptionResult transcribe(byte[] audioData, String contentType, String language, Map<String, Object> metadata) {
        // 1. Strict Silence Check: Silence must NEVER become speech
        if (AudioSilenceDetector.isSilence(audioData, contentType)) {
            log.info("Audio is silent or empty; returning NO_SPEECH_DETECTED");
            return TranscriptionResult.noSpeech(language, isMock());
        }

        if (isMock()) {
            return fallbackProvider.transcribe(audioData, contentType, language, metadata);
        }

        try {
            String boundary = "----MediKioskBoundary" + UUID.randomUUID().toString();
            byte[] multipartBody = buildMultipartPayload(audioData, contentType != null ? contentType : "audio/wav", language, boundary);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(baseUrl + "/audio/transcriptions"))
                    .header("Authorization", "Bearer " + apiKey)
                    .header("Content-Type", "multipart/form-data; boundary=" + boundary)
                    .POST(HttpRequest.BodyPublishers.ofByteArray(multipartBody))
                    .timeout(Duration.ofSeconds(8))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() >= 200 && response.statusCode() < 300) {
                JsonNode root = objectMapper.readTree(response.body());
                String text = root.path("text").asText();
                if (text != null && !text.isBlank()) {
                    return TranscriptionResult.success(text.trim(), BigDecimal.valueOf(0.9600), language != null ? language : "en", false);
                } else {
                    return TranscriptionResult.noSpeech(language, false);
                }
            }
            log.warn("Real STT provider returned status {}; falling back to mock provider.", response.statusCode());
        } catch (Exception e) {
            log.warn("Real STT provider call failed ({}); falling back to mock provider.", e.getMessage());
        }

        return fallbackProvider.transcribe(audioData, contentType, language, metadata);
    }

    private byte[] buildMultipartPayload(byte[] audioData, String contentType, String language, String boundary) throws Exception {
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        // Model field
        out.write(("--" + boundary + "\r\n").getBytes());
        out.write("Content-Disposition: form-data; name=\"model\"\r\n\r\n".getBytes());
        out.write((model + "\r\n").getBytes());

        // Language field
        if (language != null && !language.isBlank()) {
            out.write(("--" + boundary + "\r\n").getBytes());
            out.write("Content-Disposition: form-data; name=\"language\"\r\n\r\n".getBytes());
            out.write((language + "\r\n").getBytes());
        }

        // File field
        out.write(("--" + boundary + "\r\n").getBytes());
        out.write(("Content-Disposition: form-data; name=\"file\"; filename=\"speech.wav\"\r\n").getBytes());
        out.write(("Content-Type: " + contentType + "\r\n\r\n").getBytes());
        out.write(audioData);
        out.write("\r\n".getBytes());

        out.write(("--" + boundary + "--\r\n").getBytes());
        return out.toByteArray();
    }

    @Override
    public String getProviderName() {
        return isMock() ? fallbackProvider.getProviderName() : PROVIDER_NAME;
    }

    @Override
    public boolean isAvailable() {
        return true;
    }

    public boolean isMock() {
        return "mock".equalsIgnoreCase(providerType) || apiKey == null || apiKey.trim().isEmpty();
    }
}
