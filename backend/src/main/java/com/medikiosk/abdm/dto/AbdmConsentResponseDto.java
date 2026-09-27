package com.medikiosk.abdm.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.medikiosk.entity.enums.ConsentStatus;

import java.time.Instant;
import java.util.UUID;

/**
 * Result of an ABDM consent operation.
 */
public class AbdmConsentResponseDto {

    private UUID consentId;
    private UUID patientId;
    private UUID sessionId;
    private ConsentStatus consentStatus;
    private String consentType;
    private String externalArtefactId;
    private Instant consentedAt;
    private Instant revokedAt;

    @JsonProperty("isMock")
    private boolean isMock;
    private String message;

    public AbdmConsentResponseDto() {
    }

    public AbdmConsentResponseDto(UUID consentId, UUID patientId, UUID sessionId, ConsentStatus consentStatus, String consentType, String externalArtefactId, Instant consentedAt, Instant revokedAt, boolean isMock, String message) {
        this.consentId = consentId;
        this.patientId = patientId;
        this.sessionId = sessionId;
        this.consentStatus = consentStatus;
        this.consentType = consentType;
        this.externalArtefactId = externalArtefactId;
        this.consentedAt = consentedAt;
        this.revokedAt = revokedAt;
        this.isMock = isMock;
        this.message = message;
    }

    public UUID getConsentId() {
        return consentId;
    }

    public void setConsentId(UUID consentId) {
        this.consentId = consentId;
    }

    public UUID getPatientId() {
        return patientId;
    }

    public void setPatientId(UUID patientId) {
        this.patientId = patientId;
    }

    public UUID getSessionId() {
        return sessionId;
    }

    public void setSessionId(UUID sessionId) {
        this.sessionId = sessionId;
    }

    public ConsentStatus getConsentStatus() {
        return consentStatus;
    }

    public void setConsentStatus(ConsentStatus consentStatus) {
        this.consentStatus = consentStatus;
    }

    public String getConsentType() {
        return consentType;
    }

    public void setConsentType(String consentType) {
        this.consentType = consentType;
    }

    public String getExternalArtefactId() {
        return externalArtefactId;
    }

    public void setExternalArtefactId(String externalArtefactId) {
        this.externalArtefactId = externalArtefactId;
    }

    public Instant getConsentedAt() {
        return consentedAt;
    }

    public void setConsentedAt(Instant consentedAt) {
        this.consentedAt = consentedAt;
    }

    public Instant getRevokedAt() {
        return revokedAt;
    }

    public void setRevokedAt(Instant revokedAt) {
        this.revokedAt = revokedAt;
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
}
