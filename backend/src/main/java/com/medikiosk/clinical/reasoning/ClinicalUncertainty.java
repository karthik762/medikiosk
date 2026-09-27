package com.medikiosk.clinical.reasoning;

/**
 * Identifies ambiguous or missing information in the clinical conversation requiring clarification.
 */
public class ClinicalUncertainty {

    private String conceptCode;
    private String description;
    private boolean needsFollowUp;

    public ClinicalUncertainty() {
    }

    public ClinicalUncertainty(String conceptCode, String description, boolean needsFollowUp) {
        this.conceptCode = conceptCode;
        this.description = description;
        this.needsFollowUp = needsFollowUp;
    }

    public String getConceptCode() {
        return conceptCode;
    }

    public void setConceptCode(String conceptCode) {
        this.conceptCode = conceptCode;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean isNeedsFollowUp() {
        return needsFollowUp;
    }

    public void setNeedsFollowUp(boolean needsFollowUp) {
        this.needsFollowUp = needsFollowUp;
    }
}
