package com.medikiosk.controller;

import com.medikiosk.dto.ApiResponse;
import com.medikiosk.dto.intake.KioskRegistrationResponse;
import com.medikiosk.dto.intake.PatientRequest;
import com.medikiosk.entity.IntakeSession;
import com.medikiosk.entity.Patient;
import com.medikiosk.entity.User;
import com.medikiosk.entity.enums.UserRole;
import com.medikiosk.entity.enums.UserStatus;
import com.medikiosk.repository.IntakeSessionRepository;
import com.medikiosk.repository.PatientRepository;
import com.medikiosk.repository.UserRepository;
import com.medikiosk.security.JwtTokenProvider;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

/**
 * Controller handling public patient check-in and registration at hospital self-service kiosks.
 * Issues a cryptographically signed ROLE_PATIENT JWT bound to the newly registered patient profile.
 */
@RestController
@RequestMapping("/api/auth/kiosk")
public class KioskAuthController {

    private static final Logger log = LoggerFactory.getLogger(KioskAuthController.class);

    private final UserRepository userRepository;
    private final PatientRepository patientRepository;
    private final IntakeSessionRepository intakeSessionRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final PasswordEncoder passwordEncoder;

    public KioskAuthController(
            UserRepository userRepository,
            PatientRepository patientRepository,
            IntakeSessionRepository intakeSessionRepository,
            JwtTokenProvider jwtTokenProvider,
            PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.patientRepository = patientRepository;
        this.intakeSessionRepository = intakeSessionRepository;
        this.jwtTokenProvider = jwtTokenProvider;
        this.passwordEncoder = passwordEncoder;
    }

    @PostMapping("/register")
    @Transactional
    public ResponseEntity<ApiResponse<KioskRegistrationResponse>> registerKioskPatient(
            @Valid @RequestBody PatientRequest request) {

        String email = StringUtils.hasText(request.getEmail())
                ? request.getEmail().trim().toLowerCase()
                : "kiosk_" + UUID.randomUUID().toString().substring(0, 8) + "@medikiosk.local";

        // 1. Resolve or create user with ROLE_PATIENT
        org.springframework.security.core.Authentication auth = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
        User currentUser = (auth != null && auth.getPrincipal() instanceof User) ? (User) auth.getPrincipal() : null;

        User user;
        if (currentUser != null && currentUser.getRole() == UserRole.PATIENT && (!StringUtils.hasText(request.getEmail()) || currentUser.getEmail().equalsIgnoreCase(request.getEmail().trim()))) {
            user = currentUser;
        } else {
            user = userRepository.findByEmail(email).orElseGet(() -> {
                User newUser = new User();
                newUser.setEmail(email);
                newUser.setFirstName(request.getFirstName().trim());
                newUser.setLastName(request.getLastName().trim());
                newUser.setRole(UserRole.PATIENT);
                newUser.setStatus(UserStatus.ACTIVE);
                newUser.setPasswordHash(passwordEncoder.encode(UUID.randomUUID().toString()));
                return userRepository.save(newUser);
            });
        }

        // 2. Resolve or generate patient identifier
        String identifier = StringUtils.hasText(request.getPatientIdentifier())
                ? request.getPatientIdentifier().trim()
                : "MK-PAT-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();

        Patient patient = new Patient(identifier, request.getFirstName().trim(), request.getLastName().trim());
        patient.setDateOfBirth(request.getDateOfBirth());
        patient.setGender(request.getGender());
        patient.setPhone(request.getPhone());
        patient.setEmail(email);
        patient.setUserId(user.getId());

        Patient savedPatient = patientRepository.save(patient);

        // 3. Initialize intake session
        IntakeSession session = new IntakeSession(savedPatient);
        session.setLanguage("en");
        session.setExpiresAt(Instant.now().plus(24, ChronoUnit.HOURS));
        IntakeSession savedSession = intakeSessionRepository.save(session);

        // 4. Generate signed JWT token
        String token = jwtTokenProvider.generateToken(user);

        log.info("Kiosk registered patient {} (session: {})", savedPatient.getPatientIdentifier(), savedSession.getId());

        KioskRegistrationResponse response = new KioskRegistrationResponse(
                token,
                jwtTokenProvider.getExpirationMs(),
                savedPatient.getId(),
                savedPatient.getPatientIdentifier(),
                savedSession.getId(),
                savedPatient.getFirstName(),
                savedPatient.getLastName(),
                email
        );

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "Kiosk patient check-in initialized successfully"));
    }
}
