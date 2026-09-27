package com.medikiosk.dto.intake;

import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public class IntakeSessionRequest {

    @NotNull(message = "Patient ID is required")
    private UUID patientId;

    private String language = "en";

    public IntakeSessionRequest() {
    }

    public IntakeSessionRequest(UUID patientId, String language) {
        this.patientId = patientId;
        this.language = language != null ? language : "en";
    }

    public UUID getPatientId() {
        return patientId;
    }

    public void setPatientId(UUID patientId) {
        this.patientId = patientId;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }
}
