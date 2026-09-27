package com.medikiosk.dto.intake;

import jakarta.validation.constraints.NotBlank;

public class MedicationRequest {

    @NotBlank(message = "Medication name is required")
    private String medicationName;

    private String dosage;
    private String frequency;
    private String duration;
    private String source = "PATIENT";

    public MedicationRequest() {
    }

    public MedicationRequest(String medicationName, String dosage, String frequency, String duration) {
        this.medicationName = medicationName;
        this.dosage = dosage;
        this.frequency = frequency;
        this.duration = duration;
    }

    public String getMedicationName() {
        return medicationName;
    }

    public void setMedicationName(String medicationName) {
        this.medicationName = medicationName;
    }

    public String getDosage() {
        return dosage;
    }

    public void setDosage(String dosage) {
        this.dosage = dosage;
    }

    public String getFrequency() {
        return frequency;
    }

    public void setFrequency(String frequency) {
        this.frequency = frequency;
    }

    public String getDuration() {
        return duration;
    }

    public void setDuration(String duration) {
        this.duration = duration;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }
}
