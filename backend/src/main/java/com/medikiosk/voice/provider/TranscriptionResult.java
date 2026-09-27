package com.medikiosk.voice.provider;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.Map;

/**
 * Result of speech-to-text transcription from a provider.
 * Does not contain clinical inferences or diagnostic interpretation.
 */
public class TranscriptionResult {

    public enum Status {
        SUCCESS,
        FAILED,
        NO_SPEECH_DETECTED,
        UNSUPPORTED_LANGUAGE
    }

    private final String transcript;
    private final BigDecimal confidence;
    private final String language;
    private final Status status;
    private final String errorMessage;
    private final boolean isMock;
    private final Map<String, Object> metadata;

    public TranscriptionResult(String transcript, BigDecimal confidence, String language,
                               Status status, String errorMessage, boolean isMock,
                               Map<String, Object> metadata) {
        this.transcript = transcript;
        this.confidence = confidence;
        this.language = language;
        this.status = status;
        this.errorMessage = errorMessage;
        this.isMock = isMock;
        this.metadata = metadata != null ? metadata : Collections.emptyMap();
    }

    public static TranscriptionResult success(String transcript, BigDecimal confidence, String language, boolean isMock) {
        return new TranscriptionResult(transcript, confidence, language, Status.SUCCESS, null, isMock, Collections.emptyMap());
    }

    public static TranscriptionResult failure(String errorMessage, String language, boolean isMock) {
        return new TranscriptionResult(null, null, language, Status.FAILED, errorMessage, isMock, Collections.emptyMap());
    }

    public static TranscriptionResult noSpeech(String language, boolean isMock) {
        return new TranscriptionResult("", BigDecimal.ZERO, language, Status.NO_SPEECH_DETECTED, "No speech detected in audio", isMock, Collections.emptyMap());
    }

    public String getTranscript() {
        return transcript;
    }

    public BigDecimal getConfidence() {
        return confidence;
    }

    public String getLanguage() {
        return language;
    }

    public Status getStatus() {
        return status;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public boolean isMock() {
        return isMock;
    }

    public Map<String, Object> getMetadata() {
        return metadata;
    }
}
