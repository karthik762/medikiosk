package com.medikiosk.dto.intake;

import java.time.Instant;
import java.util.UUID;

public class SymptomResponse {

    private UUID id;
    private UUID sessionId;
    private String name;
    private String location;
    private String duration;
    private Integer severity;
    private String frequency;
    private String onset;
    private String source;
    private Boolean verified;
    private Instant createdAt;

    public SymptomResponse() {
    }

    public SymptomResponse(UUID id, UUID sessionId, String name, String location, String duration,
                           Integer severity, String frequency, String onset, String source,
                           Boolean verified, Instant createdAt) {
        this.id = id;
        this.sessionId = sessionId;
        this.name = name;
        this.location = location;
        this.duration = duration;
        this.severity = severity;
        this.frequency = frequency;
        this.onset = onset;
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

    public UUID getSessionId() {
        return sessionId;
    }

    public void setSessionId(UUID sessionId) {
        this.sessionId = sessionId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getDuration() {
        return duration;
    }

    public void setDuration(String duration) {
        this.duration = duration;
    }

    public Integer getSeverity() {
        return severity;
    }

    public void setSeverity(Integer severity) {
        this.severity = severity;
    }

    public String getFrequency() {
        return frequency;
    }

    public void setFrequency(String frequency) {
        this.frequency = frequency;
    }

    public String getOnset() {
        return onset;
    }

    public void setOnset(String onset) {
        this.onset = onset;
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
