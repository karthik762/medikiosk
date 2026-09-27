package com.medikiosk.dto.intake;

import java.time.Instant;
import java.util.UUID;

public class ChiefComplaintResponse {

    private UUID id;
    private UUID sessionId;
    private String complaintText;
    private String inputMethod;
    private Instant answeredAt;

    public ChiefComplaintResponse() {
    }

    public ChiefComplaintResponse(UUID id, UUID sessionId, String complaintText, String inputMethod, Instant answeredAt) {
        this.id = id;
        this.sessionId = sessionId;
        this.complaintText = complaintText;
        this.inputMethod = inputMethod;
        this.answeredAt = answeredAt;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getSessionId() {
        return sessionId;
    }

    public void setSessionId(UUID sessionId) {
        this.sessionId = sessionId;
    }

    public String getComplaintText() {
        return complaintText;
    }

    public void setComplaintText(String complaintText) {
        this.complaintText = complaintText;
    }

    public String getInputMethod() {
        return inputMethod;
    }

    public void setInputMethod(String inputMethod) {
        this.inputMethod = inputMethod;
    }

    public Instant getAnsweredAt() {
        return answeredAt;
    }

    public void setAnsweredAt(Instant answeredAt) {
        this.answeredAt = answeredAt;
    }
}
