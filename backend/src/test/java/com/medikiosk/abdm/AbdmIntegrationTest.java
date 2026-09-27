package com.medikiosk.abdm;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.medikiosk.abdm.config.AbdmProperties;
import com.medikiosk.abdm.dto.AbdmConsentRequestDto;
import com.medikiosk.abdm.dto.AbdmExchangeRequestDto;
import com.medikiosk.abdm.dto.AbhaVerificationRequestDto;
import com.medikiosk.abdm.provider.MockAbdmProvider;
import com.medikiosk.abdm.provider.SandboxAbdmProvider;
import com.medikiosk.entity.AuditLog;
import com.medikiosk.entity.ClinicalSummary;
import com.medikiosk.entity.Consent;
import com.medikiosk.entity.DoctorReview;
import com.medikiosk.entity.IntakeSession;
import com.medikiosk.entity.IntegrationRecord;
import com.medikiosk.entity.Patient;
import com.medikiosk.entity.Symptom;
import com.medikiosk.entity.TriageResult;
import com.medikiosk.entity.User;
import com.medikiosk.entity.enums.AbhaStatus;
import com.medikiosk.entity.enums.ClinicalSummaryStatus;
import com.medikiosk.entity.enums.ConsentStatus;
import com.medikiosk.entity.enums.DoctorReviewAction;
import com.medikiosk.entity.enums.IntakeSessionStatus;
import com.medikiosk.entity.enums.IntegrationStatus;
import com.medikiosk.entity.enums.IntegrationType;
import com.medikiosk.entity.enums.TriagePriority;
import com.medikiosk.entity.enums.UserRole;
import com.medikiosk.entity.enums.UserStatus;
import com.medikiosk.repository.AuditLogRepository;
import com.medikiosk.repository.ClinicalSummaryRepository;
import com.medikiosk.repository.ConsentRepository;
import com.medikiosk.repository.DoctorReviewRepository;
import com.medikiosk.repository.IntakeSessionRepository;
import com.medikiosk.repository.IntegrationRecordRepository;
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
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class AbdmIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private PatientRepository patientRepository;

    @Autowired
    private IntakeSessionRepository sessionRepository;

    @Autowired
    private ClinicalSummaryRepository summaryRepository;

    @Autowired
    private DoctorReviewRepository reviewRepository;

    @Autowired
    private ConsentRepository consentRepository;

    @Autowired
    private IntegrationRecordRepository integrationRecordRepository;

    @Autowired
    private AuditLogRepository auditLogRepository;

    @Autowired
    private SymptomRepository symptomRepository;

    @Autowired
    private TriageResultRepository triageResultRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Autowired
    private MockAbdmProvider mockAbdmProvider;

    @Autowired
    private SandboxAbdmProvider sandboxAbdmProvider;

    @Autowired
    private AbdmProperties abdmProperties;

    private User doctorUser;
    private User patientUser;
    private User otherPatientUser;
    private String doctorToken;
    private String patientToken;
    private String otherPatientToken;

    private Patient testPatient;
    private IntakeSession testSession;

    @BeforeEach
    void setUp() {
        integrationRecordRepository.deleteAll();
        auditLogRepository.deleteAll();
        consentRepository.deleteAll();
        reviewRepository.deleteAll();
        summaryRepository.deleteAll();
        triageResultRepository.deleteAll();
        symptomRepository.deleteAll();
        sessionRepository.deleteAll();
        patientRepository.deleteAll();
        userRepository.deleteAll();

        // 1. Create Doctor
        doctorUser = new User("abdm.doctor@medikiosk.org", passwordEncoder.encode("DoctorPass123!"), "Priya", "Sharma", UserRole.DOCTOR, UserStatus.ACTIVE);
        doctorUser = userRepository.save(doctorUser);
        doctorToken = jwtTokenProvider.generateToken(doctorUser);

        // 2. Create Patient User
        patientUser = new User("abdm.patient@medikiosk.org", passwordEncoder.encode("PatientPass123!"), "Aarav", "Verma", UserRole.PATIENT, UserStatus.ACTIVE);
        patientUser = userRepository.save(patientUser);
        patientToken = jwtTokenProvider.generateToken(patientUser);

        // 3. Create Other Patient User (for RBAC isolation tests)
        otherPatientUser = new User("other.patient@medikiosk.org", passwordEncoder.encode("OtherPass123!"), "Sunil", "Rao", UserRole.PATIENT, UserStatus.ACTIVE);
        otherPatientUser = userRepository.save(otherPatientUser);
        otherPatientToken = jwtTokenProvider.generateToken(otherPatientUser);

        // 4. Create Clinical Patient Record
        testPatient = new Patient("PAT-ABDM-001", "Aarav", "Verma");
        testPatient.setUserId(patientUser.getId());
        testPatient.setDateOfBirth(LocalDate.of(1988, 5, 20));
        testPatient.setGender("male");
        testPatient.setPhone("+919876543210");
        testPatient.setEmail("abdm.patient@medikiosk.org");
        testPatient.setAbhaStatus(AbhaStatus.NOT_PROVIDED);
        testPatient = patientRepository.save(testPatient);

        // 5. Create Intake Session
        testSession = new IntakeSession();
        testSession.setPatient(testPatient);
        testSession.setStatus(IntakeSessionStatus.COMPLETED);
        testSession.setLanguage("en");
        testSession.setStartedAt(Instant.now().minusSeconds(1800));
        testSession.setCompletedAt(Instant.now().minusSeconds(300));
        testSession = sessionRepository.save(testSession);

        // 6. Add Symptom
        Symptom symptom = new Symptom();
        symptom.setSession(testSession);
        symptom.setName("Chest tightness");
        symptom.setSeverity(6);
        symptom.setDuration("2 days");
        symptomRepository.save(symptom);

        // 7. Add Triage Result
        TriageResult triageResult = new TriageResult();
        triageResult.setSession(testSession);
        triageResult.setPriority(TriagePriority.URGENT);
        triageResult.setTriggeredRules("[\"RULE_CHEST_TIGHTNESS\"]");
        triageResult.setExplanation("Chest tightness evaluated");
        triageResultRepository.save(triageResult);
    }

    // =========================================================================
    // 1. Configuration & Mode Tests
    // =========================================================================

    @Test
    @DisplayName("Status Endpoint: Returns Mock status with isMock=true and connected=false")
    void testAbdmStatusEndpoint() throws Exception {
        mockMvc.perform(get("/api/abdm/status")
                        .header("Authorization", "Bearer " + doctorToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", equalTo(true)))
                .andExpect(jsonPath("$.data.mode", equalTo("MOCK")))
                .andExpect(jsonPath("$.data.isMock", equalTo(true)))
                .andExpect(jsonPath("$.data.connected", equalTo(false)))
                .andExpect(jsonPath("$.data.message", containsString("NOT CONNECTED")));
    }

    @Test
    @DisplayName("Sandbox Provider: Gracefully reports unavailable without authentic credentials")
    void testSandboxProviderWithoutCredentials() {
        AbdmProperties customProps = new AbdmProperties();
        customProps.setMode("SANDBOX");
        customProps.setClientId(null);
        customProps.setClientSecret(null);

        SandboxAbdmProvider provider = new SandboxAbdmProvider(customProps);
        var status = provider.getStatus();
        assertFalse(status.isConnected());
        assertTrue(status.getMessage().contains("missing"));
    }

    // =========================================================================
    // 2. ABHA Verification Tests
    // =========================================================================

    @Test
    @DisplayName("ABHA Initial State: Patient initializes with AbhaStatus.NOT_PROVIDED")
    void testAbhaInitialState() {
        Patient p = patientRepository.findById(testPatient.getId()).orElseThrow();
        assertEquals(AbhaStatus.NOT_PROVIDED, p.getAbhaStatus());
        assertNull(p.getAbhaId());
        assertNull(p.getAbhaVerifiedAt());
    }

    @Test
    @DisplayName("ABHA Verification: Valid 14-digit ABHA succeeds and updates patient to VERIFIED with masked view")
    void testSuccessfulAbhaVerificationNumber() throws Exception {
        AbhaVerificationRequestDto request = new AbhaVerificationRequestDto("12-3456-7890-1234");

        mockMvc.perform(post("/api/abdm/patients/" + testPatient.getId() + "/abha/verify")
                        .header("Authorization", "Bearer " + patientToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", equalTo(true)))
                .andExpect(jsonPath("$.data.verified", equalTo(true)))
                .andExpect(jsonPath("$.data.status", equalTo("VERIFIED")))
                .andExpect(jsonPath("$.data.maskedAbha", equalTo("XX-XXXX-XXXX-1234")))
                .andExpect(jsonPath("$.data.isMock", equalTo(true)));

        // Verify DB update
        Patient updated = patientRepository.findById(testPatient.getId()).orElseThrow();
        assertEquals(AbhaStatus.VERIFIED, updated.getAbhaStatus());
        assertEquals("12-3456-7890-1234", updated.getAbhaId());
        assertNotNull(updated.getAbhaVerifiedAt());
        assertEquals("XX-XXXX-XXXX-1234", updated.getMaskedAbha());

        // Verify IntegrationRecord
        List<IntegrationRecord> records = integrationRecordRepository.findByPatientId(testPatient.getId());
        assertFalse(records.isEmpty());
        assertEquals(IntegrationType.ABDM, records.get(0).getIntegrationType());
        assertEquals("ABHA_VERIFICATION", records.get(0).getOperation());
        assertEquals(IntegrationStatus.SUCCESS, records.get(0).getStatus());

        // Verify AuditLog
        List<AuditLog> auditLogs = auditLogRepository.findByAction("ABHA_VERIFICATION_SUCCEEDED");
        assertEquals(1, auditLogs.size());
    }

    @Test
    @DisplayName("ABHA Verification: Valid ABHA Address (name@abdm) succeeds and updates patient")
    void testSuccessfulAbhaVerificationAddress() throws Exception {
        AbhaVerificationRequestDto request = new AbhaVerificationRequestDto("aarav.verma@sbx");

        mockMvc.perform(post("/api/abdm/patients/" + testPatient.getId() + "/abha/verify")
                        .header("Authorization", "Bearer " + patientToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.verified", equalTo(true)))
                .andExpect(jsonPath("$.data.status", equalTo("VERIFIED")))
                .andExpect(jsonPath("$.data.maskedAbha", equalTo("aa***@sbx")));

        Patient updated = patientRepository.findById(testPatient.getId()).orElseThrow();
        assertEquals(AbhaStatus.VERIFIED, updated.getAbhaStatus());
    }

    @Test
    @DisplayName("ABHA Verification: Failed mock verification leaves status as UNVERIFIED (No false verification)")
    void testFailedAbhaVerification() throws Exception {
        AbhaVerificationRequestDto request = new AbhaVerificationRequestDto("00-0000-0000-0000");

        mockMvc.perform(post("/api/abdm/patients/" + testPatient.getId() + "/abha/verify")
                        .header("Authorization", "Bearer " + patientToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.verified", equalTo(false)))
                .andExpect(jsonPath("$.data.status", equalTo("UNVERIFIED")))
                .andExpect(jsonPath("$.data.message", containsString("failure")));

        Patient updated = patientRepository.findById(testPatient.getId()).orElseThrow();
        assertEquals(AbhaStatus.UNVERIFIED, updated.getAbhaStatus());
        assertNull(updated.getAbhaVerifiedAt());

        // Verify AuditLog logged failure
        List<AuditLog> auditLogs = auditLogRepository.findByAction("ABHA_VERIFICATION_FAILED");
        assertEquals(1, auditLogs.size());
    }

    @Test
    @DisplayName("ABHA Verification: Malformed ABHA ID returns error response")
    void testMalformedAbhaVerification() throws Exception {
        AbhaVerificationRequestDto request = new AbhaVerificationRequestDto("123");

        mockMvc.perform(post("/api/abdm/patients/" + testPatient.getId() + "/abha/verify")
                        .header("Authorization", "Bearer " + patientToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    // =========================================================================
    // 3. ABDM Consent Lifecycle Tests
    // =========================================================================

    @Test
    @DisplayName("Consent: Granting ABDM data sharing consent creates ACCEPTED consent with artefact ID")
    void testGrantAbdmConsent() throws Exception {
        AbdmConsentRequestDto request = new AbdmConsentRequestDto(testPatient.getId());
        request.setValidMonths(6);

        mockMvc.perform(post("/api/abdm/sessions/" + testSession.getId() + "/consent")
                        .header("Authorization", "Bearer " + patientToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", equalTo(true)))
                .andExpect(jsonPath("$.data.consentStatus", equalTo("ACCEPTED")))
                .andExpect(jsonPath("$.data.consentType", equalTo("ABDM_DATA_SHARE")))
                .andExpect(jsonPath("$.data.externalArtefactId", containsString("MOCK-ARTEFACT-")));

        // Verify DB Consent
        Consent consent = consentRepository.findFirstBySessionIdAndConsentTypeOrderByConsentedAtDesc(
                testSession.getId(), "ABDM_DATA_SHARE").orElseThrow();
        assertEquals(ConsentStatus.ACCEPTED, consent.getConsentStatus());
        assertNull(consent.getRevokedAt());

        // Verify AuditLog
        List<AuditLog> logs = auditLogRepository.findByAction("ABDM_CONSENT_GRANTED");
        assertEquals(1, logs.size());
    }

    @Test
    @DisplayName("Consent: Revoking ABDM consent transitions status to REVOKED with timestamp")
    void testRevokeAbdmConsent() throws Exception {
        // Grant first
        Consent consent = new Consent(testPatient, testSession, ConsentStatus.ACCEPTED, "ABDM_DATA_SHARE", "v1.0");
        consent.setExternalReference("MOCK-ARTEFACT-INITIAL");
        consentRepository.save(consent);

        mockMvc.perform(post("/api/abdm/sessions/" + testSession.getId() + "/consent/revoke")
                        .header("Authorization", "Bearer " + patientToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", equalTo(true)))
                .andExpect(jsonPath("$.data.consentStatus", equalTo("REVOKED")))
                .andExpect(jsonPath("$.data.revokedAt", notNullValue()));

        Consent updated = consentRepository.findById(consent.getId()).orElseThrow();
        assertEquals(ConsentStatus.REVOKED, updated.getConsentStatus());
        assertNotNull(updated.getRevokedAt());

        List<AuditLog> logs = auditLogRepository.findByAction("ABDM_CONSENT_REVOKED");
        assertEquals(1, logs.size());
    }

    // =========================================================================
    // 4. Clinical Data Exchange & Gating Tests
    // =========================================================================

    @Test
    @DisplayName("Exchange Gating: Unverified AI summary is blocked with PRECONDITION_FAILED (412)")
    void testExchangeBlockedForUnverifiedSummary() throws Exception {
        // Create unverified AI summary
        ClinicalSummary unverifiedSummary = new ClinicalSummary(testPatient, testSession, "Chest tightness AI draft", ClinicalSummaryStatus.AI_GENERATED);
        summaryRepository.save(unverifiedSummary);

        // Grant consent
        Consent consent = new Consent(testPatient, testSession, ConsentStatus.ACCEPTED, "ABDM_DATA_SHARE", "v1.0");
        consent.setExternalReference("MOCK-ARTEFACT-123");
        consentRepository.save(consent);

        mockMvc.perform(post("/api/abdm/sessions/" + testSession.getId() + "/exchange")
                        .header("Authorization", "Bearer " + doctorToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isPreconditionFailed())
                .andExpect(jsonPath("$.message", containsString("Clinical summary must be verified by a licensed doctor")));
    }

    @Test
    @DisplayName("Exchange Gating: Verified summary without active patient consent is blocked (412)")
    void testExchangeBlockedWithoutConsent() throws Exception {
        // Create verified summary
        ClinicalSummary verifiedSummary = new ClinicalSummary(testPatient, testSession, "Doctor verified summary", ClinicalSummaryStatus.VERIFIED);
        summaryRepository.save(verifiedSummary);

        // No consent present
        mockMvc.perform(post("/api/abdm/sessions/" + testSession.getId() + "/exchange")
                        .header("Authorization", "Bearer " + doctorToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isPreconditionFailed())
                .andExpect(jsonPath("$.message", containsString("Active patient consent for ABDM data sharing is required")));
    }

    @Test
    @DisplayName("Exchange Gating: Verified summary with revoked consent is blocked (412)")
    void testExchangeBlockedWithRevokedConsent() throws Exception {
        ClinicalSummary verifiedSummary = new ClinicalSummary(testPatient, testSession, "Doctor verified summary", ClinicalSummaryStatus.VERIFIED);
        summaryRepository.save(verifiedSummary);

        Consent revokedConsent = new Consent(testPatient, testSession, ConsentStatus.REVOKED, "ABDM_DATA_SHARE", "v1.0");
        revokedConsent.setRevokedAt(Instant.now());
        consentRepository.save(revokedConsent);

        mockMvc.perform(post("/api/abdm/sessions/" + testSession.getId() + "/exchange")
                        .header("Authorization", "Bearer " + doctorToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isPreconditionFailed())
                .andExpect(jsonPath("$.message", containsString("Active patient consent for ABDM data sharing is required")));
    }

    @Test
    @DisplayName("Exchange Success: Verified summary + active consent succeeds and generates IntegrationRecord and AuditLog")
    void testSuccessfulClinicalExchange() throws Exception {
        // Set up verified summary
        ClinicalSummary verifiedSummary = new ClinicalSummary(testPatient, testSession, "Verified clinical summary for ABDM", ClinicalSummaryStatus.VERIFIED);
        summaryRepository.save(verifiedSummary);

        // Set up active consent
        Consent consent = new Consent(testPatient, testSession, ConsentStatus.ACCEPTED, "ABDM_DATA_SHARE", "v1.0");
        consent.setExternalReference("MOCK-ARTEFACT-VALID");
        consentRepository.save(consent);

        // Associate verified ABHA
        testPatient.setAbhaId("12-3456-7890-1234");
        testPatient.setAbhaStatus(AbhaStatus.VERIFIED);
        patientRepository.save(testPatient);

        AbdmExchangeRequestDto request = new AbdmExchangeRequestDto("Routine referral exchange", "KEY-SESSION-" + testSession.getId());

        mockMvc.perform(post("/api/abdm/sessions/" + testSession.getId() + "/exchange")
                        .header("Authorization", "Bearer " + doctorToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", equalTo(true)))
                .andExpect(jsonPath("$.data.status", equalTo("SUCCESS")))
                .andExpect(jsonPath("$.data.transactionId", containsString("MOCK-TXN-")))
                .andExpect(jsonPath("$.data.bundleId", equalTo("Bundle-" + testSession.getId())))
                .andExpect(jsonPath("$.data.isMock", equalTo(true)));

        // Verify IntegrationRecord persisted
        List<IntegrationRecord> records = integrationRecordRepository.findBySessionIdAndIntegrationType(testSession.getId(), IntegrationType.ABDM);
        assertFalse(records.isEmpty());
        IntegrationRecord exchangeRec = records.stream()
                .filter(r -> "ABDM_CLINICAL_EXCHANGE".equals(r.getOperation()))
                .findFirst().orElseThrow();
        assertEquals(IntegrationStatus.SUCCESS, exchangeRec.getStatus());
        assertNotNull(exchangeRec.getExternalReference());
        assertNotNull(exchangeRec.getResponsePayload());

        // Verify AuditLog
        List<AuditLog> logs = auditLogRepository.findByAction("ABDM_SUCCEEDED");
        assertEquals(1, logs.size());
    }

    @Test
    @DisplayName("Idempotency: Replaying exchange with same idempotency key returns existing successful result without re-transmitting")
    void testIdempotentExchangeReplay() throws Exception {
        ClinicalSummary verifiedSummary = new ClinicalSummary(testPatient, testSession, "Verified clinical summary", ClinicalSummaryStatus.VERIFIED);
        summaryRepository.save(verifiedSummary);

        Consent consent = new Consent(testPatient, testSession, ConsentStatus.ACCEPTED, "ABDM_DATA_SHARE", "v1.0");
        consent.setExternalReference("MOCK-ARTEFACT-VALID");
        consentRepository.save(consent);

        String idempotencyKey = "IDEMPOTENT-KEY-999";
        AbdmExchangeRequestDto request = new AbdmExchangeRequestDto("First submission", idempotencyKey);

        // 1st request
        mockMvc.perform(post("/api/abdm/sessions/" + testSession.getId() + "/exchange")
                        .header("Authorization", "Bearer " + doctorToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        int initialAuditCount = auditLogRepository.findByAction("ABDM_SUCCEEDED").size();
        assertEquals(1, initialAuditCount);

        // 2nd duplicate request with same key
        mockMvc.perform(post("/api/abdm/sessions/" + testSession.getId() + "/exchange")
                        .header("Authorization", "Bearer " + doctorToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.message", containsString("Idempotent response")));

        // Verify no duplicate audit log for 2nd request
        int subsequentAuditCount = auditLogRepository.findByAction("ABDM_SUCCEEDED").size();
        assertEquals(1, subsequentAuditCount);
    }

    // =========================================================================
    // 5. Security & RBAC Isolation Tests
    // =========================================================================

    @Test
    @DisplayName("RBAC: Patient cannot initiate clinical exchange (Forbidden 403)")
    void testPatientCannotInitiateExchange() throws Exception {
        mockMvc.perform(post("/api/abdm/sessions/" + testSession.getId() + "/exchange")
                        .header("Authorization", "Bearer " + patientToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("RBAC: Patient cannot verify or modify ABHA for another patient (Forbidden 403)")
    void testPatientCannotModifyOtherPatientAbha() throws Exception {
        AbhaVerificationRequestDto request = new AbhaVerificationRequestDto("12-3456-7890-1234");

        // otherPatientToken attempting to verify testPatient
        mockMvc.perform(post("/api/abdm/patients/" + testPatient.getId() + "/abha/verify")
                        .header("Authorization", "Bearer " + otherPatientToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Autowired
    private com.medikiosk.abdm.adapter.AbdmAdapter abdmAdapter;

    @Test
    @DisplayName("AbdmAdapter: Verification of architectural boundary adapter facade")
    void testAbdmAdapterFacade() {
        assertTrue(abdmAdapter.isAvailable());
    }

    @Test
    @DisplayName("History: Doctor can retrieve session ABDM transaction history")
    void testDoctorCanRetrieveSessionHistory() throws Exception {
        IntegrationRecord rec = new IntegrationRecord(IntegrationType.ABDM, "ABHA_VERIFICATION", testPatient, testSession);
        rec.setStatus(IntegrationStatus.SUCCESS);
        integrationRecordRepository.save(rec);

        mockMvc.perform(get("/api/abdm/sessions/" + testSession.getId() + "/status")
                        .header("Authorization", "Bearer " + doctorToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", equalTo(true)))
                .andExpect(jsonPath("$.data", hasSize(1)));
    }
}
