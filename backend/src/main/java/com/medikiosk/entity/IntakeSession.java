package com.medikiosk.entity;

import com.medikiosk.entity.enums.IntakeSessionStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.time.Instant;

/**
 * Represents one clinical intake session for a patient.
 */
@Entity
@Table(name = "intake_sessions")
public class IntakeSession extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "patient_id", nullable = false)
    private Patient patient;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 32)
    private IntakeSessionStatus status = IntakeSessionStatus.CREATED;

    @Column(name = "language", length = 32)
    private String language = "en";

    @Column(name = "started_at")
    private Instant startedAt;

    @Column(name = "completed_at")
    private Instant completedAt;

    @Column(name = "expires_at")
    private Instant expiresAt;

    public IntakeSession() {
    }

    public IntakeSession(Patient patient) {
        this.patient = patient;
        this.status = IntakeSessionStatus.CREATED;
        this.startedAt = Instant.now();
    }

    public Patient getPatient() {
        return patient;
    }

    public void setPatient(Patient patient) {
        this.patient = patient;
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
