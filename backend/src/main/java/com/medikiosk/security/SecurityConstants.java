package com.medikiosk.security;

/**
 * Security constants for role definitions and public endpoint paths.
 */
public final class SecurityConstants {

    private SecurityConstants() {
        // Prevent instantiation
    }

    public static final String ROLE_PATIENT = "PATIENT";
    public static final String ROLE_DOCTOR = "DOCTOR";
    public static final String ROLE_ADMIN = "ADMIN";

    public static final String ROLE_PREFIX = "ROLE_";
    public static final String AUTHORITY_PATIENT = "ROLE_PATIENT";
    public static final String AUTHORITY_DOCTOR = "ROLE_DOCTOR";
    public static final String AUTHORITY_ADMIN = "ROLE_ADMIN";

    public static final String TOKEN_HEADER = "Authorization";
    public static final String TOKEN_PREFIX = "Bearer ";

    public static final String[] PUBLIC_ENDPOINTS = {
            "/api/health",
            "/api/auth/login",
            "/api/auth/kiosk/**",
            "/api/demo/**",
            "/error"
    };
}
