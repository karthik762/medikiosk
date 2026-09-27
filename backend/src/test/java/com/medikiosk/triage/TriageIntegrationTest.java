package com.medikiosk.triage;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.medikiosk.dto.intake.ChiefComplaintRequest;
import com.medikiosk.dto.intake.ConsentRequest;
import com.medikiosk.dto.intake.IntakeSessionRequest;
import com.medikiosk.dto.intake.PatientRequest;
import com.medikiosk.dto.intake.SymptomRequest;
import com.medikiosk.dto.triage.TriageEvaluateRequest;
import com.medikiosk.entity.Patient;
import com.medikiosk.entity.TriageResult;
import com.medikiosk.entity.TriageRule;
import com.medikiosk.entity.User;
import com.medikiosk.entity.enums.ConsentStatus;
import com.medikiosk.entity.enums.TriagePriority;
import com.medikiosk.entity.enums.UserRole;
import com.medikiosk.entity.enums.UserStatus;
import com.medikiosk.repository.AnswerRepository;
import com.medikiosk.repository.ConsentRepository;
import com.medikiosk.repository.IntakeSessionRepository;
import com.medikiosk.repository.PatientRepository;
import com.medikiosk.repository.SymptomRepository;
import com.medikiosk.repository.TriageResultRepository;
import com.medikiosk.repository.TriageRuleRepository;
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

import java.time.LocalDate;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class TriageIntegrationTest {

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
    private TriageResultRepository triageResultRepository;

    @Autowired
    private TriageRuleRepository triageRuleRepository;

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
    private User adminUser;
    private String tokenAdmin;

    @BeforeEach
    void setUp() {
        triageResultRepository.deleteAll();
        answerRepository.deleteAll();
        symptomRepository.deleteAll();
        consentRepository.deleteAll();
        intakeSessionRepository.deleteAll();
        patientRepository.deleteAll();
        userRepository.deleteAll();

        // Ensure baseline triage rules are active
        ensureRuleActive("TRIAGE-001");
        ensureRuleActive("TRIAGE-002");
        ensureRuleActive("TEST-001");

        patientUserA = userRepository.save(new User(
                "patientA@hospital.org",
                passwordEncoder.encode("Pass123!"),
                "Alice",
                "Patient",
                UserRole.PATIENT,
                UserStatus.ACTIVE
        ));
        tokenPatientA = jwtTokenProvider.generateToken(patientUserA);

        patientUserB = userRepository.save(new User(
                "patientB@hospital.org",
                passwordEncoder.encode("Pass123!"),
                "Bob",
                "Patient",
                UserRole.PATIENT,
                UserStatus.ACTIVE
        ));
        tokenPatientB = jwtTokenProvider.generateToken(patientUserB);

        doctorUser = userRepository.save(new User(
                "doctor@hospital.org",
                passwordEncoder.encode("DoctorPass123!"),
                "Dr. John",
                "Smith",
                UserRole.DOCTOR,
                UserStatus.ACTIVE
        ));
        tokenDoctor = jwtTokenProvider.generateToken(doctorUser);

        adminUser = userRepository.save(new User(
                "admin@hospital.org",
                passwordEncoder.encode("AdminPass123!"),
                "Admin",
                "Staff",
                UserRole.ADMIN,
                UserStatus.ACTIVE
        ));
        tokenAdmin = jwtTokenProvider.generateToken(adminUser);
    }

    private void ensureRuleActive(String ruleId) {
        Optional<TriageRule> ruleOpt = triageRuleRepository.findByRuleId(ruleId);
        if (ruleOpt.isPresent()) {
            TriageRule rule = ruleOpt.get();
            rule.setEnabled(true);
            triageRuleRepository.save(rule);
        }
    }

    private Patient createPatientForUser(User user, String identifier) throws Exception {
        PatientRequest request = new PatientRequest("John", "Doe", LocalDate.of(1990, 1, 1), "MALE", "+1234567890", user.getEmail());
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
        ConsentRequest consentRequest = new ConsentRequest(ConsentStatus.ACCEPTED, "GENERAL_INTAKE", "v1.0", "REF-CONSENT-1");
        mockMvc.perform(post("/api/intake/sessions/" + sessionId + "/consent")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(consentRequest)))
                .andExpect(status().isOk());
    }

    private void recordComplaint(UUID sessionId, String token, String text) throws Exception {
        ChiefComplaintRequest complaint = new ChiefComplaintRequest(text, "TYPED");
        mockMvc.perform(post("/api/intake/sessions/" + sessionId + "/complaint")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(complaint)))
                .andExpect(status().isOk());
    }

    private void recordSymptom(UUID sessionId, String token, String name, int severity) throws Exception {
        SymptomRequest symptom = new SymptomRequest(name, "General", "2 days", severity);
        mockMvc.perform(post("/api/intake/sessions/" + sessionId + "/symptoms")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(symptom)))
                .andExpect(status().isCreated());
    }

    // =========================================================================
    // 1. Stable/no-red-flag case returns configured STABLE priority
    // =========================================================================
    @Test
    @DisplayName("1. Stable/no-red-flag case returns configured STABLE priority")
    void testStablePriorityWhenNoRedFlagRuleTriggers() throws Exception {
        Patient patient = createPatientForUser(patientUserA, "MK-TRG-001");
        UUID sessionId = createSessionForPatient(patient, tokenPatientA);
        acceptConsent(sessionId, tokenPatientA);
        recordComplaint(sessionId, tokenPatientA, "Mild seasonal itchy eyes");
        recordSymptom(sessionId, tokenPatientA, "Itchy Eyes", 2);

        mockMvc.perform(post("/api/intake/sessions/" + sessionId + "/triage")
                        .header("Authorization", "Bearer " + tokenDoctor))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.priority").value("STABLE"))
                .andExpect(jsonPath("$.data.explanation").value("No configured red flag identified"))
                .andExpect(jsonPath("$.data.triggeredRules").isEmpty());
    }

    // =========================================================================
    // 2. Rule TRIAGE-001 explicitly triggers URGENT
    // =========================================================================
    @Test
    @DisplayName("2. Rule TRIAGE-001 explicitly triggers URGENT priority for severe symptom intensity (>= 8)")
    void testExplicitRuleTriage001TriggersUrgent() throws Exception {
        Patient patient = createPatientForUser(patientUserA, "MK-TRG-002");
        UUID sessionId = createSessionForPatient(patient, tokenPatientA);
        acceptConsent(sessionId, tokenPatientA);
        recordComplaint(sessionId, tokenPatientA, "Crushing severe chest pain radiating to left arm");
        recordSymptom(sessionId, tokenPatientA, "Chest Pain", 9);

        mockMvc.perform(post("/api/intake/sessions/" + sessionId + "/triage")
                        .header("Authorization", "Bearer " + tokenDoctor))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.priority").value("URGENT"))
                .andExpect(jsonPath("$.data.explanation").value(org.hamcrest.Matchers.containsString("Potential red flag identified")))
                .andExpect(jsonPath("$.data.triggeredRules[?(@.ruleId == 'TRIAGE-001')].priority").value("URGENT"));
    }

    // =========================================================================
    // 3. Rule TRIAGE-002 explicitly triggers PRIORITY
    // =========================================================================
    @Test
    @DisplayName("3. Rule TRIAGE-002 explicitly triggers PRIORITY priority for moderate symptom intensity (>= 5)")
    void testExplicitRuleTriage002TriggersPriority() throws Exception {
        Patient patient = createPatientForUser(patientUserA, "MK-TRG-003");
        UUID sessionId = createSessionForPatient(patient, tokenPatientA);
        acceptConsent(sessionId, tokenPatientA);
        recordComplaint(sessionId, tokenPatientA, "Moderate persistent knee ache");
        recordSymptom(sessionId, tokenPatientA, "Knee Pain", 6);

        mockMvc.perform(post("/api/intake/sessions/" + sessionId + "/triage")
                        .header("Authorization", "Bearer " + tokenDoctor))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.priority").value("PRIORITY"))
                .andExpect(jsonPath("$.data.explanation").value(org.hamcrest.Matchers.containsString("Priority clinical review recommended")))
                .andExpect(jsonPath("$.data.triggeredRules[0].ruleId").value("TRIAGE-002"));
    }

    // =========================================================================
    // 4. Rule TEST-001 explicitly triggers PRIORITY via parameters (Section 35)
    // =========================================================================
    @Test
    @DisplayName("4. Rule TEST-001 from docs/TRIAGE_RULES.md Section 35 triggers PRIORITY via testFlag")
    void testExplicitRuleTest001TriggersPriority() throws Exception {
        Patient patient = createPatientForUser(patientUserA, "MK-TRG-004");
        UUID sessionId = createSessionForPatient(patient, tokenPatientA);
        acceptConsent(sessionId, tokenPatientA);
        recordComplaint(sessionId, tokenPatientA, "Routine consultation checkup");

        Map<String, Object> params = new HashMap<>();
        params.put("testFlag", true);
        TriageEvaluateRequest evalRequest = new TriageEvaluateRequest(params);

        mockMvc.perform(post("/api/intake/sessions/" + sessionId + "/triage")
                        .header("Authorization", "Bearer " + tokenDoctor)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(evalRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.priority").value("PRIORITY"))
                .andExpect(jsonPath("$.data.triggeredRules[0].ruleId").value("TEST-001"))
                .andExpect(jsonPath("$.data.triggeredRules[0].reason").value("Test flag condition matched"));
    }

    // =========================================================================
    // 5. Non-matching rule does not trigger
    // =========================================================================
    @Test
    @DisplayName("5. Non-matching rule does not trigger when thresholds are not met")
    void testNonMatchingRuleDoesNotTrigger() throws Exception {
        Patient patient = createPatientForUser(patientUserA, "MK-TRG-005");
        UUID sessionId = createSessionForPatient(patient, tokenPatientA);
        acceptConsent(sessionId, tokenPatientA);
        recordComplaint(sessionId, tokenPatientA, "Mild sore throat");
        recordSymptom(sessionId, tokenPatientA, "Sore Throat", 3);

        MvcResult result = mockMvc.perform(post("/api/intake/sessions/" + sessionId + "/triage")
                        .header("Authorization", "Bearer " + tokenDoctor))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode root = objectMapper.readTree(result.getResponse().getContentAsString());
        assertThat(root.path("data").path("priority").asText()).isEqualTo("STABLE");
        assertThat(root.path("data").path("triggeredRules")).isEmpty();
    }

    // =========================================================================
    // 6. Multiple matching rules preserve all triggered rule IDs
    // =========================================================================
    @Test
    @DisplayName("6. Multiple matching rules preserve all triggered rule IDs in full audit trace")
    void testMultipleMatchingRulesPreservesAllTriggeredRuleIds() throws Exception {
        Patient patient = createPatientForUser(patientUserA, "MK-TRG-006");
        UUID sessionId = createSessionForPatient(patient, tokenPatientA);
        acceptConsent(sessionId, tokenPatientA);
        recordComplaint(sessionId, tokenPatientA, "Severe chest pressure");
        recordSymptom(sessionId, tokenPatientA, "Chest Pain", 9); // Matches TRIAGE-001 (>=8) and TRIAGE-002 (>=5)

        MvcResult result = mockMvc.perform(post("/api/intake/sessions/" + sessionId + "/triage")
                        .header("Authorization", "Bearer " + tokenDoctor))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode root = objectMapper.readTree(result.getResponse().getContentAsString());
        JsonNode rulesNode = root.path("data").path("triggeredRules");
        assertThat(rulesNode.isArray()).isTrue();
        assertThat(rulesNode.size()).isGreaterThanOrEqualTo(2);

        List<String> ruleIds = rulesNode.findValuesAsText("ruleId");
        assertThat(ruleIds).contains("TRIAGE-001", "TRIAGE-002");
    }

    // =========================================================================
    // 7. Highest priority wins when multiple rules trigger
    // =========================================================================
    @Test
    @DisplayName("7. Highest priority wins (URGENT > PRIORITY > STABLE) when multiple rules trigger")
    void testHighestPriorityWinsWhenMultipleRulesTrigger() throws Exception {
        Patient patient = createPatientForUser(patientUserA, "MK-TRG-007");
        UUID sessionId = createSessionForPatient(patient, tokenPatientA);
        acceptConsent(sessionId, tokenPatientA);
        recordComplaint(sessionId, tokenPatientA, "High fever and severe breathing difficulty");
        recordSymptom(sessionId, tokenPatientA, "Dyspnea", 9); // Triggers both URGENT and PRIORITY rules

        mockMvc.perform(post("/api/intake/sessions/" + sessionId + "/triage")
                        .header("Authorization", "Bearer " + tokenDoctor))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.priority").value("URGENT"));
    }

    // =========================================================================
    // 8. TriageResult is persisted in the database
    // =========================================================================
    @Test
    @DisplayName("8. TriageResult entity is correctly persisted in database with JSONB rules trace")
    void testTriageResultIsPersistedInDatabase() throws Exception {
        Patient patient = createPatientForUser(patientUserA, "MK-TRG-008");
        UUID sessionId = createSessionForPatient(patient, tokenPatientA);
        acceptConsent(sessionId, tokenPatientA);
        recordComplaint(sessionId, tokenPatientA, "Acute appendicitis suspicion");
        recordSymptom(sessionId, tokenPatientA, "Abdominal Pain", 8);

        mockMvc.perform(post("/api/intake/sessions/" + sessionId + "/triage")
                        .header("Authorization", "Bearer " + tokenDoctor))
                .andExpect(status().isOk());

        Optional<TriageResult> resultOpt = triageResultRepository.findBySessionId(sessionId);
        assertThat(resultOpt).isPresent();
        TriageResult saved = resultOpt.get();
        assertThat(saved.getPriority()).isEqualTo(TriagePriority.URGENT);
        assertThat(saved.getTriggeredRules()).contains("TRIAGE-001");
        assertThat(saved.getCreatedAt()).isNotNull();
    }

    // =========================================================================
    // 9. Re-running triage does not create uncontrolled duplicate results (Idempotent)
    // =========================================================================
    @Test
    @DisplayName("9. Re-running triage is idempotent and updates existing TriageResult without duplicates")
    void testRerunningTriageIsIdempotentWithoutDuplicates() throws Exception {
        Patient patient = createPatientForUser(patientUserA, "MK-TRG-009");
        UUID sessionId = createSessionForPatient(patient, tokenPatientA);
        acceptConsent(sessionId, tokenPatientA);
        recordComplaint(sessionId, tokenPatientA, "Headache");
        recordSymptom(sessionId, tokenPatientA, "Headache", 6); // Priority

        // First evaluation: PRIORITY
        mockMvc.perform(post("/api/intake/sessions/" + sessionId + "/triage")
                        .header("Authorization", "Bearer " + tokenDoctor))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.priority").value("PRIORITY"));

        long countAfterFirst = triageResultRepository.count();

        // Second evaluation: add severe symptom and re-run triage
        recordSymptom(sessionId, tokenPatientA, "Sudden Worsening Pain", 10);
        mockMvc.perform(post("/api/intake/sessions/" + sessionId + "/triage")
                        .header("Authorization", "Bearer " + tokenDoctor))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.priority").value("URGENT"));

        long countAfterSecond = triageResultRepository.count();
        assertThat(countAfterSecond).isEqualTo(countAfterFirst); // No duplicate rows created

        Optional<TriageResult> updatedOpt = triageResultRepository.findBySessionId(sessionId);
        assertThat(updatedOpt).isPresent();
        assertThat(updatedOpt.get().getPriority()).isEqualTo(TriagePriority.URGENT);
    }

    // =========================================================================
    // 10. Triage requires valid consent
    // =========================================================================
    @Test
    @DisplayName("10. Triage evaluation requires valid accepted consent (returns 400 Bad Request)")
    void testTriageRequiresValidConsent() throws Exception {
        Patient patient = createPatientForUser(patientUserA, "MK-TRG-010");
        UUID sessionId = createSessionForPatient(patient, tokenPatientA);
        // Do NOT accept consent

        mockMvc.perform(post("/api/intake/sessions/" + sessionId + "/triage")
                        .header("Authorization", "Bearer " + tokenPatientA))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Clinical intake cannot proceed without valid consent"));
    }

    // =========================================================================
    // 11. Revoked or declined consent blocks triage
    // =========================================================================
    @Test
    @DisplayName("11. Declined or revoked consent blocks triage evaluation (returns 400 Bad Request)")
    void testRevokedOrDeclinedConsentBlocksTriage() throws Exception {
        Patient patient = createPatientForUser(patientUserA, "MK-TRG-011");
        UUID sessionId = createSessionForPatient(patient, tokenPatientA);

        ConsentRequest declineRequest = new ConsentRequest(ConsentStatus.DECLINED, "GENERAL_INTAKE", "v1.0", null);
        mockMvc.perform(post("/api/intake/sessions/" + sessionId + "/consent")
                        .header("Authorization", "Bearer " + tokenPatientA)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(declineRequest)))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/intake/sessions/" + sessionId + "/triage")
                        .header("Authorization", "Bearer " + tokenPatientA))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Clinical intake cannot proceed without valid consent"));
    }

    // =========================================================================
    // 12. Nonexistent session returns 404 Not Found
    // =========================================================================
    @Test
    @DisplayName("12. Nonexistent session returns 404 Not Found")
    void testNonexistentSessionReturnsNotFound() throws Exception {
        UUID fakeSessionId = UUID.randomUUID();

        mockMvc.perform(post("/api/intake/sessions/" + fakeSessionId + "/triage")
                        .header("Authorization", "Bearer " + tokenDoctor))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false));
    }

    // =========================================================================
    // 13. Patient cannot triage another patient's session (403 Forbidden)
    // =========================================================================
    @Test
    @DisplayName("13. Patient cannot triage another patient's session (returns 403 Forbidden)")
    void testPatientCannotTriageAnotherPatientSession() throws Exception {
        Patient patientA = createPatientForUser(patientUserA, "MK-TRG-A13");
        UUID sessionAId = createSessionForPatient(patientA, tokenPatientA);
        acceptConsent(sessionAId, tokenPatientA);
        recordComplaint(sessionAId, tokenPatientA, "Headache");

        // Patient B attempts to evaluate Patient A's triage
        mockMvc.perform(post("/api/intake/sessions/" + sessionAId + "/triage")
                        .header("Authorization", "Bearer " + tokenPatientB))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Access denied: You do not own this intake session"));
    }

    // =========================================================================
    // 14. Doctor can access appropriate triage information
    // =========================================================================
    @Test
    @DisplayName("14. Doctor can access complete triage screening result and audit trace")
    void testDoctorCanAccessTriageInformation() throws Exception {
        Patient patient = createPatientForUser(patientUserA, "MK-TRG-014");
        UUID sessionId = createSessionForPatient(patient, tokenPatientA);
        acceptConsent(sessionId, tokenPatientA);
        recordComplaint(sessionId, tokenPatientA, "Chest tightness");
        recordSymptom(sessionId, tokenPatientA, "Chest Tightness", 9);

        // Evaluate as doctor
        mockMvc.perform(post("/api/intake/sessions/" + sessionId + "/triage")
                        .header("Authorization", "Bearer " + tokenDoctor))
                .andExpect(status().isOk());

        // GET triage result as doctor
        mockMvc.perform(get("/api/intake/sessions/" + sessionId + "/triage")
                        .header("Authorization", "Bearer " + tokenDoctor))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.priority").value("URGENT"))
                .andExpect(jsonPath("$.data.triggeredRules[0].ruleId").value("TRIAGE-001"))
                .andExpect(jsonPath("$.data.reasons").isNotEmpty());
    }

    // =========================================================================
    // 15. Admin can access appropriate triage information
    // =========================================================================
    @Test
    @DisplayName("15. Admin can access complete triage screening result and audit trace")
    void testAdminCanAccessTriageInformation() throws Exception {
        Patient patient = createPatientForUser(patientUserA, "MK-TRG-015");
        UUID sessionId = createSessionForPatient(patient, tokenPatientA);
        acceptConsent(sessionId, tokenPatientA);
        recordComplaint(sessionId, tokenPatientA, "Fever");
        recordSymptom(sessionId, tokenPatientA, "Fever", 6);

        // Evaluate triage
        mockMvc.perform(post("/api/intake/sessions/" + sessionId + "/triage")
                        .header("Authorization", "Bearer " + tokenAdmin))
                .andExpect(status().isOk());

        // GET triage result as admin
        mockMvc.perform(get("/api/intake/sessions/" + sessionId + "/triage")
                        .header("Authorization", "Bearer " + tokenAdmin))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.priority").value("PRIORITY"));
    }

    // =========================================================================
    // 16. Unauthenticated triage request is rejected
    // =========================================================================
    @Test
    @DisplayName("16. Unauthenticated triage request is rejected (returns 401 Unauthorized)")
    void testUnauthenticatedTriageRequestReturns401() throws Exception {
        UUID fakeSessionId = UUID.randomUUID();

        mockMvc.perform(post("/api/intake/sessions/" + fakeSessionId + "/triage"))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(get("/api/intake/sessions/" + fakeSessionId + "/triage"))
                .andExpect(status().isUnauthorized());
    }

    // =========================================================================
    // 17. Completed-session behavior follows specification
    // =========================================================================
    @Test
    @DisplayName("17. Completed-session behavior: Re-evaluating completed session returns 409 Conflict, GET still works")
    void testCompletedSessionTriageBehavior() throws Exception {
        Patient patient = createPatientForUser(patientUserA, "MK-TRG-017");
        UUID sessionId = createSessionForPatient(patient, tokenPatientA);
        acceptConsent(sessionId, tokenPatientA);
        recordComplaint(sessionId, tokenPatientA, "General fatigue");
        recordSymptom(sessionId, tokenPatientA, "Fatigue", 2);

        // Evaluate triage during active intake
        mockMvc.perform(post("/api/intake/sessions/" + sessionId + "/triage")
                        .header("Authorization", "Bearer " + tokenPatientA))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.priority").value("STABLE"));

        // Complete intake session
        mockMvc.perform(post("/api/intake/sessions/" + sessionId + "/complete")
                        .header("Authorization", "Bearer " + tokenPatientA))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("COMPLETED"));

        // Attempting to modify / re-evaluate completed session returns 409 Conflict
        mockMvc.perform(post("/api/intake/sessions/" + sessionId + "/triage")
                        .header("Authorization", "Bearer " + tokenPatientA))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Intake session is already completed and cannot be modified"));

        // Retrieving stored triage result on completed session via GET still succeeds
        mockMvc.perform(get("/api/intake/sessions/" + sessionId + "/triage")
                        .header("Authorization", "Bearer " + tokenDoctor))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.priority").value("STABLE"));
    }

    // =========================================================================
    // 18. Triage output contains NO diagnosis
    // =========================================================================
    @Test
    @DisplayName("18. Triage output strictly adheres to safety boundary: NO diagnosis or disease claims")
    void testTriageOutputContainsNoDiagnosisOrDisease() throws Exception {
        Patient patient = createPatientForUser(patientUserA, "MK-TRG-018");
        UUID sessionId = createSessionForPatient(patient, tokenPatientA);
        acceptConsent(sessionId, tokenPatientA);
        recordComplaint(sessionId, tokenPatientA, "Chest pain");
        recordSymptom(sessionId, tokenPatientA, "Chest Pain", 9);

        MvcResult result = mockMvc.perform(post("/api/intake/sessions/" + sessionId + "/triage")
                        .header("Authorization", "Bearer " + tokenDoctor))
                .andExpect(status().isOk())
                .andReturn();

        String content = result.getResponse().getContentAsString().toLowerCase();
        assertThat(content).doesNotContain("diagnosis");
        assertThat(content).doesNotContain("you have");
        assertThat(content).doesNotContain("patient has disease");
        assertThat(content).doesNotContain("myocardial infarction");
        assertThat(content).doesNotContain("heart attack");
    }

    // =========================================================================
    // 19. Triage output contains NO treatment recommendation
    // =========================================================================
    @Test
    @DisplayName("19. Triage output strictly contains NO treatment recommendation or prescription")
    void testTriageOutputContainsNoTreatmentRecommendation() throws Exception {
        Patient patient = createPatientForUser(patientUserA, "MK-TRG-019");
        UUID sessionId = createSessionForPatient(patient, tokenPatientA);
        acceptConsent(sessionId, tokenPatientA);
        recordComplaint(sessionId, tokenPatientA, "Severe pain");
        recordSymptom(sessionId, tokenPatientA, "Pain", 9);

        MvcResult result = mockMvc.perform(post("/api/intake/sessions/" + sessionId + "/triage")
                        .header("Authorization", "Bearer " + tokenDoctor))
                .andExpect(status().isOk())
                .andReturn();

        String content = result.getResponse().getContentAsString().toLowerCase();
        assertThat(content).doesNotContain("prescribe");
        assertThat(content).doesNotContain("take medication");
        assertThat(content).doesNotContain("take aspirin");
        assertThat(content).doesNotContain("treatment recommended");
    }

    // =========================================================================
    // 20. Disabled rule does not participate in triage
    // =========================================================================
    @Test
    @DisplayName("20. Disabled rule (enabled=false) does not participate in triage evaluation")
    void testDisabledRuleDoesNotParticipateInTriage() throws Exception {
        Patient patient = createPatientForUser(patientUserA, "MK-TRG-020");
        UUID sessionId = createSessionForPatient(patient, tokenPatientA);
        acceptConsent(sessionId, tokenPatientA);
        recordComplaint(sessionId, tokenPatientA, "Severe pain");
        recordSymptom(sessionId, tokenPatientA, "Pain", 9); // Usually matches TRIAGE-001 (URGENT) and TRIAGE-002 (PRIORITY)

        // Temporarily disable TRIAGE-001
        TriageRule urgentRule = triageRuleRepository.findByRuleId("TRIAGE-001").orElseThrow();
        urgentRule.setEnabled(false);
        triageRuleRepository.save(urgentRule);

        try {
            MvcResult result = mockMvc.perform(post("/api/intake/sessions/" + sessionId + "/triage")
                            .header("Authorization", "Bearer " + tokenDoctor))
                    .andExpect(status().isOk())
                    .andReturn();

            JsonNode root = objectMapper.readTree(result.getResponse().getContentAsString());
            // Without TRIAGE-001, TRIAGE-002 wins and priority is PRIORITY (not URGENT)
            assertThat(root.path("data").path("priority").asText()).isEqualTo("PRIORITY");
            List<String> ruleIds = root.path("data").path("triggeredRules").findValuesAsText("ruleId");
            assertThat(ruleIds).doesNotContain("TRIAGE-001");
            assertThat(ruleIds).contains("TRIAGE-002");
        } finally {
            // Restore rule state
            urgentRule.setEnabled(true);
            triageRuleRepository.save(urgentRule);
        }
    }

    // =========================================================================
    // 21. Supported alias POST /api/sessions/{sessionId}/triage
    // =========================================================================
    @Test
    @DisplayName("21. Supported alias path /api/sessions/{sessionId}/triage executes correctly")
    void testSupportedAliasPathExecutesCorrectly() throws Exception {
        Patient patient = createPatientForUser(patientUserA, "MK-TRG-021");
        UUID sessionId = createSessionForPatient(patient, tokenPatientA);
        acceptConsent(sessionId, tokenPatientA);
        recordComplaint(sessionId, tokenPatientA, "Mild headache");
        recordSymptom(sessionId, tokenPatientA, "Headache", 2);

        mockMvc.perform(post("/api/sessions/" + sessionId + "/triage")
                        .header("Authorization", "Bearer " + tokenDoctor))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.priority").value("STABLE"));

        mockMvc.perform(get("/api/sessions/" + sessionId + "/triage")
                        .header("Authorization", "Bearer " + tokenDoctor))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.priority").value("STABLE"));
    }
}
