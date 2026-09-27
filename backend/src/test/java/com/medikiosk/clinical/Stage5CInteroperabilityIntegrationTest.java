package com.medikiosk.clinical;

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
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class Stage5CInteroperabilityIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ClinicalSummaryService clinicalSummaryService;

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
    private JwtTokenProvider jwtTokenProvider;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private ObjectMapper objectMapper;

    private User doctorUser;
    private String doctorToken;
    private Patient patient;
    private IntakeSession session;

    @BeforeEach
    void setUp() {
        triageResultRepository.deleteAll();
        symptomRepository.deleteAll();
        consentRepository.deleteAll();
        intakeSessionRepository.deleteAll();
        patientRepository.deleteAll();
        userRepository.deleteAll();

        // Create Doctor
        doctorUser = new User();
        doctorUser.setEmail("stage5c_doctor@hospital.org");
        doctorUser.setPasswordHash(passwordEncoder.encode("Doctor@12345"));
        doctorUser.setFirstName("Arun");
        doctorUser.setLastName("Sharma");
        doctorUser.setRole(UserRole.DOCTOR);
        doctorUser.setStatus(UserStatus.ACTIVE);
        doctorUser = userRepository.save(doctorUser);
        doctorToken = jwtTokenProvider.generateToken(doctorUser);

        // Create Patient User & Patient Entity
        User patientUser = new User();
        patientUser.setEmail("stage5c_patient@example.org");
        patientUser.setPasswordHash(passwordEncoder.encode("Patient@12345"));
        patientUser.setFirstName("Sunita");
        patientUser.setLastName("Rao");
        patientUser.setRole(UserRole.PATIENT);
        patientUser.setStatus(UserStatus.ACTIVE);
        patientUser = userRepository.save(patientUser);

        patient = new Patient();
        patient.setPatientIdentifier("PAT-STAGE5C-999");
        patient.setFirstName("Sunita");
        patient.setLastName("Rao");
        patient.setGender("FEMALE");
        patient.setDateOfBirth(LocalDate.of(1982, 4, 15));
        patient.setEmail("stage5c_patient@example.org");
        patient.setUserId(patientUser.getId());
        patient = patientRepository.save(patient);

        session = new IntakeSession();
        session.setPatient(patient);
        session.setLanguage("en");
        session.setStatus(IntakeSessionStatus.IN_PROGRESS);
        session = intakeSessionRepository.save(session);

        Consent consent = new Consent();
        consent.setPatient(patient);
        consent.setSession(session);
        consent.setConsentType(ConsentType.GENERAL_INTAKE);
        consent.setConsentStatus(ConsentStatus.ACCEPTED);
        consent.setConsentVersion("1.0");
        consent.setConsentedAt(Instant.now());
        consentRepository.save(consent);

        Consent abdmConsent = new Consent();
        abdmConsent.setPatient(patient);
        abdmConsent.setSession(session);
        abdmConsent.setConsentType(ConsentType.ABDM_DATA_SHARE);
        abdmConsent.setConsentStatus(ConsentStatus.ACCEPTED);
        abdmConsent.setConsentVersion("1.0");
        abdmConsent.setConsentedAt(Instant.now());
        consentRepository.save(abdmConsent);

        Symptom s1 = new Symptom();
        s1.setSession(session);
        s1.setName("Chest Pain");
        s1.setSeverity(8);
        s1.setDuration("2 hours");
        s1.setSource("PATIENT_REPORTED");
        symptomRepository.save(s1);

        TriageResult triage = new TriageResult();
        triage.setSession(session);
        triage.setPriority(TriagePriority.URGENT);
        triage.setExplanation("Acute chest pain requires immediate medical evaluation");
        triage.setTriggeredRules("[{\"ruleCode\":\"CARDIO_CHEST_PAIN\",\"reason\":\"Chest pain\"}]");
        triageResultRepository.save(triage);

        clinicalSummaryService.generateSummary(session.getId(), doctorUser);
    }

    @Test
    @DisplayName("Stage 5C: Formal clinician verification locks session and triggers multi-system interoperability sync")
    void testClinicianVerificationLocksSessionAndSyncsInteroperability() throws Exception {
        DoctorVerificationRequest req = new DoctorVerificationRequest();
        req.setConfirmUnderstood(true);
        req.setExpectedVersion(1);
        req.setClinicalNotes("Verified after patient examination. Cardiac evaluation ordered.");

        // Verification POST endpoint
        mockMvc.perform(post("/api/doctor/sessions/" + session.getId() + "/summary/verify")
                        .header("Authorization", "Bearer " + doctorToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.status").value("VERIFIED"))
                .andExpect(jsonPath("$.data.doctorVerification.status").value("VERIFIED"));

        // Verify intake session status is locked (COMPLETED)
        IntakeSession updatedSession = intakeSessionRepository.findById(session.getId()).orElseThrow();
        assertThat(updatedSession.getStatus()).isEqualTo(IntakeSessionStatus.COMPLETED);

        // Fetch Interoperability Status GET endpoint
        mockMvc.perform(get("/api/doctor/sessions/" + session.getId() + "/integrations")
                        .header("Authorization", "Bearer " + doctorToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.sessionLocked").value(true))
                .andExpect(jsonPath("$.data.fhirStatus").value("SUCCESS"))
                .andExpect(jsonPath("$.data.adapterStatuses.FHIR").value("SUCCESS"));
    }

    @Test
    @DisplayName("Stage 5C: Manual re-sync endpoint executes interoperability adapters for verified session")
    void testManualInteroperabilitySyncEndpoint() throws Exception {
        // Trigger manual sync endpoint
        mockMvc.perform(post("/api/doctor/sessions/" + session.getId() + "/sync-integrations")
                        .header("Authorization", "Bearer " + doctorToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.fhirStatus").value("SUCCESS"));
    }
}
