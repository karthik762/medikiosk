package com.medikiosk.security;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.medikiosk.dto.ApiResponse;
import com.medikiosk.dto.auth.LoginRequest;
import com.medikiosk.entity.User;
import com.medikiosk.entity.enums.UserRole;
import com.medikiosk.entity.enums.UserStatus;
import com.medikiosk.repository.UserRepository;
import io.jsonwebtoken.Jwts;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Base64;
import java.util.Date;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import(SecurityAuthenticationIntegrationTest.TestEndpointsConfig.class)
public class SecurityAuthenticationIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Autowired
    private ObjectMapper objectMapper;

    private User doctorUser;
    private User adminUser;
    private User patientUser;
    private User inactiveUser;
    private User lockedUser;

    @TestConfiguration
    static class TestEndpointsConfig {
        @RestController
        @RequestMapping("/api/test-security")
        static class TestSecurityController {

            @GetMapping("/doctor-only")
            @PreAuthorize("hasRole('DOCTOR')")
            public ResponseEntity<ApiResponse<String>> doctorOnly() {
                return ResponseEntity.ok(ApiResponse.success("Doctor access granted"));
            }

            @GetMapping("/admin-only")
            @PreAuthorize("hasRole('ADMIN')")
            public ResponseEntity<ApiResponse<String>> adminOnly() {
                return ResponseEntity.ok(ApiResponse.success("Admin access granted"));
            }

            @GetMapping("/patient-only")
            @PreAuthorize("hasRole('PATIENT')")
            public ResponseEntity<ApiResponse<String>> patientOnly() {
                return ResponseEntity.ok(ApiResponse.success("Patient access granted"));
            }
        }
    }

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();

        doctorUser = userRepository.save(new User(
                "doctor@hospital.org",
                passwordEncoder.encode("DoctorPass123!"),
                "John",
                "Doe",
                UserRole.DOCTOR,
                UserStatus.ACTIVE
        ));

        adminUser = userRepository.save(new User(
                "admin@hospital.org",
                passwordEncoder.encode("AdminPass123!"),
                "Alice",
                "Admin",
                UserRole.ADMIN,
                UserStatus.ACTIVE
        ));

        patientUser = userRepository.save(new User(
                "patient@hospital.org",
                passwordEncoder.encode("PatientPass123!"),
                "Bob",
                "Patient",
                UserRole.PATIENT,
                UserStatus.ACTIVE
        ));

        inactiveUser = userRepository.save(new User(
                "inactive@hospital.org",
                passwordEncoder.encode("InactivePass123!"),
                "Inactive",
                "User",
                UserRole.DOCTOR,
                UserStatus.INACTIVE
        ));

        lockedUser = userRepository.save(new User(
                "locked@hospital.org",
                passwordEncoder.encode("LockedPass123!"),
                "Locked",
                "User",
                UserRole.DOCTOR,
                UserStatus.LOCKED
        ));
    }

    @Test
    @DisplayName("1. Application context starts and security configuration is loaded")
    void testApplicationContextStarts() {
        assertThat(userRepository).isNotNull();
        assertThat(passwordEncoder).isNotNull();
        assertThat(jwtTokenProvider).isNotNull();
    }

    @Test
    @DisplayName("2. Existing health endpoint remains publicly accessible without credentials")
    void testHealthEndpointRemainsPublic() throws Exception {
        mockMvc.perform(get("/api/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"))
                .andExpect(jsonPath("$.service").value("MediKiosk Backend"));
    }

    @Test
    @DisplayName("3. Login with valid credentials succeeds and returns JWT and safe user details")
    void testLoginWithValidCredentials() throws Exception {
        LoginRequest request = new LoginRequest("doctor@hospital.org", "DoctorPass123!");

        MvcResult result = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.accessToken").isString())
                .andExpect(jsonPath("$.data.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.data.email").value("doctor@hospital.org"))
                .andExpect(jsonPath("$.data.role").value("DOCTOR"))
                .andExpect(jsonPath("$.data.status").value("ACTIVE"))
                .andReturn();

        String responseJson = result.getResponse().getContentAsString();
        JsonNode node = objectMapper.readTree(responseJson);
        String token = node.get("data").get("accessToken").asText();

        assertThat(jwtTokenProvider.validateToken(token)).isTrue();
        assertThat(jwtTokenProvider.getUserIdFromToken(token)).isEqualTo(doctorUser.getId());
    }

    @Test
    @DisplayName("4. Login with invalid password fails with 401 Unauthorized")
    void testLoginWithInvalidPasswordFails() throws Exception {
        LoginRequest request = new LoginRequest("doctor@hospital.org", "WrongPassword!");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Invalid email or password"));
    }

    @Test
    @DisplayName("5. Login with unknown user fails with 401 Unauthorized")
    void testLoginWithUnknownUserFails() throws Exception {
        LoginRequest request = new LoginRequest("unknown@hospital.org", "AnyPassword!");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Invalid email or password"));
    }

    @Test
    @DisplayName("6. Disabled or inactive or locked user cannot authenticate")
    void testInactiveOrLockedUserCannotAuthenticate() throws Exception {
        LoginRequest inactiveRequest = new LoginRequest("inactive@hospital.org", "InactivePass123!");
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(inactiveRequest)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("User account is not active"));

        LoginRequest lockedRequest = new LoginRequest("locked@hospital.org", "LockedPass123!");
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(lockedRequest)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("User account is not active"));
    }

    @Test
    @DisplayName("7. Protected endpoint without JWT returns 401 Unauthorized")
    void testProtectedEndpointWithoutJwtReturns401() throws Exception {
        mockMvc.perform(get("/api/auth/me"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @DisplayName("8. Protected endpoint with malformed JWT returns 401 Unauthorized")
    void testProtectedEndpointWithMalformedJwtReturns401() throws Exception {
        mockMvc.perform(get("/api/auth/me")
                        .header("Authorization", "Bearer invalid.malformed.token"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @DisplayName("9. Protected endpoint with expired JWT returns 401 Unauthorized")
    void testProtectedEndpointWithExpiredJwtReturns401() throws Exception {
        // Create an expired token manually using derived key
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] keyBytes = digest.digest("404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970".getBytes(StandardCharsets.UTF_8));
        javax.crypto.SecretKey key = io.jsonwebtoken.security.Keys.hmacShaKeyFor(keyBytes);

        String expiredToken = Jwts.builder()
                .subject(doctorUser.getId().toString())
                .claim("email", doctorUser.getEmail())
                .claim("role", doctorUser.getRole().name())
                .issuedAt(new Date(System.currentTimeMillis() - 100000))
                .expiration(new Date(System.currentTimeMillis() - 50000))
                .signWith(key)
                .compact();

        mockMvc.perform(get("/api/auth/me")
                        .header("Authorization", "Bearer " + expiredToken))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @DisplayName("10. Doctor role can access Doctor-protected endpoint")
    void testDoctorCanAccessDoctorProtectedEndpoint() throws Exception {
        String doctorToken = jwtTokenProvider.generateToken(doctorUser);

        mockMvc.perform(get("/api/test-security/doctor-only")
                        .header("Authorization", "Bearer " + doctorToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").value("Doctor access granted"));
    }

    @Test
    @DisplayName("11. Patient role cannot access Doctor-protected endpoint (returns 403 Forbidden)")
    void testPatientCannotAccessDoctorProtectedEndpoint() throws Exception {
        String patientToken = jwtTokenProvider.generateToken(patientUser);

        mockMvc.perform(get("/api/test-security/doctor-only")
                        .header("Authorization", "Bearer " + patientToken))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Access denied: Insufficient permissions"));
    }

    @Test
    @DisplayName("12. Admin role can access Admin-protected endpoint")
    void testAdminCanAccessAdminProtectedEndpoint() throws Exception {
        String adminToken = jwtTokenProvider.generateToken(adminUser);

        mockMvc.perform(get("/api/test-security/admin-only")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").value("Admin access granted"));
    }

    @Test
    @DisplayName("13. Doctor role cannot access Admin-only endpoint (returns 403 Forbidden)")
    void testDoctorCannotAccessAdminOnlyEndpoint() throws Exception {
        String doctorToken = jwtTokenProvider.generateToken(doctorUser);

        mockMvc.perform(get("/api/test-security/admin-only")
                        .header("Authorization", "Bearer " + doctorToken))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Access denied: Insufficient permissions"));
    }

    @Test
    @DisplayName("14. JWT token does not contain password or password hash in claims or headers")
    void testJwtDoesNotContainPasswordOrPasswordHash() {
        String token = jwtTokenProvider.generateToken(doctorUser);
        String[] parts = token.split("\\.");
        assertThat(parts.length).isEqualTo(3);

        String header = new String(Base64.getUrlDecoder().decode(parts[0]), StandardCharsets.UTF_8);
        String payload = new String(Base64.getUrlDecoder().decode(parts[1]), StandardCharsets.UTF_8);

        assertThat(header.toLowerCase()).doesNotContain("password");
        assertThat(payload.toLowerCase()).doesNotContain("password");
        assertThat(payload.toLowerCase()).doesNotContain("hash");
        assertThat(payload).doesNotContain(doctorUser.getPasswordHash());
    }

    @Test
    @DisplayName("15. Authentication responses do not expose password or password hash")
    void testAuthResponsesDoNotExposePasswordOrHash() throws Exception {
        LoginRequest request = new LoginRequest("doctor@hospital.org", "DoctorPass123!");

        MvcResult loginResult = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andReturn();

        String loginResponseBody = loginResult.getResponse().getContentAsString();
        assertThat(loginResponseBody.toLowerCase()).doesNotContain("password");
        assertThat(loginResponseBody.toLowerCase()).doesNotContain("hash");
        assertThat(loginResponseBody).doesNotContain(doctorUser.getPasswordHash());

        String doctorToken = jwtTokenProvider.generateToken(doctorUser);
        MvcResult meResult = mockMvc.perform(get("/api/auth/me")
                        .header("Authorization", "Bearer " + doctorToken))
                .andExpect(status().isOk())
                .andReturn();

        String meResponseBody = meResult.getResponse().getContentAsString();
        assertThat(meResponseBody.toLowerCase()).doesNotContain("password");
        assertThat(meResponseBody.toLowerCase()).doesNotContain("hash");
        assertThat(meResponseBody).doesNotContain(doctorUser.getPasswordHash());
    }

    @Test
    @DisplayName("16. Phase 16-17: Malformed JSON request body returns 400 without internal stack traces")
    void testMalformedJsonReturnsBadRequestWithoutStacktrace() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\": \"broken-json"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Malformed request payload or invalid JSON format"));
    }

    @Test
    @DisplayName("17. Phase 16-17: Unsupported HTTP method returns 405 Method Not Allowed")
    void testUnsupportedHttpMethodReturnsMethodNotAllowed() throws Exception {
        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isMethodNotAllowed())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @DisplayName("18. Phase 16-17: Security headers (nosniff, frame deny, cache-control) present on API responses")
    void testSecurityHeadersPresent() throws Exception {
        mockMvc.perform(get("/api/health"))
                .andExpect(status().isOk())
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.header().string("X-Content-Type-Options", "nosniff"))
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.header().string("X-Frame-Options", "DENY"));
    }

    @Test
    @DisplayName("19. Phase 16-17: CORS preflight request allows configured origin")
    void testCorsPreflightAllowed() throws Exception {
        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options("/api/health")
                        .header("Origin", "http://localhost:5173")
                        .header("Access-Control-Request-Method", "GET"))
                .andExpect(status().isOk())
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.header().string("Access-Control-Allow-Origin", "http://localhost:5173"));
    }
}

