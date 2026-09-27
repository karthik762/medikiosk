package com.medikiosk.dto.intake;

import com.medikiosk.entity.enums.IntakeSessionStatus;
import java.time.Instant;
import java.util.UUID;

public class IntakeSessionResponse {

    private UUID id;
    private UUID patientId;
    private String patientIdentifier;
    private IntakeSessionStatus status;
    private String language;
    private Instant startedAt;
    private Instant completedAt;
    private Instant expiresAt;

    public IntakeSessionResponse() {
    }

    public IntakeSessionResponse(UUID id, UUID patientId, String patientIdentifier, IntakeSessionStatus status,
                                 String language, Instant startedAt, Instant completedAt, Instant expiresAt) {
        this.id = id;
        this.patientId = patientId;
        this.patientIdentifier = patientIdentifier;
        this.status = status;
        this.language = language;
        this.startedAt = startedAt;
        this.completedAt = completedAt;
        this.expiresAt = expiresAt;
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

    public String getPatientIdentifier() {
        return patientIdentifier;
    }

    public void setPatientIdentifier(String patientIdentifier) {
        this.patientIdentifier = patientIdentifier;
    }

    public IntakeSessionStatus getStatus() {
        return status;
    }

    public void setStatus(IntakeSessionStatus status) {
        this.status = status;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public Instant getStartedAt() {
        return startedAt;
    }

    public void setStartedAt(Instant startedAt) {
        this.startedAt = startedAt;
    }

    public Instant getCompletedAt() {
        return completedAt;
    }

    public void setCompletedAt(Instant completedAt) {
        this.completedAt = completedAt;
    }

    public Instant getExpiresAt() {
        return expiresAt;
    }

    public void setExpiresAt(Instant expiresAt) {
        this.expiresAt = expiresAt;
    }
}
