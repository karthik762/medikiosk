package com.medikiosk.dto.intake;

import jakarta.validation.constraints.NotBlank;

public class MedicalHistoryRequest {

    @NotBlank(message = "Category is required")
    private String category;

    @NotBlank(message = "Description is required")
    private String description;

    private String source = "PATIENT";

    public MedicalHistoryRequest() {
    }

    public MedicalHistoryRequest(String category, String description, String source) {
        this.category = category;
        this.description = description;
        this.source = source != null ? source : "PATIENT";
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }
}
