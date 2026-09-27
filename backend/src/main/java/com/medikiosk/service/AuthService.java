package com.medikiosk.service;

import com.medikiosk.dto.auth.CurrentUserResponse;
import com.medikiosk.dto.auth.LoginRequest;
import com.medikiosk.dto.auth.LoginResponse;

import java.util.UUID;

/**
 * Authentication service contract for user authentication and current user retrieval.
 */
public interface AuthService {

    /**
     * Authenticates user credentials and generates a JWT access token.
     *
     * @param request Login credentials
     * @return Login response containing JWT token and safe user details
     */
    LoginResponse login(LoginRequest request);

    /**
     * Retrieves the profile of the currently authenticated user.
     *
     * @param userId Unique user identifier
     * @return Safe user profile information
     */
    CurrentUserResponse getCurrentUser(UUID userId);
}
