package com.medikiosk.dto.clinical;

/**
 * Preserved discrepancies between patient statements and uploaded documents.
 */
public class ClinicalConflictDto {

    private String field;
    private String patientReported;
    private String documentReported;
    private String status = "REVIEW_REQUIRED";

    public ClinicalConflictDto() {
    }

    public ClinicalConflictDto(String field, String patientReported, String documentReported, String status) {
        this.field = field;
        this.patientReported = patientReported;
        this.documentReported = documentReported;
        this.status = status != null ? status : "REVIEW_REQUIRED";
    }

    public String getField() {
        return field;
    }

    public void setField(String field) {
        this.field = field;
    }

    public String getPatientReported() {
        return patientReported;
    }

    public void setPatientReported(String patientReported) {
        this.patientReported = patientReported;
    }

    public String getDocumentReported() {
        return documentReported;
    }

    public void setDocumentReported(String documentReported) {
        this.documentReported = documentReported;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
