package com.medikiosk.dto.clinical;

import java.util.ArrayList;
import java.util.List;

/**
 * Structured allergy record with terminology coding, provenance, confidence, and verification.
 */
public class StructuredAllergyDto {

    private String allergen;
    private String reaction;
    private String severity;
    private String source;

    // Stage 5B Extensions
    private String clinicalConceptCode;
    private List<ClinicalCodingDto> coding = new ArrayList<>();
    private Double confidence = 0.95;
    private String status = "REPORTED"; // REPORTED, SUSPECTED, POSSIBLE, EXTRACTED, CONFIRMED, RULED_OUT, UNKNOWN
    private Boolean verified = false;

    public StructuredAllergyDto() {
    }

    public StructuredAllergyDto(String allergen, String reaction, String severity, String source) {
        this.allergen = allergen;
        this.reaction = reaction;
        this.severity = severity;
        this.source = source != null ? source : "PATIENT_REPORTED";
    }

    public String getAllergen() {
        return allergen;
    }

    public void setAllergen(String allergen) {
        this.allergen = allergen;
    }

    public String getReaction() {
        return reaction;
    }

    public void setReaction(String reaction) {
        this.reaction = reaction;
    }

    public String getSeverity() {
        return severity;
    }

    public void setSeverity(String severity) {
        this.severity = severity;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getClinicalConceptCode() {
        return clinicalConceptCode;
    }

    public void setClinicalConceptCode(String clinicalConceptCode) {
        this.clinicalConceptCode = clinicalConceptCode;
    }

    public List<ClinicalCodingDto> getCoding() {
        return coding;
    }

    public void setCoding(List<ClinicalCodingDto> coding) {
        this.coding = coding;
    }

    public Double getConfidence() {
        return confidence;
    }

    public void setConfidence(Double confidence) {
        this.confidence = confidence;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Boolean getVerified() {
        return verified;
    }

    public void setVerified(Boolean verified) {
        this.verified = verified;
    }
}
