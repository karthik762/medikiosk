package com.medikiosk.clinical.state;

/**
 * Individual extracted or provided clinical fact with open-world semantics, assertion status,
 * provenance, normalization confidence, and extraction method.
 */
public class ClinicalFact {

    private String conceptCode;
    private String rawText;
    private String assertion; // AFFIRMED, NEGATED, UNCERTAIN
    private String severity;  // 0-10 or MILD/MODERATE/SEVERE
    private String duration;
    private ClinicalFactProvenance provenance;
    private double confidence;

    // Phase 2 Open-World Extensions
    private String patientExpression;
    private String sensation;
    private String anatomicalSite;
    private String laterality;
    private String onset;
    private String frequency;
    private String progression;
    private String trigger;
    private String aggravatingFactors;
    private String relievingFactors;
    private String associatedSymptoms;
    private String certainty; // PRESENT, ABSENT, UNKNOWN, UNCERTAIN, AMBIGUOUS, CONTRADICTORY
    private String normalizedConcept;
    private String conceptSystem;
    private double normalizationConfidence = 1.0;
    private String sourceTurnId;
    private String sourceText;
    private String sourceType = "PATIENT_REPORTED"; // PATIENT_REPORTED vs AI_INFERRED
    private String extractionMethod = "DETERMINISTIC_EXTRACTION"; // PATIENT_DIRECT, LLM_EXTRACTION, DETERMINISTIC_EXTRACTION, PATIENT_CORRECTION
    private String verificationStatus = "UNVERIFIED"; // UNVERIFIED, PATIENT_CORRECTED, SUPERSEDED, VERIFIED_BY_DOCTOR
    private boolean contradictory;

    public boolean isContradictory() {
        return contradictory || "CONTRADICTORY".equalsIgnoreCase(certainty) || "CONTRADICTORY".equalsIgnoreCase(assertion);
    }

    public void setContradictory(boolean contradictory) {
        this.contradictory = contradictory;
    }

    public String getVerificationStatus() {
        return verificationStatus;
    }

    public void setVerificationStatus(String verificationStatus) {
        this.verificationStatus = verificationStatus;
    }

    public ClinicalFact() {
        this.assertion = "AFFIRMED";
        this.certainty = "PRESENT";
        this.confidence = 1.0;
    }

    public ClinicalFact(String conceptCode, String rawText, String assertion, ClinicalFactProvenance provenance, double confidence) {
        this.conceptCode = conceptCode;
        this.rawText = rawText;
        this.patientExpression = rawText;
        this.assertion = assertion;
        this.certainty = "NEGATED".equalsIgnoreCase(assertion) ? "ABSENT" : "PRESENT";
        this.provenance = provenance;
        this.confidence = confidence;
        this.normalizedConcept = conceptCode;
    }

    public String getConceptCode() {
        return conceptCode != null ? conceptCode : normalizedConcept;
    }

    public void setConceptCode(String conceptCode) {
        this.conceptCode = conceptCode;
        if (this.normalizedConcept == null) {
            this.normalizedConcept = conceptCode;
        }
    }

    public String getRawText() {
        return rawText != null ? rawText : patientExpression;
    }

    public void setRawText(String rawText) {
        this.rawText = rawText;
        if (this.patientExpression == null) {
            this.patientExpression = rawText;
        }
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

    public String getSeverity() {
        return severity;
    }

    public void setSeverity(String severity) {
        this.severity = severity;
    }

    public String getDuration() {
        return duration;
    }

    public void setDuration(String duration) {
        this.duration = duration;
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

    public String getPatientExpression() {
        return patientExpression != null ? patientExpression : rawText;
    }

    public void setPatientExpression(String patientExpression) {
        this.patientExpression = patientExpression;
        if (this.rawText == null) {
            this.rawText = patientExpression;
        }
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
        return certainty != null ? certainty : ("NEGATED".equalsIgnoreCase(assertion) ? "ABSENT" : "PRESENT");
    }

    public void setCertainty(String certainty) {
        this.certainty = certainty;
        if ("ABSENT".equalsIgnoreCase(certainty)) {
            this.assertion = "NEGATED";
        }
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

    public String getSourceTurnId() {
        return sourceTurnId;
    }

    public void setSourceTurnId(String sourceTurnId) {
        this.sourceTurnId = sourceTurnId;
    }

    public String getSourceText() {
        return sourceText != null ? sourceText : getPatientExpression();
    }

    public void setSourceText(String sourceText) {
        this.sourceText = sourceText;
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

    public boolean isNegated() {
        return "NEGATED".equalsIgnoreCase(assertion) || "ABSENT".equalsIgnoreCase(certainty);
    }

    public boolean isPatientCorrected() {
        return "PATIENT_CORRECTED".equalsIgnoreCase(extractionMethod) || "PATIENT_CORRECTED".equalsIgnoreCase(sourceType);
    }

    public boolean isUnmapped() {
        return normalizedConcept == null || "UNMAPPED".equalsIgnoreCase(normalizedConcept);
    }

    public boolean isAmbiguous() {
        return "AMBIGUOUS".equalsIgnoreCase(certainty) || "AMBIGUOUS".equalsIgnoreCase(assertion);
    }
}
