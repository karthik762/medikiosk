package com.medikiosk.clinical.reasoning;

import com.medikiosk.clinical.state.ClinicalFact;
import com.medikiosk.clinical.state.ClinicalFactProvenance;

/**
 * Structured clinical fact extracted by the reasoning engine from patient statements,
 * conversation turns, or medical documents with open-world semantic properties.
 */
public class ClinicalFactExtraction {

    private String conceptCode;
    private String value;
    private String assertion = "AFFIRMED"; // AFFIRMED, NEGATED, UNCERTAIN
    private ClinicalFactProvenance provenance = ClinicalFactProvenance.AI_EXTRACTED;
    private double confidence = 0.95;
    private String rawMention;

    // Phase 2 Open-World Semantic Attributes
    private String patientExpression;
    private String sensation;
    private String anatomicalSite;
    private String laterality;
    private String onset;
    private String duration;
    private String frequency;
    private String progression;
    private String trigger;
    private String aggravatingFactors;
    private String relievingFactors;
    private String associatedSymptoms;
    private String certainty = "PRESENT"; // PRESENT, ABSENT, UNKNOWN, UNCERTAIN, AMBIGUOUS, CONTRADICTORY
    private String normalizedConcept;
    private String conceptSystem;
    private double normalizationConfidence = 1.0;
    private String sourceType = "PATIENT_REPORTED";
    private String extractionMethod = "DETERMINISTIC_EXTRACTION";
    private boolean correction;
    private boolean contradiction;

    public ClinicalFactExtraction() {
    }

    public ClinicalFactExtraction(String conceptCode, String value, String assertion, ClinicalFactProvenance provenance, double confidence) {
        this.conceptCode = conceptCode;
        this.value = value;
        this.assertion = assertion;
        this.certainty = "NEGATED".equalsIgnoreCase(assertion) ? "ABSENT" : "PRESENT";
        this.provenance = provenance;
        this.confidence = confidence;
        this.normalizedConcept = conceptCode;
    }

    public ClinicalFact toClinicalFact() {
        ClinicalFact fact = new ClinicalFact();
        fact.setConceptCode(this.getConceptCode());
        fact.setRawText(this.getRawMention() != null ? this.getRawMention() : this.getValue());
        fact.setPatientExpression(this.getPatientExpression() != null ? this.getPatientExpression() : fact.getRawText());
        fact.setAssertion(this.getAssertion());
        fact.setCertainty(this.getCertainty());
        fact.setProvenance(this.getProvenance());
        fact.setConfidence(this.getConfidence());
        fact.setSensation(this.getSensation());
        fact.setAnatomicalSite(this.getAnatomicalSite());
        fact.setLaterality(this.getLaterality());
        fact.setOnset(this.getOnset());
        fact.setDuration(this.getDuration());
        fact.setFrequency(this.getFrequency());
        fact.setProgression(this.getProgression());
        fact.setTrigger(this.getTrigger());
        fact.setAggravatingFactors(this.getAggravatingFactors());
        fact.setRelievingFactors(this.getRelievingFactors());
        fact.setAssociatedSymptoms(this.getAssociatedSymptoms());
        fact.setNormalizedConcept(this.getNormalizedConcept());
        fact.setConceptSystem(this.getConceptSystem());
        fact.setNormalizationConfidence(this.getNormalizationConfidence());
        fact.setSourceType(this.getSourceType());
        fact.setExtractionMethod(this.isCorrection() ? "PATIENT_CORRECTION" : this.getExtractionMethod());
        return fact;
    }

    public boolean isNegated() {
        return "NEGATED".equalsIgnoreCase(assertion) || "ABSENT".equalsIgnoreCase(certainty);
    }

    public boolean isAffirmed() {
        return "AFFIRMED".equalsIgnoreCase(assertion) || "PRESENT".equalsIgnoreCase(certainty);
    }

    // Getters and Setters
    public String getConceptCode() {
        return conceptCode != null ? conceptCode : normalizedConcept;
    }

    public void setConceptCode(String conceptCode) {
        this.conceptCode = conceptCode;
        if (this.normalizedConcept == null) {
            this.normalizedConcept = conceptCode;
        }
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getAssertion() {
        return assertion;
    }

    public void setAssertion(String assertion) {
        this.assertion = assertion;
        if ("NEGATED".equalsIgnoreCase(assertion)) {
            this.certainty = "ABSENT";
        }
    }

    public ClinicalFactProvenance getProvenance() {
        return provenance;
    }

    public void setProvenance(ClinicalFactProvenance provenance) {
        this.provenance = provenance;
    }

    public double getConfidence() {
        return confidence;
    }

    public void setConfidence(double confidence) {
        this.confidence = confidence;
    }

    public String getRawMention() {
        return rawMention;
    }

    public void setRawMention(String rawMention) {
        this.rawMention = rawMention;
    }

    public String getPatientExpression() {
        return patientExpression != null ? patientExpression : rawMention;
    }

    public void setPatientExpression(String patientExpression) {
        this.patientExpression = patientExpression;
    }

    public String getSensation() {
        return sensation;
    }

    public void setSensation(String sensation) {
        this.sensation = sensation;
    }

    public String getAnatomicalSite() {
        return anatomicalSite;
    }

    public void setAnatomicalSite(String anatomicalSite) {
        this.anatomicalSite = anatomicalSite;
    }

    public String getLaterality() {
        return laterality;
    }

    public void setLaterality(String laterality) {
        this.laterality = laterality;
    }

    public String getOnset() {
        return onset;
    }

    public void setOnset(String onset) {
        this.onset = onset;
    }

    public String getDuration() {
        return duration;
    }

    public void setDuration(String duration) {
        this.duration = duration;
    }

    public String getFrequency() {
        return frequency;
    }

    public void setFrequency(String frequency) {
        this.frequency = frequency;
    }

    public String getProgression() {
        return progression;
    }

    public void setProgression(String progression) {
        this.progression = progression;
    }

    public String getTrigger() {
        return trigger;
    }

    public void setTrigger(String trigger) {
        this.trigger = trigger;
    }

    public String getAggravatingFactors() {
        return aggravatingFactors;
    }

    public void setAggravatingFactors(String aggravatingFactors) {
        this.aggravatingFactors = aggravatingFactors;
    }

    public String getRelievingFactors() {
        return relievingFactors;
    }

    public void setRelievingFactors(String relievingFactors) {
        this.relievingFactors = relievingFactors;
    }

    public String getAssociatedSymptoms() {
        return associatedSymptoms;
    }

    public void setAssociatedSymptoms(String associatedSymptoms) {
        this.associatedSymptoms = associatedSymptoms;
    }

    public String getCertainty() {
        return certainty;
    }

    public void setCertainty(String certainty) {
        this.certainty = certainty;
    }

    public String getNormalizedConcept() {
        return normalizedConcept != null ? normalizedConcept : conceptCode;
    }

    public void setNormalizedConcept(String normalizedConcept) {
        this.normalizedConcept = normalizedConcept;
    }

    public String getConceptSystem() {
        return conceptSystem;
    }

    public void setConceptSystem(String conceptSystem) {
        this.conceptSystem = conceptSystem;
    }

    public double getNormalizationConfidence() {
        return normalizationConfidence;
    }

    public void setNormalizationConfidence(double normalizationConfidence) {
        this.normalizationConfidence = normalizationConfidence;
    }

    public String getSourceType() {
        return sourceType;
    }

    public void setSourceType(String sourceType) {
        this.sourceType = sourceType;
    }

    public String getExtractionMethod() {
        return extractionMethod;
    }

    public void setExtractionMethod(String extractionMethod) {
        this.extractionMethod = extractionMethod;
    }

    public boolean isCorrection() {
        return correction;
    }

    public void setCorrection(boolean correction) {
        this.correction = correction;
    }

    public boolean isContradiction() {
        return contradiction;
    }

    public void setContradiction(boolean contradiction) {
        this.contradiction = contradiction;
    }
}
