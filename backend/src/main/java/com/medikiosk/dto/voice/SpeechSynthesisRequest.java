package com.medikiosk.dto.voice;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.UUID;

/**
 * REST request to synthesize approved question or UI prompt text into speech.
 * Does NOT generate autonomous clinical advice.
 */
public class SpeechSynthesisRequest {

    @NotBlank(message = "Text to synthesize is required")
    @Size(max = 1000, message = "Text cannot exceed 1000 characters")
    private String text;

    private String language = "en";

    private UUID sessionId;

    public SpeechSynthesisRequest() {
    }

    public SpeechSynthesisRequest(String text, String language) {
        this.text = text;
        this.language = language != null ? language : "en";
    }

    public SpeechSynthesisRequest(String text, String language, UUID sessionId) {
        this.text = text;
        this.language = language != null ? language : "en";
        this.sessionId = sessionId;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public UUID getSessionId() {
        return sessionId;
    }

    public void setSessionId(UUID sessionId) {
        this.sessionId = sessionId;
    }
}
