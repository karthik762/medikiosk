package com.medikiosk.fhir;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.medikiosk.entity.Allergy;
import com.medikiosk.entity.AuditLog;
import com.medikiosk.entity.AyurvedaHistory;
import com.medikiosk.entity.ClinicalSummary;
import com.medikiosk.entity.Consent;
import com.medikiosk.entity.DoctorReview;
import com.medikiosk.entity.FhirMapping;
import com.medikiosk.entity.IntakeSession;
import com.medikiosk.entity.IntegrationRecord;
import com.medikiosk.entity.MedicalDocument;
import com.medikiosk.entity.MedicalHistory;
import com.medikiosk.entity.Medication;
import com.medikiosk.entity.Patient;
import com.medikiosk.entity.Symptom;
import com.medikiosk.entity.TriageResult;
import com.medikiosk.entity.User;
import com.medikiosk.entity.enums.ClinicalSummaryStatus;
import com.medikiosk.entity.enums.ConsentStatus;
import com.medikiosk.entity.enums.DoctorReviewAction;
import com.medikiosk.entity.enums.DocumentProcessingStatus;
import com.medikiosk.entity.enums.DocumentType;
import com.medikiosk.entity.enums.IntakeSessionStatus;
import com.medikiosk.entity.enums.TriagePriority;
import com.medikiosk.entity.enums.UserRole;
import com.medikiosk.entity.enums.UserStatus;
import com.medikiosk.repository.AllergyRepository;
import com.medikiosk.repository.AuditLogRepository;
import com.medikiosk.repository.AyurvedaHistoryRepository;
import com.medikiosk.repository.ClinicalSummaryRepository;
import com.medikiosk.repository.ConsentRepository;
import com.medikiosk.repository.DoctorReviewRepository;
import com.medikiosk.repository.FhirMappingRepository;
import com.medikiosk.repository.IntakeSessionRepository;
import com.medikiosk.repository.IntegrationRecordRepository;
import com.medikiosk.repository.MedicalDocumentRepository;
import com.medikiosk.repository.MedicalHistoryRepository;
import com.medikiosk.repository.MedicationRepository;
import com.medikiosk.repository.PatientRepository;
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

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.not;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class FhirIntegrationTest {

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
    private TriageResultRepository triageResultRepository;

    @Autowired
    private ClinicalSummaryRepository clinicalSummaryRepository;

    @Autowired
    private DoctorReviewRepository doctorReviewRepository;

    @Autowired
    private FhirMappingRepository fhirMappingRepository;

    @Autowired
    private IntegrationRecordRepository integrationRecordRepository;

    @Autowired
    private AuditLogRepository auditLogRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    private User doctorUser;
    private String doctorToken;
    private User patientUser;
    private String patientToken;
    private Patient testPatient;
    private IntakeSession testSession;

    @BeforeEach
    void setUp() {
        // Clean up repositories
        auditLogRepository.deleteAll();
        integrationRecordRepository.deleteAll();
        fhirMappingRepository.deleteAll();
        doctorReviewRepository.deleteAll();
        clinicalSummaryRepository.deleteAll();
        triageResultRepository.deleteAll();
        medicalDocumentRepository.deleteAll();
        ayurvedaHistoryRepository.deleteAll();
        allergyRepository.deleteAll();
        medicationRepository.deleteAll();
        medicalHistoryRepository.deleteAll();
        symptomRepository.deleteAll();
        consentRepository.deleteAll();
        intakeSessionRepository.deleteAll();
        patientRepository.deleteAll();
        userRepository.deleteAll();

        // 1. Create Doctor User
        doctorUser = new User("doctor.fhir@medikiosk.local", passwordEncoder.encode("Password123!"), "Vikram", "Patel", UserRole.DOCTOR, UserStatus.ACTIVE);
        doctorUser = userRepository.save(doctorUser);
        doctorToken = jwtTokenProvider.generateToken(doctorUser);

        // 2. Create Patient User & Patient
        patientUser = new User("patient.fhir@medikiosk.local", passwordEncoder.encode("Password123!"), "Ananya", "Sharma", UserRole.PATIENT, UserStatus.ACTIVE);
        patientUser = userRepository.save(patientUser);
        patientToken = jwtTokenProvider.generateToken(patientUser);

        testPatient = new Patient("PAT-" + UUID.randomUUID().toString().substring(0, 8), "Ananya", "Sharma");
        testPatient.setUserId(patientUser.getId());
        testPatient.setDateOfBirth(LocalDate.of(1990, 5, 15));
        testPatient.setGender("FEMALE");
        testPatient.setPhone("+919876543210");
        testPatient.setEmail("ananya.sharma@example.com");
        testPatient.setAbhaId("91-9876-5432-1098");
        testPatient = patientRepository.save(testPatient);

        // 3. Create Session
        testSession = new IntakeSession(testPatient);
        testSession.setStatus(IntakeSessionStatus.COMPLETED);
        testSession.setLanguage("en");
        testSession.setCompletedAt(Instant.now());
        testSession = intakeSessionRepository.save(testSession);

        // 4. Create Consent
        Consent consent = new Consent();
        consent.setPatient(testPatient);
        consent.setSession(testSession);
        consent.setConsentStatus(ConsentStatus.ACCEPTED);
        consent.setConsentType("GENERAL_INTAKE");
        consent.setConsentVersion("1.0");
        consent.setConsentedAt(Instant.now());
        consentRepository.save(consent);

        // 5. Create Symptoms (Patient reported)
        Symptom symptom = new Symptom();
        symptom.setSession(testSession);
        symptom.setName("Severe burning epigastric pain");
        symptom.setSeverity(8);
        symptom.setDuration("4 days");
        symptom.setLocation("Epigastrium");
        symptom.setOnset("Sudden");
        symptom.setSource("PATIENT");
        symptomRepository.save(symptom);

        // 6. Create Deterministic Triage Result (Rule Engine)
        TriageResult triage = new TriageResult();
        triage.setSession(testSession);
        triage.setPriority(TriagePriority.URGENT);
        triage.setTriggeredRules("[\"RULE_EPIGASTRIC_BURNING_RED_FLAG\"]");
        triage.setExplanation("Epigastric burning with severe pain requires urgent rule-based evaluation");
        triageResultRepository.save(triage);

        // 7. Create Past Medical History
        MedicalHistory history = new MedicalHistory(testPatient, "CHRONIC_CONDITION", "Type 2 Diabetes Mellitus", "PATIENT");
        history.setSession(testSession);
        medicalHistoryRepository.save(history);

        // 8. Create Medication
        Medication med = new Medication();
        med.setPatient(testPatient);
        med.setSession(testSession);
        med.setMedicationName("Metformin 500mg");
        med.setDosage("500mg");
        med.setFrequency("Twice daily with meals");
        medicationRepository.save(med);

        // 9. Create Allergy
        Allergy allergy = new Allergy();
        allergy.setPatient(testPatient);
        allergy.setSession(testSession);
        allergy.setAllergen("Penicillin");
        allergy.setReaction("Urticaria and facial angioedema");
        allergy.setSeverity("severe");
        allergyRepository.save(allergy);

        // 10. Create Medical Document (with secure internal storage reference)
        MedicalDocument doc = new MedicalDocument();
        doc.setPatient(testPatient);
        doc.setSession(testSession);
        doc.setFileName("gastroscopy_report.pdf");
        doc.setFileType("application/pdf");
        doc.setFileSize(204800L);
        doc.setStorageReference("C:\\secure\\hospital\\data\\uploads\\doc-secret-123.pdf");
        doc.setDocumentType("LAB_REPORT");
        doc.setProcessingStatus(DocumentProcessingStatus.COMPLETED);
        doc.setUploadedAt(Instant.now());
        medicalDocumentRepository.save(doc);

        // 11. Create Ayurveda History
        AyurvedaHistory ay = new AyurvedaHistory();
        ay.setPatient(testPatient);
        ay.setSession(testSession);
        ay.setPrakritiData("{\"primary\":\"Pitta\",\"secondary\":\"Vata\"}");
        ay.setNidanaData("{\"factors\":[\"Ushna ahara\",\"Tikshna\"]}");
        ay.setParikshaData("{\"agni\":\"Tikshnagni\",\"koshtha\":\"Krura\"}");
        ayurvedaHistoryRepository.save(ay);

        // 12. Create Clinical Summary (initially unverified)
        ClinicalSummary summary = new ClinicalSummary(testPatient, testSession, "AI Structured intake summary", ClinicalSummaryStatus.AI_GENERATED);
        summary.setGeneratedBy("AI");
        clinicalSummaryRepository.save(summary);
    }

    @Test
    @DisplayName("Unverified clinical summary produces preliminary FHIR Composition and unverified export status")
    void testExportBundleUnverifiedProducesPreliminaryComposition() throws Exception {
        mockMvc.perform(get("/api/fhir/sessions/" + testSession.getId() + "/bundle")
                        .header("Authorization", "Bearer " + doctorToken))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Type", containsString("application/fhir+json")))
                .andExpect(jsonPath("$.resourceType", equalTo("Bundle")))
                .andExpect(jsonPath("$.type", equalTo("document")))
                // First entry must be Composition
                .andExpect(jsonPath("$.entry[0].resource.resourceType", equalTo("Composition")))
                .andExpect(jsonPath("$.entry[0].resource.status", equalTo("preliminary")))
                .andExpect(jsonPath("$.entry[0].resource.author[0].display", containsString("MediKiosk")));

        mockMvc.perform(get("/api/fhir/sessions/" + testSession.getId() + "/export")
                        .header("Authorization", "Bearer " + doctorToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.verificationStatus", equalTo("UNVERIFIED")))
                .andExpect(jsonPath("$.data.compositionStatus", equalTo("preliminary")));
    }

    @Test
    @DisplayName("Doctor verified clinical summary produces final FHIR Composition with Practitioner author")
    void testExportBundleVerifiedProducesFinalCompositionAndDoctorAuthor() throws Exception {
        // Mark summary verified by doctor
        ClinicalSummary summary = clinicalSummaryRepository.findBySessionId(testSession.getId()).orElseThrow();
        summary.setStatus(ClinicalSummaryStatus.VERIFIED);
        clinicalSummaryRepository.save(summary);

        DoctorReview review = new DoctorReview();
        review.setSummary(summary);
        review.setDoctor(doctorUser);
        review.setAction(DoctorReviewAction.VERIFIED);
        review.setComments("Clinically verified. Patient urgently triaged to Emergency Gastroenterology.");
        review.setSummaryVersion(1);
        review.setVerifiedAt(Instant.now());
        doctorReviewRepository.save(review);

        mockMvc.perform(post("/api/fhir/sessions/" + testSession.getId() + "/export")
                        .header("Authorization", "Bearer " + doctorToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", equalTo(true)))
                .andExpect(jsonPath("$.data.verificationStatus", equalTo("VERIFIED")))
                .andExpect(jsonPath("$.data.compositionStatus", equalTo("final")))
                .andExpect(jsonPath("$.data.validationStatus", equalTo("VALID")));

        // Verify JSON bundle structure
        String response = mockMvc.perform(get("/api/fhir/sessions/" + testSession.getId() + "/bundle")
                        .header("Authorization", "Bearer " + doctorToken))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        JsonNode bundleNode = objectMapper.readTree(response);
        assertEquals("Bundle", bundleNode.get("resourceType").asText());
        JsonNode firstResource = bundleNode.get("entry").get(0).get("resource");
        assertEquals("Composition", firstResource.get("resourceType").asText());
        assertEquals("final", firstResource.get("status").asText());
        // Doctor author display
        String authorDisplay = firstResource.get("author").get(0).get("display").asText();
        assertTrue(authorDisplay.contains("Dr. Vikram Patel"));
    }

    @Test
    @DisplayName("Deterministic triage result is preserved as Observation with RULE_ENGINE source extensions")
    void testDeterministicTriageObservationPreserved() throws Exception {
        String response = mockMvc.perform(get("/api/fhir/sessions/" + testSession.getId() + "/bundle")
                        .header("Authorization", "Bearer " + doctorToken))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        JsonNode bundleNode = objectMapper.readTree(response);
        boolean triageFound = false;

        for (JsonNode entry : bundleNode.get("entry")) {
            JsonNode res = entry.get("resource");
            if ("Observation".equals(res.get("resourceType").asText()) 
                    && res.has("code") && res.get("code").has("coding")) {
                JsonNode codings = res.get("code").get("coding");
                for (JsonNode coding : codings) {
                    if ("triage-category".equals(coding.path("code").asText())) {
                        triageFound = true;
                        assertEquals("URGENT", res.get("valueString").asText());
                        // Check extensions
                        JsonNode extensions = res.get("extension");
                        assertNotNull(extensions);
                        boolean ruleEngineExt = false;
                        for (JsonNode ext : extensions) {
                            if ("https://medikiosk.local/fhir/StructureDefinition/triage-source".equals(ext.path("url").asText())) {
                                assertEquals("RULE_ENGINE", ext.path("valueString").asText());
                                ruleEngineExt = true;
                            }
                        }
                        assertTrue(ruleEngineExt, "Observation must contain triage-source RULE_ENGINE extension");
                    }
                }
            }
        }

        assertTrue(triageFound, "Triage Observation not found in bundle");
    }

    @Test
    @DisplayName("Patient symptoms remain Observation resources and are NEVER transformed into Condition diagnoses")
    void testSymptomsPreservedAsObservationsNotConditions() throws Exception {
        String response = mockMvc.perform(get("/api/fhir/sessions/" + testSession.getId() + "/bundle")
                        .header("Authorization", "Bearer " + doctorToken))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        JsonNode bundleNode = objectMapper.readTree(response);

        int observationCount = 0;
        int conditionCount = 0;

        for (JsonNode entry : bundleNode.get("entry")) {
            JsonNode res = entry.get("resource");
            String resType = res.get("resourceType").asText();

            if ("Observation".equals(resType)) {
                observationCount++;
                // Check if symptom epigastric pain is inside an observation
                if (res.has("code") && res.get("code").has("text")) {
                    if (res.get("code").get("text").asText().contains("Severe burning epigastric pain")) {
                        // Must be observation, with severity component
                        assertTrue(res.get("valueString").asText().contains("Severity: 8/10"));
                    }
                }
            } else if ("Condition".equals(resType)) {
                conditionCount++;
                // Condition must strictly be medical history, NOT the acute epigastric pain symptom
                String condText = res.get("code").get("text").asText();
                assertTrue(condText.contains("Diabetes"), "Condition must only be past medical history");
                assertFalse(condText.contains("epigastric pain"), "Symptom must NOT be transformed into Condition diagnosis");
            }
        }

        assertTrue(observationCount >= 2, "Expected symptom and triage observations");
        assertEquals(1, conditionCount, "Expected 1 past medical history Condition");
    }

    @Test
    @DisplayName("DocumentReference attachment URL is authenticated REST endpoint and NEVER leaks local file paths")
    void testDocumentReferenceUsesRestUrlNotLocalFilesystem() throws Exception {
        String response = mockMvc.perform(get("/api/fhir/sessions/" + testSession.getId() + "/bundle")
                        .header("Authorization", "Bearer " + doctorToken))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        JsonNode bundleNode = objectMapper.readTree(response);
        boolean docFound = false;

        for (JsonNode entry : bundleNode.get("entry")) {
            JsonNode res = entry.get("resource");
            if ("DocumentReference".equals(res.get("resourceType").asText())) {
                docFound = true;
                JsonNode content = res.get("content").get(0).get("attachment");
                String url = content.get("url").asText();

                // Verification of REST URI
                assertTrue(url.startsWith("/api/intake/sessions/" + testSession.getId() + "/documents/"), "URL must be REST endpoint");
                assertTrue(url.endsWith("/download"));

                // Ensure NO local OS path leaks
                assertFalse(url.contains(":\\"), "Must not leak Windows drive paths");
                assertFalse(url.startsWith("file:"), "Must not leak file:// URIs");
                assertFalse(url.contains("uploads"), "Must not leak storage folders");
            }
        }

        assertTrue(docFound, "DocumentReference resource not found in bundle");
    }

    @Test
    @DisplayName("FHIR validation validates bundle compliance with zero errors")
    void testFhirValidationDetectsValidBundle() throws Exception {
        mockMvc.perform(get("/api/fhir/sessions/" + testSession.getId() + "/validate")
                        .header("Authorization", "Bearer " + doctorToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", equalTo(true)))
                .andExpect(jsonPath("$.data.valid", equalTo(true)))
                .andExpect(jsonPath("$.data.errors", hasSize(0)))
                .andExpect(jsonPath("$.data.totalChecked", greaterThan(5)));
    }

    @Test
    @DisplayName("Exporting clinical bundle persists FhirMapping, IntegrationRecord, and AuditLog")
    void testFhirExportPersistsMappingsAndIntegrationRecordAndAuditLog() throws Exception {
        mockMvc.perform(post("/api/fhir/sessions/" + testSession.getId() + "/export")
                        .header("Authorization", "Bearer " + doctorToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", equalTo(true)));

        // Verify fhir_mappings table entries
        List<FhirMapping> mappings = fhirMappingRepository.findBySessionId(testSession.getId());
        assertFalse(mappings.isEmpty(), "fhir_mappings should not be empty");
        assertTrue(mappings.stream().anyMatch(m -> "Composition".equals(m.getFhirResourceType())));
        assertTrue(mappings.stream().anyMatch(m -> "Patient".equals(m.getFhirResourceType())));
        assertTrue(mappings.stream().anyMatch(m -> "Observation".equals(m.getFhirResourceType())));
        assertTrue(mappings.stream().anyMatch(m -> "DocumentReference".equals(m.getFhirResourceType())));

        // Verify integration_records table entry
        List<IntegrationRecord> records = integrationRecordRepository.findBySessionId(testSession.getId());
        assertFalse(records.isEmpty(), "integration_records should have record");
        IntegrationRecord ir = records.get(0);
        assertEquals("PENDING", ir.getStatus().name());
        assertTrue(ir.getErrorMessage().contains("Future adapter required"));

        // Verify audit_logs table entry
        List<AuditLog> logs = auditLogRepository.findByAction("FHIR_BUNDLE_EXPORTED");
        assertFalse(logs.isEmpty(), "audit_logs should have FHIR_BUNDLE_EXPORTED record");
        assertEquals(doctorUser.getId(), logs.get(0).getUserId());
    }

    @Test
    @DisplayName("Access control enforces authentication and blocks unauthorized roles")
    void testRoleBasedSecurity() throws Exception {
        // 1. Unauthenticated request -> 401
        mockMvc.perform(get("/api/fhir/sessions/" + testSession.getId() + "/bundle"))
                .andExpect(status().isUnauthorized());

        // 2. Patient role -> 403 Forbidden
        mockMvc.perform(get("/api/fhir/sessions/" + testSession.getId() + "/bundle")
                        .header("Authorization", "Bearer " + patientToken))
                .andExpect(status().isForbidden());

        // 3. Doctor role -> 200 OK
        mockMvc.perform(get("/api/fhir/sessions/" + testSession.getId() + "/bundle")
                        .header("Authorization", "Bearer " + doctorToken))
                .andExpect(status().isOk());
    }
}
