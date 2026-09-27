package com.medikiosk.healthlake.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration properties for AWS HealthLake FHIR datastore integration.
 * Conforms to docs/HEALTHLAKE_SPEC.md and docs/SECURITY.md.
 */
@Configuration
@ConfigurationProperties(prefix = "app.healthlake")
public class HealthLakeProperties {

    /**
     * Integration mode: MOCK (default for local development & automated testing),
     * SANDBOX, or PRODUCTION.
     */
    private String mode = "MOCK";

    /**
     * Flag indicating if AWS HealthLake integration is enabled.
     */
    private boolean enabled = true;

    /**
     * AWS HealthLake FHIR R4 datastore endpoint URL.
     */
    private String endpoint;

    /**
     * AWS HealthLake Datastore ID.
     */
    private String datastoreId;

    /**
     * AWS Region (e.g. ap-south-1, us-east-1).
     */
    private String region = "ap-south-1";

    /**
     * Network timeout for HealthLake calls in milliseconds.
     */
    private int timeoutMs = 5000;

    public String getMode() {
        return mode;
    }

    public void setMode(String mode) {
        this.mode = mode;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getEndpoint() {
        return endpoint;
    }

    public void setEndpoint(String endpoint) {
        this.endpoint = endpoint;
    }

    public String getDatastoreId() {
        return datastoreId;
    }

    public void setDatastoreId(String datastoreId) {
        this.datastoreId = datastoreId;
    }

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    public int getTimeoutMs() {
        return timeoutMs;
    }

    public void setTimeoutMs(int timeoutMs) {
        this.timeoutMs = timeoutMs;
    }

    public boolean isMock() {
        return "MOCK".equalsIgnoreCase(this.mode);
    }

    public boolean hasCredentials() {
        return endpoint != null && !endpoint.isBlank()
                && datastoreId != null && !datastoreId.isBlank();
    }
}
