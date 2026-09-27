package com.medikiosk.security.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration properties for public kiosk registration rate limiting.
 * Enforces IP-level throttling on POST /api/auth/kiosk/register.
 */
@Configuration
@ConfigurationProperties(prefix = "kiosk.registration.rate-limit")
public class KioskRateLimitProperties {

    /**
     * Flag indicating if kiosk registration rate limiting is enabled.
     */
    private boolean enabled = true;

    /**
     * Maximum capacity of registration request tokens per IP.
     */
    private int capacity = 10;

    /**
     * Number of tokens refilled every refill period.
     */
    private int refillTokens = 10;

    /**
     * Duration in seconds for token refill period.
     */
    private long refillDurationSeconds = 60;

    /**
     * Flag indicating if reverse proxy IP headers (X-Forwarded-For, X-Real-IP) should be trusted.
     * When false (default for standalone deployments), the actual servlet remote address is used to prevent header spoofing.
     */
    private boolean allowTrustedProxyHeaders = false;

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public int getCapacity() {
        return capacity;
    }

    public void setCapacity(int capacity) {
        this.capacity = capacity;
    }

    public int getRefillTokens() {
        return refillTokens;
    }

    public void setRefillTokens(int refillTokens) {
        this.refillTokens = refillTokens;
    }

    public long getRefillDurationSeconds() {
        return refillDurationSeconds;
    }

    public void setRefillDurationSeconds(long refillDurationSeconds) {
        this.refillDurationSeconds = refillDurationSeconds;
    }

    public boolean isAllowTrustedProxyHeaders() {
        return allowTrustedProxyHeaders;
    }

    public void setAllowTrustedProxyHeaders(boolean allowTrustedProxyHeaders) {
        this.allowTrustedProxyHeaders = allowTrustedProxyHeaders;
    }
}
