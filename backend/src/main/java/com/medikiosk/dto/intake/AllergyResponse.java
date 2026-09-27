package com.medikiosk.dto.intake;

import java.time.Instant;
import java.util.UUID;

public class AllergyResponse {

    private UUID id;
    private UUID patientId;
    private UUID sessionId;
    private String allergen;
    private String reaction;
    private String severity;
    private String source;
    private Boolean verified;
    private Instant createdAt;

    public AllergyResponse() {
    }

    public AllergyResponse(UUID id, UUID patientId, UUID sessionId, String allergen,
                           String reaction, String severity, String source,
                           Boolean verified, Instant createdAt) {
        this.id = id;
        this.patientId = patientId;
        this.sessionId = sessionId;
        this.allergen = allergen;
        this.reaction = reaction;
        this.severity = severity;
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

    public String getAllergen() {
        return allergen;
    }

    public void setAllergen(String allergen) {
        this.allergen = allergen;
    }

    public String getReaction() {
        return reaction;
    }

    public void setReaction(String reaction) {
        this.reaction = reaction;
    }

    public String getSeverity() {
        return severity;
    }

    public void setSeverity(String severity) {
        this.severity = severity;
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
