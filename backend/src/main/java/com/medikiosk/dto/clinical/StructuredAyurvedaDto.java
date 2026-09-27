package com.medikiosk.dto.clinical;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Structured Ayurveda case-taking findings summary (Prakriti, Vikriti, Agni, Nidana, Pariksha).
 * Clearly separated from conventional medical diagnosis.
 * Preserves whether information is PATIENT_REPORTED, RULE_DERIVED, AI_GENERATED, or CLINICIAN_VERIFIED.
 */
public class StructuredAyurvedaDto {

    private String prakriti;
    private String vikriti;
    private String agni;
    private List<String> symptoms = new ArrayList<>();
    private List<String> documentedObservations = new ArrayList<>();
    private String source = "PATIENT_REPORTED"; // PATIENT_REPORTED, RULE_DERIVED, AI_GENERATED, CLINICIAN_VERIFIED
    private Boolean verified = false;
    private Double confidence = 0.85;

    private Map<String, Object> prakritiResponses;
    private Map<String, Object> nidanaResponses;
    private Map<String, Object> parikshaResponses;

    public StructuredAyurvedaDto() {
    }

    public StructuredAyurvedaDto(Map<String, Object> prakritiResponses, Map<String, Object> nidanaResponses, Map<String, Object> parikshaResponses, List<String> documentedObservations) {
        this.prakritiResponses = prakritiResponses;
        this.nidanaResponses = nidanaResponses;
        this.parikshaResponses = parikshaResponses;
        if (documentedObservations != null) {
            this.documentedObservations = documentedObservations;
        }
    }

    public String getPrakriti() {
        return prakriti;
    }

    public void setPrakriti(String prakriti) {
        this.prakriti = prakriti;
    }

    public String getVikriti() {
        return vikriti;
    }

    public void setVikriti(String vikriti) {
        this.vikriti = vikriti;
    }

    public String getAgni() {
        return agni;
    }

    public void setAgni(String agni) {
        this.agni = agni;
    }

    public List<String> getSymptoms() {
        return symptoms;
    }

    public void setSymptoms(List<String> symptoms) {
        this.symptoms = symptoms;
    }

    public List<String> getDocumentedObservations() {
        return documentedObservations;
    }

    public void setDocumentedObservations(List<String> documentedObservations) {
        this.documentedObservations = documentedObservations;
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

    public Double getConfidence() {
        return confidence;
    }

    public void setConfidence(Double confidence) {
        this.confidence = confidence;
    }

    public Map<String, Object> getPrakritiResponses() {
        return prakritiResponses;
    }

    public void setPrakritiResponses(Map<String, Object> prakritiResponses) {
        this.prakritiResponses = prakritiResponses;
    }

    public Map<String, Object> getNidanaResponses() {
        return nidanaResponses;
    }

    public void setNidanaResponses(Map<String, Object> nidanaResponses) {
        this.nidanaResponses = nidanaResponses;
    }

    public Map<String, Object> getParikshaResponses() {
        return parikshaResponses;
    }

    public void setParikshaResponses(Map<String, Object> parikshaResponses) {
        this.parikshaResponses = parikshaResponses;
    }
}
