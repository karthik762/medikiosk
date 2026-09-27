package com.medikiosk.clinical.nlp;

import com.medikiosk.clinical.state.ClinicalFactProvenance;

/**
 * Request payload for clinical NLP extraction.
 */
public class ClinicalExtractionRequest {

    private String text;
    private String language = "en";
    private ClinicalFactProvenance provenance = ClinicalFactProvenance.PATIENT_TEXT;

    public ClinicalExtractionRequest() {
    }

    public ClinicalExtractionRequest(String text) {
        this.text = text;
    }

    public ClinicalExtractionRequest(String text, String language, ClinicalFactProvenance provenance) {
        this.text = text;
        this.language = language != null ? language : "en";
        this.provenance = provenance != null ? provenance : ClinicalFactProvenance.PATIENT_TEXT;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public ClinicalFactProvenance getProvenance() {
        return provenance;
    }

    public void setProvenance(ClinicalFactProvenance provenance) {
        this.provenance = provenance;
    }
}
