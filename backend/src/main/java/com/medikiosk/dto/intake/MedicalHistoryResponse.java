package com.medikiosk.dto.intake;

import java.time.Instant;
import java.util.UUID;

public class MedicalHistoryResponse {

    private UUID id;
    private UUID patientId;
    private UUID sessionId;
    private String category;
    private String description;
    private String source;
    private Boolean verified;
    private Instant createdAt;

    public MedicalHistoryResponse() {
    }

    public MedicalHistoryResponse(UUID id, UUID patientId, UUID sessionId, String category,
                                  String description, String source, Boolean verified, Instant createdAt) {
        this.id = id;
        this.patientId = patientId;
        this.sessionId = sessionId;
        this.category = category;
        this.description = description;
        this.source = source;
        this.verified = verified;
        this.createdAt = createdAt;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getPatientId() {
        return patientId;
    }

    public void setPatientId(UUID patientId) {
        this.patientId = patientId;
    }

    public UUID getSessionId() {
        return sessionId;
    }

    public void setSessionId(UUID sessionId) {
        this.sessionId = sessionId;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public Boolean getVerified() {
        return verified;
    }

    public void setVerified(Boolean verified) {
        this.verified = verified;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }
}
