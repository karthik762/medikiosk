package com.medikiosk.admin;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.medikiosk.dto.admin.CreateUserRequestDto;
import com.medikiosk.entity.User;
import com.medikiosk.entity.enums.UserRole;
import com.medikiosk.entity.enums.UserStatus;
import com.medikiosk.repository.AuditLogRepository;
import com.medikiosk.repository.UserRepository;
import com.medikiosk.security.JwtTokenProvider;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class Stage6AdminWorkstationIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AuditLogRepository auditLogRepository;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private ObjectMapper objectMapper;

    private User adminUser;
    private User doctorUser;
    private User patientUser;

    private String adminToken;
    private String doctorToken;
    private String patientToken;

    @BeforeEach
    void setUp() {
        auditLogRepository.deleteAll();
        userRepository.deleteAll();

        // Create Admin
        adminUser = new User();
        adminUser.setEmail("stage6_admin@hospital.org");
        adminUser.setPasswordHash(passwordEncoder.encode("Admin@12345"));
        adminUser.setFirstName("Super");
        adminUser.setLastName("Admin");
        adminUser.setRole(UserRole.ADMIN);
        adminUser.setStatus(UserStatus.ACTIVE);
        adminUser = userRepository.save(adminUser);
        adminToken = jwtTokenProvider.generateToken(adminUser);

        // Create Doctor
        doctorUser = new User();
        doctorUser.setEmail("stage6_doctor@hospital.org");
        doctorUser.setPasswordHash(passwordEncoder.encode("Doctor@12345"));
        doctorUser.setFirstName("Vikram");
        doctorUser.setLastName("Seth");
        doctorUser.setRole(UserRole.DOCTOR);
        doctorUser.setStatus(UserStatus.ACTIVE);
        doctorUser = userRepository.save(doctorUser);
        doctorToken = jwtTokenProvider.generateToken(doctorUser);

        // Create Patient
        patientUser = new User();
        patientUser.setEmail("stage6_patient@example.org");
        patientUser.setPasswordHash(passwordEncoder.encode("Patient@12345"));
        patientUser.setFirstName("Rahul");
        patientUser.setLastName("Kumar");
        patientUser.setRole(UserRole.PATIENT);
        patientUser.setStatus(UserStatus.ACTIVE);
        patientUser = userRepository.save(patientUser);
        patientToken = jwtTokenProvider.generateToken(patientUser);
    }

    @Test
    @DisplayName("Stage 6: Admin analytics endpoint returns operational metrics for ADMIN role")
    void testAdminAnalyticsEndpoint() throws Exception {
        mockMvc.perform(get("/api/admin/analytics")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.totalDoctors").value(1))
                .andExpect(jsonPath("$.data.triageDistribution.EMERGENCY").exists())
                .andExpect(jsonPath("$.data.triageDistribution.ROUTINE").exists());
    }

    @Test
    @DisplayName("Stage 6: Non-admin users (DOCTOR, PATIENT) receive HTTP 403 Forbidden on Admin endpoints")
    void testNonAdminForbiddenAccess() throws Exception {
        // Doctor attempting admin endpoint
        mockMvc.perform(get("/api/admin/analytics")
                        .header("Authorization", "Bearer " + doctorToken))
                .andExpect(status().isForbidden());

        // Patient attempting admin endpoint
        mockMvc.perform(get("/api/admin/analytics")
                        .header("Authorization", "Bearer " + patientToken))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Stage 6: Admin user management (get, create, update status)")
    void testAdminUserManagement() throws Exception {
        // 1. Get users
        mockMvc.perform(get("/api/admin/users")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.length()").value(3));

        // 2. Create new doctor
        CreateUserRequestDto newDoctorReq = new CreateUserRequestDto();
        newDoctorReq.setEmail("new_clinician@hospital.org");
        newDoctorReq.setPassword("Password@123");
        newDoctorReq.setFirstName("Anita");
        newDoctorReq.setLastName("Deshmukh");
        newDoctorReq.setRole(UserRole.DOCTOR);

        mockMvc.perform(post("/api/admin/users")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newDoctorReq)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.email").value("new_clinician@hospital.org"))
                .andExpect(jsonPath("$.data.role").value("DOCTOR"));

        User createdDoctor = userRepository.findByEmail("new_clinician@hospital.org").orElseThrow();
        assertThat(createdDoctor.getStatus()).isEqualTo(UserStatus.ACTIVE);

        // 3. Deactivate user status
        mockMvc.perform(put("/api/admin/users/" + createdDoctor.getId() + "/status?status=INACTIVE")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.status").value("INACTIVE"));

        User deactivatedDoctor = userRepository.findById(createdDoctor.getId()).orElseThrow();
        assertThat(deactivatedDoctor.getStatus()).isEqualTo(UserStatus.INACTIVE);
    }

    @Test
    @DisplayName("Stage 6: Admin audit log explorer & system config summary")
    void testAuditLogsAndConfigSummary() throws Exception {
        // 1. Fetch config summary
        mockMvc.perform(get("/api/admin/config")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.environment").value("PRODUCTION_READY"))
                .andExpect(jsonPath("$.data.abdmMock").value(true));

        // 2. Fetch audit logs
        mockMvc.perform(get("/api/admin/audit-logs")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }
}
