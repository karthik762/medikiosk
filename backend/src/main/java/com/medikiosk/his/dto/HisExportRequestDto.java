package com.medikiosk.his.dto;

/**
 * Request payload to export a doctor-verified clinical summary and FHIR bundle to HIS.
 */
public class HisExportRequestDto {

    private String intakeQueue;
    private String hisId;
    private String idempotencyKey;
    private String notes;

    public HisExportRequestDto() {
    }

    public HisExportRequestDto(String intakeQueue, String hisId, String idempotencyKey, String notes) {
        this.intakeQueue = intakeQueue;
        this.hisId = hisId;
        this.idempotencyKey = idempotencyKey;
        this.notes = notes;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String intakeQueue;
        private String hisId;
        private String idempotencyKey;
        private String notes;

        public Builder intakeQueue(String intakeQueue) {
            this.intakeQueue = intakeQueue;
            return this;
        }

        public Builder hisId(String hisId) {
            this.hisId = hisId;
            return this;
        }

        public Builder idempotencyKey(String idempotencyKey) {
            this.idempotencyKey = idempotencyKey;
            return this;
        }

        public Builder notes(String notes) {
            this.notes = notes;
            return this;
        }

        public HisExportRequestDto build() {
            return new HisExportRequestDto(intakeQueue, hisId, idempotencyKey, notes);
        }
    }

    public String getIntakeQueue() {
        return intakeQueue;
    }

    public void setIntakeQueue(String intakeQueue) {
        this.intakeQueue = intakeQueue;
    }

    public String getHisId() {
        return hisId;
    }

    public void setHisId(String hisId) {
        this.hisId = hisId;
    }

    public String getIdempotencyKey() {
        return idempotencyKey;
    }

    public void setIdempotencyKey(String idempotencyKey) {
        this.idempotencyKey = idempotencyKey;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }
}
