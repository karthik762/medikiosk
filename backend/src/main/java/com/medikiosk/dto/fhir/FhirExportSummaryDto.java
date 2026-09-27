package com.medikiosk.dto.fhir;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

public class FhirExportSummaryDto {
    private UUID sessionId;
    private UUID patientId;
    private String bundleId;
    private String bundleType;
    private int totalResources;
    private Map<String, Integer> resourceTypeCounts;
    private String verificationStatus;
    private String compositionStatus;
    private String validationStatus;
    private Instant exportedAt;
    private String fhirBundleJson;

    public FhirExportSummaryDto() {
    }

    public UUID getSessionId() {
        return sessionId;
    }

    public void setSessionId(UUID sessionId) {
        this.sessionId = sessionId;
    }

    public UUID getPatientId() {
        return patientId;
    }

    public void setPatientId(UUID patientId) {
        this.patientId = patientId;
    }

    public String getBundleId() {
        return bundleId;
    }

    public void setBundleId(String bundleId) {
        this.bundleId = bundleId;
    }

    public String getBundleType() {
        return bundleType;
    }

    public void setBundleType(String bundleType) {
        this.bundleType = bundleType;
    }

    public int getTotalResources() {
        return totalResources;
    }

    public void setTotalResources(int totalResources) {
        this.totalResources = totalResources;
    }

    public Map<String, Integer> getResourceTypeCounts() {
        return resourceTypeCounts;
    }

    public void setResourceTypeCounts(Map<String, Integer> resourceTypeCounts) {
        this.resourceTypeCounts = resourceTypeCounts;
    }

    public String getVerificationStatus() {
        return verificationStatus;
    }

    public void setVerificationStatus(String verificationStatus) {
        this.verificationStatus = verificationStatus;
    }

    public String getCompositionStatus() {
        return compositionStatus;
    }

    public void setCompositionStatus(String compositionStatus) {
        this.compositionStatus = compositionStatus;
    }

    public String getValidationStatus() {
        return validationStatus;
    }

    public void setValidationStatus(String validationStatus) {
        this.validationStatus = validationStatus;
    }

    public Instant getExportedAt() {
        return exportedAt;
    }

    public void setExportedAt(Instant exportedAt) {
        this.exportedAt = exportedAt;
    }

    public String getFhirBundleJson() {
        return fhirBundleJson;
    }

    public void setFhirBundleJson(String fhirBundleJson) {
        this.fhirBundleJson = fhirBundleJson;
    }
}
