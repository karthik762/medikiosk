package com.medikiosk.voice.provider;

import com.medikiosk.voice.util.AudioSilenceDetector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.util.Map;

/**
 * Mock development implementation of SpeechToTextProvider.
 * Explicitly marked as mock (isMock = true) when external cloud speech services are not configured.
 *
 * Conforms to strict silence and no-speech rules:
 * - Silent audio or empty input returns Status.NO_SPEECH_DETECTED (never invents speech).
 * - Honors test metadata and embedded test audio for automated tests.
 * - Does NOT generate clinical diagnoses or autonomously classify medical conditions.
 */
public class MockSpeechToTextProvider implements SpeechToTextProvider {

    private static final Logger log = LoggerFactory.getLogger(MockSpeechToTextProvider.class);

    public static final String PROVIDER_NAME = "MOCK_STT_PROVIDER";

    @Override
    public TranscriptionResult transcribe(byte[] audioData, String contentType, String language, Map<String, Object> metadata) {
        log.info("MockSpeechToTextProvider processing audio input (size: {} bytes, format: {}, language: {})",
                audioData != null ? audioData.length : 0, contentType, language);

        if (metadata != null && Boolean.TRUE.equals(metadata.get("simulate_failure"))) {
            log.warn("MockSpeechToTextProvider simulating provider failure as requested by test metadata");
            return TranscriptionResult.failure("Speech recognition service temporarily unavailable. Please retry or use touch/text input.", language, true);
        }

        // 1. Strict Silence Check: Silence must NEVER become speech
        if (AudioSilenceDetector.isSilence(audioData, contentType)) {
            log.info("MockSpeechToTextProvider: Audio is silent or below speech energy threshold. Returning NO_SPEECH_DETECTED.");
            return TranscriptionResult.noSpeech(language, true);
        }

        BigDecimal confidence = BigDecimal.valueOf(0.9500);
        if (metadata != null && metadata.containsKey("test_confidence")) {
            Object confObj = metadata.get("test_confidence");
            if (confObj instanceof BigDecimal) {
                confidence = (BigDecimal) confObj;
            } else if (confObj instanceof Number) {
                confidence = BigDecimal.valueOf(((Number) confObj).doubleValue());
            }
        }

        // 2. Deterministic test transcript from test metadata
        if (metadata != null && metadata.containsKey("test_transcript")) {
            String transcript = (String) metadata.get("test_transcript");
            log.info("MockSpeechToTextProvider using test metadata transcript: '{}'", transcript);
            return TranscriptionResult.success(transcript, confidence, language, true);
        }

        // 3. Embedded test transcript from audio payload
        String embedded = extractEmbeddedWavTranscript(audioData);
        if (embedded != null && !embedded.trim().isEmpty()) {
            if ("hi".equalsIgnoreCase(language) && !containsDevanagari(embedded)) {
                return TranscriptionResult.success(getDefaultTranscriptForLanguage("hi"), confidence, language, true);
            } else if ("te".equalsIgnoreCase(language) && !containsTelugu(embedded)) {
                return TranscriptionResult.success(getDefaultTranscriptForLanguage("te"), confidence, language, true);
            }
            log.info("MockSpeechToTextProvider using embedded audio metadata transcript: '{}'", embedded);
            return TranscriptionResult.success(embedded.trim(), confidence, language, true);
        }

        // 4. Default non-silent audio in automated tests
        return TranscriptionResult.success(getDefaultTranscriptForLanguage(language), confidence, language, true);
    }

    private boolean containsDevanagari(String s) {
        if (s == null) return false;
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (c >= '\u0900' && c <= '\u097F') {
                return true;
            }
        }
        return false;
    }

    private boolean containsTelugu(String s) {
        if (s == null) return false;
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (c >= '\u0C00' && c <= '\u0C7F') {
                return true;
            }
        }
        return false;
    }

    private String getDefaultTranscriptForLanguage(String language) {
        if (language == null) {
            return "I have had a mild headache and fever for two days.";
        }
        String langLower = language.toLowerCase();
        if (langLower.startsWith("hi")) {
            return "मुझे दो दिनों से हल्का सिरदर्द और बुखार है।";
        } else if (langLower.startsWith("te")) {
            return "నాకు రెండు రోజులుగా తేలికపాటి తలనొప్పి మరియు జ్వరం ఉంది.";
        } else {
            return "I have had a mild headache and fever for two days.";
        }
    }

    /**
     * Extracts embedded INAM (title) or ICMT (comment) metadata from a RIFF WAV file if present.
     */
    public static String extractEmbeddedWavTranscript(byte[] data) {
        if (data == null || data.length < 24) {
            return null;
        }

        try {
            int offset = 12; // Skip "RIFF" + size + "WAVE"
            while (offset + 8 <= data.length) {
                byte c0 = data[offset];
                byte c1 = data[offset + 1];
                byte c2 = data[offset + 2];
                byte c3 = data[offset + 3];

                int chunkSize = ByteBuffer.wrap(data, offset + 4, 4).order(ByteOrder.LITTLE_ENDIAN).getInt();
                if (chunkSize < 0 || offset + 8 + chunkSize > data.length + 1) {
                    break;
                }

                // Check for "LIST" chunk
                if (c0 == 'L' && c1 == 'I' && c2 == 'S' && c3 == 'T' && chunkSize >= 4) {
                    int subOffset = offset + 8;
                    // Skip LIST type (e.g. "INFO")
                    subOffset += 4;
                    while (subOffset + 8 <= offset + 8 + chunkSize) {
                        byte s0 = data[subOffset];
                        byte s1 = data[subOffset + 1];
                        byte s2 = data[subOffset + 2];
                        byte s3 = data[subOffset + 3];
                        int subChunkSize = ByteBuffer.wrap(data, subOffset + 4, 4).order(ByteOrder.LITTLE_ENDIAN).getInt();

                        if (subChunkSize > 0 && subOffset + 8 + subChunkSize <= data.length) {
                            if ((s0 == 'I' && s1 == 'N' && s2 == 'A' && s3 == 'M') ||
                                    (s0 == 'I' && s1 == 'C' && s2 == 'M' && s3 == 'T')) {
                                return new String(data, subOffset + 8, subChunkSize, StandardCharsets.UTF_8).trim();
                            }
                        }
                        int advance = 8 + Math.max(0, subChunkSize);
                        if (advance % 2 != 0) advance++;
                        subOffset += advance;
                    }
                }

                // Check for direct INAM or ICMT chunk
                if ((c0 == 'I' && c1 == 'N' && c2 == 'A' && c3 == 'M') ||
                        (c0 == 'I' && c1 == 'C' && c2 == 'M' && c3 == 'T')) {
                    if (chunkSize > 0 && offset + 8 + chunkSize <= data.length) {
                        return new String(data, offset + 8, chunkSize, StandardCharsets.UTF_8).trim();
                    }
                }

                int advance = 8 + Math.max(0, chunkSize);
                if (advance % 2 != 0) advance++;
                offset += advance;
            }
        } catch (Exception e) {
            log.debug("Error while inspecting WAV for embedded transcript: {}", e.getMessage());
        }

        return null;
    }

    @Override
    public String getProviderName() {
        return PROVIDER_NAME;
    }

    @Override
    public boolean isAvailable() {
        return true;
    }
}
