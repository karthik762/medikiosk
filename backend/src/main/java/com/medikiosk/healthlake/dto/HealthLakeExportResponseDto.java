package com.medikiosk.healthlake.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.medikiosk.entity.enums.IntegrationStatus;

import java.time.Instant;
import java.util.UUID;

/**
 * Result of clinical data export to AWS HealthLake.
 */
public class HealthLakeExportResponseDto {

    private UUID integrationRecordId;
    private UUID sessionId;
    private UUID patientId;
    private IntegrationStatus status;
    private String healthlakeRef;
    private String bundleId;
    private String datastoreId;
    private String idempotencyKey;

    @JsonProperty("isMock")
    private boolean isMock;

    private Instant attemptedAt;
    private Instant completedAt;
    private String errorMessage;
    private String message;

    public HealthLakeExportResponseDto() {
    }

    public HealthLakeExportResponseDto(UUID integrationRecordId, UUID sessionId, UUID patientId,
                                       IntegrationStatus status, String healthlakeRef, String bundleId,
                                       String datastoreId, String idempotencyKey, boolean isMock,
                                       Instant attemptedAt, Instant completedAt, String errorMessage,
                                       String message) {
        this.integrationRecordId = integrationRecordId;
        this.sessionId = sessionId;
        this.patientId = patientId;
        this.status = status;
        this.healthlakeRef = healthlakeRef;
        this.bundleId = bundleId;
        this.datastoreId = datastoreId;
        this.idempotencyKey = idempotencyKey;
        this.isMock = isMock;
        this.attemptedAt = attemptedAt;
        this.completedAt = completedAt;
        this.errorMessage = errorMessage;
        this.message = message;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private UUID integrationRecordId;
        private UUID sessionId;
        private UUID patientId;
        private IntegrationStatus status;
        private String healthlakeRef;
        private String bundleId;
        private String datastoreId;
        private String idempotencyKey;
        private boolean isMock;
        private Instant attemptedAt;
        private Instant completedAt;
        private String errorMessage;
        private String message;

        public Builder integrationRecordId(UUID integrationRecordId) {
            this.integrationRecordId = integrationRecordId;
            return this;
        }

        public Builder sessionId(UUID sessionId) {
            this.sessionId = sessionId;
            return this;
        }

        public Builder patientId(UUID patientId) {
            this.patientId = patientId;
            return this;
        }

        public Builder status(IntegrationStatus status) {
            this.status = status;
            return this;
        }

        public Builder healthlakeRef(String healthlakeRef) {
            this.healthlakeRef = healthlakeRef;
            return this;
        }

        public Builder bundleId(String bundleId) {
            this.bundleId = bundleId;
            return this;
        }

        public Builder datastoreId(String datastoreId) {
            this.datastoreId = datastoreId;
            return this;
        }

        public Builder idempotencyKey(String idempotencyKey) {
            this.idempotencyKey = idempotencyKey;
            return this;
        }

        public Builder isMock(boolean isMock) {
            this.isMock = isMock;
            return this;
        }

        public Builder attemptedAt(Instant attemptedAt) {
            this.attemptedAt = attemptedAt;
            return this;
        }

        public Builder completedAt(Instant completedAt) {
            this.completedAt = completedAt;
            return this;
        }

        public Builder errorMessage(String errorMessage) {
            this.errorMessage = errorMessage;
            return this;
        }

        public Builder message(String message) {
            this.message = message;
            return this;
        }

        public HealthLakeExportResponseDto build() {
            return new HealthLakeExportResponseDto(integrationRecordId, sessionId, patientId,
                    status, healthlakeRef, bundleId, datastoreId, idempotencyKey, isMock,
                    attemptedAt, completedAt, errorMessage, message);
        }
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

    public String getHealthlakeRef() {
        return healthlakeRef;
    }

    public void setHealthlakeRef(String healthlakeRef) {
        this.healthlakeRef = healthlakeRef;
    }

    public String getBundleId() {
        return bundleId;
    }

    public void setBundleId(String bundleId) {
        this.bundleId = bundleId;
    }

    public String getDatastoreId() {
        return datastoreId;
    }

    public void setDatastoreId(String datastoreId) {
        this.datastoreId = datastoreId;
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
