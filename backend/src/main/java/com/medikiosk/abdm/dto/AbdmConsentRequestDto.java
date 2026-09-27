package com.medikiosk.abdm.dto;

import java.util.UUID;

/**
 * Request to grant consent for ABDM health information exchange.
 */
public class AbdmConsentRequestDto {

    private UUID patientId;
    private String consentType = "ABDM_DATA_SHARE";
    private String purpose = "Clinical care coordination and record exchange";
    private Integer validMonths = 12;

    public AbdmConsentRequestDto() {
    }

    public AbdmConsentRequestDto(UUID patientId) {
        this.patientId = patientId;
    }

    public UUID getPatientId() {
        return patientId;
    }

    public void setPatientId(UUID patientId) {
        this.patientId = patientId;
    }

    public String getConsentType() {
        return consentType;
    }

    public void setConsentType(String consentType) {
        this.consentType = consentType;
    }

    public String getPurpose() {
        return purpose;
    }

    public void setPurpose(String purpose) {
        this.purpose = purpose;
    }

    public Integer getValidMonths() {
        return validMonths;
    }

    public void setValidMonths(Integer validMonths) {
        this.validMonths = validMonths;
    }
}
