package com.medikiosk.abdm.dto;

import com.medikiosk.entity.IntegrationRecord;
import com.medikiosk.entity.enums.IntegrationStatus;
import com.medikiosk.entity.enums.IntegrationType;

import java.time.Instant;
import java.util.UUID;

/**
 * Clean data transfer object for IntegrationRecord entities.
 * Avoids exposing JPA entities and Hibernate proxies in REST API responses.
 */
public class IntegrationRecordDto {

    private UUID id;
    private UUID patientId;
    private UUID sessionId;
    private IntegrationType integrationType;
    private String operation;
    private String externalReference;
    private String idempotencyKey;
    private IntegrationStatus status;
    private String errorMessage;
    private Integer attemptCount;
    private Instant attemptedAt;
    private Instant completedAt;

    public IntegrationRecordDto() {
    }

    public static IntegrationRecordDto fromEntity(IntegrationRecord entity) {
        if (entity == null) {
            return null;
        }
        IntegrationRecordDto dto = new IntegrationRecordDto();
        dto.setId(entity.getId());
        dto.setPatientId(entity.getPatient() != null ? entity.getPatient().getId() : null);
        dto.setSessionId(entity.getSession() != null ? entity.getSession().getId() : null);
        dto.setIntegrationType(entity.getIntegrationType());
        dto.setOperation(entity.getOperation());
        dto.setExternalReference(entity.getExternalReference());
        dto.setIdempotencyKey(entity.getIdempotencyKey());
        dto.setStatus(entity.getStatus());
        dto.setErrorMessage(entity.getErrorMessage());
        dto.setAttemptCount(entity.getAttemptCount());
        dto.setAttemptedAt(entity.getAttemptedAt());
        dto.setCompletedAt(entity.getCompletedAt());
        return dto;
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
