package com.medikiosk.abdm.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Request payload for verifying an Ayushman Bharat Health Account (ABHA).
 */
public class AbhaVerificationRequestDto {

    @NotBlank(message = "ABHA ID or Address is required")
    @Size(min = 5, max = 64, message = "ABHA ID must be between 5 and 64 characters")
    private String abhaId;

    public AbhaVerificationRequestDto() {
    }

    public AbhaVerificationRequestDto(String abhaId) {
        this.abhaId = abhaId;
    }

    public String getAbhaId() {
        return abhaId;
    }

    public void setAbhaId(String abhaId) {
        this.abhaId = abhaId;
    }
}
