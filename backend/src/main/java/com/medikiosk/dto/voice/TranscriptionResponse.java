package com.medikiosk.dto.voice;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * REST response representing transcribed speech and transcript provenance.
 * Must be presented to the patient for review and confirmation before final saving.
 * Exposes full provenance: inputMethod, provider, providerType, language, timestamp, session.
 */
public class TranscriptionResponse {

    private UUID sessionId;
    private String transcript;
    private BigDecimal confidence;
    private String language;
    private String status;
    private Boolean isMock;

    // Spec provenance fields
    private String inputMethod = "VOICE";
    private String provider;
    private String providerType; // "REAL" or "MOCK"
    private Instant timestamp = Instant.now();

    public TranscriptionResponse() {
    }

    public TranscriptionResponse(UUID sessionId, String transcript, BigDecimal confidence,
                                 String language, String status, Boolean isMock) {
        this.sessionId = sessionId;
        this.transcript = transcript;
        this.confidence = confidence;
        this.language = language;
        this.status = status;
        this.isMock = isMock;
        this.provider = Boolean.TRUE.equals(isMock) ? "MOCK_STT_PROVIDER" : "REAL";
        this.providerType = Boolean.TRUE.equals(isMock) ? "MOCK" : "REAL";
        this.inputMethod = "VOICE";
        this.timestamp = Instant.now();
    }

    public TranscriptionResponse(UUID sessionId, String transcript, BigDecimal confidence,
                                 String language, String status, Boolean isMock,
                                 String provider, String providerType) {
        this.sessionId = sessionId;
        this.transcript = transcript;
        this.confidence = confidence;
        this.language = language;
        this.status = status;
        this.isMock = isMock;
        this.provider = provider;
        this.providerType = providerType;
        this.inputMethod = "VOICE";
        this.timestamp = Instant.now();
    }

    public UUID getSessionId() {
        return sessionId;
    }

    public void setSessionId(UUID sessionId) {
        this.sessionId = sessionId;
    }

    public String getTranscript() {
        return transcript;
    }

    public void setTranscript(String transcript) {
        this.transcript = transcript;
    }

    public BigDecimal getConfidence() {
        return confidence;
    }

    public void setConfidence(BigDecimal confidence) {
        this.confidence = confidence;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Boolean getIsMock() {
        return isMock;
    }

    public void setIsMock(Boolean isMock) {
        this.isMock = isMock;
    }

    public String getInputMethod() {
        return inputMethod;
    }

    public void setInputMethod(String inputMethod) {
        this.inputMethod = inputMethod;
    }

    public String getProvider() {
        return provider;
    }

    public void setProvider(String provider) {
        this.provider = provider;
    }

    public String getProviderType() {
        return providerType;
    }

    public void setProviderType(String providerType) {
        this.providerType = providerType;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Instant timestamp) {
        this.timestamp = timestamp;
    }
}
