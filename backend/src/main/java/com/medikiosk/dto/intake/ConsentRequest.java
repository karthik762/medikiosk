package com.medikiosk.dto.intake;

import com.medikiosk.entity.enums.ConsentStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class ConsentRequest {

    @NotNull(message = "Consent status is required")
    private ConsentStatus consentStatus;

    @NotBlank(message = "Consent type is required")
    private String consentType = "GENERAL_INTAKE";

    @NotBlank(message = "Consent version is required")
    private String consentVersion = "v1.0";

    private String externalReference;

    public ConsentRequest() {
    }

    public ConsentRequest(ConsentStatus consentStatus, String consentType, String consentVersion, String externalReference) {
        this.consentStatus = consentStatus;
        this.consentType = consentType;
        this.consentVersion = consentVersion;
        this.externalReference = externalReference;
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
}
