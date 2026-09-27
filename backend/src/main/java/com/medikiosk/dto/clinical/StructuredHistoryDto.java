package com.medikiosk.dto.clinical;

import java.util.ArrayList;
import java.util.List;

/**
 * Structured medical history entry with terminology coding, provenance, and status.
 */
public class StructuredHistoryDto {

    private String category;
    private String description;
    private String source;
    private Boolean verified = false;

    // Stage 5B Extensions
    private String clinicalConceptCode;
    private List<ClinicalCodingDto> coding = new ArrayList<>();
    private Double confidence = 0.95;
    private String status = "REPORTED"; // REPORTED, SUSPECTED, POSSIBLE, EXTRACTED, CONFIRMED, RULED_OUT, UNKNOWN

    public StructuredHistoryDto() {
    }

    public StructuredHistoryDto(String category, String description, String source, Boolean verified) {
        this.category = category;
        this.description = description;
        this.source = source != null ? source : "PATIENT_REPORTED";
        this.verified = verified != null ? verified : false;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public Boolean getVerified() {
        return verified;
    }

    public void setVerified(Boolean verified) {
        this.verified = verified;
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
}
