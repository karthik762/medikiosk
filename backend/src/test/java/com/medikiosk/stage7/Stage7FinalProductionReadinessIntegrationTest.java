package com.medikiosk.stage7;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.medikiosk.dto.doctor.DoctorVerificationRequest;
import com.medikiosk.entity.Consent;
import com.medikiosk.entity.IntakeSession;
import com.medikiosk.entity.Patient;
import com.medikiosk.entity.Symptom;
import com.medikiosk.entity.TriageResult;
import com.medikiosk.entity.User;
import com.medikiosk.entity.enums.ConsentStatus;
import com.medikiosk.entity.enums.ConsentType;
import com.medikiosk.entity.enums.IntakeSessionStatus;
import com.medikiosk.entity.enums.TriagePriority;
import com.medikiosk.entity.enums.UserRole;
import com.medikiosk.entity.enums.UserStatus;
import com.medikiosk.repository.AuditLogRepository;
import com.medikiosk.repository.ClinicalSummaryRepository;
import com.medikiosk.repository.ConsentRepository;
import com.medikiosk.repository.IntakeSessionRepository;
import com.medikiosk.repository.PatientRepository;
import com.medikiosk.repository.SymptomRepository;
import com.medikiosk.repository.TriageResultRepository;
import com.medikiosk.repository.UserRepository;
import com.medikiosk.security.JwtTokenProvider;
import com.medikiosk.service.ClinicalSummaryService;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class Stage7FinalProductionReadinessIntegrationTest {

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
    private TriageResultRepository triageResultRepository;

    @Autowired
    private ClinicalSummaryRepository clinicalSummaryRepository;

    @Autowired
    private AuditLogRepository auditLogRepository;

    @Autowired
    private ClinicalSummaryService clinicalSummaryService;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private ObjectMapper objectMapper;

    private User adminUser;
    private User doctorUser;
    private User patientUser;
    private String adminToken;
    private String doctorToken;
    private Patient patient;
    private IntakeSession session;

    @BeforeEach
    void setUp() {
        auditLogRepository.deleteAll();
        triageResultRepository.deleteAll();
        clinicalSummaryRepository.deleteAll();
        symptomRepository.deleteAll();
        consentRepository.deleteAll();
        intakeSessionRepository.deleteAll();
        patientRepository.deleteAll();
        userRepository.deleteAll();

        // 1. Create Admin User
        adminUser = new User();
        adminUser.setEmail("stage7_admin@hospital.org");
        adminUser.setPasswordHash(passwordEncoder.encode("Admin@12345"));
        adminUser.setFirstName("Director");
        adminUser.setLastName("Admin");
        adminUser.setRole(UserRole.ADMIN);
        adminUser.setStatus(UserStatus.ACTIVE);
        adminUser = userRepository.save(adminUser);
        adminToken = jwtTokenProvider.generateToken(adminUser);

        // 2. Create Doctor User
        doctorUser = new User();
        doctorUser.setEmail("stage7_doctor@hospital.org");
        doctorUser.setPasswordHash(passwordEncoder.encode("Doctor@12345"));
        doctorUser.setFirstName("Priya");
        doctorUser.setLastName("Nair");
        doctorUser.setRole(UserRole.DOCTOR);
        doctorUser.setStatus(UserStatus.ACTIVE);
        doctorUser = userRepository.save(doctorUser);
        doctorToken = jwtTokenProvider.generateToken(doctorUser);

        // 3. Create Patient User & Patient Record
        patientUser = new User();
        patientUser.setEmail("stage7_patient@example.org");
        patientUser.setPasswordHash(passwordEncoder.encode("Patient@12345"));
        patientUser.setFirstName("Rohan");
        patientUser.setLastName("Verma");
        patientUser.setRole(UserRole.PATIENT);
        patientUser.setStatus(UserStatus.ACTIVE);
        patientUser = userRepository.save(patientUser);

        patient = new Patient();
        patient.setPatientIdentifier("PAT-STAGE7-100");
        patient.setFirstName("Rohan");
        patient.setLastName("Verma");
        patient.setGender("MALE");
        patient.setDateOfBirth(LocalDate.of(1984, 8, 20));
        patient.setEmail("stage7_patient@example.org");
        patient.setUserId(patientUser.getId());
        patient = patientRepository.save(patient);

        // 4. Create Intake Session
        session = new IntakeSession();
        session.setPatient(patient);
        session.setLanguage("en");
        session.setStatus(IntakeSessionStatus.IN_PROGRESS);
        session = intakeSessionRepository.save(session);

        // 5. Consents
        Consent consentIntake = new Consent();
        consentIntake.setPatient(patient);
        consentIntake.setSession(session);
        consentIntake.setConsentType(ConsentType.GENERAL_INTAKE);
        consentIntake.setConsentStatus(ConsentStatus.ACCEPTED);
        consentIntake.setConsentVersion("1.0");
        consentIntake.setConsentedAt(Instant.now());
        consentRepository.save(consentIntake);

        Consent consentAbdm = new Consent();
        consentAbdm.setPatient(patient);
        consentAbdm.setSession(session);
        consentAbdm.setConsentType(ConsentType.ABDM_DATA_SHARE);
        consentAbdm.setConsentStatus(ConsentStatus.ACCEPTED);
        consentAbdm.setConsentVersion("1.0");
        consentAbdm.setConsentedAt(Instant.now());
        consentRepository.save(consentAbdm);

        // 6. Symptom & Triage Result
        Symptom symptom = new Symptom();
        symptom.setSession(session);
        symptom.setName("Acute Chest Pain");
        symptom.setSeverity(9);
        symptom.setDuration("1 hour");
        symptom.setSource("PATIENT_REPORTED");
        symptomRepository.save(symptom);

        TriageResult triage = new TriageResult();
        triage.setSession(session);
        triage.setPriority(TriagePriority.URGENT);
        triage.setExplanation("Acute chest pain with radiation triggers level 1 emergency protocol");
        triage.setTriggeredRules("[{\"ruleCode\":\"CARDIO_ACUTE_CHEST_PAIN\",\"reason\":\"Severe chest pain\"}]");
        triageResultRepository.save(triage);

        // 7. Generate Initial AI Summary
        clinicalSummaryService.generateSummary(session.getId(), doctorUser);
    }

    @Test
    @DisplayName("Stage 7: Full End-to-End Pipeline Verification across all 7 stages")
    void testFullEndToEndLifecyclePipeline() throws Exception {
        // Step A: Public Health Check
        mockMvc.perform(get("/api/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"))
                .andExpect(jsonPath("$.service").value("MediKiosk Backend"));

        // Step B: Doctor Consultation Queue Retrieval
        mockMvc.perform(get("/api/doctor/queue")
                        .header("Authorization", "Bearer " + doctorToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].sessionId").value(session.getId().toString()));

        // Step C: Doctor Verification & Digital Sign-off
        DoctorVerificationRequest verifyReq = new DoctorVerificationRequest();
        verifyReq.setConfirmUnderstood(true);
        verifyReq.setExpectedVersion(1);
        verifyReq.setClinicalNotes("Verified after clinical examination. ECG and cardiac biomarkers ordered.");

        mockMvc.perform(post("/api/doctor/sessions/" + session.getId() + "/summary/verify")
                        .header("Authorization", "Bearer " + doctorToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(verifyReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.status").value("VERIFIED"));

        // Verify Session is locked (COMPLETED)
        IntakeSession lockedSession = intakeSessionRepository.findById(session.getId()).orElseThrow();
        assertThat(lockedSession.getStatus()).isEqualTo(IntakeSessionStatus.COMPLETED);

        // Step D: Interoperability Integration Status
        mockMvc.perform(get("/api/doctor/sessions/" + session.getId() + "/integrations")
                        .header("Authorization", "Bearer " + doctorToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.sessionLocked").value(true))
                .andExpect(jsonPath("$.data.fhirStatus").value("SUCCESS"));

        // Step E: Admin Workstation Analytics & Audit Log Verification
        mockMvc.perform(get("/api/admin/analytics")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.totalIntakes").value(1))
                .andExpect(jsonPath("$.data.completedIntakes").value(1));

        mockMvc.perform(get("/api/admin/audit-logs")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }
}
