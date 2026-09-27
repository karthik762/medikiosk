package com.medikiosk.dto.clinical;

import java.time.Instant;

/**
 * Audit and metadata tracking for the AI clinical structuring engine.
 */
public class SummaryMetadataDto {

    private String providerName;
    private String modelIdentifier;
    private Boolean isMock;
    private Instant generatedAt;
    private Integer outputVersion;
    private String disclaimer = "AI-generated pre-consultation summary — Doctor review required. MediKiosk is not an autonomous diagnostic system.";

    public SummaryMetadataDto() {
    }

    public SummaryMetadataDto(String providerName, String modelIdentifier, Boolean isMock, Instant generatedAt, Integer outputVersion) {
        this.providerName = providerName;
        this.modelIdentifier = modelIdentifier;
        this.isMock = isMock;
        this.generatedAt = generatedAt;
        this.outputVersion = outputVersion;
    }

    public String getProviderName() {
        return providerName;
    }

    public void setProviderName(String providerName) {
        this.providerName = providerName;
    }

    public String getModelIdentifier() {
        return modelIdentifier;
    }

    public void setModelIdentifier(String modelIdentifier) {
        this.modelIdentifier = modelIdentifier;
    }

    public Boolean getIsMock() {
        return isMock;
    }

    public void setIsMock(Boolean isMock) {
        this.isMock = isMock;
    }

    public Instant getGeneratedAt() {
        return generatedAt;
    }

    public void setGeneratedAt(Instant generatedAt) {
        this.generatedAt = generatedAt;
    }

    public Integer getOutputVersion() {
        return outputVersion;
    }

    public void setOutputVersion(Integer outputVersion) {
        this.outputVersion = outputVersion;
    }

    public String getDisclaimer() {
        return disclaimer;
    }

    public void setDisclaimer(String disclaimer) {
        this.disclaimer = disclaimer;
    }
}
