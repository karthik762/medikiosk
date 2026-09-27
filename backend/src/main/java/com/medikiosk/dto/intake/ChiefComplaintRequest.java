package com.medikiosk.dto.intake;

import jakarta.validation.constraints.NotBlank;
import java.math.BigDecimal;

public class ChiefComplaintRequest {

    @NotBlank(message = "Chief complaint text is required")
    private String complaintText;

    private String inputMethod = "TEXT";
    private String originalTranscription;
    private BigDecimal confidenceScore;

    public ChiefComplaintRequest() {
    }

    public ChiefComplaintRequest(String complaintText, String inputMethod) {
        this.complaintText = complaintText;
        this.inputMethod = inputMethod != null ? inputMethod : "TEXT";
    }

    public String getComplaintText() {
        return complaintText;
    }

    public void setComplaintText(String complaintText) {
        this.complaintText = complaintText;
    }

    public String getInputMethod() {
        return inputMethod;
    }

    public void setInputMethod(String inputMethod) {
        this.inputMethod = inputMethod;
    }

    public String getOriginalTranscription() {
        return originalTranscription;
    }

    public void setOriginalTranscription(String originalTranscription) {
        this.originalTranscription = originalTranscription;
    }

    public BigDecimal getConfidenceScore() {
        return confidenceScore;
    }

    public void setConfidenceScore(BigDecimal confidenceScore) {
        this.confidenceScore = confidenceScore;
    }
}
