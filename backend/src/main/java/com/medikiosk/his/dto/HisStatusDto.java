package com.medikiosk.his.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.Instant;

/**
 * Health and operating mode status of the Hospital Information System (HIS) adapter.
 * Conforms to docs/HIS_SPEC.md.
 */
public class HisStatusDto {

    private String mode;

    @JsonProperty("isMock")
    private boolean isMock;

    private boolean enabled;
    private String baseUrl;
    private boolean connected;
    private String message;
    private Instant timestamp;

    public HisStatusDto() {
    }

    public HisStatusDto(String mode, boolean isMock, boolean enabled, String baseUrl, boolean connected, String message) {
        this.mode = mode;
        this.isMock = isMock;
        this.enabled = enabled;
        this.baseUrl = baseUrl;
        this.connected = connected;
        this.message = message;
        this.timestamp = Instant.now();
    }

    public String getMode() {
        return mode;
    }

    public void setMode(String mode) {
        this.mode = mode;
    }

    public boolean isMock() {
        return isMock;
    }

    public void setMock(boolean mock) {
        isMock = mock;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getBaseUrl() {
        return baseUrl;
    }

    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public boolean isConnected() {
        return connected;
    }

    public void setConnected(boolean connected) {
        this.connected = connected;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Instant timestamp) {
        this.timestamp = timestamp;
    }
}
