package com.medikiosk.doctor;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.medikiosk.dto.clinical.ClinicalSummaryDto;
import com.medikiosk.dto.doctor.DoctorAmendmentRequest;
import com.medikiosk.dto.doctor.DoctorVerificationRequest;
import com.medikiosk.entity.Allergy;
import com.medikiosk.entity.Answer;
import com.medikiosk.entity.ClinicalSummary;
import com.medikiosk.entity.Consent;
import com.medikiosk.entity.DoctorReview;
import com.medikiosk.entity.DocumentExtraction;
import com.medikiosk.entity.IntakeSession;
import com.medikiosk.entity.MedicalDocument;
import com.medikiosk.entity.MedicalHistory;
import com.medikiosk.entity.Medication;
import com.medikiosk.entity.Patient;
import com.medikiosk.entity.Question;
import com.medikiosk.entity.Symptom;
import com.medikiosk.entity.TriageResult;
import com.medikiosk.entity.User;
import com.medikiosk.entity.enums.ClinicalSummaryStatus;
import com.medikiosk.entity.enums.ConsentStatus;
import com.medikiosk.entity.enums.DoctorReviewAction;
import com.medikiosk.entity.enums.DocumentProcessingStatus;
import com.medikiosk.entity.enums.DocumentType;
import com.medikiosk.entity.enums.ExtractionStatus;
import com.medikiosk.entity.enums.IntakeSessionStatus;
import com.medikiosk.entity.enums.TriagePriority;
import com.medikiosk.entity.enums.UserRole;
import com.medikiosk.entity.enums.UserStatus;
import com.medikiosk.repository.AllergyRepository;
import com.medikiosk.repository.AnswerRepository;
import com.medikiosk.repository.AuditLogRepository;
import com.medikiosk.repository.AyurvedaHistoryRepository;
import com.medikiosk.repository.ClinicalSummaryRepository;
import com.medikiosk.repository.ConsentRepository;
import com.medikiosk.repository.DoctorReviewRepository;
import com.medikiosk.repository.DocumentExtractionRepository;
import com.medikiosk.repository.IntakeSessionRepository;
import com.medikiosk.repository.MedicalDocumentRepository;
import com.medikiosk.repository.MedicalHistoryRepository;
import com.medikiosk.repository.MedicationRepository;
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

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class DoctorReviewIntegrationTest {

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
    private ConsentRepository consentRepository;

    @Autowired
    private QuestionRepository questionRepository;

    @Autowired
    private AnswerRepository answerRepository;

    @Autowired
    private SymptomRepository symptomRepository;

    @Autowired
    private MedicalHistoryRepository medicalHistoryRepository;

    @Autowired
    private MedicationRepository medicationRepository;

    @Autowired
    private AllergyRepository allergyRepository;

    @Autowired
    private AyurvedaHistoryRepository ayurvedaHistoryRepository;

    @Autowired
    private MedicalDocumentRepository medicalDocumentRepository;

    @Autowired
    private DocumentExtractionRepository documentExtractionRepository;

    @Autowired
    private TriageResultRepository triageResultRepository;

    @Autowired
    private ClinicalSummaryRepository clinicalSummaryRepository;

    @Autowired
    private DoctorReviewRepository doctorReviewRepository;

    @Autowired
    private AuditLogRepository auditLogRepository;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private User doctorUser;
    private User patientUser;
    private String doctorToken;
    private String patientToken;

    private Patient patientA;
    private IntakeSession sessionA;
    private Patient patientB;
    private IntakeSession sessionB;

    @BeforeEach
    void setUp() {
        // Clean child tables first
        doctorReviewRepository.deleteAll();
        clinicalSummaryRepository.deleteAll();
        triageResultRepository.deleteAll();
        documentExtractionRepository.deleteAll();
        medicalDocumentRepository.deleteAll();
        ayurvedaHistoryRepository.deleteAll();
        allergyRepository.deleteAll();
        medicationRepository.deleteAll();
        medicalHistoryRepository.deleteAll();
        symptomRepository.deleteAll();
        answerRepository.deleteAll();
        consentRepository.deleteAll();
        intakeSessionRepository.deleteAll();
        patientRepository.deleteAll();
        userRepository.deleteAll();

        // Setup Doctor User
        doctorUser = new User();
        doctorUser.setEmail("dr.smith@medikiosk.org");
        doctorUser.setPasswordHash(passwordEncoder.encode("DoctorPass123!"));
        doctorUser.setFirstName("Sarah");
        doctorUser.setLastName("Smith");
        doctorUser.setRole(UserRole.DOCTOR);
        doctorUser.setStatus(UserStatus.ACTIVE);
        doctorUser = userRepository.save(doctorUser);
        doctorToken = jwtTokenProvider.generateToken(doctorUser);

        // Setup Patient User
        patientUser = new User();
        patientUser.setEmail("john.doe@patient.org");
        patientUser.setPasswordHash(passwordEncoder.encode("PatientPass123!"));
        patientUser.setFirstName("John");
        patientUser.setLastName("Doe");
        patientUser.setRole(UserRole.PATIENT);
        patientUser.setStatus(UserStatus.ACTIVE);
        patientUser = userRepository.save(patientUser);
        patientToken = jwtTokenProvider.generateToken(patientUser);

        // Setup Patients & Sessions
        patientA = new Patient();
        patientA.setUserId(patientUser.getId());
        patientA.setPatientIdentifier("PAT-DOC-001");
        patientA.setFirstName("John");
        patientA.setLastName("Doe");
        patientA.setDateOfBirth(LocalDate.of(1985, 5, 20));
        patientA.setGender("MALE");
        patientA.setPhone("9876543210");
        patientA = patientRepository.save(patientA);

        sessionA = new IntakeSession(patientA);
        sessionA.setLanguage("en");
        sessionA.setStatus(IntakeSessionStatus.IN_PROGRESS);
        sessionA = intakeSessionRepository.save(sessionA);

        Consent consentA = new Consent(patientA, sessionA, ConsentStatus.ACCEPTED, "GENERAL_INTAKE", "v1.0");
        consentRepository.save(consentA);

        patientB = new Patient();
        patientB.setPatientIdentifier("PAT-DOC-002");
        patientB.setFirstName("Alice");
        patientB.setLastName("Walker");
        patientB.setDateOfBirth(LocalDate.of(1992, 10, 15));
        patientB.setGender("FEMALE");
        patientB.setPhone("9123456789");
        patientB = patientRepository.save(patientB);

        sessionB = new IntakeSession(patientB);
        sessionB.setLanguage("en");
        sessionB.setStatus(IntakeSessionStatus.COMPLETED);
        sessionB = intakeSessionRepository.save(sessionB);

        Consent consentB = new Consent(patientB, sessionB, ConsentStatus.ACCEPTED, "GENERAL_INTAKE", "v1.0");
        consentRepository.save(consentB);
    }

    @Test
    @DisplayName("1. Unauthenticated request to doctor queue returns 401 Unauthorized")
    void testUnauthenticatedAccessReturns401() throws Exception {
        mockMvc.perform(get("/api/doctor/queue"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("2. Patient role accessing doctor queue returns 403 Forbidden")
    void testPatientAccessReturns403() throws Exception {
        mockMvc.perform(get("/api/doctor/queue")
                        .header("Authorization", "Bearer " + patientToken))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message", containsString("Requires DOCTOR or ADMIN role")));
    }

    @Test
    @DisplayName("3. Patient role accessing case review endpoint returns 403 Forbidden")
    void testPatientAccessToReviewReturns403() throws Exception {
        mockMvc.perform(get("/api/doctor/sessions/" + sessionA.getId() + "/review")
                        .header("Authorization", "Bearer " + patientToken))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("4. Doctor queue retrieval orders cases by triage priority (URGENT -> PRIORITY -> STABLE)")
    void testDoctorQueueRetrievalWithPriorityOrdering() throws Exception {
        // Session A has PRIORITY triage
        TriageResult trA = new TriageResult(sessionA, TriagePriority.PRIORITY, "Moderate fever and cough");
        triageResultRepository.save(trA);

        // Session B has URGENT triage
        TriageResult trB = new TriageResult(sessionB, TriagePriority.URGENT, "Severe chest pain radiating to left arm");
        triageResultRepository.save(trB);

        mockMvc.perform(get("/api/doctor/queue")
                        .header("Authorization", "Bearer " + doctorToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data", hasSize(2)))
                // First item must be URGENT case (Session B)
                .andExpect(jsonPath("$.data[0].sessionId").value(sessionB.getId().toString()))
                .andExpect(jsonPath("$.data[0].triagePriority").value("URGENT"))
                .andExpect(jsonPath("$.data[0].urgent").value(true))
                .andExpect(jsonPath("$.data[0].patientName").value("A. Walker"))
                // Second item is PRIORITY case (Session A)
                .andExpect(jsonPath("$.data[1].sessionId").value(sessionA.getId().toString()))
                .andExpect(jsonPath("$.data[1].triagePriority").value("PRIORITY"))
                .andExpect(jsonPath("$.data[1].urgent").value(false));
    }

    @Test
    @DisplayName("5. Doctor queue filtering by priority")
    void testDoctorQueueFilteringByPriority() throws Exception {
        triageResultRepository.save(new TriageResult(sessionA, TriagePriority.STABLE, "Mild cold"));
        triageResultRepository.save(new TriageResult(sessionB, TriagePriority.URGENT, "Acute severe dyspnea"));

        mockMvc.perform(get("/api/doctor/queue?priority=URGENT")
                        .header("Authorization", "Bearer " + doctorToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data", hasSize(1)))
                .andExpect(jsonPath("$.data[0].triagePriority").value("URGENT"))
                .andExpect(jsonPath("$.data[0].sessionId").value(sessionB.getId().toString()));
    }

    @Test
    @DisplayName("6. Doctor retrieves full case review details across all clinical subsystems")
    void testDoctorRetrievesFullCaseReviewDetails() throws Exception {
        // Setup Chief Complaint & Intake Answer
        Question q = questionRepository.findByQuestionCode("CHIEF_COMPLAINT")
                .orElseGet(() -> questionRepository.save(new Question("CHIEF_COMPLAINT", "What brings you in?", "TEXT", "CHIEF_COMPLAINT")));
        answerRepository.save(new Answer(sessionA, q, "Persistent dry cough and fever", "VOICE"));

        // Setup Symptoms
        symptomRepository.save(new Symptom(sessionA, "Fever", 7, "4 days"));
        symptomRepository.save(new Symptom(sessionA, "Dry Cough", 5, "4 days"));

        // Setup Medical History
        medicalHistoryRepository.save(new MedicalHistory(patientA, "CHRONIC_CONDITION", "Hypertension", "PATIENT_RESPONSE"));

        // Setup Medication
        Medication med = new Medication(patientA, "Amlodipine", "5 mg", "Once daily");
        med.setSession(sessionA);
        medicationRepository.save(med);

        // Setup Allergy
        Allergy al = new Allergy(patientA, "Penicillin", "Rash", "MODERATE");
        al.setSession(sessionA);
        allergyRepository.save(al);

        // Setup Triage
        TriageResult tr = new TriageResult(sessionA, TriagePriority.PRIORITY, "Screened moderate priority");
        tr.setTriggeredRules("[{\"ruleId\":\"TRIAGE-008\",\"reason\":\"Persistent fever\"}]");
        triageResultRepository.save(tr);

        // Setup Document & OCR
        MedicalDocument doc = new MedicalDocument(patientA, DocumentType.PRESCRIPTION, "rx.png", "/storage/rx.png");
        doc.setSession(sessionA);
        doc.setProcessingStatus(DocumentProcessingStatus.COMPLETED);
        doc = medicalDocumentRepository.save(doc);

        DocumentExtraction ext = new DocumentExtraction(doc, "Rx: Amlodipine 5mg", BigDecimal.valueOf(0.92));
        ext.setStatus(ExtractionStatus.SUCCESS);
        documentExtractionRepository.save(ext);

        // Setup AI Clinical Summary
        ClinicalSummary summary = new ClinicalSummary(patientA, sessionA, "Persistent dry cough and fever", ClinicalSummaryStatus.AI_GENERATED);
        summary.setSummaryData("{\"chiefComplaint\":\"Persistent dry cough and fever\",\"symptoms\":[{\"name\":\"Fever\",\"severity\":7}],\"doctorVerification\":{\"status\":\"PENDING\"}}");
        summary.setVersion(1);
        clinicalSummaryRepository.save(summary);

        mockMvc.perform(get("/api/doctor/sessions/" + sessionA.getId() + "/review")
                        .header("Authorization", "Bearer " + doctorToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.sessionId").value(sessionA.getId().toString()))
                .andExpect(jsonPath("$.data.patientIdentifier").value("PAT-DOC-001"))
                .andExpect(jsonPath("$.data.age").value(41))
                .andExpect(jsonPath("$.data.aiSummaryAvailable").value(true))
                .andExpect(jsonPath("$.data.currentVersion").value(1))
                .andExpect(jsonPath("$.data.currentStatus").value("AI_GENERATED"))
                .andExpect(jsonPath("$.data.verified").value(false))
                .andExpect(jsonPath("$.data.triage.priority").value("PRIORITY"))
                .andExpect(jsonPath("$.data.triage.source").value("RULE_ENGINE"))
                .andExpect(jsonPath("$.data.originalPatientData.chiefComplaint").value("Persistent dry cough and fever"))
                .andExpect(jsonPath("$.data.originalPatientData.symptoms", hasSize(2)))
                .andExpect(jsonPath("$.data.originalPatientData.medicalHistory", hasSize(1)))
                .andExpect(jsonPath("$.data.originalPatientData.medications", hasSize(1)))
                .andExpect(jsonPath("$.data.originalPatientData.allergies", hasSize(1)))
                .andExpect(jsonPath("$.data.documents", hasSize(1)))
                .andExpect(jsonPath("$.data.documents[0].extraction.confidenceScore").value(0.92));
    }

    @Test
    @DisplayName("7. Doctor can review patient case even when AI summary is unavailable (AI failure resiliency)")
    void testDoctorReviewWhenAiSummaryUnavailable() throws Exception {
        symptomRepository.save(new Symptom(sessionB, "Migraine", 8, "2 days"));

        mockMvc.perform(get("/api/doctor/sessions/" + sessionB.getId() + "/review")
                        .header("Authorization", "Bearer " + doctorToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.aiSummaryAvailable").value(false))
                .andExpect(jsonPath("$.data.clinicalSummary").doesNotExist())
                .andExpect(jsonPath("$.data.originalPatientData.symptoms", hasSize(1)))
                .andExpect(jsonPath("$.data.originalPatientData.symptoms[0].name").value("Migraine"));
    }

    @Test
    @DisplayName("8. Doctor amends summary: increments version, transitions status to DOCTOR_REVIEWED, and preserves original AI snapshot")
    void testDoctorAmendSummarySuccess() throws Exception {
        String originalAiJson = "{\"chiefComplaint\":\"Fever 3 days\",\"symptoms\":[{\"name\":\"Fever\",\"severity\":6}],\"doctorVerification\":{\"status\":\"PENDING\"}}";
        ClinicalSummary summary = new ClinicalSummary(patientA, sessionA, "Fever 3 days", ClinicalSummaryStatus.AI_GENERATED);
        summary.setSummaryData(originalAiJson);
        summary.setVersion(1);
        summary = clinicalSummaryRepository.save(summary);

        // Doctor edits summary: changes chief complaint and corrects symptom severity
        ClinicalSummaryDto amendedDto = new ClinicalSummaryDto();
        amendedDto.setChiefComplaint("Acute Viral Fever with mild dehydration (Corrected)");

        DoctorAmendmentRequest request = new DoctorAmendmentRequest();
        request.setExpectedVersion(1);
        request.setAmendedSummary(amendedDto);
        request.setClinicalNotes("Corrected chief complaint after patient palpation and hydration assessment.");

        mockMvc.perform(put("/api/doctor/sessions/" + sessionA.getId() + "/summary/amend")
                        .header("Authorization", "Bearer " + doctorToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.chiefComplaint").value("Acute Viral Fever with mild dehydration (Corrected)"))
                .andExpect(jsonPath("$.data.doctorVerification.status").value("AMENDED"))
                .andExpect(jsonPath("$.data.doctorVerification.verifiedByName").value("Sarah Smith"));

        // Verify database state
        ClinicalSummary updatedSummary = clinicalSummaryRepository.findBySessionId(sessionA.getId()).orElseThrow();
        assertEquals(2, updatedSummary.getVersion());
        assertEquals(ClinicalSummaryStatus.DOCTOR_REVIEWED, updatedSummary.getStatus());
        assertNotNull(updatedSummary.getOriginalAiData());
        assertEquals(originalAiJson, updatedSummary.getOriginalAiData());

        // Verify doctor_reviews audit table
        List<DoctorReview> reviews = doctorReviewRepository.findBySummaryId(updatedSummary.getId());
        assertEquals(1, reviews.size());
        assertEquals(DoctorReviewAction.EDITED, reviews.get(0).getAction());
        assertEquals(2, reviews.get(0).getSummaryVersion());
        assertEquals("dr.smith@medikiosk.org", reviews.get(0).getDoctor().getEmail());
    }

    @Test
    @DisplayName("9. Optimistic locking: Amending summary with stale expectedVersion returns 409 Conflict")
    void testDoctorAmendSummaryOptimisticLockingConflict() throws Exception {
        ClinicalSummary summary = new ClinicalSummary(patientA, sessionA, "Fever", ClinicalSummaryStatus.AI_GENERATED);
        summary.setSummaryData("{\"chiefComplaint\":\"Fever\"}");
        summary.setVersion(2); // Current version in DB is 2
        clinicalSummaryRepository.save(summary);

        DoctorAmendmentRequest staleRequest = new DoctorAmendmentRequest();
        staleRequest.setExpectedVersion(1); // Stale version
        staleRequest.setAmendedSummary(new ClinicalSummaryDto());
        staleRequest.setClinicalNotes("Stale edit attempt");

        mockMvc.perform(put("/api/doctor/sessions/" + sessionA.getId() + "/summary/amend")
                        .header("Authorization", "Bearer " + doctorToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(staleRequest)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message", containsString("modified by another clinician")));
    }

    @Test
    @DisplayName("10. Doctor verifies summary: increments version, status transitions to VERIFIED, and doctor identity is recorded")
    void testDoctorVerifySummarySuccess() throws Exception {
        ClinicalSummary summary = new ClinicalSummary(patientA, sessionA, "Fever and cough", ClinicalSummaryStatus.DOCTOR_REVIEWED);
        summary.setSummaryData("{\"chiefComplaint\":\"Fever and cough\",\"doctorVerification\":{\"status\":\"PENDING\"}}");
        summary.setVersion(2);
        summary = clinicalSummaryRepository.save(summary);

        DoctorVerificationRequest request = new DoctorVerificationRequest();
        request.setExpectedVersion(2);
        request.setConfirmUnderstood(true);
        request.setClinicalNotes("Verified patient history and examination. Ready for prescription.");

        mockMvc.perform(post("/api/doctor/sessions/" + sessionA.getId() + "/summary/verify")
                        .header("Authorization", "Bearer " + doctorToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.status").value("VERIFIED"))
                .andExpect(jsonPath("$.data.doctorVerification.status").value("VERIFIED"))
                .andExpect(jsonPath("$.data.doctorVerification.verifiedByName").value("Sarah Smith"))
                .andExpect(jsonPath("$.data.doctorVerification.comments").value("Verified patient history and examination. Ready for prescription."));

        // Verify database state
        ClinicalSummary verifiedSummary = clinicalSummaryRepository.findBySessionId(sessionA.getId()).orElseThrow();
        assertEquals(ClinicalSummaryStatus.VERIFIED, verifiedSummary.getStatus());
        assertEquals(3, verifiedSummary.getVersion());

        // Verify doctor review audit record
        List<DoctorReview> reviews = doctorReviewRepository.findBySummaryIdAndAction(verifiedSummary.getId(), DoctorReviewAction.VERIFIED);
        assertEquals(1, reviews.size());
        assertNotNull(reviews.get(0).getVerifiedAt());
    }

    @Test
    @DisplayName("11. Patient cannot submit verification or forge doctor identity")
    void testPatientCannotVerifySummary() throws Exception {
        ClinicalSummary summary = new ClinicalSummary(patientA, sessionA, "Fever", ClinicalSummaryStatus.AI_GENERATED);
        summary.setSummaryData("{\"chiefComplaint\":\"Fever\"}");
        summary.setVersion(1);
        clinicalSummaryRepository.save(summary);

        DoctorVerificationRequest request = new DoctorVerificationRequest(1, "Patient attempt", true);

        mockMvc.perform(post("/api/doctor/sessions/" + sessionA.getId() + "/summary/verify")
                        .header("Authorization", "Bearer " + patientToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }
}
