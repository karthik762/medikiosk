package com.medikiosk.clinical;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.medikiosk.ai.provider.AiStructuringContext;
import com.medikiosk.dto.clinical.ClinicalSummaryDto;
import com.medikiosk.dto.clinical.DoctorVerificationDto;
import com.medikiosk.dto.clinical.StructuredMedicationDto;
import com.medikiosk.dto.clinical.TriageSummaryDto;
import com.medikiosk.dto.intake.ConsentRequest;
import com.medikiosk.dto.intake.IntakeSessionRequest;
import com.medikiosk.dto.intake.PatientRequest;
import com.medikiosk.entity.Allergy;
import com.medikiosk.entity.Answer;
import com.medikiosk.entity.AyurvedaHistory;
import com.medikiosk.entity.ClinicalSummary;
import com.medikiosk.entity.Consent;
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
import com.medikiosk.entity.enums.DocumentProcessingStatus;
import com.medikiosk.entity.enums.DocumentType;
import com.medikiosk.entity.enums.ExtractionStatus;
import com.medikiosk.entity.enums.TriagePriority;
import com.medikiosk.entity.enums.UserRole;
import com.medikiosk.entity.enums.UserStatus;
import com.medikiosk.repository.AllergyRepository;
import com.medikiosk.repository.AnswerRepository;
import com.medikiosk.repository.AuditLogRepository;
import com.medikiosk.repository.AyurvedaHistoryRepository;
import com.medikiosk.repository.ClinicalSummaryRepository;
import com.medikiosk.repository.ConsentRepository;
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
import org.springframework.test.web.servlet.MvcResult;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class ClinicalSummaryIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Autowired
    private PasswordEncoder passwordEncoder;

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
    private TriageResultRepository triageResultRepository;

    @Autowired
    private MedicalDocumentRepository medicalDocumentRepository;

    @Autowired
    private DocumentExtractionRepository documentExtractionRepository;

    @Autowired
    private ClinicalSummaryRepository clinicalSummaryRepository;

    @Autowired
    private AuditLogRepository auditLogRepository;

    private User patientUserA;
    private User patientUserB;
    private User doctorUser;
    private Patient patientA;
    private Patient patientB;
    private IntakeSession sessionA;
    private String tokenA;
    private String tokenB;
    private String tokenDoctor;

    @BeforeEach
    void setUp() throws Exception {
        clinicalSummaryRepository.deleteAll();
        documentExtractionRepository.deleteAll();
        medicalDocumentRepository.deleteAll();
        auditLogRepository.deleteAll();
        triageResultRepository.deleteAll();
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

        patientUserA = userRepository.save(new User(
                "patA.summary@test.com",
                passwordEncoder.encode("Pass123!"),
                "Alice",
                "Brown",
                UserRole.PATIENT,
                UserStatus.ACTIVE
        ));
        tokenA = jwtTokenProvider.generateToken(patientUserA);

        patientUserB = userRepository.save(new User(
                "patB.summary@test.com",
                passwordEncoder.encode("Pass123!"),
                "Bob",
                "Green",
                UserRole.PATIENT,
                UserStatus.ACTIVE
        ));
        tokenB = jwtTokenProvider.generateToken(patientUserB);

        doctorUser = userRepository.save(new User(
                "doc.summary@test.com",
                passwordEncoder.encode("Doctor123!"),
                "Dr. David",
                "Smith",
                UserRole.DOCTOR,
                UserStatus.ACTIVE
        ));
        tokenDoctor = jwtTokenProvider.generateToken(doctorUser);

        patientA = createPatient(patientUserA, "PAT-SUM-001");
        sessionA = createSessionWithConsent(patientA, tokenA);

        patientB = createPatient(patientUserB, "PAT-SUM-002");
    }

    private Patient createPatient(User user, String identifier) throws Exception {
        PatientRequest request = new PatientRequest("Test", "Patient", LocalDate.of(1990, 1, 1), "MALE", "+919876543210", user.getEmail());
        request.setPatientIdentifier(identifier);
        String token = user.equals(patientUserA) ? tokenA : tokenB;

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

    private IntakeSession createSessionWithConsent(Patient patient, String token) throws Exception {
        IntakeSessionRequest sessionRequest = new IntakeSessionRequest(patient.getId(), "en");
        MvcResult sessionResult = mockMvc.perform(post("/api/intake/sessions")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sessionRequest)))
                .andExpect(status().isCreated())
                .andReturn();

        JsonNode root = objectMapper.readTree(sessionResult.getResponse().getContentAsString());
        UUID sessionId = UUID.fromString(root.path("data").path("id").asText());

        ConsentRequest consentRequest = new ConsentRequest(
                ConsentStatus.ACCEPTED,
                "GENERAL_INTAKE",
                "v1.0",
                "REF-DOC-1"
        );

        mockMvc.perform(post("/api/intake/sessions/" + sessionId + "/consent")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(consentRequest)))
                .andExpect(status().isOk());

        return intakeSessionRepository.findById(sessionId).orElseThrow();
    }

    @Test
    @DisplayName("1. Generate clinical summary from complete intake with patient data, Ayurveda, documents, and triage")
    void testGenerateCompleteSummary() throws Exception {
        // Setup intake answers
        Question q1 = questionRepository.save(new Question("CHIEF_COMPLAINT", "What is your main health concern?", "TEXT", "GENERAL"));
        answerRepository.save(new Answer(sessionA, q1, "Fever and mild persistent cough", "VOICE"));

        // Setup symptoms
        Symptom s1 = new Symptom(sessionA, "Fever", 6, "3 days");
        s1.setLocation("Whole body");
        s1.setOnset("Acute");
        s1.setFrequency("Continuous");
        s1.setSource("PATIENT_RESPONSE");
        symptomRepository.save(s1);

        Symptom s2 = new Symptom(sessionA, "Cough", 4, "5 days");
        s2.setLocation("Chest");
        s2.setOnset("Gradual");
        s2.setFrequency("Intermittent");
        s2.setSource("PATIENT_RESPONSE");
        symptomRepository.save(s2);

        // Setup medical history
        MedicalHistory mh = new MedicalHistory(patientA, "CHRONIC_CONDITION", "Type 2 Diabetes Mellitus", "PATIENT_RESPONSE");
        mh.setSession(sessionA);
        medicalHistoryRepository.save(mh);

        // Setup patient medication
        Medication m1 = new Medication(patientA, "Metformin", "500 mg", "Twice daily");
        m1.setSession(sessionA);
        m1.setDuration("2 years");
        m1.setSource("PATIENT_RESPONSE");
        medicationRepository.save(m1);

        // Setup patient allergy
        Allergy a1 = new Allergy(patientA, "Penicillin", "Skin rash and hives", "MODERATE");
        a1.setSession(sessionA);
        a1.setSource("PATIENT_RESPONSE");
        allergyRepository.save(a1);

        // Setup Ayurveda history
        AyurvedaHistory ah = new AyurvedaHistory(patientA, sessionA);
        ah.setPrakritiData("{\"agni\":\"Tikshnagni\",\"kostha\":\"Krura\"}");
        ah.setNidanaData("{\"diet\":\"Irregular spicy food\"}");
        ayurvedaHistoryRepository.save(ah);

        // Setup Triage Result
        TriageResult tr = new TriageResult(sessionA, TriagePriority.PRIORITY, "Screened for moderate priority review");
        tr.setTriggeredRules("[{\"ruleId\":\"TRIAGE-008\",\"reason\":\"Moderate fever with cough\"}]");
        triageResultRepository.save(tr);

        // Setup OCR Document
        MedicalDocument doc = new MedicalDocument(patientA, DocumentType.PRESCRIPTION, "rx.png", "/storage/rx.png");
        doc.setSession(sessionA);
        doc.setProcessingStatus(DocumentProcessingStatus.COMPLETED);
        doc = medicalDocumentRepository.save(doc);

        DocumentExtraction ext = new DocumentExtraction(doc, "Rx: Metformin 500 mg daily", BigDecimal.valueOf(0.95));
        ext.setProcessingEngine("MOCK_OCR_PROVIDER");
        ext.setStatus(ExtractionStatus.SUCCESS);
        ext.setExtractionData("{\"medications\":[{\"name\":\"Metformin\",\"dosage\":\"500 mg\",\"frequency\":\"Twice daily\"}]}");
        documentExtractionRepository.save(ext);

        // Execute generation
        mockMvc.perform(post("/api/intake/sessions/" + sessionA.getId() + "/clinical-summary/generate")
                        .header("Authorization", "Bearer " + tokenA))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.chiefComplaint").value("Fever and mild persistent cough"))
                .andExpect(jsonPath("$.data.symptoms", hasSize(2)))
                .andExpect(jsonPath("$.data.symptoms[0].name").value("Fever"))
                .andExpect(jsonPath("$.data.symptoms[0].severity").value(6))
                .andExpect(jsonPath("$.data.medicalHistory", hasSize(1)))
                .andExpect(jsonPath("$.data.medicalHistory[0].description").value("Type 2 Diabetes Mellitus"))
                .andExpect(jsonPath("$.data.medications", hasSize(1)))
                .andExpect(jsonPath("$.data.medications[0].medicationName").value("Metformin"))
                .andExpect(jsonPath("$.data.allergies", hasSize(1)))
                .andExpect(jsonPath("$.data.allergies[0].allergen").value("Penicillin"))
                .andExpect(jsonPath("$.data.triage.priority").value("PRIORITY"))
                .andExpect(jsonPath("$.data.triage.source").value("RULE_ENGINE"))
                .andExpect(jsonPath("$.data.documentFindings", hasSize(1)))
                .andExpect(jsonPath("$.data.doctorVerification.status").value("PENDING"))
                .andExpect(jsonPath("$.data.metadata.isMock").value(true));
    }

    @Test
    @DisplayName("2. Low-confidence OCR document preserves uncertainty warnings")
    void testLowConfidenceOcrPreserved() throws Exception {
        MedicalDocument doc = new MedicalDocument(patientA, DocumentType.PRESCRIPTION, "blurry_rx.png", "/storage/blurry.png");
        doc.setSession(sessionA);
        doc.setProcessingStatus(DocumentProcessingStatus.REVIEW_REQUIRED);
        doc = medicalDocumentRepository.save(doc);

        DocumentExtraction ext = new DocumentExtraction(doc, "Rx: Metformin 50O mg", BigDecimal.valueOf(0.55));
        ext.setProcessingEngine("MOCK_OCR_PROVIDER");
        ext.setStatus(ExtractionStatus.LOW_CONFIDENCE);
        documentExtractionRepository.save(ext);

        mockMvc.perform(post("/api/intake/sessions/" + sessionA.getId() + "/clinical-summary/generate")
                        .header("Authorization", "Bearer " + tokenA))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.documentFindings[0].uncertaintyFlags", hasItem("LOW_CONFIDENCE_OCR")))
                .andExpect(jsonPath("$.data.documentFindings[0].uncertaintyFlags", hasItem("DOCTOR_VERIFICATION_REQUIRED")));
    }

    @Test
    @DisplayName("3. Medication conflict between patient and document is preserved as discrepancy")
    void testMedicationConflictPreserved() throws Exception {
        // Patient reports Metformin 500 mg
        Medication m1 = new Medication(patientA, "Metformin", "500 mg", "Once daily");
        m1.setSession(sessionA);
        medicationRepository.save(m1);

        // Document reports Metformin 850 mg
        MedicalDocument doc = new MedicalDocument(patientA, DocumentType.PRESCRIPTION, "rx.png", "/storage/rx.png");
        doc.setSession(sessionA);
        doc = medicalDocumentRepository.save(doc);

        DocumentExtraction ext = new DocumentExtraction(doc, "Rx: Metformin 850 mg", BigDecimal.valueOf(0.92));
        ext.setProcessingEngine("MOCK_OCR_PROVIDER");
        ext.setStatus(ExtractionStatus.SUCCESS);
        ext.setExtractionData("{\"medications\":[{\"name\":\"Metformin\",\"dosage\":\"850 mg\"}]}");
        documentExtractionRepository.save(ext);

        mockMvc.perform(post("/api/intake/sessions/" + sessionA.getId() + "/clinical-summary/generate")
                        .header("Authorization", "Bearer " + tokenA))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.medications[0].conflictFlag").value(true))
                .andExpect(jsonPath("$.data.conflicts", hasSize(1)))
                .andExpect(jsonPath("$.data.conflicts[0].patientReported").value("500 mg"))
                .andExpect(jsonPath("$.data.conflicts[0].documentReported").value("850 mg"));
    }

    @Test
    @DisplayName("4. Missing information is explicitly identified without inventing data")
    void testMissingInformationIdentified() throws Exception {
        // Symptom without severity
        Symptom s1 = new Symptom(sessionA, "Mild Head Ache", null, "1 day");
        s1.setSource("PATIENT_RESPONSE");
        symptomRepository.save(s1);

        mockMvc.perform(post("/api/intake/sessions/" + sessionA.getId() + "/clinical-summary/generate")
                        .header("Authorization", "Bearer " + tokenA))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.missingInformation[*].field", hasItem("severity")))
                .andExpect(jsonPath("$.data.missingInformation[*].field", hasItem("allergies")));
    }

    @Test
    @DisplayName("5. Idempotent regeneration updates version and does not duplicate summaries")
    void testIdempotentRegeneration() throws Exception {
        // First generation
        mockMvc.perform(post("/api/intake/sessions/" + sessionA.getId() + "/clinical-summary/generate")
                        .header("Authorization", "Bearer " + tokenA))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.version").value(1));

        // Second generation updates existing summary to version 2
        mockMvc.perform(post("/api/intake/sessions/" + sessionA.getId() + "/clinical-summary/generate")
                        .header("Authorization", "Bearer " + tokenA))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.version").value(2));

        // Verify only 1 summary entity exists for this session
        List<ClinicalSummary> summaries = clinicalSummaryRepository.findAll();
        org.junit.jupiter.api.Assertions.assertEquals(1, summaries.size());
        org.junit.jupiter.api.Assertions.assertEquals(2, summaries.get(0).getVersion());
    }

    @Test
    @DisplayName("6. Doctor can access any clinical summary; Cross-patient access is rejected with 403")
    void testSecurityAccessBoundaries() throws Exception {
        // Generate summary for session A
        mockMvc.perform(post("/api/intake/sessions/" + sessionA.getId() + "/clinical-summary/generate")
                        .header("Authorization", "Bearer " + tokenA))
                .andExpect(status().isCreated());

        // Patient B cannot read session A summary (403 Forbidden)
        mockMvc.perform(get("/api/intake/sessions/" + sessionA.getId() + "/clinical-summary")
                        .header("Authorization", "Bearer " + tokenB))
                .andExpect(status().isForbidden());

        // Doctor can access session A summary (200 OK)
        mockMvc.perform(get("/api/intake/sessions/" + sessionA.getId() + "/clinical-summary")
                        .header("Authorization", "Bearer " + tokenDoctor))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.sessionId").value(sessionA.getId().toString()));

        // Unauthenticated access rejected (401 Unauthorized)
        mockMvc.perform(get("/api/intake/sessions/" + sessionA.getId() + "/clinical-summary"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("7. Generation is blocked when active consent is missing")
    void testConsentRequirement() throws Exception {
        consentRepository.deleteAll(); // Remove consent

        mockMvc.perform(post("/api/intake/sessions/" + sessionA.getId() + "/clinical-summary/generate")
                        .header("Authorization", "Bearer " + tokenA))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", containsString("consent")));
    }

    @Test
    @DisplayName("8. Cannot overwrite already doctor-verified clinical summary")
    void testCannotOverwriteDoctorVerifiedSummary() throws Exception {
        ClinicalSummary verifiedSummary = new ClinicalSummary(patientA, sessionA, "Verified summary", ClinicalSummaryStatus.VERIFIED);
        verifiedSummary.setVersion(1);
        clinicalSummaryRepository.save(verifiedSummary);

        mockMvc.perform(post("/api/intake/sessions/" + sessionA.getId() + "/clinical-summary/generate")
                        .header("Authorization", "Bearer " + tokenA))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message", containsString("already verified")));
    }

    // =========================================================================
    // SAFETY FILTER DETERMINISTIC REJECTION TESTS
    // =========================================================================

    @Test
    @DisplayName("Safety Case B: Invented autonomous diagnosis in AI output is strictly rejected")
    void testSafetyCaseB_InventedDiagnosisRejected() {
        ClinicalSummaryDto badSummary = new ClinicalSummaryDto();
        badSummary.setChiefComplaint("Patient has diabetes and confirmed covid");
        badSummary.setHumanReadableSummary("Patient is definitively diagnosed with hypertension.");

        AiStructuringContext context = new AiStructuringContext();
        context.setTriageResult(null);

        com.medikiosk.ai.safety.AiSafetyValidator validator = new com.medikiosk.ai.safety.AiSafetyValidator(objectMapper);
        org.junit.jupiter.api.Assertions.assertThrows(
                com.medikiosk.ai.safety.AiSafetyViolationException.class,
                () -> validator.validate(badSummary, context)
        );
    }

    @Test
    @DisplayName("Safety Case C: Treatment / prescription recommendation is strictly rejected")
    void testSafetyCaseC_TreatmentRecommendationRejected() {
        ClinicalSummaryDto badSummary = new ClinicalSummaryDto();
        badSummary.setChiefComplaint("Headache");
        badSummary.setHumanReadableSummary("Recommended treatment: start taking 500 mg paracetamol daily.");

        AiStructuringContext context = new AiStructuringContext();

        com.medikiosk.ai.safety.AiSafetyValidator validator = new com.medikiosk.ai.safety.AiSafetyValidator(objectMapper);
        org.junit.jupiter.api.Assertions.assertThrows(
                com.medikiosk.ai.safety.AiSafetyViolationException.class,
                () -> validator.validate(badSummary, context)
        );
    }

    @Test
    @DisplayName("Safety Case D: Invented / ungrounded medication is strictly rejected")
    void testSafetyCaseD_InventedMedicationRejected() {
        ClinicalSummaryDto badSummary = new ClinicalSummaryDto();
        badSummary.setChiefComplaint("Headache");
        badSummary.setHumanReadableSummary("Patient summary without recommendations");

        List<StructuredMedicationDto> meds = new ArrayList<>();
        meds.add(new StructuredMedicationDto("Atorvastatin", "20 mg", "Daily", null, "AI_INFERRED", false, null));
        badSummary.setMedications(meds);

        AiStructuringContext context = new AiStructuringContext();
        Medication m = new Medication(patientA, "Metformin", "500 mg", "Daily");
        m.setSession(sessionA);
        context.setMedications(List.of(m));

        com.medikiosk.ai.safety.AiSafetyValidator validator = new com.medikiosk.ai.safety.AiSafetyValidator(objectMapper);
        org.junit.jupiter.api.Assertions.assertThrows(
                com.medikiosk.ai.safety.AiSafetyViolationException.class,
                () -> validator.validate(badSummary, context)
        );
    }

    @Test
    @DisplayName("Safety Case F: AI attempting to override deterministic triage is strictly rejected")
    void testSafetyCaseF_TriageOverrideRejected() {
        ClinicalSummaryDto badSummary = new ClinicalSummaryDto();
        badSummary.setChiefComplaint("Chest pain");
        badSummary.setHumanReadableSummary("Patient summary");
        badSummary.setTriage(new TriageSummaryDto(TriagePriority.STABLE, "AI downgraded triage", null));

        AiStructuringContext context = new AiStructuringContext();
        TriageResult tr = new TriageResult(sessionA, TriagePriority.URGENT, "Deterministic red-flag triggered");
        context.setTriageResult(tr);

        com.medikiosk.ai.safety.AiSafetyValidator validator = new com.medikiosk.ai.safety.AiSafetyValidator(objectMapper);
        org.junit.jupiter.api.Assertions.assertThrows(
                com.medikiosk.ai.safety.AiSafetyViolationException.class,
                () -> validator.validate(badSummary, context)
        );
    }

    @Test
    @DisplayName("Safety Case G: AI attempting to mark output as doctor verified is strictly rejected")
    void testSafetyCaseG_VerificationSpoofingRejected() {
        ClinicalSummaryDto badSummary = new ClinicalSummaryDto();
        badSummary.setChiefComplaint("Cough");
        badSummary.setHumanReadableSummary("Patient summary");
        badSummary.setDoctorVerification(new DoctorVerificationDto("VERIFIED", UUID.randomUUID(), "Dr. AI", null, null));

        AiStructuringContext context = new AiStructuringContext();

        com.medikiosk.ai.safety.AiSafetyValidator validator = new com.medikiosk.ai.safety.AiSafetyValidator(objectMapper);
        org.junit.jupiter.api.Assertions.assertThrows(
                com.medikiosk.ai.safety.AiSafetyViolationException.class,
                () -> validator.validate(badSummary, context)
        );
    }
}
