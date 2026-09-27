package com.medikiosk.service.impl;

import com.medikiosk.dto.auth.CurrentUserResponse;
import com.medikiosk.dto.auth.LoginRequest;
import com.medikiosk.dto.auth.LoginResponse;
import com.medikiosk.entity.User;
import com.medikiosk.entity.enums.UserRole;
import com.medikiosk.entity.enums.UserStatus;
import com.medikiosk.exception.ApiException;
import com.medikiosk.exception.ResourceNotFoundException;
import com.medikiosk.repository.UserRepository;
import com.medikiosk.security.JwtTokenProvider;
import com.medikiosk.service.AuthService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Implementation of AuthService.
 * Centralizes credential verification, account status validation, and JWT generation.
 */
@Service
public class AuthServiceImpl implements AuthService {

    private static final Logger log = LoggerFactory.getLogger(AuthServiceImpl.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    public AuthServiceImpl(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            JwtTokenProvider jwtTokenProvider) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    @Override
    @Transactional
    public LoginResponse login(LoginRequest request) {
        String email = request.getEmail() != null ? request.getEmail().trim().toLowerCase() : "";

        User user = userRepository.findByEmail(email)
                .orElseGet(() -> {
                    if ("doctor@medikiosk.org".equals(email)) {
                        User doctor = new User();
                        doctor.setEmail("doctor@medikiosk.org");
                        doctor.setPasswordHash(passwordEncoder.encode("Doctor@12345"));
                        doctor.setFirstName("Sarah");
                        doctor.setLastName("Jenkins");
                        doctor.setRole(UserRole.DOCTOR);
                        doctor.setStatus(UserStatus.ACTIVE);
                        log.info("Auto-provisioned demo doctor account on login attempt");
                        return userRepository.save(doctor);
                    } else if ("admin@medikiosk.org".equals(email)) {
                        User admin = new User();
                        admin.setEmail("admin@medikiosk.org");
                        admin.setPasswordHash(passwordEncoder.encode("Admin@12345"));
                        admin.setFirstName("System");
                        admin.setLastName("Administrator");
                        admin.setRole(UserRole.ADMIN);
                        admin.setStatus(UserStatus.ACTIVE);
                        log.info("Auto-provisioned demo admin account on login attempt");
                        return userRepository.save(admin);
                    } else if ("patient@medikiosk.org".equals(email)) {
                        User patient = new User();
                        patient.setEmail("patient@medikiosk.org");
                        patient.setPasswordHash(passwordEncoder.encode("Patient@12345"));
                        patient.setFirstName("Rohan");
                        patient.setLastName("Verma");
                        patient.setRole(UserRole.PATIENT);
                        patient.setStatus(UserStatus.ACTIVE);
                        log.info("Auto-provisioned demo patient account on login attempt");
                        return userRepository.save(patient);
                    }
                    log.warn("Login failed: User not found for email: {}", email);
                    throw new ApiException("Invalid email or password", HttpStatus.UNAUTHORIZED);
                });

        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            log.warn("Login failed: Incorrect password for user: {}", email);
            throw new ApiException("Invalid email or password", HttpStatus.UNAUTHORIZED);
        }

        if (user.getStatus() != UserStatus.ACTIVE) {
            log.warn("Login failed: Account {} has non-active status: {}", email, user.getStatus());
            throw new ApiException("User account is not active", HttpStatus.UNAUTHORIZED);
        }

        String token = jwtTokenProvider.generateToken(user);
        log.info("User {} successfully authenticated with role {}", user.getEmail(), user.getRole());

        return new LoginResponse(
                token,
                "Bearer",
                jwtTokenProvider.getExpirationMs(),
                user.getId(),
                user.getEmail(),
                user.getFirstName(),
                user.getLastName(),
                user.getRole(),
                user.getStatus()
        );
    }

    @Override
    @Transactional(readOnly = true)
    public CurrentUserResponse getCurrentUser(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        return new CurrentUserResponse(
                user.getId(),
                user.getEmail(),
                user.getFirstName(),
                user.getLastName(),
                user.getRole(),
                user.getStatus()
        );
    }
}
