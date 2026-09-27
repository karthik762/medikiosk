package com.medikiosk.dto.doctor;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotNull;

/**
 * Request payload for doctor clinical verification.
 * Enforces deliberate clinician confirmation and optimistic concurrency control.
 */
public class DoctorVerificationRequest {

    @NotNull(message = "expectedVersion is required for optimistic concurrency control")
    private Integer expectedVersion;

    private String clinicalNotes;

    @NotNull(message = "Confirmation must be explicitly provided")
    @AssertTrue(message = "Doctor must acknowledge that they are the final clinical authority and have reviewed the case")
    private Boolean confirmUnderstood;

    public DoctorVerificationRequest() {
    }

    public DoctorVerificationRequest(Integer expectedVersion, String clinicalNotes, Boolean confirmUnderstood) {
        this.expectedVersion = expectedVersion;
        this.clinicalNotes = clinicalNotes;
        this.confirmUnderstood = confirmUnderstood;
    }

    public Integer getExpectedVersion() {
        return expectedVersion;
    }

    public void setExpectedVersion(Integer expectedVersion) {
        this.expectedVersion = expectedVersion;
    }

    public String getClinicalNotes() {
        return clinicalNotes;
    }

    public void setClinicalNotes(String clinicalNotes) {
        this.clinicalNotes = clinicalNotes;
    }

    public Boolean getConfirmUnderstood() {
        return confirmUnderstood;
    }

    public void setConfirmUnderstood(Boolean confirmUnderstood) {
        this.confirmUnderstood = confirmUnderstood;
    }
}
