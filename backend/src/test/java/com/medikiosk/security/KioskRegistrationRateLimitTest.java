package com.medikiosk.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.medikiosk.dto.auth.LoginRequest;
import com.medikiosk.dto.intake.PatientRequest;
import com.medikiosk.entity.User;
import com.medikiosk.entity.enums.UserRole;
import com.medikiosk.entity.enums.UserStatus;
import com.medikiosk.repository.IntakeSessionRepository;
import com.medikiosk.repository.PatientRepository;
import com.medikiosk.repository.UserRepository;
import com.medikiosk.security.config.KioskRateLimitProperties;
import com.medikiosk.security.filter.KioskRegistrationRateLimitFilter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class KioskRegistrationRateLimitTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PatientRepository patientRepository;

    @Autowired
    private IntakeSessionRepository intakeSessionRepository;

    @Autowired
    private KioskRegistrationRateLimitFilter rateLimitFilter;

    @Autowired
    private KioskRateLimitProperties rateLimitProperties;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void setup() {
        intakeSessionRepository.deleteAll();
        patientRepository.deleteAll();
        userRepository.deleteAll();
        rateLimitFilter.reset();
        rateLimitProperties.setEnabled(true);
        rateLimitProperties.setCapacity(3);
        rateLimitProperties.setRefillTokens(3);
        rateLimitProperties.setRefillDurationSeconds(60);
        rateLimitProperties.setAllowTrustedProxyHeaders(false);
    }

    private PatientRequest createSamplePatientRequest(String name) {
        return new PatientRequest(
                name,
                "Test",
                LocalDate.of(1990, 1, 1),
                "MALE",
                "+919876543210",
                name.toLowerCase() + "@example.com"
        );
    }

    @Test
    @DisplayName("1. First registration request succeeds")
    void testFirstRegistrationSuccess() throws Exception {
        PatientRequest request = createSamplePatientRequest("First");

        mockMvc.perform(post("/api/auth/kiosk/register")
                        .with(req -> { req.setRemoteAddr("192.168.1.100"); return req; })
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.accessToken").isString());
    }

    @Test
    @DisplayName("2. Requests within configured capacity (3) succeed")
    void testRequestsWithinCapacitySucceed() throws Exception {
        for (int i = 1; i <= 3; i++) {
            PatientRequest request = createSamplePatientRequest("User" + i);
            mockMvc.perform(post("/api/auth/kiosk/register")
                            .with(req -> { req.setRemoteAddr("192.168.1.101"); return req; })
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated());
        }
    }

    @Test
    @DisplayName("3 & 7 & 8. Request exceeding capacity returns sanitized HTTP 429 with Retry-After header")
    void testRequestExceedingCapacityReturns429() throws Exception {
        // First 3 requests exhaust capacity
        for (int i = 1; i <= 3; i++) {
            mockMvc.perform(post("/api/auth/kiosk/register")
                            .with(req -> { req.setRemoteAddr("192.168.1.102"); return req; })
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(createSamplePatientRequest("Allowed" + i))))
                    .andExpect(status().isCreated());
        }

        // 4th request must fail with HTTP 429 TOO_MANY_REQUESTS
        mockMvc.perform(post("/api/auth/kiosk/register")
                        .with(req -> { req.setRemoteAddr("192.168.1.102"); return req; })
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createSamplePatientRequest("Exceeded"))))
                .andExpect(status().isTooManyRequests())
                .andExpect(header().string("Retry-After", "60"))
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error").value("REGISTRATION_RATE_LIMITED"))
                .andExpect(jsonPath("$.message").value("Too many registration attempts. Please try again shortly."))
                .andExpect(jsonPath("$.timestamp").isString())
                // Ensure no internal details are leaked
                .andExpect(jsonPath("$.stackTrace").doesNotExist())
                .andExpect(jsonPath("$.clientIp").doesNotExist())
                .andExpect(jsonPath("$.bucketState").doesNotExist());
    }

    @Test
    @DisplayName("4 & 5. Different source IPs are independently limited")
    void testDifferentIpsIndependentlyLimited() throws Exception {
        // Exhaust IP 1
        for (int i = 1; i <= 3; i++) {
            mockMvc.perform(post("/api/auth/kiosk/register")
                            .with(req -> { req.setRemoteAddr("10.0.0.1"); return req; })
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(createSamplePatientRequest("IP1User" + i))))
                    .andExpect(status().isCreated());
        }

        // IP 1 4th request is rate limited
        mockMvc.perform(post("/api/auth/kiosk/register")
                        .with(req -> { req.setRemoteAddr("10.0.0.1"); return req; })
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createSamplePatientRequest("IP1Blocked"))))
                .andExpect(status().isTooManyRequests());

        // IP 2 is still allowed
        mockMvc.perform(post("/api/auth/kiosk/register")
                        .with(req -> { req.setRemoteAddr("10.0.0.2"); return req; })
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createSamplePatientRequest("IP2Allowed"))))
                .andExpect(status().isCreated());
    }

    @Test
    @DisplayName("5b. Spoofed X-Forwarded-For headers are ignored when trusted proxy mode is disabled")
    void testSpoofedHeadersIgnoredWhenProxyTrustDisabled() throws Exception {
        rateLimitProperties.setAllowTrustedProxyHeaders(false);

        // Send requests with changing X-Forwarded-For headers but same remoteAddr
        for (int i = 1; i <= 3; i++) {
            mockMvc.perform(post("/api/auth/kiosk/register")
                            .with(req -> { req.setRemoteAddr("172.16.0.5"); return req; })
                            .header("X-Forwarded-For", "203.0.113." + i)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(createSamplePatientRequest("Spoof" + i))))
                    .andExpect(status().isCreated());
        }

        // 4th request should be BLOCKED because remoteAddr 172.16.0.5 is rate limited despite fake header
        mockMvc.perform(post("/api/auth/kiosk/register")
                        .with(req -> { req.setRemoteAddr("172.16.0.5"); return req; })
                        .header("X-Forwarded-For", "203.0.113.99")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createSamplePatientRequest("SpoofBlocked"))))
                .andExpect(status().isTooManyRequests());
    }

    @Test
    @DisplayName("6. Rate limit bucket reset/clearing works correctly")
    void testRateLimitReset() throws Exception {
        // Exhaust capacity
        for (int i = 1; i <= 3; i++) {
            mockMvc.perform(post("/api/auth/kiosk/register")
                            .with(req -> { req.setRemoteAddr("192.168.1.105"); return req; })
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(createSamplePatientRequest("ResetUser" + i))))
                    .andExpect(status().isCreated());
        }

        // Verify blocked
        mockMvc.perform(post("/api/auth/kiosk/register")
                        .with(req -> { req.setRemoteAddr("192.168.1.105"); return req; })
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createSamplePatientRequest("ResetBlocked"))))
                .andExpect(status().isTooManyRequests());

        // Perform reset
        rateLimitFilter.reset();

        // Should succeed again
        mockMvc.perform(post("/api/auth/kiosk/register")
                        .with(req -> { req.setRemoteAddr("192.168.1.105"); return req; })
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createSamplePatientRequest("ResetSuccess"))))
                .andExpect(status().isCreated());
    }

    @Test
    @DisplayName("10. Clinician login endpoint (/api/auth/login) is unaffected by registration rate limit")
    void testClinicianAuthUnaffected() throws Exception {
        // Create clinician user
        User doctor = new User();
        doctor.setEmail("dr.smith@hospital.org");
        doctor.setFirstName("John");
        doctor.setLastName("Smith");
        doctor.setRole(UserRole.DOCTOR);
        doctor.setStatus(UserStatus.ACTIVE);
        doctor.setPasswordHash(passwordEncoder.encode("DoctorPass123!"));
        userRepository.save(doctor);

        // Exhaust kiosk registration rate limit for IP 192.168.1.200
        for (int i = 1; i <= 3; i++) {
            mockMvc.perform(post("/api/auth/kiosk/register")
                            .with(req -> { req.setRemoteAddr("192.168.1.200"); return req; })
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(createSamplePatientRequest("Exhaust" + i))))
                    .andExpect(status().isCreated());
        }
        mockMvc.perform(post("/api/auth/kiosk/register")
                        .with(req -> { req.setRemoteAddr("192.168.1.200"); return req; })
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createSamplePatientRequest("ExhaustBlocked"))))
                .andExpect(status().isTooManyRequests());

        // Clinician login from same IP 192.168.1.200 must still succeed!
        LoginRequest loginReq = new LoginRequest("dr.smith@hospital.org", "DoctorPass123!");
        mockMvc.perform(post("/api/auth/login")
                        .with(req -> { req.setRemoteAddr("192.168.1.200"); return req; })
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.token").isString());
    }

    @Test
    @DisplayName("11. Existing authenticated intake endpoints are unaffected")
    void testAuthenticatedIntakeEndpointsUnaffected() throws Exception {
        // Register patient once
        PatientRequest request = createSamplePatientRequest("IntakePatient");
        String responseJson = mockMvc.perform(post("/api/auth/kiosk/register")
                        .with(req -> { req.setRemoteAddr("192.168.1.201"); return req; })
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        String token = objectMapper.readTree(responseJson).path("data").path("accessToken").asText();
        String sessionId = objectMapper.readTree(responseJson).path("data").path("sessionId").asText();

        // Exhaust registration limit for IP 192.168.1.201
        for (int i = 2; i <= 3; i++) {
            mockMvc.perform(post("/api/auth/kiosk/register")
                            .with(req -> { req.setRemoteAddr("192.168.1.201"); return req; })
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(createSamplePatientRequest("IntakePatient" + i))))
                    .andExpect(status().isCreated());
        }
        mockMvc.perform(post("/api/auth/kiosk/register")
                        .with(req -> { req.setRemoteAddr("192.168.1.201"); return req; })
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createSamplePatientRequest("IntakePatientBlocked"))))
                .andExpect(status().isTooManyRequests());

        // Authenticated intake session API call from same IP still succeeds!
        mockMvc.perform(get("/api/intake/sessions/" + sessionId)
                        .with(req -> { req.setRemoteAddr("192.168.1.201"); return req; })
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @DisplayName("12. Disabled test configuration allows unlimited requests")
    void testDisabledRateLimitAllowsUnlimitedRequests() throws Exception {
        rateLimitProperties.setEnabled(false);

        for (int i = 1; i <= 10; i++) {
            mockMvc.perform(post("/api/auth/kiosk/register")
                            .with(req -> { req.setRemoteAddr("192.168.1.202"); return req; })
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(createSamplePatientRequest("DisabledLimit" + i))))
                    .andExpect(status().isCreated());
        }
    }
}
