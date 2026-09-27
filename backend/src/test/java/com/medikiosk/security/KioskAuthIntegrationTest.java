package com.medikiosk.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.medikiosk.dto.intake.PatientRequest;
import com.medikiosk.entity.enums.UserRole;
import com.medikiosk.repository.IntakeSessionRepository;
import com.medikiosk.repository.PatientRepository;
import com.medikiosk.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class KioskAuthIntegrationTest {

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

    @BeforeEach
    void cleanDb() {
        intakeSessionRepository.deleteAll();
        patientRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    @DisplayName("Public walk-up kiosk registration succeeds without prior credentials and returns signed JWT")
    void testKioskRegistrationSuccess() throws Exception {
        PatientRequest request = new PatientRequest(
                "Vikram",
                "Patel",
                LocalDate.of(1982, 5, 20),
                "MALE",
                "+919876543210",
                "vikram.patel@example.com"
        );

        String responseJson = mockMvc.perform(post("/api/auth/kiosk/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.accessToken").isString())
                .andExpect(jsonPath("$.data.sessionId").isString())
                .andExpect(jsonPath("$.data.patientIdentifier").isString())
                .andExpect(jsonPath("$.data.firstName").value("Vikram"))
                .andExpect(jsonPath("$.data.lastName").value("Patel"))
                .andReturn().getResponse().getContentAsString();

        String token = objectMapper.readTree(responseJson).path("data").path("accessToken").asText();
        String sessionId = objectMapper.readTree(responseJson).path("data").path("sessionId").asText();

        // Verify the issued JWT immediately authorizes session calls as ROLE_PATIENT
        mockMvc.perform(get("/api/intake/sessions/" + sessionId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.patientIdentifier").isString());
    }
}
