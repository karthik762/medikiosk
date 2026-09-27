package com.medikiosk.dto.doctor;

import com.medikiosk.dto.clinical.ClinicalSummaryDto;
import jakarta.validation.constraints.NotNull;

import java.util.Map;

/**
 * Request payload for doctor amendments/corrections to an AI pre-consult summary.
 * Enforces optimistic concurrency control using expectedVersion.
 */
public class DoctorAmendmentRequest {

    @NotNull(message = "expectedVersion is required for optimistic concurrency control")
    private Integer expectedVersion;

    @NotNull(message = "amendedSummary must be provided")
    private ClinicalSummaryDto amendedSummary;

    private String clinicalNotes;

    private Map<String, String> conflictResolutions;

    public DoctorAmendmentRequest() {
    }

    public DoctorAmendmentRequest(Integer expectedVersion, ClinicalSummaryDto amendedSummary, String clinicalNotes) {
        this.expectedVersion = expectedVersion;
        this.amendedSummary = amendedSummary;
        this.clinicalNotes = clinicalNotes;
    }

    public Integer getExpectedVersion() {
        return expectedVersion;
    }

    public void setExpectedVersion(Integer expectedVersion) {
        this.expectedVersion = expectedVersion;
    }

    public ClinicalSummaryDto getAmendedSummary() {
        return amendedSummary;
    }

    public void setAmendedSummary(ClinicalSummaryDto amendedSummary) {
        this.amendedSummary = amendedSummary;
    }

    public String getClinicalNotes() {
        return clinicalNotes;
    }

    public void setClinicalNotes(String clinicalNotes) {
        this.clinicalNotes = clinicalNotes;
    }

    public Map<String, String> getConflictResolutions() {
        return conflictResolutions;
    }

    public void setConflictResolutions(Map<String, String> conflictResolutions) {
        this.conflictResolutions = conflictResolutions;
    }
}
