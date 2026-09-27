package com.medikiosk.clinical.state;

/**
 * Encapsulates an identified discrepancy between multiple clinical data sources.
 * For example, patient reporting no allergies while document OCR discovers a Penicillin allergy.
 */
public class ClinicalConflict {

    private String concept;
    private String description;
    private String primarySource;
    private String primaryValue;
    private String conflictingSource;
    private String conflictingValue;

    public ClinicalConflict() {
    }

    public ClinicalConflict(String concept, String description, String primarySource, String primaryValue, String conflictingSource, String conflictingValue) {
        this.concept = concept;
        this.description = description;
        this.primarySource = primarySource;
        this.primaryValue = primaryValue;
        this.conflictingSource = conflictingSource;
        this.conflictingValue = conflictingValue;
    }

    public String getConcept() {
        return concept;
    }

    public String getCategory() {
        return concept;
    }

    public void setConcept(String concept) {
        this.concept = concept;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getPrimarySource() {
        return primarySource;
    }

    public void setPrimarySource(String primarySource) {
        this.primarySource = primarySource;
    }

    public String getPrimaryValue() {
        return primaryValue;
    }

    public void setPrimaryValue(String primaryValue) {
        this.primaryValue = primaryValue;
    }

    public String getConflictingSource() {
        return conflictingSource;
    }

    public void setConflictingSource(String conflictingSource) {
        this.conflictingSource = conflictingSource;
    }

    public String getConflictingValue() {
        return conflictingValue;
    }

    public void setConflictingValue(String conflictingValue) {
        this.conflictingValue = conflictingValue;
    }
}
