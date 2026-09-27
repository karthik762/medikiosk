package com.medikiosk.dto.intake;

import com.medikiosk.entity.enums.ConsentStatus;
import java.time.Instant;
import java.util.UUID;

public class ConsentResponse {

    private UUID id;
    private UUID patientId;
    private UUID sessionId;
    private ConsentStatus consentStatus;
    private String consentType;
    private String consentVersion;
    private String externalReference;
    private Instant consentedAt;

    public ConsentResponse() {
    }

    public ConsentResponse(UUID id, UUID patientId, UUID sessionId, ConsentStatus consentStatus,
                           String consentType, String consentVersion, String externalReference, Instant consentedAt) {
        this.id = id;
        this.patientId = patientId;
        this.sessionId = sessionId;
        this.consentStatus = consentStatus;
        this.consentType = consentType;
        this.consentVersion = consentVersion;
        this.externalReference = externalReference;
        this.consentedAt = consentedAt;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
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

    public String getConsentVersion() {
        return consentVersion;
    }

    public void setConsentVersion(String consentVersion) {
        this.consentVersion = consentVersion;
    }

    public String getExternalReference() {
        return externalReference;
    }

    public void setExternalReference(String externalReference) {
        this.externalReference = externalReference;
    }

    public Instant getConsentedAt() {
        return consentedAt;
    }

    public void setConsentedAt(Instant consentedAt) {
        this.consentedAt = consentedAt;
    }
}
