package com.medikiosk.healthlake.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.Instant;

/**
 * Health and operating mode status of the AWS HealthLake FHIR adapter.
 * Conforms to docs/HEALTHLAKE_SPEC.md.
 */
public class HealthLakeStatusDto {

    private String mode;

    @JsonProperty("isMock")
    private boolean isMock;

    private boolean enabled;
    private String endpoint;
    private String datastoreId;
    private String region;
    private boolean connected;
    private String message;
    private Instant timestamp;

    public HealthLakeStatusDto() {
    }

    public HealthLakeStatusDto(String mode, boolean isMock, boolean enabled, String endpoint,
                               String datastoreId, String region, boolean connected, String message) {
        this.mode = mode;
        this.isMock = isMock;
        this.enabled = enabled;
        this.endpoint = endpoint;
        this.datastoreId = datastoreId;
        this.region = region;
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
