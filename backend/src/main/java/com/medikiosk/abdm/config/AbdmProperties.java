package com.medikiosk.abdm.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration properties for Ayushman Bharat Digital Mission (ABDM) integration.
 * Conforms to docs/ABDM_SPEC.md and docs/SECURITY.md.
 */
@Configuration
@ConfigurationProperties(prefix = "app.abdm")
public class AbdmProperties {

    /**
     * Integration mode: MOCK (default for local development & automated testing),
     * SANDBOX (ABDM sandbox gateway), or PRODUCTION.
     */
    private String mode = "MOCK";

    /**
     * ABDM Gateway base URL.
     */
    private String baseUrl = "https://dev.abdm.gov.in/gateway";

    /**
     * ABDM Client ID (sandbox or production credential).
     */
    private String clientId;

    /**
     * ABDM Client Secret.
     */
    private String clientSecret;

    /**
     * Network timeout for ABDM API calls in milliseconds.
     */
    private int timeoutMs = 5000;

    /**
     * Flag indicating if ABDM integration is enabled.
     */
    private boolean enabled = true;

    public String getMode() {
        return mode;
    }

    public void setMode(String mode) {
        this.mode = mode;
    }

    public String getBaseUrl() {
        return baseUrl;
    }

    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public String getClientSecret() {
        return clientSecret;
    }

    public void setClientSecret(String clientSecret) {
        this.clientSecret = clientSecret;
    }

    public int getTimeoutMs() {
        return timeoutMs;
    }

    public void setTimeoutMs(int timeoutMs) {
        this.timeoutMs = timeoutMs;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public boolean isMock() {
        return "MOCK".equalsIgnoreCase(this.mode);
    }

    public boolean hasCredentials() {
        return clientId != null && !clientId.isBlank()
                && clientSecret != null && !clientSecret.isBlank();
    }
}
