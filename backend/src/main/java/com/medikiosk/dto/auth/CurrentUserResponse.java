package com.medikiosk.dto.auth;

import com.medikiosk.entity.enums.UserRole;
import com.medikiosk.entity.enums.UserStatus;

import java.util.UUID;

/**
 * DTO for current authenticated user profile (/api/auth/me).
 * Never exposes passwords or sensitive credentials.
 */
public class CurrentUserResponse {

    private UUID userId;
    private String email;
    private String firstName;
    private String lastName;
    private UserRole role;
    private UserStatus status;

    public CurrentUserResponse() {
    }

    public CurrentUserResponse(UUID userId, String email, String firstName, String lastName,
                               UserRole role, UserStatus status) {
        this.userId = userId;
        this.email = email;
        this.firstName = firstName;
        this.lastName = lastName;
        this.role = role;
        this.status = status;
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
