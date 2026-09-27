package com.medikiosk.dto.fhir;

import java.util.ArrayList;
import java.util.List;

public class FhirValidationResponseDto {
    private boolean valid;
    private List<String> errors = new ArrayList<>();
    private List<String> warnings = new ArrayList<>();
    private int totalChecked;

    public FhirValidationResponseDto() {
    }

    public FhirValidationResponseDto(boolean valid, List<String> errors, List<String> warnings, int totalChecked) {
        this.valid = valid;
        this.errors = errors != null ? errors : new ArrayList<>();
        this.warnings = warnings != null ? warnings : new ArrayList<>();
        this.totalChecked = totalChecked;
    }

    public boolean isValid() {
        return valid;
    }

    public void setValid(boolean valid) {
        this.valid = valid;
    }

    public List<String> getErrors() {
        return errors;
    }

    public void setErrors(List<String> errors) {
        this.errors = errors;
    }

    public List<String> getWarnings() {
        return warnings;
    }

    public void setWarnings(List<String> warnings) {
        this.warnings = warnings;
    }

    public int getTotalChecked() {
        return totalChecked;
    }

    public void setTotalChecked(int totalChecked) {
        this.totalChecked = totalChecked;
    }
}
