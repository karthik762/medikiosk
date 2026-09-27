package com.medikiosk.phase15;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.medikiosk.abdm.dto.AbdmConsentRequestDto;
import com.medikiosk.abdm.dto.AbdmExchangeRequestDto;
import com.medikiosk.dto.ayurveda.AyurvedaHistoryRequest;
import com.medikiosk.dto.ayurveda.PrakritiDataDto;
import com.medikiosk.dto.clinical.ClinicalSummaryDto;
import com.medikiosk.dto.doctor.DoctorAmendmentRequest;
import com.medikiosk.dto.doctor.DoctorVerificationRequest;
import com.medikiosk.dto.intake.AllergyRequest;
import com.medikiosk.dto.intake.AnswerRequest;
import com.medikiosk.dto.intake.ChiefComplaintRequest;
import com.medikiosk.dto.intake.ConsentRequest;
import com.medikiosk.dto.intake.IntakeSessionRequest;
import com.medikiosk.dto.intake.MedicalHistoryRequest;
import com.medikiosk.dto.intake.MedicationRequest;
import com.medikiosk.dto.intake.PatientRequest;
import com.medikiosk.dto.intake.SymptomRequest;
import com.medikiosk.entity.ClinicalSummary;
import com.medikiosk.entity.Consent;
import com.medikiosk.entity.IntegrationRecord;
import com.medikiosk.entity.Question;
import com.medikiosk.entity.User;
import com.medikiosk.entity.enums.ClinicalSummaryStatus;
import com.medikiosk.entity.enums.ConsentStatus;
import com.medikiosk.entity.enums.ConsentType;
import com.medikiosk.entity.enums.IntegrationStatus;
import com.medikiosk.entity.enums.IntegrationType;
import com.medikiosk.entity.enums.UserRole;
import com.medikiosk.entity.enums.UserStatus;
import com.medikiosk.healthlake.dto.HealthLakeExportRequestDto;
import com.medikiosk.his.dto.HisExportRequestDto;
import com.medikiosk.repository.AllergyRepository;
import com.medikiosk.repository.AnswerRepository;
import com.medikiosk.repository.AuditLogRepository;
import com.medikiosk.repository.AyurvedaHistoryRepository;
import com.medikiosk.repository.ClinicalSummaryRepository;
import com.medikiosk.repository.ConsentRepository;
import com.medikiosk.repository.DoctorReviewRepository;
import com.medikiosk.repository.DocumentExtractionRepository;
import com.medikiosk.repository.FhirMappingRepository;
import com.medikiosk.repository.IntakeSessionRepository;
import com.medikiosk.repository.IntegrationRecordRepository;
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
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Phase 15 — End-to-end clinical workflow hardening coverage.
 * Exercises the full patient → doctor → FHIR → mock integration path via REST.
 * Does not perform live ABDM / HIS / HealthLake network calls.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class Phase15EndToEndWorkflowIntegrationTest {

    private static final byte[] VALID_PDF_BYTES = new byte[]{
            0x25, 0x50, 0x44, 0x46, 0x2D, 0x31, 0x2E, 0x34, 0x0A, 0x25, (byte) 0xE2, (byte) 0xE3, (byte) 0xCF, (byte) 0xD3
    };

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @Autowired private UserRepository userRepository;
    @Autowired private PatientRepository patientRepository;
    @Autowired private IntakeSessionRepository intakeSessionRepository;
    @Autowired private ConsentRepository consentRepository;
    @Autowired private AnswerRepository answerRepository;
    @Autowired private SymptomRepository symptomRepository;
    @Autowired private MedicalHistoryRepository medicalHistoryRepository;
    @Autowired private MedicationRepository medicationRepository;
    @Autowired private AllergyRepository allergyRepository;
    @Autowired private QuestionRepository questionRepository;
    @Autowired private TriageResultRepository triageResultRepository;
    @Autowired private AyurvedaHistoryRepository ayurvedaHistoryRepository;
    @Autowired private MedicalDocumentRepository medicalDocumentRepository;
    @Autowired private DocumentExtractionRepository documentExtractionRepository;
    @Autowired private ClinicalSummaryRepository clinicalSummaryRepository;
    @Autowired private DoctorReviewRepository doctorReviewRepository;
    @Autowired private FhirMappingRepository fhirMappingRepository;
    @Autowired private IntegrationRecordRepository integrationRecordRepository;
    @Autowired private AuditLogRepository auditLogRepository;
    @Autowired private JwtTokenProvider jwtTokenProvider;
    @Autowired private PasswordEncoder passwordEncoder;

    private User patientUser;
    private User otherPatientUser;
    private User doctorUser;
    private User doctorBUser;
    private User adminUser;
    private String patientToken;
    private String otherPatientToken;
    private String doctorToken;
    private String doctorBToken;
    private String adminToken;

    @BeforeEach
    void setUp() {
        integrationRecordRepository.deleteAll();
        fhirMappingRepository.deleteAll();
        doctorReviewRepository.deleteAll();
        clinicalSummaryRepository.deleteAll();
        documentExtractionRepository.deleteAll();
        medicalDocumentRepository.deleteAll();
        ayurvedaHistoryRepository.deleteAll();
        triageResultRepository.deleteAll();
        allergyRepository.deleteAll();
        medicationRepository.deleteAll();
        medicalHistoryRepository.deleteAll();
        symptomRepository.deleteAll();
        answerRepository.deleteAll();
        consentRepository.deleteAll();
        intakeSessionRepository.deleteAll();
        patientRepository.deleteAll();
        userRepository.deleteAll();

        patientUser = saveUser("phase15.patient@medikiosk.org", "Pat", "ient", UserRole.PATIENT);
        otherPatientUser = saveUser("phase15.other@medikiosk.org", "Other", "Patient", UserRole.PATIENT);
        doctorUser = saveUser("phase15.doctor@medikiosk.org", "Doc", "Alpha", UserRole.DOCTOR);
        doctorBUser = saveUser("phase15.doctorb@medikiosk.org", "Doc", "Beta", UserRole.DOCTOR);
        adminUser = saveUser("phase15.admin@medikiosk.org", "Ad", "Min", UserRole.ADMIN);

        patientToken = jwtTokenProvider.generateToken(patientUser);
        otherPatientToken = jwtTokenProvider.generateToken(otherPatientUser);
        doctorToken = jwtTokenProvider.generateToken(doctorUser);
        doctorBToken = jwtTokenProvider.generateToken(doctorBUser);
        adminToken = jwtTokenProvider.generateToken(adminUser);
    }

    @Test
    @Order(1)
    @DisplayName("E2E: Full patient journey through doctor verification and mock integration boundaries")
    void testCompleteClinicalWorkflowEndToEnd() throws Exception {
        // 1. Create patient
        PatientRequest patientRequest = new PatientRequest("Aarav", "Sharma", LocalDate.of(1988, 3, 15), "MALE", "+919876543210", "aarav@example.org");
        patientRequest.setPatientIdentifier("MK-P15-E2E-001");
        MvcResult patientResult = mockMvc.perform(post("/api/patients")
                        .header("Authorization", "Bearer " + patientToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(patientRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.patientIdentifier").value("MK-P15-E2E-001"))
                .andReturn();
        UUID patientId = UUID.fromString(objectMapper.readTree(patientResult.getResponse().getContentAsString())
                .path("data").path("id").asText());

        // 2. Create intake session
        IntakeSessionRequest sessionRequest = new IntakeSessionRequest(patientId, "en");
        MvcResult sessionResult = mockMvc.perform(post("/api/intake/sessions")
                        .header("Authorization", "Bearer " + patientToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sessionRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.status").value("CREATED"))
                .andReturn();
        UUID sessionId = UUID.fromString(objectMapper.readTree(sessionResult.getResponse().getContentAsString())
                .path("data").path("id").asText());

        // 3. Required GENERAL_INTAKE consent
        ConsentRequest consentRequest = new ConsentRequest(ConsentStatus.ACCEPTED, ConsentType.GENERAL_INTAKE, "v1.0", "P15-CONSENT");
        mockMvc.perform(post("/api/intake/sessions/" + sessionId + "/consent")
                        .header("Authorization", "Bearer " + patientToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(consentRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.consentStatus").value("ACCEPTED"))
                .andExpect(jsonPath("$.data.consentType").value(ConsentType.GENERAL_INTAKE));

        mockMvc.perform(get("/api/intake/sessions/" + sessionId)
                        .header("Authorization", "Bearer " + patientToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("IN_PROGRESS"));

        // 4–8. Clinical intake payload
        mockMvc.perform(post("/api/intake/sessions/" + sessionId + "/complaint")
                        .header("Authorization", "Bearer " + patientToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new ChiefComplaintRequest("Chest discomfort and shortness of breath for 1 day", "TEXT"))))
                .andExpect(status().isOk());

        SymptomRequest symptom = new SymptomRequest("Chest Pain", "Chest", "1 day", 8);
        symptom.setOnset("Sudden");
        mockMvc.perform(post("/api/intake/sessions/" + sessionId + "/symptoms")
                        .header("Authorization", "Bearer " + patientToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(symptom)))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/intake/sessions/" + sessionId + "/medical-history")
                        .header("Authorization", "Bearer " + patientToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new MedicalHistoryRequest("CHRONIC", "Hypertension diagnosed 2019", "PATIENT"))))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/intake/sessions/" + sessionId + "/medications")
                        .header("Authorization", "Bearer " + patientToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new MedicationRequest("Amlodipine", "5mg", "OD", "ongoing"))))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/intake/sessions/" + sessionId + "/allergies")
                        .header("Authorization", "Bearer " + patientToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new AllergyRequest("Penicillin", "Rash", "MODERATE"))))
                .andExpect(status().isCreated());

        Question medsQuestion = questionRepository.findByQuestionCode("Q_CURRENT_MEDS").orElseThrow();
        mockMvc.perform(post("/api/intake/sessions/" + sessionId + "/answers")
                        .header("Authorization", "Bearer " + patientToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new AnswerRequest(medsQuestion.getId(), "Yes", "TOUCH"))))
                .andExpect(status().isCreated());

        // 9. Triage (rule-based). Patient view omits internal rule IDs; clinician view preserves them.
        mockMvc.perform(post("/api/intake/sessions/" + sessionId + "/triage")
                        .header("Authorization", "Bearer " + patientToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.priority").value("URGENT"));

        MvcResult triageResult = mockMvc.perform(get("/api/intake/sessions/" + sessionId + "/triage")
                        .header("Authorization", "Bearer " + doctorToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.priority").value("URGENT"))
                .andExpect(jsonPath("$.data.triggeredRules").isArray())
                .andExpect(jsonPath("$.data.triggeredRules[0].ruleId").exists())
                .andReturn();
        JsonNode triageNode = objectMapper.readTree(triageResult.getResponse().getContentAsString()).path("data");
        assertThat(triageNode.path("triggeredRules").size()).isGreaterThan(0);

        // 10. Ayurveda (optional)
        AyurvedaHistoryRequest ayurvedaRequest = new AyurvedaHistoryRequest();
        PrakritiDataDto prakriti = new PrakritiDataDto();
        prakriti.setBodyFrame("MEDIUM");
        prakriti.setWeatherPreference("DISLIKES_HEAT");
        prakriti.setDigestionPattern("REGULAR");
        prakriti.setSleepPattern("SOUND");
        ayurvedaRequest.setPrakriti(prakriti);
        mockMvc.perform(post("/api/intake/sessions/" + sessionId + "/ayurveda")
                        .header("Authorization", "Bearer " + patientToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(ayurvedaRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("DRAFT"));

        // 11. Document upload / OCR
        MockMultipartFile file = new MockMultipartFile(
                "file", "rx_amlodipine.pdf", "application/pdf", VALID_PDF_BYTES);
        mockMvc.perform(multipart("/api/intake/sessions/" + sessionId + "/documents")
                        .file(file)
                        .param("documentType", "PRESCRIPTION")
                        .header("Authorization", "Bearer " + patientToken))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.processingStatus").exists());

        // Complete intake — answers immutable afterwards
        mockMvc.perform(post("/api/intake/sessions/" + sessionId + "/complete")
                        .header("Authorization", "Bearer " + patientToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("COMPLETED"));

        mockMvc.perform(post("/api/intake/sessions/" + sessionId + "/complaint")
                        .header("Authorization", "Bearer " + patientToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new ChiefComplaintRequest("Should not be accepted after completion", "TEXT"))))
                .andExpect(status().isConflict());

        // 12–13. Generate clinical summary — remains AI_GENERATED / unverified
        MvcResult summaryResult = mockMvc.perform(post("/api/intake/sessions/" + sessionId + "/clinical-summary/generate")
                        .header("Authorization", "Bearer " + patientToken))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.status").value("AI_GENERATED"))
                .andExpect(jsonPath("$.data.chiefComplaint", containsString("Chest")))
                .andReturn();
        JsonNode summaryNode = objectMapper.readTree(summaryResult.getResponse().getContentAsString()).path("data");
        int summaryVersion = summaryNode.path("version").asInt(1);
        assertThat(summaryNode.path("status").asText()).isEqualTo("AI_GENERATED");

        // Unverified summary cannot be exported to external integrations
        mockMvc.perform(post("/api/abdm/sessions/" + sessionId + "/exchange")
                        .header("Authorization", "Bearer " + doctorToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new AbdmExchangeRequestDto("attempt", "P15-ABDM-BLOCK"))))
                .andExpect(status().isPreconditionFailed());

        mockMvc.perform(post("/api/his/sessions/" + sessionId + "/export")
                        .header("Authorization", "Bearer " + doctorToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                HisExportRequestDto.builder().idempotencyKey("P15-HIS-BLOCK").build())))
                .andExpect(status().isPreconditionFailed());

        mockMvc.perform(post("/api/healthlake/sessions/" + sessionId + "/export")
                        .header("Authorization", "Bearer " + doctorToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                HealthLakeExportRequestDto.builder().idempotencyKey("P15-HL-BLOCK").build())))
                .andExpect(status().isPreconditionFailed());

        // FHIR preliminary for unverified (raw FHIR Bundle document, not ApiResponse envelope)
        mockMvc.perform(get("/api/fhir/sessions/" + sessionId + "/bundle")
                        .header("Authorization", "Bearer " + doctorToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resourceType").value("Bundle"))
                .andExpect(jsonPath("$.entry[0].resource.resourceType").value("Composition"))
                .andExpect(jsonPath("$.entry[0].resource.status").value("preliminary"));

        // 14–15. Doctor queue + amend
        mockMvc.perform(get("/api/doctor/queue")
                        .header("Authorization", "Bearer " + doctorToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[*].sessionId", hasItem(sessionId.toString())));

        ClinicalSummaryDto amended = objectMapper.treeToValue(summaryNode, ClinicalSummaryDto.class);
        amended.setChiefComplaint("Chest discomfort — clinician clarified as non-exertional pressure");
        DoctorAmendmentRequest amendRequest = new DoctorAmendmentRequest(summaryVersion, amended, "Clarified history");
        MvcResult amendResult = mockMvc.perform(put("/api/doctor/sessions/" + sessionId + "/summary/amend")
                        .header("Authorization", "Bearer " + doctorToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(amendRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("DOCTOR_REVIEWED"))
                .andReturn();
        int amendedVersion = objectMapper.readTree(amendResult.getResponse().getContentAsString())
                .path("data").path("version").asInt();
        if (amendedVersion <= 0) {
            amendedVersion = clinicalSummaryRepository.findBySessionId(sessionId).orElseThrow().getVersion();
        }

        ClinicalSummary midSummary = clinicalSummaryRepository.findBySessionId(sessionId).orElseThrow();
        assertThat(midSummary.getOriginalAiData()).isNotBlank();
        assertThat(midSummary.getStatus()).isEqualTo(ClinicalSummaryStatus.DOCTOR_REVIEWED);

        // DOCTOR_REVIEWED still cannot export
        mockMvc.perform(post("/api/his/sessions/" + sessionId + "/export")
                        .header("Authorization", "Bearer " + doctorToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                HisExportRequestDto.builder().idempotencyKey("P15-HIS-REVIEWED-BLOCK").build())))
                .andExpect(status().isPreconditionFailed());

        // 16. Doctor verify
        DoctorVerificationRequest verifyRequest = new DoctorVerificationRequest(amendedVersion, "Verified for consult", true);
        mockMvc.perform(post("/api/doctor/sessions/" + sessionId + "/summary/verify")
                        .header("Authorization", "Bearer " + doctorToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(verifyRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("VERIFIED"));

        ClinicalSummary verified = clinicalSummaryRepository.findBySessionId(sessionId).orElseThrow();
        assertThat(verified.getStatus()).isEqualTo(ClinicalSummaryStatus.VERIFIED);
        assertThat(verified.getOriginalAiData()).isNotBlank();

        // Amend after VERIFIED is blocked
        DoctorAmendmentRequest postVerifyAmend = new DoctorAmendmentRequest(
                verified.getVersion(), amended, "Should fail");
        mockMvc.perform(put("/api/doctor/sessions/" + sessionId + "/summary/amend")
                        .header("Authorization", "Bearer " + doctorToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(postVerifyAmend)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message", containsString("Verified clinical summaries cannot be amended")));

        // 17–18. FHIR final + validate
        mockMvc.perform(get("/api/fhir/sessions/" + sessionId + "/bundle")
                        .header("Authorization", "Bearer " + doctorToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resourceType").value("Bundle"))
                .andExpect(jsonPath("$.entry[0].resource.status").value("final"));

        mockMvc.perform(get("/api/fhir/sessions/" + sessionId + "/validate")
                        .header("Authorization", "Bearer " + doctorToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.valid").value(true));

        // Mock integration status — connected=false
        mockMvc.perform(get("/api/abdm/status").header("Authorization", "Bearer " + doctorToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.connected").value(false))
                .andExpect(jsonPath("$.data.isMock").value(true));
        mockMvc.perform(get("/api/his/status").header("Authorization", "Bearer " + doctorToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.connected").value(false))
                .andExpect(jsonPath("$.data.isMock").value(true));
        mockMvc.perform(get("/api/healthlake/status").header("Authorization", "Bearer " + doctorToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.connected").value(false))
                .andExpect(jsonPath("$.data.isMock").value(true));

        // 19. ABDM — intake consent alone is insufficient; grant ABDM_DATA_SHARE then exchange
        mockMvc.perform(post("/api/abdm/sessions/" + sessionId + "/exchange")
                        .header("Authorization", "Bearer " + doctorToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new AbdmExchangeRequestDto("no abdm consent", "P15-ABDM-NOCONSENT"))))
                .andExpect(status().isPreconditionFailed());

        AbdmConsentRequestDto abdmConsent = new AbdmConsentRequestDto(patientId);
        mockMvc.perform(post("/api/abdm/sessions/" + sessionId + "/consent")
                        .header("Authorization", "Bearer " + patientToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(abdmConsent)))
                .andExpect(status().isOk());

        List<Consent> consents = consentRepository.findAllBySessionId(sessionId);
        assertThat(consents).extracting(Consent::getConsentType)
                .contains(ConsentType.GENERAL_INTAKE, ConsentType.ABDM_DATA_SHARE);

        String abdmKey = "P15-ABDM-EXCHANGE-" + sessionId;
        mockMvc.perform(post("/api/abdm/sessions/" + sessionId + "/exchange")
                        .header("Authorization", "Bearer " + doctorToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new AbdmExchangeRequestDto("mock exchange", abdmKey))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("SUCCESS"))
                .andExpect(jsonPath("$.data.isMock").value(true));

        // Idempotent ABDM replay
        mockMvc.perform(post("/api/abdm/sessions/" + sessionId + "/exchange")
                        .header("Authorization", "Bearer " + doctorToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new AbdmExchangeRequestDto("replay", abdmKey))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("SUCCESS"));

        assertThat(integrationRecordRepository.findBySessionIdAndIntegrationType(sessionId, IntegrationType.ABDM)
                .stream().filter(r -> abdmKey.equals(r.getIdempotencyKey())).count()).isEqualTo(1);

        // 20. HIS export + idempotency
        String hisKey = "P15-HIS-EXPORT-" + sessionId;
        mockMvc.perform(post("/api/his/sessions/" + sessionId + "/export")
                        .header("Authorization", "Bearer " + doctorToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                HisExportRequestDto.builder().idempotencyKey(hisKey).build())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("SUCCESS"))
                .andExpect(jsonPath("$.data.isMock").value(true));
        mockMvc.perform(post("/api/his/sessions/" + sessionId + "/export")
                        .header("Authorization", "Bearer " + doctorToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                HisExportRequestDto.builder().idempotencyKey(hisKey).build())))
                .andExpect(status().isOk());
        assertThat(integrationRecordRepository.findBySessionIdAndIntegrationType(sessionId, IntegrationType.HIS)
                .stream().filter(r -> hisKey.equals(r.getIdempotencyKey())).count()).isEqualTo(1);

        // 21. HealthLake export + idempotency
        String hlKey = "P15-HL-EXPORT-" + sessionId;
        mockMvc.perform(post("/api/healthlake/sessions/" + sessionId + "/export")
                        .header("Authorization", "Bearer " + doctorToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                HealthLakeExportRequestDto.builder().idempotencyKey(hlKey).build())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("SUCCESS"))
                .andExpect(jsonPath("$.data.isMock").value(true));
        mockMvc.perform(post("/api/healthlake/sessions/" + sessionId + "/export")
                        .header("Authorization", "Bearer " + doctorToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                HealthLakeExportRequestDto.builder().idempotencyKey(hlKey).build())))
                .andExpect(status().isOk());
        assertThat(integrationRecordRepository.findBySessionIdAndIntegrationType(sessionId, IntegrationType.HEALTHLAKE)
                .stream().filter(r -> hlKey.equals(r.getIdempotencyKey())).count()).isEqualTo(1);

        // Clinical data remains intact after integrations
        assertThat(clinicalSummaryRepository.findBySessionId(sessionId).orElseThrow().getStatus())
                .isEqualTo(ClinicalSummaryStatus.VERIFIED);
        assertThat(symptomRepository.findBySessionId(sessionId)).isNotEmpty();
        assertThat(triageResultRepository.findBySessionId(sessionId)).isPresent();
    }

    @Test
    @Order(2)
    @DisplayName("Consent boundaries: GENERAL_INTAKE alone does not authorize ABDM; ABDM alone does not authorize intake")
    void testConsentTypeBoundariesRemainDistinct() throws Exception {
        UUID patientId = createPatient(patientToken, "MK-P15-CONSENT-001");
        UUID sessionId = createSession(patientId, patientToken);

        // ABDM consent only — clinical intake must remain blocked
        AbdmConsentRequestDto abdmOnly = new AbdmConsentRequestDto(patientId);
        mockMvc.perform(post("/api/abdm/sessions/" + sessionId + "/consent")
                        .header("Authorization", "Bearer " + patientToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(abdmOnly)))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/intake/sessions/" + sessionId + "/complaint")
                        .header("Authorization", "Bearer " + patientToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new ChiefComplaintRequest("Should be blocked", "TEXT"))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", containsString("consent")));

        // GENERAL_INTAKE permits intake
        mockMvc.perform(post("/api/intake/sessions/" + sessionId + "/consent")
                        .header("Authorization", "Bearer " + patientToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new ConsentRequest(ConsentStatus.ACCEPTED, ConsentType.GENERAL_INTAKE, "v1.0", null))))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/intake/sessions/" + sessionId + "/complaint")
                        .header("Authorization", "Bearer " + patientToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new ChiefComplaintRequest("Now allowed", "TEXT"))))
                .andExpect(status().isOk());

        List<Consent> consents = consentRepository.findAllBySessionId(sessionId);
        assertThat(consents).hasSizeGreaterThanOrEqualTo(2);
        assertThat(consents).extracting(Consent::getConsentType)
                .contains(ConsentType.GENERAL_INTAKE, ConsentType.ABDM_DATA_SHARE);
    }

    @Test
    @Order(3)
    @DisplayName("Cross-module RBAC: PATIENT cannot access doctor queue, amend/verify, or trigger exports")
    void testPatientCannotAccessDoctorOrIntegrationExports() throws Exception {
        WorkflowSeed seed = seedVerifiedSessionReadyForExport();

        mockMvc.perform(get("/api/doctor/queue")
                        .header("Authorization", "Bearer " + patientToken))
                .andExpect(status().isForbidden());

        mockMvc.perform(put("/api/doctor/sessions/" + seed.sessionId + "/summary/amend")
                        .header("Authorization", "Bearer " + patientToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new DoctorAmendmentRequest(seed.version, new ClinicalSummaryDto(), "nope"))))
                .andExpect(status().isForbidden());

        mockMvc.perform(post("/api/doctor/sessions/" + seed.sessionId + "/summary/verify")
                        .header("Authorization", "Bearer " + patientToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new DoctorVerificationRequest(seed.version, "nope", true))))
                .andExpect(status().isForbidden());

        mockMvc.perform(post("/api/abdm/sessions/" + seed.sessionId + "/exchange")
                        .header("Authorization", "Bearer " + patientToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new AbdmExchangeRequestDto("x", "P15-RBAC-ABDM"))))
                .andExpect(status().isForbidden());

        mockMvc.perform(post("/api/his/sessions/" + seed.sessionId + "/export")
                        .header("Authorization", "Bearer " + patientToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                HisExportRequestDto.builder().idempotencyKey("P15-RBAC-HIS").build())))
                .andExpect(status().isForbidden());

        mockMvc.perform(post("/api/healthlake/sessions/" + seed.sessionId + "/export")
                        .header("Authorization", "Bearer " + patientToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                HealthLakeExportRequestDto.builder().idempotencyKey("P15-RBAC-HL").build())))
                .andExpect(status().isForbidden());

        // Other patient cannot read this clinical summary
        mockMvc.perform(get("/api/intake/sessions/" + seed.sessionId + "/clinical-summary")
                        .header("Authorization", "Bearer " + otherPatientToken))
                .andExpect(status().isForbidden());
    }

    @Test
    @Order(4)
    @DisplayName("Concurrency: Doctor B stale amendment receives 409; AI snapshot preserved")
    void testOptimisticLockingAcrossDoctors() throws Exception {
        UUID patientId = createPatient(patientToken, "MK-P15-LOCK-001");
        UUID sessionId = createSession(patientId, patientToken);
        acceptGeneralConsent(sessionId);
        submitMinimalClinicalData(sessionId);
        mockMvc.perform(post("/api/intake/sessions/" + sessionId + "/complete")
                        .header("Authorization", "Bearer " + patientToken))
                .andExpect(status().isOk());
        mockMvc.perform(post("/api/intake/sessions/" + sessionId + "/clinical-summary/generate")
                        .header("Authorization", "Bearer " + patientToken))
                .andExpect(status().isCreated());

        ClinicalSummary summary = clinicalSummaryRepository.findBySessionId(sessionId).orElseThrow();
        String originalAi = summary.getSummaryData();
        int version = summary.getVersion();

        ClinicalSummaryDto amendA = objectMapper.readValue(summary.getSummaryData(), ClinicalSummaryDto.class);
        amendA.setChiefComplaint("Doctor A amendment");
        mockMvc.perform(put("/api/doctor/sessions/" + sessionId + "/summary/amend")
                        .header("Authorization", "Bearer " + doctorToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new DoctorAmendmentRequest(version, amendA, "A wins"))))
                .andExpect(status().isOk());

        ClinicalSummaryDto amendB = objectMapper.readValue(originalAi, ClinicalSummaryDto.class);
        amendB.setChiefComplaint("Doctor B stale amendment");
        mockMvc.perform(put("/api/doctor/sessions/" + sessionId + "/summary/amend")
                        .header("Authorization", "Bearer " + doctorBToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new DoctorAmendmentRequest(version, amendB, "B stale"))))
                .andExpect(status().isConflict());

        ClinicalSummary after = clinicalSummaryRepository.findBySessionId(sessionId).orElseThrow();
        assertThat(after.getOriginalAiData()).isEqualTo(originalAi);
        assertThat(after.getSummaryData()).contains("Doctor A amendment");
        assertThat(after.getSummaryData()).doesNotContain("Doctor B stale amendment");
    }

    @Test
    @Order(5)
    @DisplayName("Error resilience: failed mock integration does not destroy clinical workflow state")
    void testIntegrationFailurePreservesClinicalData() throws Exception {
        WorkflowSeed seed = seedVerifiedSessionReadyForExport();

        mockMvc.perform(post("/api/his/sessions/" + seed.sessionId + "/export")
                        .header("Authorization", "Bearer " + doctorToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                HisExportRequestDto.builder().idempotencyKey("HIS-TRIGGER_TIMEOUT-" + seed.sessionId).build())))
                .andExpect(status().isBadGateway())
                .andExpect(jsonPath("$.data.status").value("FAILED"));

        List<IntegrationRecord> hisRecords = integrationRecordRepository
                .findBySessionIdAndIntegrationType(seed.sessionId, IntegrationType.HIS);
        assertThat(hisRecords).anyMatch(r -> r.getStatus() == IntegrationStatus.FAILED);

        // Clinical state intact; doctor workflow still usable; retry with fresh key succeeds
        assertThat(clinicalSummaryRepository.findBySessionId(seed.sessionId).orElseThrow().getStatus())
                .isEqualTo(ClinicalSummaryStatus.VERIFIED);
        mockMvc.perform(get("/api/doctor/sessions/" + seed.sessionId + "/review")
                        .header("Authorization", "Bearer " + doctorToken))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/his/sessions/" + seed.sessionId + "/export")
                        .header("Authorization", "Bearer " + doctorToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                HisExportRequestDto.builder().idempotencyKey("HIS-RETRY-OK-" + seed.sessionId).build())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("SUCCESS"));
    }

    @Test
    @Order(6)
    @DisplayName("ADMIN retains access to doctor review and mock integration status")
    void testAdminAccessRetained() throws Exception {
        mockMvc.perform(get("/api/doctor/queue")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk());
        mockMvc.perform(get("/api/abdm/status").header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.connected", equalTo(false)));
        mockMvc.perform(get("/api/his/status").header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.connected", equalTo(false)));
        mockMvc.perform(get("/api/healthlake/status").header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.connected", equalTo(false)));
    }

    private User saveUser(String email, String first, String last, UserRole role) {
        User user = new User(email, passwordEncoder.encode("Pass123!"), first, last, role, UserStatus.ACTIVE);
        return userRepository.save(user);
    }

    private UUID createPatient(String token, String identifier) throws Exception {
        PatientRequest request = new PatientRequest("Test", "Patient", LocalDate.of(1990, 1, 1), "FEMALE", "+911111111111", "t@example.org");
        request.setPatientIdentifier(identifier);
        MvcResult result = mockMvc.perform(post("/api/patients")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn();
        return UUID.fromString(objectMapper.readTree(result.getResponse().getContentAsString()).path("data").path("id").asText());
    }

    private UUID createSession(UUID patientId, String token) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/intake/sessions")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new IntakeSessionRequest(patientId, "en"))))
                .andExpect(status().isCreated())
                .andReturn();
        return UUID.fromString(objectMapper.readTree(result.getResponse().getContentAsString()).path("data").path("id").asText());
    }

    private void acceptGeneralConsent(UUID sessionId) throws Exception {
        mockMvc.perform(post("/api/intake/sessions/" + sessionId + "/consent")
                        .header("Authorization", "Bearer " + patientToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new ConsentRequest(ConsentStatus.ACCEPTED, ConsentType.GENERAL_INTAKE, "v1.0", null))))
                .andExpect(status().isOk());
    }

    private void submitMinimalClinicalData(UUID sessionId) throws Exception {
        mockMvc.perform(post("/api/intake/sessions/" + sessionId + "/complaint")
                        .header("Authorization", "Bearer " + patientToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new ChiefComplaintRequest("Mild fever for two days", "TEXT"))))
                .andExpect(status().isOk());
        mockMvc.perform(post("/api/intake/sessions/" + sessionId + "/symptoms")
                        .header("Authorization", "Bearer " + patientToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new SymptomRequest("Fever", "Body", "2 days", 4))))
                .andExpect(status().isCreated());
    }

    private WorkflowSeed seedVerifiedSessionReadyForExport() throws Exception {
        UUID patientId = createPatient(patientToken, "MK-P15-SEED-" + UUID.randomUUID().toString().substring(0, 8));
        UUID sessionId = createSession(patientId, patientToken);
        acceptGeneralConsent(sessionId);
        submitMinimalClinicalData(sessionId);
        mockMvc.perform(post("/api/intake/sessions/" + sessionId + "/complete")
                        .header("Authorization", "Bearer " + patientToken))
                .andExpect(status().isOk());
        MvcResult summaryResult = mockMvc.perform(post("/api/intake/sessions/" + sessionId + "/clinical-summary/generate")
                        .header("Authorization", "Bearer " + patientToken))
                .andExpect(status().isCreated())
                .andReturn();
        int version = objectMapper.readTree(summaryResult.getResponse().getContentAsString()).path("data").path("version").asInt(1);
        mockMvc.perform(post("/api/doctor/sessions/" + sessionId + "/summary/verify")
                        .header("Authorization", "Bearer " + doctorToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new DoctorVerificationRequest(version, "seed verify", true))))
                .andExpect(status().isOk());
        ClinicalSummary verified = clinicalSummaryRepository.findBySessionId(sessionId).orElseThrow();
        return new WorkflowSeed(patientId, sessionId, verified.getVersion());
    }

    private record WorkflowSeed(UUID patientId, UUID sessionId, int version) {
    }
}
