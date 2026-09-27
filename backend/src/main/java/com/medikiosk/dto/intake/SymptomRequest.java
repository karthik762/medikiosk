package com.medikiosk.dto.intake;

import jakarta.validation.constraints.NotBlank;

public class SymptomRequest {

    @NotBlank(message = "Symptom name is required")
    private String name;

    private String location;
    private String duration;
    private Integer severity;
    private String frequency;
    private String onset;
    private String source = "PATIENT";

    public SymptomRequest() {
    }

    public SymptomRequest(String name, String location, String duration, Integer severity) {
        this.name = name;
        this.location = location;
        this.duration = duration;
        this.severity = severity;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getDuration() {
        return duration;
    }

    public void setDuration(String duration) {
        this.duration = duration;
    }

    public Integer getSeverity() {
        return severity;
    }

    public void setSeverity(Integer severity) {
        this.severity = severity;
    }

    public String getFrequency() {
        return frequency;
    }

    public void setFrequency(String frequency) {
        this.frequency = frequency;
    }

    public String getOnset() {
        return onset;
    }

    public void setOnset(String onset) {
        this.onset = onset;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }
}
