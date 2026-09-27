package com.medikiosk.dto.intake;

import java.util.UUID;

/**
 * Response DTO returned when a patient registers or starts check-in at a physical kiosk.
 * Provides access token, patient identifiers, and initialized intake session ID.
 */
public class KioskRegistrationResponse {

    private String accessToken;
    private String tokenType = "Bearer";
    private long expiresIn;
    private UUID patientId;
    private String patientIdentifier;
    private UUID sessionId;
    private String firstName;
    private String lastName;
    private String email;

    public KioskRegistrationResponse() {
    }

    public KioskRegistrationResponse(String accessToken, long expiresIn, UUID patientId, String patientIdentifier, UUID sessionId, String firstName, String lastName, String email) {
        this.accessToken = accessToken;
        this.expiresIn = expiresIn;
        this.patientId = patientId;
        this.patientIdentifier = patientIdentifier;
        this.sessionId = sessionId;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public String getToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public String getTokenType() {
        return tokenType;
    }

    public void setTokenType(String tokenType) {
        this.tokenType = tokenType;
    }

    public long getExpiresIn() {
        return expiresIn;
    }

    public void setExpiresIn(long expiresIn) {
        this.expiresIn = expiresIn;
    }

    public UUID getPatientId() {
        return patientId;
    }

    public void setPatientId(UUID patientId) {
        this.patientId = patientId;
    }

    public String getPatientIdentifier() {
        return patientIdentifier;
    }

    public void setPatientIdentifier(String patientIdentifier) {
        this.patientIdentifier = patientIdentifier;
    }

    public UUID getSessionId() {
        return sessionId;
    }

    public void setSessionId(UUID sessionId) {
        this.sessionId = sessionId;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}
