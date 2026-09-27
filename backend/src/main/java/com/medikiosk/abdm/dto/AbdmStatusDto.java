package com.medikiosk.abdm.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.Instant;

/**
 * Health and mode status of the ABDM integration adapter.
 * Conforms to docs/ABDM_SPEC.md.
 */
public class AbdmStatusDto {

    private String mode;

    @JsonProperty("isMock")
    private boolean isMock;
    private boolean enabled;
    private String gatewayBaseUrl;
    private boolean connected;
    private String message;
    private Instant timestamp;

    public AbdmStatusDto() {
    }

    public AbdmStatusDto(String mode, boolean isMock, boolean enabled, String gatewayBaseUrl, boolean connected, String message) {
        this.mode = mode;
        this.isMock = isMock;
        this.enabled = enabled;
        this.gatewayBaseUrl = gatewayBaseUrl;
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

    public String getGatewayBaseUrl() {
        return gatewayBaseUrl;
    }

    public void setGatewayBaseUrl(String gatewayBaseUrl) {
        this.gatewayBaseUrl = gatewayBaseUrl;
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
