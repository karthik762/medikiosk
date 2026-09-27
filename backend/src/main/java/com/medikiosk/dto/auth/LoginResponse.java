package com.medikiosk.dto.auth;

import com.medikiosk.entity.enums.UserRole;
import com.medikiosk.entity.enums.UserStatus;

import java.util.UUID;

/**
 * Authentication response DTO containing JWT token and safe user profile information.
 * Never exposes passwords or sensitive credentials.
 */
public class LoginResponse {

    private String accessToken;
    private String tokenType = "Bearer";
    private long expiresIn;
    private UUID userId;
    private String email;
    private String firstName;
    private String lastName;
    private UserRole role;
    private UserStatus status;

    public LoginResponse() {
    }

    public LoginResponse(String accessToken, String tokenType, long expiresIn, UUID userId,
                         String email, String firstName, String lastName,
                         UserRole role, UserStatus status) {
        this.accessToken = accessToken;
        this.tokenType = tokenType != null ? tokenType : "Bearer";
        this.expiresIn = expiresIn;
        this.userId = userId;
        this.email = email;
        this.firstName = firstName;
        this.lastName = lastName;
        this.role = role;
        this.status = status;
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

    public UUID getUserId() {
        return userId;
    }

    public void setUserId(UUID userId) {
        this.userId = userId;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
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

    public UserRole getRole() {
        return role;
    }

    public void setRole(UserRole role) {
        this.role = role;
    }

    public UserStatus getStatus() {
        return status;
    }

    public void setStatus(UserStatus status) {
        this.status = status;
    }
}
