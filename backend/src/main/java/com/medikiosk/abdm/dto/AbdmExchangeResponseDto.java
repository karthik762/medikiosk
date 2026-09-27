package com.medikiosk.abdm.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.medikiosk.entity.enums.IntegrationStatus;

import java.time.Instant;
import java.util.UUID;

/**
 * Result of clinical data exchange with ABDM.
 */
public class AbdmExchangeResponseDto {

    private UUID integrationRecordId;
    private UUID sessionId;
    private UUID patientId;
    private IntegrationStatus status;
    private String transactionId;
    private String bundleId;
    private String idempotencyKey;

    @JsonProperty("isMock")
    private boolean isMock;
    private Instant attemptedAt;
    private Instant completedAt;
    private String errorMessage;
    private String message;

    public AbdmExchangeResponseDto() {
    }

    public AbdmExchangeResponseDto(UUID integrationRecordId, UUID sessionId, UUID patientId, IntegrationStatus status, String transactionId, String bundleId, String idempotencyKey, boolean isMock, Instant attemptedAt, Instant completedAt, String errorMessage, String message) {
        this.integrationRecordId = integrationRecordId;
        this.sessionId = sessionId;
        this.patientId = patientId;
        this.status = status;
        this.transactionId = transactionId;
        this.bundleId = bundleId;
        this.idempotencyKey = idempotencyKey;
        this.isMock = isMock;
        this.attemptedAt = attemptedAt;
        this.completedAt = completedAt;
        this.errorMessage = errorMessage;
        this.message = message;
    }

    public UUID getIntegrationRecordId() {
        return integrationRecordId;
    }

    public void setIntegrationRecordId(UUID integrationRecordId) {
        this.integrationRecordId = integrationRecordId;
    }

    public UUID getSessionId() {
        return sessionId;
    }

    public void setSessionId(UUID sessionId) {
        this.sessionId = sessionId;
    }

    public UUID getPatientId() {
        return patientId;
    }

    public void setPatientId(UUID patientId) {
        this.patientId = patientId;
    }

    public IntegrationStatus getStatus() {
        return status;
    }

    public void setStatus(IntegrationStatus status) {
        this.status = status;
    }

    public String getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }

    public String getBundleId() {
        return bundleId;
    }

    public void setBundleId(String bundleId) {
        this.bundleId = bundleId;
    }

    public String getIdempotencyKey() {
        return idempotencyKey;
    }

    public void setIdempotencyKey(String idempotencyKey) {
        this.idempotencyKey = idempotencyKey;
    }

    public boolean isMock() {
        return isMock;
    }

    public void setMock(boolean mock) {
        isMock = mock;
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

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
