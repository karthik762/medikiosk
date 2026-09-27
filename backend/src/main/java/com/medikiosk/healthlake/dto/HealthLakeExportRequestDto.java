package com.medikiosk.healthlake.dto;

/**
 * Request payload to export a doctor-verified clinical summary and FHIR R4 bundle to AWS HealthLake.
 */
public class HealthLakeExportRequestDto {

    private String healthlakeRef;
    private String datastoreId;
    private String idempotencyKey;
    private String notes;

    public HealthLakeExportRequestDto() {
    }

    public HealthLakeExportRequestDto(String healthlakeRef, String datastoreId, String idempotencyKey, String notes) {
        this.healthlakeRef = healthlakeRef;
        this.datastoreId = datastoreId;
        this.idempotencyKey = idempotencyKey;
        this.notes = notes;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String healthlakeRef;
        private String datastoreId;
        private String idempotencyKey;
        private String notes;

        public Builder healthlakeRef(String healthlakeRef) {
            this.healthlakeRef = healthlakeRef;
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

        public Builder notes(String notes) {
            this.notes = notes;
            return this;
        }

        public HealthLakeExportRequestDto build() {
            return new HealthLakeExportRequestDto(healthlakeRef, datastoreId, idempotencyKey, notes);
        }
    }

    public String getHealthlakeRef() {
        return healthlakeRef;
    }

    public void setHealthlakeRef(String healthlakeRef) {
        this.healthlakeRef = healthlakeRef;
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

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }
}
