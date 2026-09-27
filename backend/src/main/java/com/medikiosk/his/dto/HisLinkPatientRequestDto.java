package com.medikiosk.his.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * Request to associate an existing MediKiosk patient with a hospital HIS MRN.
 */
public class HisLinkPatientRequestDto {

    @NotBlank(message = "Hospital HIS MRN / ID must not be blank")
    private String hisId;

    private String notes;

    public HisLinkPatientRequestDto() {
    }

    public HisLinkPatientRequestDto(String hisId, String notes) {
        this.hisId = hisId;
        this.notes = notes;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String hisId;
        private String notes;

        public Builder hisId(String hisId) {
            this.hisId = hisId;
            return this;
        }

        public Builder notes(String notes) {
            this.notes = notes;
            return this;
        }

        public HisLinkPatientRequestDto build() {
            return new HisLinkPatientRequestDto(hisId, notes);
        }
    }

    public String getHisId() {
        return hisId;
    }

    public void setHisId(String hisId) {
        this.hisId = hisId;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }
}
