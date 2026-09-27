package com.medikiosk.intake;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.medikiosk.dto.intake.AllergyRequest;
import com.medikiosk.dto.intake.AnswerRequest;
import com.medikiosk.dto.intake.ChiefComplaintRequest;
import com.medikiosk.dto.intake.ConsentRequest;
import com.medikiosk.dto.intake.IntakeSessionRequest;
import com.medikiosk.dto.intake.MedicalHistoryRequest;
import com.medikiosk.dto.intake.MedicationRequest;
import com.medikiosk.dto.intake.PatientRequest;
import com.medikiosk.dto.intake.SymptomRequest;
import com.medikiosk.entity.Patient;
import com.medikiosk.entity.Question;
import com.medikiosk.entity.User;
import com.medikiosk.entity.enums.ConsentStatus;
import com.medikiosk.entity.enums.IntakeSessionStatus;
import com.medikiosk.entity.enums.UserRole;
import com.medikiosk.entity.enums.UserStatus;
import com.medikiosk.repository.ConsentRepository;
import com.medikiosk.repository.IntakeSessionRepository;
import com.medikiosk.repository.PatientRepository;
import com.medikiosk.repository.QuestionRepository;
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
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class PatientIntakeIntegrationTest {

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
    private QuestionRepository questionRepository;

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
        intakeSessionRepository.deleteAll();
        patientRepository.deleteAll();
        userRepository.deleteAll();

        patientUserA = userRepository.save(new User(
                "patientA@medikiosk.org",
                passwordEncoder.encode("Pass123!"),
                "Alice",
                "Patient",
                UserRole.PATIENT,
                UserStatus.ACTIVE
        ));
        tokenPatientA = jwtTokenProvider.generateToken(patientUserA);

        patientUserB = userRepository.save(new User(
                "patientB@medikiosk.org",
                passwordEncoder.encode("Pass123!"),
                "Bob",
                "Patient",
                UserRole.PATIENT,
                UserStatus.ACTIVE
        ));
        tokenPatientB = jwtTokenProvider.generateToken(patientUserB);

        doctorUser = userRepository.save(new User(
                "doctor@medikiosk.org",
                passwordEncoder.encode("DoctorPass123!"),
                "David",
                "Doctor",
                UserRole.DOCTOR,
                UserStatus.ACTIVE
        ));
        tokenDoctor = jwtTokenProvider.generateToken(doctorUser);
    }

    @Test
    @DisplayName("1. Patient creation succeeds with valid data")
    void testPatientCreationSucceeds() throws Exception {
        PatientRequest request = new PatientRequest("John", "Smith", LocalDate.of(1985, 4, 12), "MALE", "+919876543210", "john@smith.org");
        request.setPatientIdentifier("MK-TEST-001");

        mockMvc.perform(post("/api/patients")
                        .header("Authorization", "Bearer " + tokenPatientA)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.patientIdentifier").value("MK-TEST-001"))
                .andExpect(jsonPath("$.data.firstName").value("John"))
                .andExpect(jsonPath("$.data.userId").value(patientUserA.getId().toString()));
    }

    @Test
    @DisplayName("2. Duplicate patient identifier is rejected with 409 Conflict")
    void testDuplicatePatientIdentifierRejected() throws Exception {
        PatientRequest req1 = new PatientRequest("John", "Smith", LocalDate.of(1985, 4, 12), "MALE", "+919876543210", "john@smith.org");
        req1.setPatientIdentifier("MK-DUPLICATE-ID");

        mockMvc.perform(post("/api/patients")
                        .header("Authorization", "Bearer " + tokenPatientA)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req1)))
                .andExpect(status().isCreated());

        PatientRequest req2 = new PatientRequest("Another", "User", LocalDate.of(1990, 1, 1), "FEMALE", "+919876543211", "another@smith.org");
        req2.setPatientIdentifier("MK-DUPLICATE-ID");

        mockMvc.perform(post("/api/patients")
                        .header("Authorization", "Bearer " + tokenPatientA)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req2)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @DisplayName("3. Intake session can be created for a patient")
    void testIntakeSessionCreationSucceeds() throws Exception {
        Patient patient = createPatientForUser(patientUserA, "MK-PAT-003");

        IntakeSessionRequest request = new IntakeSessionRequest(patient.getId(), "en");

        mockMvc.perform(post("/api/intake/sessions")
                        .header("Authorization", "Bearer " + tokenPatientA)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.status").value("CREATED"))
                .andExpect(jsonPath("$.data.patientId").value(patient.getId().toString()));
    }

    @Test
    @DisplayName("4. Intake session cannot be created for a nonexistent patient (404 Not Found)")
    void testIntakeSessionForNonexistentPatientFails() throws Exception {
        IntakeSessionRequest request = new IntakeSessionRequest(UUID.randomUUID(), "en");

        mockMvc.perform(post("/api/intake/sessions")
                        .header("Authorization", "Bearer " + tokenPatientA)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @DisplayName("5. Consent can be recorded and advances session to IN_PROGRESS")
    void testConsentCanBeRecorded() throws Exception {
        Patient patient = createPatientForUser(patientUserA, "MK-PAT-005");
        UUID sessionId = createSessionForPatient(patient, tokenPatientA);

        ConsentRequest consentRequest = new ConsentRequest(ConsentStatus.ACCEPTED, "GENERAL_INTAKE", "v1.0", "REF-123");

        mockMvc.perform(post("/api/intake/sessions/" + sessionId + "/consent")
                        .header("Authorization", "Bearer " + tokenPatientA)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(consentRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.consentStatus").value("ACCEPTED"));

        mockMvc.perform(get("/api/intake/sessions/" + sessionId)
                        .header("Authorization", "Bearer " + tokenPatientA))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("IN_PROGRESS"));
    }

    @Test
    @DisplayName("6. Clinical intake cannot proceed without valid consent (400 Bad Request)")
    void testClinicalIntakeBlockedWithoutConsent() throws Exception {
        Patient patient = createPatientForUser(patientUserA, "MK-PAT-006");
        UUID sessionId = createSessionForPatient(patient, tokenPatientA);

        ChiefComplaintRequest complaintRequest = new ChiefComplaintRequest("Severe headache for 2 days", "TEXT");

        // Without consent recorded, expect 400 Bad Request
        mockMvc.perform(post("/api/intake/sessions/" + sessionId + "/complaint")
                        .header("Authorization", "Bearer " + tokenPatientA)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(complaintRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Clinical intake cannot proceed without valid consent"));

        // If consent is DECLINED, still blocked
        ConsentRequest declineRequest = new ConsentRequest(ConsentStatus.DECLINED, "GENERAL_INTAKE", "v1.0", null);
        mockMvc.perform(post("/api/intake/sessions/" + sessionId + "/consent")
                        .header("Authorization", "Bearer " + tokenPatientA)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(declineRequest)))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/intake/sessions/" + sessionId + "/complaint")
                        .header("Authorization", "Bearer " + tokenPatientA)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(complaintRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Clinical intake cannot proceed without valid consent"));
    }

    @Test
    @DisplayName("7. Chief complaint can be stored")
    void testChiefComplaintStored() throws Exception {
        Patient patient = createPatientForUser(patientUserA, "MK-PAT-007");
        UUID sessionId = createSessionForPatient(patient, tokenPatientA);
        acceptConsent(sessionId, tokenPatientA);

        ChiefComplaintRequest complaint = new ChiefComplaintRequest("Fever and cough for three days", "VOICE");
        complaint.setOriginalTranscription("fever and cough for three days");

        mockMvc.perform(post("/api/intake/sessions/" + sessionId + "/complaint")
                        .header("Authorization", "Bearer " + tokenPatientA)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(complaint)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.complaintText").value("Fever and cough for three days"))
                .andExpect(jsonPath("$.data.inputMethod").value("VOICE"));
    }

    @Test
    @DisplayName("8. Symptoms can be stored")
    void testSymptomsStored() throws Exception {
        Patient patient = createPatientForUser(patientUserA, "MK-PAT-008");
        UUID sessionId = createSessionForPatient(patient, tokenPatientA);
        acceptConsent(sessionId, tokenPatientA);

        SymptomRequest symptom = new SymptomRequest("Abdominal Pain", "Lower abdomen", "3 days", 7);
        symptom.setFrequency("Intermittent");
        symptom.setOnset("Sudden");

        mockMvc.perform(post("/api/intake/sessions/" + sessionId + "/symptoms")
                        .header("Authorization", "Bearer " + tokenPatientA)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(symptom)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.name").value("Abdominal Pain"))
                .andExpect(jsonPath("$.data.severity").value(7))
                .andExpect(jsonPath("$.data.location").value("Lower abdomen"));
    }

    @Test
    @DisplayName("9. Medical history can be stored")
    void testMedicalHistoryStored() throws Exception {
        Patient patient = createPatientForUser(patientUserA, "MK-PAT-009");
        UUID sessionId = createSessionForPatient(patient, tokenPatientA);
        acceptConsent(sessionId, tokenPatientA);

        MedicalHistoryRequest history = new MedicalHistoryRequest("PAST_HISTORY", "Diagnosed with Type 2 Diabetes in 2018", "PATIENT");

        mockMvc.perform(post("/api/intake/sessions/" + sessionId + "/medical-history")
                        .header("Authorization", "Bearer " + tokenPatientA)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(history)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.category").value("PAST_HISTORY"))
                .andExpect(jsonPath("$.data.description").value("Diagnosed with Type 2 Diabetes in 2018"));
    }

    @Test
    @DisplayName("10. Medication can be stored")
    void testMedicationStored() throws Exception {
        Patient patient = createPatientForUser(patientUserA, "MK-PAT-010");
        UUID sessionId = createSessionForPatient(patient, tokenPatientA);
        acceptConsent(sessionId, tokenPatientA);

        MedicationRequest medication = new MedicationRequest("Metformin", "500mg", "Twice daily", "Ongoing");

        mockMvc.perform(post("/api/intake/sessions/" + sessionId + "/medications")
                        .header("Authorization", "Bearer " + tokenPatientA)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(medication)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.medicationName").value("Metformin"))
                .andExpect(jsonPath("$.data.dosage").value("500mg"));
    }

    @Test
    @DisplayName("11. Allergy can be stored")
    void testAllergyStored() throws Exception {
        Patient patient = createPatientForUser(patientUserA, "MK-PAT-011");
        UUID sessionId = createSessionForPatient(patient, tokenPatientA);
        acceptConsent(sessionId, tokenPatientA);

        AllergyRequest allergy = new AllergyRequest("Penicillin", "Hives and facial swelling", "SEVERE");

        mockMvc.perform(post("/api/intake/sessions/" + sessionId + "/allergies")
                        .header("Authorization", "Bearer " + tokenPatientA)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(allergy)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.allergen").value("Penicillin"))
                .andExpect(jsonPath("$.data.severity").value("SEVERE"));
    }

    @Test
    @DisplayName("12. Questions can be retrieved and adaptive conditions are evaluated")
    void testQuestionsRetrievedWithAdaptiveConditions() throws Exception {
        Patient patient = createPatientForUser(patientUserA, "MK-PAT-012");
        UUID sessionId = createSessionForPatient(patient, tokenPatientA);
        acceptConsent(sessionId, tokenPatientA);

        // Before answering Q_PAST_CONDITIONS, Q_PAST_CONDITIONS_DETAIL should NOT appear
        MvcResult resultBefore = mockMvc.perform(get("/api/intake/sessions/" + sessionId + "/questions")
                        .header("Authorization", "Bearer " + tokenPatientA))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andReturn();

        String bodyBefore = resultBefore.getResponse().getContentAsString();
        assertThat(bodyBefore).contains("Q_PAST_CONDITIONS");
        assertThat(bodyBefore).doesNotContain("Q_PAST_CONDITIONS_DETAIL");

        // Answer Q_PAST_CONDITIONS with "YES"
        Question qPast = questionRepository.findByQuestionCode("Q_PAST_CONDITIONS").orElseThrow();
        AnswerRequest ans = new AnswerRequest(qPast.getId(), "YES", "TOUCH");

        mockMvc.perform(post("/api/intake/sessions/" + sessionId + "/answers")
                        .header("Authorization", "Bearer " + tokenPatientA)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(ans)))
                .andExpect(status().isCreated());

        // Now Q_PAST_CONDITIONS_DETAIL SHOULD appear!
        MvcResult resultAfter = mockMvc.perform(get("/api/intake/sessions/" + sessionId + "/questions")
                        .header("Authorization", "Bearer " + tokenPatientA))
                .andExpect(status().isOk())
                .andReturn();

        String bodyAfter = resultAfter.getResponse().getContentAsString();
        assertThat(bodyAfter).contains("Q_PAST_CONDITIONS_DETAIL");
    }

    @Test
    @DisplayName("13. Answers can be stored against the correct question/session")
    void testAnswerStoredSuccessfully() throws Exception {
        Patient patient = createPatientForUser(patientUserA, "MK-PAT-013");
        UUID sessionId = createSessionForPatient(patient, tokenPatientA);
        acceptConsent(sessionId, tokenPatientA);

        Question question = questionRepository.findByQuestionCode("Q_CURRENT_MEDS").orElseThrow();
        AnswerRequest request = new AnswerRequest(question.getId(), "NO", "TEXT");

        mockMvc.perform(post("/api/intake/sessions/" + sessionId + "/answers")
                        .header("Authorization", "Bearer " + tokenPatientA)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.questionCode").value("Q_CURRENT_MEDS"))
                .andExpect(jsonPath("$.data.answerText").value("NO"));
    }

    @Test
    @DisplayName("14. Invalid question ID is rejected with 404 Not Found")
    void testInvalidQuestionIdRejected() throws Exception {
        Patient patient = createPatientForUser(patientUserA, "MK-PAT-014");
        UUID sessionId = createSessionForPatient(patient, tokenPatientA);
        acceptConsent(sessionId, tokenPatientA);

        AnswerRequest request = new AnswerRequest(UUID.randomUUID(), "YES", "TEXT");

        mockMvc.perform(post("/api/intake/sessions/" + sessionId + "/answers")
                        .header("Authorization", "Bearer " + tokenPatientA)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @DisplayName("15. A patient cannot access another patient's session (403 Forbidden)")
    void testPatientCannotAccessAnotherPatientsSession() throws Exception {
        Patient patientA = createPatientForUser(patientUserA, "MK-PAT-A15");
        UUID sessionAId = createSessionForPatient(patientA, tokenPatientA);

        // Patient B attempts to GET Patient A's session
        mockMvc.perform(get("/api/intake/sessions/" + sessionAId)
                        .header("Authorization", "Bearer " + tokenPatientB))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Access denied: You do not own this intake session"));
    }

    @Test
    @DisplayName("16. A patient cannot modify another patient's session (403 Forbidden)")
    void testPatientCannotModifyAnotherPatientsSession() throws Exception {
        Patient patientA = createPatientForUser(patientUserA, "MK-PAT-A16");
        UUID sessionAId = createSessionForPatient(patientA, tokenPatientA);

        ConsentRequest consent = new ConsentRequest(ConsentStatus.ACCEPTED, "GENERAL_INTAKE", "v1", null);

        // Patient B attempts to record consent on Patient A's session
        mockMvc.perform(post("/api/intake/sessions/" + sessionAId + "/consent")
                        .header("Authorization", "Bearer " + tokenPatientB)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(consent)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @DisplayName("17. Unauthenticated access to protected intake APIs is rejected (401)")
    void testUnauthenticatedAccessRejected() throws Exception {
        mockMvc.perform(post("/api/patients")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(get("/api/intake/sessions/" + UUID.randomUUID()))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("18. Doctor can access intake sessions across patients")
    void testDoctorCanAccessIntakeSessions() throws Exception {
        Patient patientA = createPatientForUser(patientUserA, "MK-PAT-A18");
        UUID sessionAId = createSessionForPatient(patientA, tokenPatientA);

        mockMvc.perform(get("/api/intake/sessions/" + sessionAId)
                        .header("Authorization", "Bearer " + tokenDoctor))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(sessionAId.toString()));
    }

    @Test
    @DisplayName("19. Session can be completed when requirements are satisfied")
    void testSessionCanBeCompleted() throws Exception {
        Patient patient = createPatientForUser(patientUserA, "MK-PAT-019");
        UUID sessionId = createSessionForPatient(patient, tokenPatientA);
        acceptConsent(sessionId, tokenPatientA);

        ChiefComplaintRequest complaint = new ChiefComplaintRequest("Fever and sore throat", "TEXT");
        mockMvc.perform(post("/api/intake/sessions/" + sessionId + "/complaint")
                        .header("Authorization", "Bearer " + tokenPatientA)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(complaint)))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/intake/sessions/" + sessionId + "/complete")
                        .header("Authorization", "Bearer " + tokenPatientA))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.status").value("COMPLETED"))
                .andExpect(jsonPath("$.data.completedAt").isNotEmpty());
    }

    @Test
    @DisplayName("20. Incomplete session without consent or clinical data cannot be completed")
    void testIncompleteSessionCannotBeCompleted() throws Exception {
        Patient patient = createPatientForUser(patientUserA, "MK-PAT-020");
        UUID sessionId = createSessionForPatient(patient, tokenPatientA);

        // Attempting to complete without consent -> 400 Bad Request
        mockMvc.perform(post("/api/intake/sessions/" + sessionId + "/complete")
                        .header("Authorization", "Bearer " + tokenPatientA))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));

        // Accept consent, but do NOT provide any complaint, symptom, or answer
        acceptConsent(sessionId, tokenPatientA);

        mockMvc.perform(post("/api/intake/sessions/" + sessionId + "/complete")
                        .header("Authorization", "Bearer " + tokenPatientA))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Cannot complete intake session: No clinical intake data has been provided"));
    }

    @Test
    @DisplayName("21. Completed session cannot be modified or re-completed")
    void testCompletedSessionCannotBeModified() throws Exception {
        Patient patient = createPatientForUser(patientUserA, "MK-PAT-021");
        UUID sessionId = createSessionForPatient(patient, tokenPatientA);
        acceptConsent(sessionId, tokenPatientA);

        ChiefComplaintRequest complaint = new ChiefComplaintRequest("Cough", "TEXT");
        mockMvc.perform(post("/api/intake/sessions/" + sessionId + "/complaint")
                        .header("Authorization", "Bearer " + tokenPatientA)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(complaint)))
                .andExpect(status().isOk());

        // Complete session
        mockMvc.perform(post("/api/intake/sessions/" + sessionId + "/complete")
                        .header("Authorization", "Bearer " + tokenPatientA))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("COMPLETED"));

        // Attempting to re-complete returns 409 Conflict
        mockMvc.perform(post("/api/intake/sessions/" + sessionId + "/complete")
                        .header("Authorization", "Bearer " + tokenPatientA))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("Session is already completed"));

        // Attempting to add symptoms after completion returns 409 Conflict
        SymptomRequest symptom = new SymptomRequest("Fever", null, "1 day", 5);
        mockMvc.perform(post("/api/intake/sessions/" + sessionId + "/symptoms")
                        .header("Authorization", "Bearer " + tokenPatientA)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(symptom)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("Intake session is already completed and cannot be modified"));
    }

    // --- Helper Methods ---

    private Patient createPatientForUser(User user, String identifier) {
        Patient patient = new Patient(identifier, user.getFirstName(), user.getLastName());
        patient.setUserId(user.getId());
        return patientRepository.save(patient);
    }

    private UUID createSessionForPatient(Patient patient, String token) throws Exception {
        IntakeSessionRequest request = new IntakeSessionRequest(patient.getId(), "en");
        MvcResult result = mockMvc.perform(post("/api/intake/sessions")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn();

        JsonNode node = objectMapper.readTree(result.getResponse().getContentAsString());
        return UUID.fromString(node.get("data").get("id").asText());
    }

    private void acceptConsent(UUID sessionId, String token) throws Exception {
        ConsentRequest consent = new ConsentRequest(ConsentStatus.ACCEPTED, "GENERAL_INTAKE", "v1.0", null);
        mockMvc.perform(post("/api/intake/sessions/" + sessionId + "/consent")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(consent)))
                .andExpect(status().isOk());
    }
}
