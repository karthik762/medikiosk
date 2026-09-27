package com.medikiosk.his;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.medikiosk.entity.AuditLog;
import com.medikiosk.entity.ClinicalSummary;
import com.medikiosk.entity.DoctorReview;
import com.medikiosk.entity.IntakeSession;
import com.medikiosk.entity.IntegrationRecord;
import com.medikiosk.entity.Patient;
import com.medikiosk.entity.Symptom;
import com.medikiosk.entity.TriageResult;
import com.medikiosk.entity.User;
import com.medikiosk.entity.enums.ClinicalSummaryStatus;
import com.medikiosk.entity.enums.DoctorReviewAction;
import com.medikiosk.entity.enums.IntakeSessionStatus;
import com.medikiosk.entity.enums.IntegrationStatus;
import com.medikiosk.entity.enums.IntegrationType;
import com.medikiosk.entity.enums.TriagePriority;
import com.medikiosk.entity.enums.UserRole;
import com.medikiosk.entity.enums.UserStatus;
import com.medikiosk.his.config.HisProperties;
import com.medikiosk.his.dto.HisExportRequestDto;
import com.medikiosk.his.dto.HisLinkPatientRequestDto;
import com.medikiosk.his.provider.HospitalHisProvider;
import com.medikiosk.repository.AuditLogRepository;
import com.medikiosk.repository.ClinicalSummaryRepository;
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

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class HisIntegrationTest {

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

    private User doctorUser;
    private String doctorToken;

    private User adminUser;
    private String adminToken;

    private User patientUser;
    private String patientToken;

    private Patient testPatient;
    private IntakeSession testSession;
    private ClinicalSummary testSummary;

    @BeforeEach
    void setUp() {
        integrationRecordRepository.deleteAll();
        auditLogRepository.deleteAll();
        reviewRepository.deleteAll();
        summaryRepository.deleteAll();
        triageResultRepository.deleteAll();
        symptomRepository.deleteAll();
        sessionRepository.deleteAll();
        patientRepository.deleteAll();
        userRepository.deleteAll();

        // 1. Create Doctor
        doctorUser = new User("dr.chen@medikiosk.org", passwordEncoder.encode("DoctorPass123!"), "Robert", "Chen", UserRole.DOCTOR, UserStatus.ACTIVE);
        doctorUser = userRepository.save(doctorUser);
        doctorToken = jwtTokenProvider.generateToken(doctorUser);

        // 2. Create Admin
        adminUser = new User("admin.his@medikiosk.org", passwordEncoder.encode("AdminPass123!"), "Hospital", "Admin", UserRole.ADMIN, UserStatus.ACTIVE);
        adminUser = userRepository.save(adminUser);
        adminToken = jwtTokenProvider.generateToken(adminUser);

        // 3. Create Patient User & Clinical Record
        patientUser = new User("rajesh.kumar@medikiosk.org", passwordEncoder.encode("PatientPass123!"), "Rajesh", "Kumar", UserRole.PATIENT, UserStatus.ACTIVE);
        patientUser = userRepository.save(patientUser);
        patientToken = jwtTokenProvider.generateToken(patientUser);

        testPatient = new Patient("PAT-HIS-001", "Rajesh", "Kumar");
        testPatient.setUserId(patientUser.getId());
        testPatient.setDateOfBirth(LocalDate.of(1982, 3, 15));
        testPatient.setGender("male");
        testPatient.setPhone("+919876543210");
        testPatient.setEmail("rajesh.kumar@medikiosk.org");
        testPatient = patientRepository.save(testPatient);

        // 4. Create Intake Session
        testSession = new IntakeSession();
        testSession.setPatient(testPatient);
        testSession.setStatus(IntakeSessionStatus.COMPLETED);
        testSession.setLanguage("en");
        testSession.setStartedAt(Instant.now().minusSeconds(1800));
        testSession.setCompletedAt(Instant.now().minusSeconds(300));
        testSession = sessionRepository.save(testSession);

        // 5. Add Symptom
        Symptom symptom = new Symptom();
        symptom.setSession(testSession);
        symptom.setName("Chest tightness");
        symptom.setSeverity(6);
        symptom.setDuration("2 days");
        symptomRepository.save(symptom);

        // 6. Add Triage Result
        TriageResult triage = new TriageResult();
        triage.setSession(testSession);
        triage.setPriority(TriagePriority.URGENT);
        triage.setTriggeredRules("[\"RULE_CHEST_TIGHTNESS\"]");
        triage.setExplanation("Chest tightness evaluated");
        triageResultRepository.save(triage);

        // 7. Add Clinical Summary (defaults to AI_GENERATED)
        testSummary = new ClinicalSummary(testPatient, testSession, "Patient reports acute chest tightness starting 48 hours ago.", ClinicalSummaryStatus.AI_GENERATED);
        testSummary = summaryRepository.save(testSummary);
    }

    @Test
    @DisplayName("HIS Status: Returns MOCK mode, isMock=true, and explicitly NOT CONNECTED")
    void testHisStatus_returnsMockModeAndNotConnected() throws Exception {
        mockMvc.perform(get("/api/his/status")
                        .header("Authorization", "Bearer " + doctorToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", equalTo(true)))
                .andExpect(jsonPath("$.data.mode", equalTo("MOCK")))
                .andExpect(jsonPath("$.data.isMock", equalTo(true)))
                .andExpect(jsonPath("$.data.connected", equalTo(false)))
                .andExpect(jsonPath("$.data.message", containsString("NOT CONNECTED")));
    }

    @Test
    @DisplayName("HIS Export Gate: Blocked with HTTP 412 PRECONDITION_FAILED when summary is unverified")
    void testHisExport_blockedWhenUnverified_returnsPreconditionFailed() throws Exception {
        // testSummary is currently in AI_GENERATED status
        HisExportRequestDto request = HisExportRequestDto.builder()
                .intakeQueue("OPD_INTAKE")
                .idempotencyKey("HIS-TEST-KEY-1")
                .build();

        mockMvc.perform(post("/api/his/sessions/" + testSession.getId() + "/export")
                        .header("Authorization", "Bearer " + doctorToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isPreconditionFailed())
                .andExpect(jsonPath("$.success", equalTo(false)))
                .andExpect(jsonPath("$.message", containsString("Clinical summary must be verified by a licensed doctor")));

        // Verify no integration records created
        assertEquals(0, integrationRecordRepository.count());
    }

    @Test
    @DisplayName("HIS Export: Succeeds when summary is doctor verified, persists IntegrationRecord, and logs audit events")
    void testHisExport_succeedsWhenDoctorVerified() throws Exception {
        // 1. Doctor verifies summary
        DoctorReview review = new DoctorReview();
        review.setSummary(testSummary);
        review.setDoctor(doctorUser);
        review.setAction(DoctorReviewAction.VERIFIED);
        review.setComments("Verified. Patient cleared for OPD consultation queue.");
        review.setSummaryVersion(1);
        review.setVerifiedAt(Instant.now());
        reviewRepository.save(review);

        testSummary.setStatus(ClinicalSummaryStatus.VERIFIED);
        summaryRepository.save(testSummary);

        // 2. Perform HIS Export
        HisExportRequestDto request = HisExportRequestDto.builder()
                .intakeQueue("OPD_INTAKE")
                .hisId("HIS-MRN-90210")
                .idempotencyKey("HIS-TEST-KEY-2")
                .build();

        mockMvc.perform(post("/api/his/sessions/" + testSession.getId() + "/export")
                        .header("Authorization", "Bearer " + doctorToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", equalTo(true)))
                .andExpect(jsonPath("$.data.status", equalTo("SUCCESS")))
                .andExpect(jsonPath("$.data.encounterReference", notNullValue()))
                .andExpect(jsonPath("$.data.isMock", equalTo(true)))
                .andExpect(jsonPath("$.data.hisId", equalTo("HIS-MRN-90210")))
                .andExpect(jsonPath("$.data.intakeQueue", equalTo("OPD_INTAKE")));

        // 3. Verify IntegrationRecord stored
        List<IntegrationRecord> records = integrationRecordRepository.findBySessionIdAndIntegrationType(testSession.getId(), IntegrationType.HIS);
        assertEquals(1, records.size());
        IntegrationRecord rec = records.get(0);
        assertEquals(IntegrationType.HIS, rec.getIntegrationType());
        assertEquals("HIS_CLINICAL_EXPORT", rec.getOperation());
        assertEquals(IntegrationStatus.SUCCESS, rec.getStatus());
        assertNotNull(rec.getExternalReference());
        assertNotNull(rec.getCompletedAt());

        // 4. Verify AuditLog entries
        List<AuditLog> auditLogs = auditLogRepository.findAll();
        boolean requestedFound = auditLogs.stream().anyMatch(a -> "HIS_REQUESTED".equals(a.getAction()));
        boolean succeededFound = auditLogs.stream().anyMatch(a -> "HIS_SUCCEEDED".equals(a.getAction()));
        assertTrue(requestedFound, "HIS_REQUESTED audit log should be present");
        assertTrue(succeededFound, "HIS_SUCCEEDED audit log should be present");
    }

    @Test
    @DisplayName("HIS Export: Idempotent replay returns existing record without duplicate transmission")
    void testHisExport_idempotentReplayReturnsExistingRecord() throws Exception {
        testSummary.setStatus(ClinicalSummaryStatus.VERIFIED);
        summaryRepository.save(testSummary);

        HisExportRequestDto request = HisExportRequestDto.builder()
                .intakeQueue("TRIAGE")
                .idempotencyKey("HIS-IDEMPOTENT-REPLAY-KEY")
                .build();

        // First call
        mockMvc.perform(post("/api/his/sessions/" + testSession.getId() + "/export")
                        .header("Authorization", "Bearer " + doctorToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status", equalTo("SUCCESS")));

        assertEquals(1, integrationRecordRepository.count());

        // Second call with identical key
        mockMvc.perform(post("/api/his/sessions/" + testSession.getId() + "/export")
                        .header("Authorization", "Bearer " + doctorToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status", equalTo("SUCCESS")))
                .andExpect(jsonPath("$.data.message", containsString("Idempotent response")));

        assertEquals(1, integrationRecordRepository.count());
    }

    @Test
    @DisplayName("HIS Export: Simulated timeout and server error handled gracefully")
    void testHisExport_simulatedTimeoutAndServerError_handledGracefully() throws Exception {
        testSummary.setStatus(ClinicalSummaryStatus.VERIFIED);
        testSummary.setSummaryText("Patient TRIGGER_TIMEOUT test case");
        summaryRepository.save(testSummary);

        HisExportRequestDto timeoutRequest = HisExportRequestDto.builder()
                .intakeQueue("OPD_INTAKE")
                .idempotencyKey("HIS-TIMEOUT-KEY")
                .build();

        mockMvc.perform(post("/api/his/sessions/" + testSession.getId() + "/export")
                        .header("Authorization", "Bearer " + doctorToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(timeoutRequest)))
                .andExpect(status().isBadGateway())
                .andExpect(jsonPath("$.data.status", equalTo("FAILED")))
                .andExpect(jsonPath("$.data.errorMessage", containsString("timeout")));

        List<IntegrationRecord> records = integrationRecordRepository.findAll();
        assertEquals(1, records.size());
        assertEquals(IntegrationStatus.FAILED, records.get(0).getStatus());
        assertTrue(records.get(0).getErrorMessage().contains("timeout"));
    }

    @Test
    @DisplayName("HIS Patient Search: Returns matching hospital candidates in mock mode")
    void testHisPatientSearch_returnsCandidatesInMockMode() throws Exception {
        mockMvc.perform(get("/api/his/patients/search?query=Aarav")
                        .header("Authorization", "Bearer " + doctorToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", equalTo(true)))
                .andExpect(jsonPath("$.data.isMock", equalTo(true)))
                .andExpect(jsonPath("$.data.totalFound", equalTo(1)))
                .andExpect(jsonPath("$.data.candidates[0].displayName", equalTo("Aarav Sharma")))
                .andExpect(jsonPath("$.data.candidates[0].hisId", equalTo("HIS-MRN-90210")));

        // Admin role can also search
        mockMvc.perform(get("/api/his/patients/search?query=Priya")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.totalFound", equalTo(1)))
                .andExpect(jsonPath("$.data.candidates[0].hisId", equalTo("HIS-MRN-84521")));

        // Empty query search
        mockMvc.perform(get("/api/his/patients/search?query=EMPTY")
                        .header("Authorization", "Bearer " + doctorToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.totalFound", equalTo(0)));
    }

    @Test
    @DisplayName("HIS Patient Link: Associates hospital HIS MRN with patient, creates record, and logs audit")
    void testHisLinkPatient_linksHisIdToPatientSuccessfully() throws Exception {
        HisLinkPatientRequestDto linkRequest = HisLinkPatientRequestDto.builder()
                .hisId("HIS-MRN-90210")
                .notes("Verified with hospital OPD desk")
                .build();

        mockMvc.perform(post("/api/his/patients/" + testPatient.getId() + "/link")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(linkRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", equalTo(true)))
                .andExpect(jsonPath("$.data.patientId", equalTo(testPatient.getId().toString())))
                .andExpect(jsonPath("$.data.hisId", equalTo("HIS-MRN-90210")));

        // Verify patient record updated in DB
        Patient updatedPatient = patientRepository.findById(testPatient.getId()).orElseThrow();
        assertEquals("HIS-MRN-90210", updatedPatient.getHisId());

        // Verify IntegrationRecord created
        List<IntegrationRecord> records = integrationRecordRepository.findAll();
        assertEquals(1, records.size());
        assertEquals("HIS_PATIENT_LINK", records.get(0).getOperation());
        assertEquals("HIS-MRN-90210", records.get(0).getExternalReference());

        // Verify AuditLog created
        boolean linkAuditFound = auditLogRepository.findAll().stream()
                .anyMatch(a -> "HIS_PATIENT_LINK".equals(a.getAction()));
        assertTrue(linkAuditFound, "HIS_PATIENT_LINK audit log should be present");
    }

    @Test
    @DisplayName("HIS Security: Patients cannot export or link hospital records (HTTP 403 FORBIDDEN)")
    void testHisSecurity_patientRoleForbidden() throws Exception {
        HisExportRequestDto exportRequest = HisExportRequestDto.builder()
                .intakeQueue("OPD_INTAKE")
                .idempotencyKey("HIS-UNAUTHORIZED-KEY")
                .build();

        // Patient attempting export -> 403 Forbidden
        mockMvc.perform(post("/api/his/sessions/" + testSession.getId() + "/export")
                        .header("Authorization", "Bearer " + patientToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(exportRequest)))
                .andExpect(status().isForbidden());

        // Patient attempting patient search -> 403 Forbidden
        mockMvc.perform(get("/api/his/patients/search?query=Aarav")
                        .header("Authorization", "Bearer " + patientToken))
                .andExpect(status().isForbidden());

        // Patient attempting patient link -> 403 Forbidden
        HisLinkPatientRequestDto linkRequest = HisLinkPatientRequestDto.builder()
                .hisId("HIS-MRN-90210")
                .build();

        mockMvc.perform(post("/api/his/patients/" + testPatient.getId() + "/link")
                        .header("Authorization", "Bearer " + patientToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(linkRequest)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("HospitalHisProvider: Reports unavailable and missing credentials when unconfigured")
    void testHospitalHisProvider_reportsUnavailableWhenCredsMissing() {
        HisProperties properties = new HisProperties();
        properties.setMode("LIVE");
        properties.setEnabled(true);

        HospitalHisProvider provider = new HospitalHisProvider(properties);
        var status = provider.getStatus();

        assertFalse(status.isMock());
        assertFalse(status.isConnected());
        assertTrue(status.getMessage().contains("credentials missing"));
    }
}
