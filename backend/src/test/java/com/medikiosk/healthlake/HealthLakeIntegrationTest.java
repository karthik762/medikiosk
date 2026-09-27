package com.medikiosk.healthlake;

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
import com.medikiosk.healthlake.config.HealthLakeProperties;
import com.medikiosk.healthlake.dto.HealthLakeExportRequestDto;
import com.medikiosk.healthlake.dto.HealthLakeStatusDto;
import com.medikiosk.healthlake.provider.AwsHealthLakeProvider;
import com.medikiosk.healthlake.provider.HealthLakeExportResult;
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
public class HealthLakeIntegrationTest {

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
        doctorUser = new User("dr.healthlake@medikiosk.org", passwordEncoder.encode("DoctorPass123!"), "Maya", "Sharma", UserRole.DOCTOR, UserStatus.ACTIVE);
        doctorUser = userRepository.save(doctorUser);
        doctorToken = jwtTokenProvider.generateToken(doctorUser);

        // 2. Create Admin
        adminUser = new User("admin.healthlake@medikiosk.org", passwordEncoder.encode("AdminPass123!"), "Cloud", "Admin", UserRole.ADMIN, UserStatus.ACTIVE);
        adminUser = userRepository.save(adminUser);
        adminToken = jwtTokenProvider.generateToken(adminUser);

        // 3. Create Patient User & Patient
        patientUser = new User("sunita.devi@medikiosk.org", passwordEncoder.encode("PatientPass123!"), "Sunita", "Devi", UserRole.PATIENT, UserStatus.ACTIVE);
        patientUser = userRepository.save(patientUser);
        patientToken = jwtTokenProvider.generateToken(patientUser);

        testPatient = new Patient("PAT-HL-001", "Sunita", "Devi");
        testPatient.setUserId(patientUser.getId());
        testPatient.setDateOfBirth(LocalDate.of(1985, 8, 14));
        testPatient.setGender("female");
        testPatient.setPhone("+919123456789");
        testPatient.setEmail("sunita.devi@medikiosk.org");
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
        symptom.setName("Shortness of breath");
        symptom.setSeverity(7);
        symptom.setDuration("3 days");
        symptomRepository.save(symptom);

        // 6. Add Triage Result
        TriageResult triage = new TriageResult();
        triage.setSession(testSession);
        triage.setPriority(TriagePriority.URGENT);
        triage.setTriggeredRules("[\"RULE_RESPIRATORY_DISTRESS\"]");
        triage.setExplanation("Shortness of breath requiring clinical evaluation");
        triageResultRepository.save(triage);

        // 7. Add Clinical Summary (defaults to AI_GENERATED)
        testSummary = new ClinicalSummary(testPatient, testSession, "Patient presents with shortness of breath on exertion for 3 days.", ClinicalSummaryStatus.AI_GENERATED);
        testSummary = summaryRepository.save(testSummary);
    }

    @Test
    @DisplayName("HealthLake Status: Returns MOCK mode, isMock=true, and explicitly NOT CONNECTED")
    void testHealthLakeStatus_returnsMockModeAndNotConnected() throws Exception {
        mockMvc.perform(get("/api/healthlake/status")
                        .header("Authorization", "Bearer " + doctorToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", equalTo(true)))
                .andExpect(jsonPath("$.data.mode", equalTo("MOCK")))
                .andExpect(jsonPath("$.data.isMock", equalTo(true)))
                .andExpect(jsonPath("$.data.connected", equalTo(false)))
                .andExpect(jsonPath("$.data.message", containsString("NOT CONNECTED")));
    }

    @Test
    @DisplayName("HealthLake Export Gate: Blocked with HTTP 412 PRECONDITION_FAILED when summary is unverified")
    void testHealthLakeExport_blockedWhenUnverified_returnsPreconditionFailed() throws Exception {
        HealthLakeExportRequestDto request = HealthLakeExportRequestDto.builder()
                .idempotencyKey("HL-TEST-KEY-1")
                .build();

        mockMvc.perform(post("/api/healthlake/sessions/" + testSession.getId() + "/export")
                        .header("Authorization", "Bearer " + doctorToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isPreconditionFailed())
                .andExpect(jsonPath("$.success", equalTo(false)))
                .andExpect(jsonPath("$.message", containsString("Clinical summary must be verified by a licensed doctor")));

        assertEquals(0, integrationRecordRepository.count());
    }

    @Test
    @DisplayName("HealthLake Export: Succeeds when summary is doctor verified, persists IntegrationRecord, updates healthlakeRef, and logs audit events")
    void testHealthLakeExport_succeedsWhenDoctorVerified() throws Exception {
        // 1. Doctor verifies summary
        DoctorReview review = new DoctorReview();
        review.setSummary(testSummary);
        review.setDoctor(doctorUser);
        review.setAction(DoctorReviewAction.VERIFIED);
        review.setComments("Verified and approved for cloud FHIR export.");
        review.setSummaryVersion(1);
        review.setVerifiedAt(Instant.now());
        reviewRepository.save(review);

        testSummary.setStatus(ClinicalSummaryStatus.VERIFIED);
        summaryRepository.save(testSummary);

        // 2. Perform Export
        HealthLakeExportRequestDto request = HealthLakeExportRequestDto.builder()
                .idempotencyKey("HL-TEST-KEY-2")
                .notes("Export to primary HealthLake datastore")
                .build();

        mockMvc.perform(post("/api/healthlake/sessions/" + testSession.getId() + "/export")
                        .header("Authorization", "Bearer " + doctorToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", equalTo(true)))
                .andExpect(jsonPath("$.data.status", equalTo("SUCCESS")))
                .andExpect(jsonPath("$.data.bundleId", equalTo("Bundle-" + testSession.getId())))
                .andExpect(jsonPath("$.data.isMock", equalTo(true)))
                .andExpect(jsonPath("$.data.healthlakeRef", notNullValue()))
                .andExpect(jsonPath("$.data.integrationRecordId", notNullValue()));

        // 3. Verify IntegrationRecord persistence
        List<IntegrationRecord> records = integrationRecordRepository.findAll();
        assertEquals(1, records.size());
        IntegrationRecord rec = records.get(0);
        assertEquals(IntegrationType.HEALTHLAKE, rec.getIntegrationType());
        assertEquals("HEALTHLAKE_CLINICAL_EXPORT", rec.getOperation());
        assertEquals(IntegrationStatus.SUCCESS, rec.getStatus());
        assertEquals("HL-TEST-KEY-2", rec.getIdempotencyKey());
        assertNotNull(rec.getExternalReference());
        assertNotNull(rec.getResponsePayload());

        // 4. Verify Patient healthlakeRef update
        Patient updatedPatient = patientRepository.findById(testPatient.getId()).orElseThrow();
        assertNotNull(updatedPatient.getHealthlakeRef());
        assertTrue(updatedPatient.getHealthlakeRef().startsWith("HL-PAT-"));

        // 5. Verify AuditLog events
        List<AuditLog> auditLogs = auditLogRepository.findAll();
        assertTrue(auditLogs.stream().anyMatch(a -> a.getAction().equals("HEALTHLAKE_REQUESTED")));
        assertTrue(auditLogs.stream().anyMatch(a -> a.getAction().equals("HEALTHLAKE_SUCCEEDED")));
    }

    @Test
    @DisplayName("HealthLake Export: Idempotency replay returns existing successful transmission without re-executing")
    void testHealthLakeExport_idempotencyReplay() throws Exception {
        // 1. Verify summary
        testSummary.setStatus(ClinicalSummaryStatus.VERIFIED);
        summaryRepository.save(testSummary);

        HealthLakeExportRequestDto request = HealthLakeExportRequestDto.builder()
                .idempotencyKey("HL-IDEMPOTENT-KEY-3")
                .build();

        // First call
        mockMvc.perform(post("/api/healthlake/sessions/" + testSession.getId() + "/export")
                        .header("Authorization", "Bearer " + doctorToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status", equalTo("SUCCESS")));

        assertEquals(1, integrationRecordRepository.count());

        // Second call with identical key
        mockMvc.perform(post("/api/healthlake/sessions/" + testSession.getId() + "/export")
                        .header("Authorization", "Bearer " + doctorToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status", equalTo("SUCCESS")))
                .andExpect(jsonPath("$.data.message", containsString("Idempotent response")));

        assertEquals(1, integrationRecordRepository.count());
    }

    @Test
    @DisplayName("HealthLake Export: Simulated network timeout triggers HTTP 502 and records FAILED status")
    void testHealthLakeExport_timeoutSimulation_returnsBadGateway() throws Exception {
        testSummary.setStatus(ClinicalSummaryStatus.VERIFIED);
        summaryRepository.save(testSummary);

        HealthLakeExportRequestDto request = HealthLakeExportRequestDto.builder()
                .idempotencyKey("TIMEOUT-HL-KEY-4")
                .build();

        mockMvc.perform(post("/api/healthlake/sessions/" + testSession.getId() + "/export")
                        .header("Authorization", "Bearer " + doctorToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadGateway())
                .andExpect(jsonPath("$.data.status", equalTo("FAILED")))
                .andExpect(jsonPath("$.data.errorMessage", containsString("timeout")));

        List<IntegrationRecord> records = integrationRecordRepository.findAll();
        assertEquals(1, records.size());
        assertEquals(IntegrationStatus.FAILED, records.get(0).getStatus());

        List<AuditLog> auditLogs = auditLogRepository.findAll();
        assertTrue(auditLogs.stream().anyMatch(a -> a.getAction().equals("HEALTHLAKE_FAILED")));
    }

    @Test
    @DisplayName("HealthLake Export: Simulated server error triggers HTTP 502")
    void testHealthLakeExport_serverErrorSimulation_returnsBadGateway() throws Exception {
        testSummary.setStatus(ClinicalSummaryStatus.VERIFIED);
        summaryRepository.save(testSummary);

        HealthLakeExportRequestDto request = HealthLakeExportRequestDto.builder()
                .idempotencyKey("SERVER_ERROR-HL-KEY-5")
                .build();

        mockMvc.perform(post("/api/healthlake/sessions/" + testSession.getId() + "/export")
                        .header("Authorization", "Bearer " + doctorToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadGateway())
                .andExpect(jsonPath("$.data.status", equalTo("FAILED")))
                .andExpect(jsonPath("$.data.errorMessage", containsString("500 Internal Server Error")));
    }

    @Test
    @DisplayName("HealthLake Export: Admin role is authorized to export")
    void testHealthLakeExport_adminAuthorized() throws Exception {
        testSummary.setStatus(ClinicalSummaryStatus.VERIFIED);
        summaryRepository.save(testSummary);

        HealthLakeExportRequestDto request = HealthLakeExportRequestDto.builder()
                .idempotencyKey("ADMIN-HL-KEY-6")
                .build();

        mockMvc.perform(post("/api/healthlake/sessions/" + testSession.getId() + "/export")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status", equalTo("SUCCESS")));
    }

    @Test
    @DisplayName("HealthLake Security: Patient role is forbidden from exporting")
    void testHealthLakeExport_patientForbidden() throws Exception {
        HealthLakeExportRequestDto request = HealthLakeExportRequestDto.builder()
                .idempotencyKey("PATIENT-FORBIDDEN-KEY")
                .build();

        mockMvc.perform(post("/api/healthlake/sessions/" + testSession.getId() + "/export")
                        .header("Authorization", "Bearer " + patientToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("HealthLake History: Retrieves integration records for session")
    void testHealthLakeHistory_retrievesRecords() throws Exception {
        testSummary.setStatus(ClinicalSummaryStatus.VERIFIED);
        summaryRepository.save(testSummary);

        HealthLakeExportRequestDto request = HealthLakeExportRequestDto.builder()
                .idempotencyKey("HL-HIST-KEY-7")
                .build();

        mockMvc.perform(post("/api/healthlake/sessions/" + testSession.getId() + "/export")
                        .header("Authorization", "Bearer " + doctorToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/healthlake/sessions/" + testSession.getId() + "/status")
                        .header("Authorization", "Bearer " + doctorToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", equalTo(true)))
                .andExpect(jsonPath("$.data[0].integrationType", equalTo("HEALTHLAKE")))
                .andExpect(jsonPath("$.data[0].status", equalTo("SUCCESS")));
    }

    @Test
    @DisplayName("AwsHealthLakeProvider boundary: Safely reports unconfigured status and rejects without credentials")
    void testAwsHealthLakeProvider_safeBoundary() {
        HealthLakeProperties props = new HealthLakeProperties();
        props.setMode("LIVE");
        props.setEndpoint(null);
        props.setDatastoreId(null);

        AwsHealthLakeProvider provider = new AwsHealthLakeProvider(props);
        HealthLakeStatusDto status = provider.getStatus();

        assertFalse(status.isConnected());
        assertFalse(status.isMock());
        assertTrue(status.getMessage().contains("missing"));

        HealthLakeExportResult result = provider.exportClinicalBundle("{}", null, null, "KEY");
        assertFalse(result.success());
        assertTrue(result.errorMessage().contains("not configured"));
    }
}
