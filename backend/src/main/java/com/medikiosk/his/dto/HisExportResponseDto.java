package com.medikiosk.his.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.medikiosk.entity.enums.IntegrationStatus;

import java.time.Instant;
import java.util.UUID;

/**
 * Result of clinical data export to Hospital Information System (HIS).
 */
public class HisExportResponseDto {

    private UUID integrationRecordId;
    private UUID sessionId;
    private UUID patientId;
    private IntegrationStatus status;
    private String encounterReference;
    private String bundleId;
    private String hisId;
    private String intakeQueue;
    private String idempotencyKey;

    @JsonProperty("isMock")
    private boolean isMock;

    private Instant attemptedAt;
    private Instant completedAt;
    private String errorMessage;
    private String message;

    public HisExportResponseDto() {
    }

    public HisExportResponseDto(UUID integrationRecordId, UUID sessionId, UUID patientId, IntegrationStatus status,
                                String encounterReference, String bundleId, String hisId, String intakeQueue,
                                String idempotencyKey, boolean isMock, Instant attemptedAt, Instant completedAt,
                                String errorMessage, String message) {
        this.integrationRecordId = integrationRecordId;
        this.sessionId = sessionId;
        this.patientId = patientId;
        this.status = status;
        this.encounterReference = encounterReference;
        this.bundleId = bundleId;
        this.hisId = hisId;
        this.intakeQueue = intakeQueue;
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
        private String encounterReference;
        private String bundleId;
        private String hisId;
        private String intakeQueue;
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

        public Builder encounterReference(String encounterReference) {
            this.encounterReference = encounterReference;
            return this;
        }

        public Builder bundleId(String bundleId) {
            this.bundleId = bundleId;
            return this;
        }

        public Builder hisId(String hisId) {
            this.hisId = hisId;
            return this;
        }

        public Builder intakeQueue(String intakeQueue) {
            this.intakeQueue = intakeQueue;
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

        public HisExportResponseDto build() {
            return new HisExportResponseDto(integrationRecordId, sessionId, patientId, status,
                    encounterReference, bundleId, hisId, intakeQueue, idempotencyKey, isMock,
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

    public String getEncounterReference() {
        return encounterReference;
    }

    public void setEncounterReference(String encounterReference) {
        this.encounterReference = encounterReference;
    }

    public String getBundleId() {
        return bundleId;
    }

    public void setBundleId(String bundleId) {
        this.bundleId = bundleId;
    }

    public String getHisId() {
        return hisId;
    }

    public void setHisId(String hisId) {
        this.hisId = hisId;
    }

    public String getIntakeQueue() {
        return intakeQueue;
    }

    public void setIntakeQueue(String intakeQueue) {
        this.intakeQueue = intakeQueue;
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
