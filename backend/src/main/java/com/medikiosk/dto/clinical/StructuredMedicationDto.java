package com.medikiosk.dto.clinical;

import java.util.ArrayList;
import java.util.List;

/**
 * Structured medication representation with origin, conflict notes, terminology coding,
 * confidence score, and verification status.
 */
public class StructuredMedicationDto {

    private String medicationName;
    private String dosage;
    private String frequency;
    private String duration;
    private String source;
    private Boolean conflictFlag;
    private String verificationNotes;

    // Stage 5B Extensions
    private String clinicalConceptCode;
    private List<ClinicalCodingDto> coding = new ArrayList<>();
    private Double confidence = 0.95;
    private String status = "REPORTED"; // REPORTED, SUSPECTED, POSSIBLE, EXTRACTED, CONFIRMED, RULED_OUT, UNKNOWN
    private Boolean verified = false;

    public StructuredMedicationDto() {
    }

    public StructuredMedicationDto(String medicationName, String dosage, String frequency, String duration, String source, Boolean conflictFlag, String verificationNotes) {
        this.medicationName = medicationName;
        this.dosage = dosage;
        this.frequency = frequency;
        this.duration = duration;
        this.source = source != null ? source : "PATIENT_REPORTED";
        this.conflictFlag = conflictFlag != null ? conflictFlag : false;
        this.verificationNotes = verificationNotes;
    }

    public String getMedicationName() {
        return medicationName;
    }

    public void setMedicationName(String medicationName) {
        this.medicationName = medicationName;
    }

    public String getDosage() {
        return dosage;
    }

    public void setDosage(String dosage) {
        this.dosage = dosage;
    }

    public String getFrequency() {
        return frequency;
    }

    public void setFrequency(String frequency) {
        this.frequency = frequency;
    }

    public String getDuration() {
        return duration;
    }

    public void setDuration(String duration) {
        this.duration = duration;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public Boolean getConflictFlag() {
        return conflictFlag;
    }

    public void setConflictFlag(Boolean conflictFlag) {
        this.conflictFlag = conflictFlag;
    }

    public String getVerificationNotes() {
        return verificationNotes;
    }

    public void setVerificationNotes(String verificationNotes) {
        this.verificationNotes = verificationNotes;
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
