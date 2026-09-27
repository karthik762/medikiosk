package com.medikiosk.voice.provider;

import java.util.Map;

/**
 * Pluggable provider interface for speech-to-text transcription.
 * Isolates external vendor / engine implementations from MediKiosk domain.
 */
public interface SpeechToTextProvider {

    /**
     * Transcribes input audio byte stream into text.
     *
     * @param audioData   Raw audio data bytes.
     * @param contentType Audio MIME format (e.g., audio/wav, audio/webm).
     * @param language    Target language code (e.g. en, hi, te).
     * @param metadata    Optional provider parameters.
     * @return Transcription result containing text, confidence, and status.
     */
    TranscriptionResult transcribe(byte[] audioData, String contentType, String language, Map<String, Object> metadata);

    /**
     * Identifies provider name.
     */
    String getProviderName();

    /**
     * Checks if provider is configured and available.
     */
    boolean isAvailable();
}
