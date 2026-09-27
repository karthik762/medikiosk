package com.medikiosk.controller;

import com.medikiosk.dto.ApiResponse;
import com.medikiosk.dto.auth.CurrentUserResponse;
import com.medikiosk.dto.auth.LoginRequest;
import com.medikiosk.dto.auth.LoginResponse;
import com.medikiosk.entity.User;
import com.medikiosk.exception.ApiException;
import com.medikiosk.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controller handling authentication and current user profile endpoints.
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    /**
     * Authenticates user credentials and returns a JWT access token.
     * Public endpoint.
     *
     * @param request User login credentials
     * @return Standard ApiResponse enclosing LoginResponse
     */
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponse>> login(@Valid @RequestBody LoginRequest request) {
        LoginResponse response = authService.login(request);
        return ResponseEntity.ok(ApiResponse.success(response, "Authentication successful"));
    }

    /**
     * Retrieves the profile of the currently authenticated user.
     * Protected endpoint requiring a valid Bearer JWT.
     *
     * @param authentication Current Spring Security authentication principal
     * @return Standard ApiResponse enclosing CurrentUserResponse
     */
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<CurrentUserResponse>> getCurrentUser(Authentication authentication) {
        if (authentication == null || !(authentication.getPrincipal() instanceof User user)) {
            throw new ApiException("User is not authenticated", HttpStatus.UNAUTHORIZED);
        }
        CurrentUserResponse response = authService.getCurrentUser(user.getId());
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
