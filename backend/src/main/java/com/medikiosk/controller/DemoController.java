package com.medikiosk.controller;

import com.medikiosk.dto.ApiResponse;
import com.medikiosk.dto.auth.LoginResponse;
import com.medikiosk.entity.User;
import com.medikiosk.exception.ApiException;
import com.medikiosk.repository.UserRepository;
import com.medikiosk.security.JwtTokenProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * Controller providing demo status and safe 1-click login for demonstration environments.
 * Guarded by the 'medikiosk.demo.enabled' property. In production (where demo mode is disabled),
 * demo authentication requests are strictly rejected with HTTP 403 Forbidden.
 * No credentials or passwords are ever exposed.
 */
@RestController
@RequestMapping("/api/demo")
public class DemoController {

    private final UserRepository userRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final PasswordEncoder passwordEncoder;
    private final boolean demoEnabled;

    public DemoController(
            UserRepository userRepository,
            JwtTokenProvider jwtTokenProvider,
            PasswordEncoder passwordEncoder,
            @Value("${medikiosk.demo.enabled:true}") boolean demoEnabled) {
        this.userRepository = userRepository;
        this.jwtTokenProvider = jwtTokenProvider;
        this.passwordEncoder = passwordEncoder;
        this.demoEnabled = demoEnabled;
    }

    /**
     * Public endpoint to check if demonstration mode is enabled in the current environment.
     *
     * @return ApiResponse containing boolean flag 'demoEnabled'
     */
    @GetMapping("/status")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getDemoStatus() {
        return ResponseEntity.ok(ApiResponse.success(Map.of("demoEnabled", demoEnabled)));
    }

    /**
     * Authenticates as the demonstration clinician when demo mode is explicitly enabled.
     * Rejects with HTTP 403 in production-like environments.
     *
     * @return Standard ApiResponse enclosing LoginResponse with JWT
     */
    @PostMapping("/doctor-login")
    public ResponseEntity<ApiResponse<LoginResponse>> demoDoctorLogin() {
        if (!demoEnabled) {
            throw new ApiException("Demo mode is disabled in this environment", HttpStatus.FORBIDDEN);
        }

        User doctor = userRepository.findByEmail("doctor@medikiosk.org")
                .orElseGet(this::createDemoDoctor);

        String token = jwtTokenProvider.generateToken(doctor);
        LoginResponse response = new LoginResponse(
                token,
                "Bearer",
                jwtTokenProvider.getExpirationMs(),
                doctor.getId(),
                doctor.getEmail(),
                doctor.getFirstName(),
                doctor.getLastName(),
                doctor.getRole(),
                doctor.getStatus()
        );
        return ResponseEntity.ok(ApiResponse.success(response, "Demo doctor login successful"));
    }

    /**
     * Authenticates as the demonstration patient when demo mode is explicitly enabled.
     * Rejects with HTTP 403 in production-like environments.
     *
     * @return Standard ApiResponse enclosing LoginResponse with JWT
     */
    @PostMapping("/patient-login")
    public ResponseEntity<ApiResponse<LoginResponse>> demoPatientLogin() {
        if (!demoEnabled) {
            throw new ApiException("Demo mode is disabled in this environment", HttpStatus.FORBIDDEN);
        }

        User patient = userRepository.findByEmail("patient@medikiosk.org")
                .orElseGet(this::createDemoPatient);

        String token = jwtTokenProvider.generateToken(patient);
        LoginResponse response = new LoginResponse(
                token,
                "Bearer",
                jwtTokenProvider.getExpirationMs(),
                patient.getId(),
                patient.getEmail(),
                patient.getFirstName(),
                patient.getLastName(),
                patient.getRole(),
                patient.getStatus()
        );
        return ResponseEntity.ok(ApiResponse.success(response, "Demo patient login successful"));
    }

    private User createDemoDoctor() {
        User doctor = new User();
        doctor.setEmail("doctor@medikiosk.org");
        doctor.setPasswordHash(passwordEncoder.encode("Doctor@12345"));
        doctor.setFirstName("Sarah");
        doctor.setLastName("Jenkins");
        doctor.setRole(com.medikiosk.entity.enums.UserRole.DOCTOR);
        doctor.setStatus(com.medikiosk.entity.enums.UserStatus.ACTIVE);
        return userRepository.save(doctor);
    }

    private User createDemoPatient() {
        User patient = new User();
        patient.setEmail("patient@medikiosk.org");
        patient.setPasswordHash(passwordEncoder.encode("Patient@12345"));
        patient.setFirstName("Rohan");
        patient.setLastName("Verma");
        patient.setRole(com.medikiosk.entity.enums.UserRole.PATIENT);
        patient.setStatus(com.medikiosk.entity.enums.UserStatus.ACTIVE);
        return userRepository.save(patient);
    }
}
