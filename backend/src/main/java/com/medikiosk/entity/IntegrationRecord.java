package com.medikiosk.entity;

import com.medikiosk.entity.enums.IntegrationStatus;
import com.medikiosk.entity.enums.IntegrationType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

/**
 * Audit log of integration transactions with ABDM, HIS, AWS HealthLake, etc.
 */
@Entity
@Table(name = "integration_records")
public class IntegrationRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "patient_id")
    private Patient patient;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "session_id")
    private IntakeSession session;

    @Enumerated(EnumType.STRING)
    @Column(name = "integration_type", nullable = false, length = 32)
    private IntegrationType integrationType;

    @Column(name = "operation", nullable = false, length = 32)
    private String operation;

    @Column(name = "external_reference")
    private String externalReference;

    @Column(name = "idempotency_key", length = 128)
    private String idempotencyKey;

    @Column(name = "response_payload", columnDefinition = "TEXT")
    private String responsePayload;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 32)
    private IntegrationStatus status = IntegrationStatus.PENDING;

    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;

    @Column(name = "attempt_count")
    private Integer attemptCount = 1;

    @Column(name = "attempted_at", nullable = false)
    private Instant attemptedAt;

    @Column(name = "completed_at")
    private Instant completedAt;

    @PrePersist
    protected void onCreate() {
        if (this.attemptedAt == null) {
            this.attemptedAt = Instant.now();
        }
    }

    public IntegrationRecord() {
    }

    public IntegrationRecord(IntegrationType integrationType, String operation, Patient patient, IntakeSession session) {
        this.integrationType = integrationType;
        this.operation = operation;
        this.patient = patient;
        this.session = session;
        this.status = IntegrationStatus.PENDING;
        this.attemptCount = 1;
        this.attemptedAt = Instant.now();
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public Patient getPatient() {
        return patient;
    }

    public void setPatient(Patient patient) {
        this.patient = patient;
    }

    public IntakeSession getSession() {
        return session;
    }

    public void setSession(IntakeSession session) {
        this.session = session;
    }

    public IntegrationType getIntegrationType() {
        return integrationType;
    }

    public void setIntegrationType(IntegrationType integrationType) {
        this.integrationType = integrationType;
    }

    public String getOperation() {
        return operation;
    }

    public void setOperation(String operation) {
        this.operation = operation;
    }

    public String getExternalReference() {
        return externalReference;
    }

    public void setExternalReference(String externalReference) {
        this.externalReference = externalReference;
    }

    public String getIdempotencyKey() {
        return idempotencyKey;
    }

    public void setIdempotencyKey(String idempotencyKey) {
        this.idempotencyKey = idempotencyKey;
    }

    public String getResponsePayload() {
        return responsePayload;
    }

    public void setResponsePayload(String responsePayload) {
        this.responsePayload = responsePayload;
    }

    public IntegrationStatus getStatus() {
        return status;
    }

    public void setStatus(IntegrationStatus status) {
        this.status = status;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public Integer getAttemptCount() {
        return attemptCount;
    }

    public void setAttemptCount(Integer attemptCount) {
        this.attemptCount = attemptCount;
    }

    public Instant getAttemptedAt() {
        return attemptedAt;
    }

    public void setAttemptedAt(Instant attemptedAt) {
        this.attemptedAt = attemptedAt;
    }

    public Instant getCompletedAt() {
        return completedAt;
    }

    public void setCompletedAt(Instant completedAt) {
        this.completedAt = completedAt;
    }
}
