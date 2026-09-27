package com.medikiosk.dto.intake;

import java.time.Instant;
import java.util.UUID;

public class MedicationResponse {

    private UUID id;
    private UUID patientId;
    private UUID sessionId;
    private String medicationName;
    private String dosage;
    private String frequency;
    private String duration;
    private String source;
    private Boolean verified;
    private Instant createdAt;

    public MedicationResponse() {
    }

    public MedicationResponse(UUID id, UUID patientId, UUID sessionId, String medicationName,
                              String dosage, String frequency, String duration, String source,
                              Boolean verified, Instant createdAt) {
        this.id = id;
        this.patientId = patientId;
        this.sessionId = sessionId;
        this.medicationName = medicationName;
        this.dosage = dosage;
        this.frequency = frequency;
        this.duration = duration;
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

    public String getMedicationName() {
        return medicationName;
    }

    public void setMedicationName(String medicationName) {
        this.medicationName = medicationName;
    }

    public String getDosage() {
        return dosage;
    }

    public void setDosage(String dosage) {
        this.dosage = dosage;
    }

    public String getFrequency() {
        return frequency;
    }

    public void setFrequency(String frequency) {
        this.frequency = frequency;
    }

    public String getDuration() {
        return duration;
    }

    public void setDuration(String duration) {
        this.duration = duration;
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
