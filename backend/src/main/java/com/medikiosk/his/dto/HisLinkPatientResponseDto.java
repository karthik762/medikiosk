package com.medikiosk.his.dto;

import java.time.Instant;
import java.util.UUID;

/**
 * Response returned after linking a MediKiosk patient to an external hospital HIS MRN.
 */
public class HisLinkPatientResponseDto {

    private UUID patientId;
    private String patientIdentifier;
    private String hisId;
    private Instant linkedAt;
    private String message;

    public HisLinkPatientResponseDto() {
    }

    public HisLinkPatientResponseDto(UUID patientId, String patientIdentifier, String hisId,
                                     Instant linkedAt, String message) {
        this.patientId = patientId;
        this.patientIdentifier = patientIdentifier;
        this.hisId = hisId;
        this.linkedAt = linkedAt;
        this.message = message;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private UUID patientId;
        private String patientIdentifier;
        private String hisId;
        private Instant linkedAt;
        private String message;

        public Builder patientId(UUID patientId) {
            this.patientId = patientId;
            return this;
        }

        public Builder patientIdentifier(String patientIdentifier) {
            this.patientIdentifier = patientIdentifier;
            return this;
        }

        public Builder hisId(String hisId) {
            this.hisId = hisId;
            return this;
        }

        public Builder linkedAt(Instant linkedAt) {
            this.linkedAt = linkedAt;
            return this;
        }

        public Builder message(String message) {
            this.message = message;
            return this;
        }

        public HisLinkPatientResponseDto build() {
            return new HisLinkPatientResponseDto(patientId, patientIdentifier, hisId, linkedAt, message);
        }
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

    public String getHisId() {
        return hisId;
    }

    public void setHisId(String hisId) {
        this.hisId = hisId;
    }

    public Instant getLinkedAt() {
        return linkedAt;
    }

    public void setLinkedAt(Instant linkedAt) {
        this.linkedAt = linkedAt;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
