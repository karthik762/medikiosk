package com.medikiosk.clinical;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.medikiosk.concept.ConceptRegistry;
import com.medikiosk.dto.clinical.ClinicalCodingDto;
import com.medikiosk.dto.clinical.ClinicalSummaryDto;
import com.medikiosk.dto.clinical.DualCodedConceptDto;
import com.medikiosk.dto.clinical.StructuredAyurvedaDto;
import com.medikiosk.dto.clinical.StructuredSymptomDto;
import com.medikiosk.entity.AyurvedaHistory;
import com.medikiosk.entity.Consent;
import com.medikiosk.entity.IntakeSession;
import com.medikiosk.entity.Patient;
import com.medikiosk.entity.Symptom;
import com.medikiosk.entity.TriageResult;
import com.medikiosk.entity.User;
import com.medikiosk.entity.enums.ConsentStatus;
import com.medikiosk.entity.enums.ConsentType;
import com.medikiosk.entity.enums.TriagePriority;
import com.medikiosk.entity.enums.UserRole;
import com.medikiosk.entity.enums.UserStatus;
import com.medikiosk.repository.AyurvedaHistoryRepository;
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
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class Stage5BClinicalSummaryIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ConceptRegistry conceptRegistry;

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
    private AyurvedaHistoryRepository ayurvedaHistoryRepository;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private User doctorUser;
    private String doctorToken;
    private User patientUser;
    private String patientToken;
    private Patient patient;
    private IntakeSession session;

    @BeforeEach
    void setUp() {
        // Clean up database entities
        symptomRepository.deleteAll();
        triageResultRepository.deleteAll();
        ayurvedaHistoryRepository.deleteAll();
        consentRepository.deleteAll();
        intakeSessionRepository.deleteAll();
        patientRepository.deleteAll();
        userRepository.deleteAll();

        // Create Doctor
        doctorUser = new User();
        doctorUser.setEmail("stage5b_doctor@hospital.org");
        doctorUser.setPasswordHash(passwordEncoder.encode("Password123!"));
        doctorUser.setFirstName("Ramesh");
        doctorUser.setLastName("Sharma");
        doctorUser.setRole(UserRole.DOCTOR);
        doctorUser.setStatus(UserStatus.ACTIVE);
        doctorUser = userRepository.save(doctorUser);
        doctorToken = jwtTokenProvider.generateToken(doctorUser);

        // Create Patient User & Patient Entity
        patientUser = new User();
        patientUser.setEmail("stage5b_patient@example.org");
        patientUser.setPasswordHash(passwordEncoder.encode("Password123!"));
        patientUser.setFirstName("Suresh");
        patientUser.setLastName("Kumar");
        patientUser.setRole(UserRole.PATIENT);
        patientUser.setStatus(UserStatus.ACTIVE);
        patientUser = userRepository.save(patientUser);
        patientToken = jwtTokenProvider.generateToken(patientUser);

        patient = new Patient();
        patient.setPatientIdentifier("PAT-5B-1001");
        patient.setFirstName("Suresh");
        patient.setLastName("Kumar");
        patient.setDateOfBirth(LocalDate.of(1985, 6, 15));
        patient.setGender("MALE");
        patient.setPhone("9876543210");
        patient.setEmail("stage5b_patient@example.org");
        patient.setUserId(patientUser.getId());
        patient = patientRepository.save(patient);

        // Create Intake Session & Consent
        session = new IntakeSession();
        session.setPatient(patient);
        session.setLanguage("en");
        session.setStatus(com.medikiosk.entity.enums.IntakeSessionStatus.IN_PROGRESS);
        session = intakeSessionRepository.save(session);

        Consent consent = new Consent();
        consent.setPatient(patient);
        consent.setSession(session);
        consent.setConsentType(ConsentType.GENERAL_INTAKE);
        consent.setConsentStatus(ConsentStatus.ACCEPTED);
        consent.setConsentVersion("1.0");
        consent.setConsentedAt(Instant.now());
        consentRepository.save(consent);

        // Add Symptoms (Fever & Chest Pain)
        Symptom s1 = new Symptom();
        s1.setSession(session);
        s1.setName("Fever");
        s1.setSeverity(6);
        s1.setDuration("2 days");
        s1.setSource("PATIENT_REPORTED");
        symptomRepository.save(s1);

        Symptom s2 = new Symptom();
        s2.setSession(session);
        s2.setName("Chest Pain");
        s2.setSeverity(8);
        s2.setDuration("1 day");
        s2.setSource("PATIENT_REPORTED");
        symptomRepository.save(s2);

        // Add Triage
        TriageResult triage = new TriageResult();
        triage.setSession(session);
        triage.setPriority(TriagePriority.URGENT);
        triage.setExplanation("Chest pain reported with elevated severity");
        triage.setTriggeredRules("[{\"ruleCode\":\"CARDIO_CHEST_PAIN\",\"reason\":\"Chest pain\"}]");
        triageResultRepository.save(triage);

        // Add Ayurveda History
        AyurvedaHistory ah = new AyurvedaHistory();
        ah.setSession(session);
        ah.setPatient(patient);
        ah.setPrakritiData("{\"dominantDosha\":\"Pitta-Kapha\"}");
        ah.setNidanaData("{\"imbalance\":\"Pitta Aggravation\"}");
        ah.setParikshaData("{\"agni\":\"Tikshnagni\"}");
        ayurvedaHistoryRepository.save(ah);
    }

    @Test
    @DisplayName("Dual Terminology Coding: Valid mapping returns ICD-10 and NAMASTE codes, unknown concept returns UNMAPPED without fabrication")
    void testConceptRegistryDualCoding() {
        // 1. Valid mapped term: Fever
        List<ClinicalCodingDto> feverCodings = conceptRegistry.getCodingsForTerm("Fever");
        assertThat(feverCodings).isNotEmpty();
        assertThat(feverCodings).anyMatch(c -> "ICD".equals(c.getSystem()) && "R50.9".equals(c.getCode()));
        assertThat(feverCodings).anyMatch(c -> "NAMASTE".equals(c.getSystem()) && "NAM-JWARA".equals(c.getCode()));

        // 2. Mapped term: Chest Pain
        List<ClinicalCodingDto> chestPainCodings = conceptRegistry.getCodingsForTerm("Chest Pain");
        assertThat(chestPainCodings).anyMatch(c -> "ICD".equals(c.getSystem()) && "R07.9".equals(c.getCode()));
        assertThat(chestPainCodings).anyMatch(c -> "NAMASTE".equals(c.getSystem()) && "NAM-UROS-SHULA".equals(c.getCode()));

        // 3. Unknown / unmapped concept: Random Nonexistent Symptom
        List<ClinicalCodingDto> unknownCodings = conceptRegistry.getCodingsForTerm("Random Nonexistent Symptom");
        assertThat(unknownCodings).hasSize(1);
        ClinicalCodingDto unmapped = unknownCodings.get(0);
        assertThat(unmapped.getSystem()).isEqualTo("UNMAPPED");
        assertThat(unmapped.getCode()).isEqualTo("UNMAPPED");
        assertThat(unmapped.getConfidence()).isEqualTo(0.0);
    }

    @Test
    @DisplayName("Stage 5B: Clinical summary generation preserves triage, ayurveda, dual coding, provenance, and confidence")
    void testGenerateClinicalSummary() throws Exception {
        mockMvc.perform(post("/api/sessions/" + session.getId() + "/clinical-summary/generate")
                        .header("Authorization", "Bearer " + doctorToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.patientInfo.patientIdentifier").value("PAT-5B-1001"))
                .andExpect(jsonPath("$.data.patientInfo.firstName").value("Suresh"))
                .andExpect(jsonPath("$.data.triage.priority").value("URGENT"))
                .andExpect(jsonPath("$.data.triage.source").value("RULE_ENGINE"))
                .andExpect(jsonPath("$.data.ayurveda.prakriti").value("Pitta-Kapha"))
                .andExpect(jsonPath("$.data.ayurveda.source").value("PATIENT_REPORTED"))
                .andExpect(jsonPath("$.data.symptoms[0].coding[0].system").value("ICD"))
                .andExpect(jsonPath("$.data.symptoms[0].coding[0].code").value("R50.9"))
                .andExpect(jsonPath("$.data.doctorVerification.status").value("PENDING"));
    }

    @Test
    @DisplayName("Stage 5B: Patient endpoint GET /api/clinical-summary/{patientId} retrieves latest summary")
    void testGetPatientClinicalSummaryEndpoint() throws Exception {
        // First generate summary
        clinicalSummaryService.generateSummary(session.getId(), doctorUser);

        // Fetch via patient ID endpoint
        mockMvc.perform(get("/api/clinical-summary/" + patient.getId())
                        .header("Authorization", "Bearer " + doctorToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.patientId").value(patient.getId().toString()))
                .andExpect(jsonPath("$.data.chiefComplaint").exists());
    }

    @Test
    @DisplayName("Uncertainty: High severity symptom marked SUSPECTED, not confirmed diagnosis")
    void testUncertaintyRepresentation() {
        ClinicalSummaryDto summary = clinicalSummaryService.generateSummary(session.getId(), doctorUser);

        StructuredSymptomDto chestPain = summary.getSymptoms().stream()
                .filter(s -> "Chest Pain".equalsIgnoreCase(s.getName()))
                .findFirst()
                .orElseThrow();

        assertThat(chestPain.getStatus()).isEqualTo("SUSPECTED");
        assertThat(chestPain.getVerified()).isFalse();
        assertThat(summary.getDoctorVerification().getStatus()).isEqualTo("PENDING");
    }
}
