package com.medikiosk.dto.clinical;

import java.util.ArrayList;
import java.util.List;

/**
 * High-level dual-coded concept wrapper representing a clinical finding
 * along with ICD/NAMASTE codings, provenance source, uncertainty status, and confidence score.
 */
public class DualCodedConceptDto {

    private String concept;
    private String clinicalConceptCode;
    private List<ClinicalCodingDto> coding = new ArrayList<>();
    private String status = "REPORTED"; // REPORTED, SUSPECTED, POSSIBLE, EXTRACTED, CONFIRMED, RULED_OUT, UNKNOWN
    private Double confidence = 0.95;
    private String source = "PATIENT_REPORTED"; // PATIENT_REPORTED, OCR_EXTRACTED, DOCUMENT_EXTRACTED, TRIAGE_ENGINE, AYURVEDA_ENGINE, AI_GENERATED, FHIR_DATA, CLINICIAN_VERIFIED
    private Boolean verified = false;

    public DualCodedConceptDto() {
    }

    public DualCodedConceptDto(String concept, String clinicalConceptCode, List<ClinicalCodingDto> coding, String status, Double confidence, String source, Boolean verified) {
        this.concept = concept;
        this.clinicalConceptCode = clinicalConceptCode;
        if (coding != null) {
            this.coding = coding;
        }
        this.status = status != null ? status : "REPORTED";
        this.confidence = confidence != null ? confidence : 0.95;
        this.source = source != null ? source : "PATIENT_REPORTED";
        this.verified = verified != null ? verified : false;
    }

    public String getConcept() {
        return concept;
    }

    public void setConcept(String concept) {
        this.concept = concept;
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

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Double getConfidence() {
        return confidence;
    }

    public void setConfidence(Double confidence) {
        this.confidence = confidence;
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
}
