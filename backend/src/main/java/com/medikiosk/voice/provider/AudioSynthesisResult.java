package com.medikiosk.voice.provider;

import java.util.Collections;
import java.util.Map;

/**
 * Result of text-to-speech audio synthesis from a provider.
 */
public class AudioSynthesisResult {

    public enum Status {
        SUCCESS,
        FAILED,
        UNSUPPORTED_LANGUAGE
    }

    private final byte[] audioData;
    private final String contentType;
    private final String language;
    private final Status status;
    private final String errorMessage;
    private final boolean isMock;
    private final Map<String, Object> metadata;

    public AudioSynthesisResult(byte[] audioData, String contentType, String language,
                                Status status, String errorMessage, boolean isMock,
                                Map<String, Object> metadata) {
        this.audioData = audioData;
        this.contentType = contentType;
        this.language = language;
        this.status = status;
        this.errorMessage = errorMessage;
        this.isMock = isMock;
        this.metadata = metadata != null ? metadata : Collections.emptyMap();
    }

    public static AudioSynthesisResult success(byte[] audioData, String contentType, String language, boolean isMock) {
        return new AudioSynthesisResult(audioData, contentType, language, Status.SUCCESS, null, isMock, Collections.emptyMap());
    }

    public static AudioSynthesisResult failure(String errorMessage, String language, boolean isMock) {
        return new AudioSynthesisResult(null, null, language, Status.FAILED, errorMessage, isMock, Collections.emptyMap());
    }

    public byte[] getAudioData() {
        return audioData;
    }

    public String getContentType() {
        return contentType;
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
