package com.medikiosk.clinical;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.medikiosk.dto.intake.ChiefComplaintRequest;
import com.medikiosk.dto.intake.ConsentRequest;
import com.medikiosk.dto.intake.PatientRequest;
import com.medikiosk.entity.enums.ConsentStatus;
import com.medikiosk.entity.enums.ConsentType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AdaptiveQuestionIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("End-to-end adaptive question flow: register -> consent -> complaint -> next-question")
    void testAdaptiveQuestioningFlow() throws Exception {
        // 1. Kiosk Registration
        PatientRequest reg = new PatientRequest("Rajesh", "Kumar", LocalDate.of(1975, 8, 14), "MALE", "+919876500000", "rajesh@example.com");
        String regJson = mockMvc.perform(post("/api/auth/kiosk/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(reg)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        String token = objectMapper.readTree(regJson).path("data").path("accessToken").asText();
        String sessionId = objectMapper.readTree(regJson).path("data").path("sessionId").asText();

        // 2. Consent
        ConsentRequest consentReq = new ConsentRequest();
        consentReq.setConsentStatus(ConsentStatus.ACCEPTED);
        consentReq.setConsentVersion("v1.0");
        consentReq.setConsentType(ConsentType.GENERAL_INTAKE);
        mockMvc.perform(post("/api/intake/sessions/" + sessionId + "/consent")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(consentReq)))
                .andExpect(status().isOk());

        // 3. Record Chief Complaint (Chest pain without explicit severity rating)
        ChiefComplaintRequest complaintReq = new ChiefComplaintRequest();
        complaintReq.setComplaintText("I have chest pain since yesterday");
        complaintReq.setInputMethod("TEXT");
        mockMvc.perform(post("/api/intake/sessions/" + sessionId + "/complaint")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(complaintReq)))
                .andExpect(status().isOk());

        // 4. Request Adaptive Next Question (Turn 1 -> CHEST_PAIN_CHARACTER)
        String nextQuestionResponse = mockMvc.perform(get("/api/intake/sessions/" + sessionId + "/next-question")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.completed").value(false))
                .andExpect(jsonPath("$.data.questionCode").value("Q_CHEST_PAIN_CHARACTER"))
                .andExpect(jsonPath("$.data.clinicalConceptCode").value("CHEST_PAIN"))
                .andExpect(jsonPath("$.data.intent").value("CHEST_PAIN_CHARACTER"))
                .andReturn().getResponse().getContentAsString();

        String questionId = objectMapper.readTree(nextQuestionResponse).path("data").path("questionId").asText();

        // 5. Submit Answer to Turn 1: "It's more like pressure."
        mockMvc.perform(post("/api/intake/sessions/" + sessionId + "/answers")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"questionId\":\"" + questionId + "\",\"answerText\":\"It's more like pressure.\",\"inputMethod\":\"TEXT\"}"))
                .andExpect(status().isCreated());

        // 6. Request Next Question (Turn 2 -> CHEST_PAIN_AGGRAVATING with pressure context)
        mockMvc.perform(get("/api/intake/sessions/" + sessionId + "/next-question")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.questionCode").value("Q_CHEST_PAIN_AGGRAVATING"))
                .andExpect(jsonPath("$.data.questionText").value("Does anything make the pressure better or worse?"));

        // 7. Check Clinical State Endpoint
        mockMvc.perform(get("/api/intake/sessions/" + sessionId + "/clinical-state")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.chiefComplaint").value("I have chest pain since yesterday"));
    }
}
