package com.medikiosk.voice.provider;

import java.util.Map;

/**
 * Pluggable provider interface for text-to-speech audio synthesis.
 * Isolates external vendor / engine implementations from MediKiosk domain.
 */
public interface TextToSpeechProvider {

    /**
     * Synthesizes approved UI or question text into spoken audio.
     *
     * @param text     Approved prompt text to read aloud.
     * @param language Target spoken language code (e.g. en, hi, te).
     * @param metadata Optional provider parameters.
     * @return Audio synthesis result containing audio bytes, format, and status.
     */
    AudioSynthesisResult synthesize(String text, String language, Map<String, Object> metadata);

    /**
     * Identifies provider name.
     */
    String getProviderName();

    /**
     * Checks if provider is configured and available.
     */
    boolean isAvailable();
}
