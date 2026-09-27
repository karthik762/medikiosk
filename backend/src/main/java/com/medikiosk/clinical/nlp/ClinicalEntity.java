package com.medikiosk.clinical.nlp;

/**
 * An individual clinical entity extracted from unstructured patient text or audio transcript.
 */
public class ClinicalEntity {

    private String text;
    private ClinicalEntityType entityType;
    private String conceptCode;
    private String assertion = "AFFIRMED"; // AFFIRMED, NEGATED, UNCERTAIN
    private double confidence = 1.0;
    private int startOffset;
    private int endOffset;

    public ClinicalEntity() {
    }

    public ClinicalEntity(String text, ClinicalEntityType entityType, String conceptCode, String assertion, double confidence) {
        this.text = text;
        this.entityType = entityType;
        this.conceptCode = conceptCode;
        this.assertion = assertion;
        this.confidence = confidence;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public ClinicalEntityType getEntityType() {
        return entityType;
    }

    public void setEntityType(ClinicalEntityType entityType) {
        this.entityType = entityType;
    }

    public String getConceptCode() {
        return conceptCode;
    }

    public void setConceptCode(String conceptCode) {
        this.conceptCode = conceptCode;
    }

    public String getAssertion() {
        return assertion;
    }

    public void setAssertion(String assertion) {
        this.assertion = assertion;
    }

    public double getConfidence() {
        return confidence;
    }

    public void setConfidence(double confidence) {
        this.confidence = confidence;
    }

    public int getStartOffset() {
        return startOffset;
    }

    public void setStartOffset(int startOffset) {
        this.startOffset = startOffset;
    }

    public int getEndOffset() {
        return endOffset;
    }

    public void setEndOffset(int endOffset) {
        this.endOffset = endOffset;
    }

    public boolean isNegated() {
        return "NEGATED".equalsIgnoreCase(assertion);
    }
}
