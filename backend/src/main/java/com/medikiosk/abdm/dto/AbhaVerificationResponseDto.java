package com.medikiosk.abdm.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.medikiosk.entity.enums.AbhaStatus;

import java.time.Instant;

/**
 * Result of ABHA verification via ABDM Adapter.
 */
public class AbhaVerificationResponseDto {

    private boolean verified;
    private AbhaStatus status;
    private String maskedAbha;
    private String verificationMethod;

    @JsonProperty("isMock")
    private boolean isMock;
    private String message;
    private Instant verifiedAt;

    public AbhaVerificationResponseDto() {
    }

    public AbhaVerificationResponseDto(boolean verified, AbhaStatus status, String maskedAbha, String verificationMethod, boolean isMock, String message, Instant verifiedAt) {
        this.verified = verified;
        this.status = status;
        this.maskedAbha = maskedAbha;
        this.verificationMethod = verificationMethod;
        this.isMock = isMock;
        this.message = message;
        this.verifiedAt = verifiedAt;
    }

    public boolean isVerified() {
        return verified;
    }

    public void setVerified(boolean verified) {
        this.verified = verified;
    }

    public AbhaStatus getStatus() {
        return status;
    }

    public void setStatus(AbhaStatus status) {
        this.status = status;
    }

    public String getMaskedAbha() {
        return maskedAbha;
    }

    public void setMaskedAbha(String maskedAbha) {
        this.maskedAbha = maskedAbha;
    }

    public String getVerificationMethod() {
        return verificationMethod;
    }

    public void setVerificationMethod(String verificationMethod) {
        this.verificationMethod = verificationMethod;
    }

    public boolean isMock() {
        return isMock;
    }

    public void setMock(boolean mock) {
        isMock = mock;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Instant getVerifiedAt() {
        return verifiedAt;
    }

    public void setVerifiedAt(Instant verifiedAt) {
        this.verifiedAt = verifiedAt;
    }
}
