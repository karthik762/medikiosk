package com.medikiosk.ayurveda;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.medikiosk.dto.ayurveda.AyurvedaHistoryRequest;
import com.medikiosk.dto.ayurveda.AyurvedaTerminologyDto;
import com.medikiosk.dto.ayurveda.NidanaDataDto;
import com.medikiosk.dto.ayurveda.ParikshaDataDto;
import com.medikiosk.dto.ayurveda.PrakritiDataDto;
import com.medikiosk.dto.intake.AnswerRequest;
import com.medikiosk.dto.intake.ConsentRequest;
import com.medikiosk.dto.intake.IntakeSessionRequest;
import com.medikiosk.dto.intake.PatientRequest;
import com.medikiosk.dto.intake.SymptomRequest;
import com.medikiosk.entity.AuditLog;
import com.medikiosk.entity.AyurvedaHistory;
import com.medikiosk.entity.IntakeSession;
import com.medikiosk.entity.Patient;
import com.medikiosk.entity.Question;
import com.medikiosk.entity.User;
import com.medikiosk.entity.enums.AyurvedaHistoryStatus;
import com.medikiosk.entity.enums.ConsentStatus;
import com.medikiosk.entity.enums.IntakeSessionStatus;
import com.medikiosk.entity.enums.UserRole;
import com.medikiosk.entity.enums.UserStatus;
import com.medikiosk.repository.AnswerRepository;
import com.medikiosk.repository.AuditLogRepository;
import com.medikiosk.repository.AyurvedaHistoryRepository;
import com.medikiosk.repository.ConsentRepository;
import com.medikiosk.repository.IntakeSessionRepository;
import com.medikiosk.repository.PatientRepository;
import com.medikiosk.repository.QuestionRepository;
import com.medikiosk.repository.SymptomRepository;
import com.medikiosk.repository.TriageResultRepository;
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
import org.springframework.test.web.servlet.MvcResult;

import java.time.Instant;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class AyurvedaIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PatientRepository patientRepository;

    @Autowired
    private IntakeSessionRepository intakeSessionRepository;

    @Autowired
    private ConsentRepository consentRepository;

    @Autowired
    private SymptomRepository symptomRepository;

    @Autowired
    private AnswerRepository answerRepository;

    @Autowired
    private QuestionRepository questionRepository;

    @Autowired
    private AyurvedaHistoryRepository ayurvedaHistoryRepository;

    @Autowired
    private AuditLogRepository auditLogRepository;

    @Autowired
    private TriageResultRepository triageResultRepository;

    @Autowired
    private AyurvedaIntakeService ayurvedaIntakeService;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private ObjectMapper objectMapper;

    private User patientUserA;
    private String tokenPatientA;
    private User patientUserB;
    private String tokenPatientB;
    private User doctorUser;
    private String tokenDoctor;

    @BeforeEach
    void setUp() {
        ayurvedaHistoryRepository.deleteAll();
        auditLogRepository.deleteAll();
        triageResultRepository.deleteAll();
        answerRepository.deleteAll();
        symptomRepository.deleteAll();
        consentRepository.deleteAll();
        intakeSessionRepository.deleteAll();
        patientRepository.deleteAll();
        userRepository.deleteAll();

        patientUserA = userRepository.save(new User(
                "patientA@ayurveda.test",
                passwordEncoder.encode("Pass123!"),
                "Aarav",
                "Sharma",
                UserRole.PATIENT,
                UserStatus.ACTIVE
        ));
        tokenPatientA = jwtTokenProvider.generateToken(patientUserA);

        patientUserB = userRepository.save(new User(
                "patientB@ayurveda.test",
                passwordEncoder.encode("Pass123!"),
                "Diya",
                "Patel",
                UserRole.PATIENT,
                UserStatus.ACTIVE
        ));
        tokenPatientB = jwtTokenProvider.generateToken(patientUserB);

        doctorUser = userRepository.save(new User(
                "doctor@ayurveda.test",
                passwordEncoder.encode("Doctor123!"),
                "Dr. Vaidya",
                "Rao",
                UserRole.DOCTOR,
                UserStatus.ACTIVE
        ));
        tokenDoctor = jwtTokenProvider.generateToken(doctorUser);
    }

    private Patient createPatientForUser(User user, String identifier) throws Exception {
        PatientRequest request = new PatientRequest("Test", "Patient", LocalDate.of(1992, 5, 10), "MALE", "+919876543210", user.getEmail());
        request.setPatientIdentifier(identifier);

        String token = user.equals(patientUserA) ? tokenPatientA : tokenPatientB;

        MvcResult result = mockMvc.perform(post("/api/patients")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn();

        JsonNode root = objectMapper.readTree(result.getResponse().getContentAsString());
        UUID patientId = UUID.fromString(root.path("data").path("id").asText());
        return patientRepository.findById(patientId).orElseThrow();
    }

    private UUID createSessionForPatient(Patient patient, String token) throws Exception {
        IntakeSessionRequest request = new IntakeSessionRequest(patient.getId(), "en");
        MvcResult result = mockMvc.perform(post("/api/intake/sessions")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn();

        JsonNode root = objectMapper.readTree(result.getResponse().getContentAsString());
        return UUID.fromString(root.path("data").path("id").asText());
    }

    private void acceptConsent(UUID sessionId, String token) throws Exception {
        ConsentRequest consentRequest = new ConsentRequest(ConsentStatus.ACCEPTED, "GENERAL_INTAKE", "v1.0", "REF-AYUR-1");
        mockMvc.perform(post("/api/intake/sessions/" + sessionId + "/consent")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(consentRequest)))
                .andExpect(status().isOk());
    }

    private AyurvedaHistoryRequest createSampleAyurvedaRequest() {
        AyurvedaHistoryRequest request = new AyurvedaHistoryRequest();

        PrakritiDataDto prakriti = new PrakritiDataDto();
        prakriti.setBodyFrame("SLENDER");
        prakriti.setWeatherPreference("DISLIKES_COLD");
        prakriti.setDigestionPattern("IRREGULAR");
        prakriti.setSleepPattern("LIGHT_INTERRUPTED");
        request.setPrakriti(prakriti);

        NidanaDataDto nidana = new NidanaDataDto();
        nidana.setDietaryHabits("IRREGULAR_MEALS");
        nidana.setActivityPattern("SEDENTARY");
        nidana.setStressFactors("HIGH_MENTAL_STRESS");
        nidana.setSleepSchedule("LATE_NIGHT");
        request.setNidana(nidana);

        ParikshaDataDto pariksha = new ParikshaDataDto();
        pariksha.setKoshtha("KRURA");
        pariksha.setBala("MADHYAMA");
        pariksha.setJihva("WHITE_COATING");
        pariksha.setAgni("VISHAMA");
        request.setPariksha(pariksha);

        request.setNotes("Patient reports dry skin and variable appetite.");
        return request;
    }

    @Test
    @DisplayName("Case 1: Happy path - opt-in and record Prakriti, Nidana, Pariksha data")
    void testRecordAyurvedaHistory_HappyPath() throws Exception {
        Patient patientA = createPatientForUser(patientUserA, "PID-AYUR-001");
        UUID sessionId = createSessionForPatient(patientA, tokenPatientA);
        acceptConsent(sessionId, tokenPatientA);

        AyurvedaHistoryRequest request = createSampleAyurvedaRequest();

        mockMvc.perform(post("/api/intake/sessions/" + sessionId + "/ayurveda")
                        .header("Authorization", "Bearer " + tokenPatientA)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.status").value("DRAFT"))
                .andExpect(jsonPath("$.data.patientConfirmed").value(false))
                .andExpect(jsonPath("$.data.prakriti.bodyFrame").value("SLENDER"))
                .andExpect(jsonPath("$.data.prakriti.weatherPreference").value("DISLIKES_COLD"))
                .andExpect(jsonPath("$.data.nidana.dietaryHabits").value("IRREGULAR_MEALS"))
                .andExpect(jsonPath("$.data.pariksha.koshtha").value("KRURA"))
                .andExpect(jsonPath("$.data.notes").value("Patient reports dry skin and variable appetite."));

        // Verify retrieval via GET
        mockMvc.perform(get("/api/intake/sessions/" + sessionId + "/ayurveda")
                        .header("Authorization", "Bearer " + tokenPatientA))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("DRAFT"))
                .andExpect(jsonPath("$.data.prakriti.bodyFrame").value("SLENDER"));
    }

    @Test
    @DisplayName("Case 2: Confirm history - transitions status to PATIENT_CONFIRMED, records confirmedAt")
    void testConfirmAyurvedaHistory_Success() throws Exception {
        Patient patientA = createPatientForUser(patientUserA, "PID-AYUR-002");
        UUID sessionId = createSessionForPatient(patientA, tokenPatientA);
        acceptConsent(sessionId, tokenPatientA);

        // Save draft first
        mockMvc.perform(post("/api/intake/sessions/" + sessionId + "/ayurveda")
                        .header("Authorization", "Bearer " + tokenPatientA)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createSampleAyurvedaRequest())))
                .andExpect(status().isOk());

        // Patient confirms
        mockMvc.perform(post("/api/intake/sessions/" + sessionId + "/ayurveda/confirm")
                        .header("Authorization", "Bearer " + tokenPatientA))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.status").value("PATIENT_CONFIRMED"))
                .andExpect(jsonPath("$.data.patientConfirmed").value(true))
                .andExpect(jsonPath("$.data.confirmedAt").isNotEmpty());

        AyurvedaHistory history = ayurvedaHistoryRepository.findBySessionId(sessionId).orElseThrow();
        assertThat(history.getStatus()).isEqualTo(AyurvedaHistoryStatus.PATIENT_CONFIRMED);
        assertThat(history.isPatientConfirmed()).isTrue();
        assertThat(history.getConfirmedAt()).isNotNull();
    }

    @Test
    @DisplayName("Case 3: Question retrieval with deterministic branching for Ayurveda opt-in")
    void testAyurvedaQuestionRetrieval_AndBranching() throws Exception {
        Patient patientA = createPatientForUser(patientUserA, "PID-AYUR-003");
        UUID sessionId = createSessionForPatient(patientA, tokenPatientA);
        acceptConsent(sessionId, tokenPatientA);

        // Initial questions: Q_AYURVEDA_OPT_IN should be present, dependent ones should NOT be present initially
        MvcResult res1 = mockMvc.perform(get("/api/intake/sessions/" + sessionId + "/questions")
                        .header("Authorization", "Bearer " + tokenPatientA))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode initialQuestions = objectMapper.readTree(res1.getResponse().getContentAsString()).path("data");
        boolean hasOptIn = false;
        boolean hasPrakritiQuestion = false;
        Question optInQuestion = null;

        for (JsonNode q : initialQuestions) {
            String code = q.path("questionCode").asText();
            if ("Q_AYURVEDA_OPT_IN".equals(code)) {
                hasOptIn = true;
                optInQuestion = questionRepository.findByQuestionCode("Q_AYURVEDA_OPT_IN").orElseThrow();
            }
            if ("Q_PRAKRITI_BODY_FRAME".equals(code)) {
                hasPrakritiQuestion = true;
            }
        }

        assertThat(hasOptIn).isTrue();
        assertThat(hasPrakritiQuestion).isFalse();

        // Answer Q_AYURVEDA_OPT_IN with YES
        AnswerRequest answerReq = new AnswerRequest(optInQuestion.getId(), "YES", "TOUCH");
        mockMvc.perform(post("/api/intake/sessions/" + sessionId + "/answers")
                        .header("Authorization", "Bearer " + tokenPatientA)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(answerReq)))
                .andExpect(status().isCreated());

        // Now Q_PRAKRITI_BODY_FRAME, Q_NIDANA_DIET, Q_PARIKSHA_BOWEL should appear
        MvcResult res2 = mockMvc.perform(get("/api/intake/sessions/" + sessionId + "/questions")
                        .header("Authorization", "Bearer " + tokenPatientA))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode branchedQuestions = objectMapper.readTree(res2.getResponse().getContentAsString()).path("data");
        boolean hasBranchedPrakriti = false;
        for (JsonNode q : branchedQuestions) {
            if ("Q_PRAKRITI_BODY_FRAME".equals(q.path("questionCode").asText())) {
                hasBranchedPrakriti = true;
                break;
            }
        }
        assertThat(hasBranchedPrakriti).isTrue();
    }

    @Test
    @DisplayName("Case 4 & 5: Modifying after confirmation resets status to DRAFT and patientConfirmed = false")
    void testModifyingAfterConfirmation_RevertsToDraft() throws Exception {
        Patient patientA = createPatientForUser(patientUserA, "PID-AYUR-004");
        UUID sessionId = createSessionForPatient(patientA, tokenPatientA);
        acceptConsent(sessionId, tokenPatientA);

        // Record and confirm
        mockMvc.perform(post("/api/intake/sessions/" + sessionId + "/ayurveda")
                        .header("Authorization", "Bearer " + tokenPatientA)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createSampleAyurvedaRequest())))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/intake/sessions/" + sessionId + "/ayurveda/confirm")
                        .header("Authorization", "Bearer " + tokenPatientA))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("PATIENT_CONFIRMED"));

        // Now modify with an update
        AyurvedaHistoryRequest updateReq = createSampleAyurvedaRequest();
        updateReq.setNotes("Updated complaint: digestion has improved slightly.");

        mockMvc.perform(post("/api/intake/sessions/" + sessionId + "/ayurveda")
                        .header("Authorization", "Bearer " + tokenPatientA)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("DRAFT"))
                .andExpect(jsonPath("$.data.patientConfirmed").value(false))
                .andExpect(jsonPath("$.data.confirmedAt").isEmpty())
                .andExpect(jsonPath("$.data.notes").value("Updated complaint: digestion has improved slightly."));
    }

    @Test
    @DisplayName("Case 6: Terminology placeholders initialized with UNRESOLVED status and no fake codes")
    void testTerminologyPlaceholders_UnresolvedMappingAndNoFakeCodes() throws Exception {
        Patient patientA = createPatientForUser(patientUserA, "PID-AYUR-006");
        UUID sessionId = createSessionForPatient(patientA, tokenPatientA);
        acceptConsent(sessionId, tokenPatientA);

        mockMvc.perform(post("/api/intake/sessions/" + sessionId + "/ayurveda")
                        .header("Authorization", "Bearer " + tokenPatientA)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createSampleAyurvedaRequest())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.terminology").isArray())
                .andExpect(jsonPath("$.data.terminology[0].mappingStatus").value("UNRESOLVED"))
                .andExpect(jsonPath("$.data.terminology[0].namasteCode").isEmpty())
                .andExpect(jsonPath("$.data.terminology[0].icd11Tm2Code").isEmpty());
    }

    @Test
    @DisplayName("Case 7: Architectural AyurvedaIntakeService interface contract")
    void testArchitecturalInterface_AyurvedaIntakeService() throws Exception {
        Patient patientA = createPatientForUser(patientUserA, "PID-AYUR-007");
        UUID sessionId = createSessionForPatient(patientA, tokenPatientA);

        Map<String, Object> data = new HashMap<>();
        data.put("notes", "Direct architectural intake service invocation");
        data.put("prakriti", Map.of("bodyFrame", "MEDIUM", "weatherPreference", "DISLIKES_HEAT"));

        ayurvedaIntakeService.saveAyurvedaIntake(sessionId, data);

        Map<String, Object> retrieved = ayurvedaIntakeService.getAyurvedaIntake(sessionId);
        assertThat(retrieved).isNotNull();
        assertThat(retrieved.get("notes")).isEqualTo("Direct architectural intake service invocation");
        assertThat(retrieved.get("status")).isEqualTo("DRAFT");
        assertThat(retrieved.get("patientConfirmed")).isEqualTo(false);
    }

    @Test
    @DisplayName("Case 8 & 9: Authorization - Patient owns session, Doctor can view any session")
    void testAuthorization_PatientAndDoctorAccess() throws Exception {
        Patient patientA = createPatientForUser(patientUserA, "PID-AYUR-008");
        UUID sessionId = createSessionForPatient(patientA, tokenPatientA);
        acceptConsent(sessionId, tokenPatientA);

        // Patient creates
        mockMvc.perform(post("/api/intake/sessions/" + sessionId + "/ayurveda")
                        .header("Authorization", "Bearer " + tokenPatientA)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createSampleAyurvedaRequest())))
                .andExpect(status().isOk());

        // Doctor retrieves successfully
        mockMvc.perform(get("/api/intake/sessions/" + sessionId + "/ayurveda")
                        .header("Authorization", "Bearer " + tokenDoctor))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.prakriti.bodyFrame").value("SLENDER"));
    }

    @Test
    @DisplayName("Case 10: Authorization - Cross-patient access returns 403 Forbidden")
    void testAuthorization_CrossPatientAccessForbidden() throws Exception {
        Patient patientA = createPatientForUser(patientUserA, "PID-AYUR-010A");
        Patient patientB = createPatientForUser(patientUserB, "PID-AYUR-010B");
        UUID sessionA = createSessionForPatient(patientA, tokenPatientA);
        acceptConsent(sessionA, tokenPatientA);

        // Patient B attempts to access Patient A's session
        mockMvc.perform(get("/api/intake/sessions/" + sessionA + "/ayurveda")
                        .header("Authorization", "Bearer " + tokenPatientB))
                .andExpect(status().isForbidden());

        // Patient B attempts to write to Patient A's session
        mockMvc.perform(post("/api/intake/sessions/" + sessionA + "/ayurveda")
                        .header("Authorization", "Bearer " + tokenPatientB)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createSampleAyurvedaRequest())))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Case 11: Unauthenticated access returns 401 Unauthorized")
    void testUnauthenticatedAccess_Returns401() throws Exception {
        UUID randomSessionId = UUID.randomUUID();
        mockMvc.perform(get("/api/intake/sessions/" + randomSessionId + "/ayurveda"))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(post("/api/intake/sessions/" + randomSessionId + "/ayurveda")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Case 12: Invalid session ID returns 404 Not Found")
    void testInvalidSessionId_Returns404() throws Exception {
        UUID nonExistent = UUID.randomUUID();
        mockMvc.perform(get("/api/intake/sessions/" + nonExistent + "/ayurveda")
                        .header("Authorization", "Bearer " + tokenDoctor))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Case 13 & 14: Consent enforcement - Missing or rejected consent returns 400 Bad Request")
    void testConsentEnforcement_MissingOrRejectedConsentBlocksRecording() throws Exception {
        Patient patientA = createPatientForUser(patientUserA, "PID-AYUR-013");
        UUID sessionId = createSessionForPatient(patientA, tokenPatientA);

        // Missing consent -> 400
        mockMvc.perform(post("/api/intake/sessions/" + sessionId + "/ayurveda")
                        .header("Authorization", "Bearer " + tokenPatientA)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createSampleAyurvedaRequest())))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsStringIgnoringCase("consent")));

        // Declined consent -> 400
        ConsentRequest rejectedConsent = new ConsentRequest(ConsentStatus.DECLINED, "GENERAL_INTAKE", "v1.0", "REF-REJ");
        mockMvc.perform(post("/api/intake/sessions/" + sessionId + "/consent")
                        .header("Authorization", "Bearer " + tokenPatientA)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(rejectedConsent)))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/intake/sessions/" + sessionId + "/ayurveda")
                        .header("Authorization", "Bearer " + tokenPatientA)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createSampleAyurvedaRequest())))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Case 15: Completed session rejected for modification with 409 Conflict")
    void testCompletedSession_RejectedForModification() throws Exception {
        Patient patientA = createPatientForUser(patientUserA, "PID-AYUR-015");
        UUID sessionId = createSessionForPatient(patientA, tokenPatientA);
        acceptConsent(sessionId, tokenPatientA);

        // Add dummy symptom so session can be completed
        SymptomRequest symptom = new SymptomRequest("Fatigue", "General", "1 week", 3);
        mockMvc.perform(post("/api/intake/sessions/" + sessionId + "/symptoms")
                        .header("Authorization", "Bearer " + tokenPatientA)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(symptom)))
                .andExpect(status().isCreated());

        // Complete the session
        mockMvc.perform(post("/api/intake/sessions/" + sessionId + "/complete")
                        .header("Authorization", "Bearer " + tokenPatientA))
                .andExpect(status().isOk());

        // Attempting to modify completed session -> 409
        mockMvc.perform(post("/api/intake/sessions/" + sessionId + "/ayurveda")
                        .header("Authorization", "Bearer " + tokenPatientA)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createSampleAyurvedaRequest())))
                .andExpect(status().isConflict());

        // Attempting to confirm completed session -> 409
        mockMvc.perform(post("/api/intake/sessions/" + sessionId + "/ayurveda/confirm")
                        .header("Authorization", "Bearer " + tokenPatientA))
                .andExpect(status().isConflict());
    }

    @Test
    @DisplayName("Case 16: Expired session rejected for modification with 409 Conflict")
    void testExpiredSession_RejectedForModification() throws Exception {
        Patient patientA = createPatientForUser(patientUserA, "PID-AYUR-016");
        UUID sessionId = createSessionForPatient(patientA, tokenPatientA);
        acceptConsent(sessionId, tokenPatientA);

        // Expire the session in database
        IntakeSession session = intakeSessionRepository.findById(sessionId).orElseThrow();
        session.setStatus(IntakeSessionStatus.EXPIRED);
        session.setExpiresAt(Instant.now().minus(1, ChronoUnit.HOURS));
        intakeSessionRepository.save(session);

        mockMvc.perform(post("/api/intake/sessions/" + sessionId + "/ayurveda")
                        .header("Authorization", "Bearer " + tokenPatientA)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createSampleAyurvedaRequest())))
                .andExpect(status().isConflict());
    }

    @Test
    @DisplayName("Case 17: Audit log verification for AYURVEDA_RECORDED and AYURVEDA_CONFIRMED")
    void testAuditLogGenerated_OnRecordAndConfirm() throws Exception {
        Patient patientA = createPatientForUser(patientUserA, "PID-AYUR-017");
        UUID sessionId = createSessionForPatient(patientA, tokenPatientA);
        acceptConsent(sessionId, tokenPatientA);

        // Record
        mockMvc.perform(post("/api/intake/sessions/" + sessionId + "/ayurveda")
                        .header("Authorization", "Bearer " + tokenPatientA)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createSampleAyurvedaRequest())))
                .andExpect(status().isOk());

        // Confirm
        mockMvc.perform(post("/api/intake/sessions/" + sessionId + "/ayurveda/confirm")
                        .header("Authorization", "Bearer " + tokenPatientA))
                .andExpect(status().isOk());

        List<AuditLog> auditLogs = auditLogRepository.findAll();
        boolean recordedFound = auditLogs.stream().anyMatch(l -> "AYURVEDA_RECORDED".equals(l.getAction()));
        boolean confirmedFound = auditLogs.stream().anyMatch(l -> "AYURVEDA_CONFIRMED".equals(l.getAction()));

        assertThat(recordedFound).isTrue();
        assertThat(confirmedFound).isTrue();
    }
}
