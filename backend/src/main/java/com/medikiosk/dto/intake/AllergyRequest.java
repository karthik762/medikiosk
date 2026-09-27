package com.medikiosk.dto.intake;

import jakarta.validation.constraints.NotBlank;

public class AllergyRequest {

    @NotBlank(message = "Allergen is required")
    private String allergen;

    private String reaction;
    private String severity;
    private String source = "PATIENT";

    public AllergyRequest() {
    }

    public AllergyRequest(String allergen, String reaction, String severity) {
        this.allergen = allergen;
        this.reaction = reaction;
        this.severity = severity;
    }

    public String getAllergen() {
        return allergen;
    }

    public void setAllergen(String allergen) {
        this.allergen = allergen;
    }

    public String getReaction() {
        return reaction;
    }

    public void setReaction(String reaction) {
        this.reaction = reaction;
    }

    public String getSeverity() {
        return severity;
    }

    public void setSeverity(String severity) {
        this.severity = severity;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }
}
