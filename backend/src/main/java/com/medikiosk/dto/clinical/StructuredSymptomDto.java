package com.medikiosk.dto.clinical;

import java.util.ArrayList;
import java.util.List;

/**
 * Structured symptom representation with source attribution, dual terminology coding,
 * confidence score, uncertainty status, and clinician verification status.
 */
public class StructuredSymptomDto {

    private String name;
    private String duration;
    private Integer severity;
    private String location;
    private String onset;
    private String frequency;
    private String source;

    // Stage 5B Extensions
    private String clinicalConceptCode;
    private List<ClinicalCodingDto> coding = new ArrayList<>();
    private Double confidence = 0.95;
    private String status = "REPORTED"; // REPORTED, SUSPECTED, POSSIBLE, EXTRACTED, CONFIRMED, RULED_OUT, UNKNOWN
    private Boolean verified = false;

    public StructuredSymptomDto() {
    }

    public StructuredSymptomDto(String name, String duration, Integer severity, String location, String onset, String frequency, String source) {
        this.name = name;
        this.duration = duration;
        this.severity = severity;
        this.location = location;
        this.onset = onset;
        this.frequency = frequency;
        this.source = source != null ? source : "PATIENT_REPORTED";
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDuration() {
        return duration;
    }

    public void setDuration(String duration) {
        this.duration = duration;
    }

    public Integer getSeverity() {
        return severity;
    }

    public void setSeverity(Integer severity) {
        this.severity = severity;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getOnset() {
        return onset;
    }

    public void setOnset(String onset) {
        this.onset = onset;
    }

    public String getFrequency() {
        return frequency;
    }

    public void setFrequency(String frequency) {
        this.frequency = frequency;
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
