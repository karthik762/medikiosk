package com.medikiosk.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.medikiosk.dto.clinical.ClinicalSummaryDto;
import com.medikiosk.dto.clinical.StructuredHistoryDto;
import com.medikiosk.dto.clinical.StructuredMedicationDto;
import com.medikiosk.dto.clinical.StructuredSymptomDto;
import com.medikiosk.dto.clinical.TriageSummaryDto;
import com.medikiosk.entity.Answer;
import com.medikiosk.entity.ClinicalSummary;
import com.medikiosk.entity.Consent;
import com.medikiosk.entity.IntakeSession;
import com.medikiosk.entity.MedicalHistory;
import com.medikiosk.entity.Medication;
import com.medikiosk.entity.Patient;
import com.medikiosk.entity.Question;
import com.medikiosk.entity.Symptom;
import com.medikiosk.entity.TriageResult;
import com.medikiosk.entity.User;
import com.medikiosk.entity.enums.ClinicalSummaryStatus;
import com.medikiosk.entity.enums.ConsentStatus;
import com.medikiosk.entity.enums.ConsentType;
import com.medikiosk.entity.enums.IntakeSessionStatus;
import com.medikiosk.entity.enums.TriagePriority;
import com.medikiosk.entity.enums.UserRole;
import com.medikiosk.entity.enums.UserStatus;
import com.medikiosk.repository.AnswerRepository;
import com.medikiosk.repository.ClinicalSummaryRepository;
import com.medikiosk.repository.ConsentRepository;
import com.medikiosk.repository.IntakeSessionRepository;
import com.medikiosk.repository.MedicalHistoryRepository;
import com.medikiosk.repository.MedicationRepository;
import com.medikiosk.repository.PatientRepository;
import com.medikiosk.repository.QuestionRepository;
import com.medikiosk.repository.SymptomRepository;
import com.medikiosk.repository.TriageResultRepository;
import com.medikiosk.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Initializes default demonstration credentials and a sample clinical intake session
 * when running locally in development or standalone demo environments.
 * Guarded by medikiosk.demo.enabled=true and disabled for the 'test' profile.
 */
@Component
@Profile("!test")
@ConditionalOnProperty(name = "medikiosk.demo.enabled", havingValue = "true", matchIfMissing = true)
public class DemoDataInitializer implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(DemoDataInitializer.class);

    private final UserRepository userRepository;
    private final PatientRepository patientRepository;
    private final IntakeSessionRepository intakeSessionRepository;
    private final ConsentRepository consentRepository;
    private final SymptomRepository symptomRepository;
    private final MedicalHistoryRepository medicalHistoryRepository;
    private final MedicationRepository medicationRepository;
    private final QuestionRepository questionRepository;
    private final AnswerRepository answerRepository;
    private final TriageResultRepository triageResultRepository;
    private final ClinicalSummaryRepository clinicalSummaryRepository;
    private final PasswordEncoder passwordEncoder;
    private final ObjectMapper objectMapper;

    public DemoDataInitializer(
            UserRepository userRepository,
            PatientRepository patientRepository,
            IntakeSessionRepository intakeSessionRepository,
            ConsentRepository consentRepository,
            SymptomRepository symptomRepository,
            MedicalHistoryRepository medicalHistoryRepository,
            MedicationRepository medicationRepository,
            QuestionRepository questionRepository,
            AnswerRepository answerRepository,
            TriageResultRepository triageResultRepository,
            ClinicalSummaryRepository clinicalSummaryRepository,
            PasswordEncoder passwordEncoder,
            ObjectMapper objectMapper) {
        this.userRepository = userRepository;
        this.patientRepository = patientRepository;
        this.intakeSessionRepository = intakeSessionRepository;
        this.consentRepository = consentRepository;
        this.symptomRepository = symptomRepository;
        this.medicalHistoryRepository = medicalHistoryRepository;
        this.medicationRepository = medicationRepository;
        this.questionRepository = questionRepository;
        this.answerRepository = answerRepository;
        this.triageResultRepository = triageResultRepository;
        this.clinicalSummaryRepository = clinicalSummaryRepository;
        this.passwordEncoder = passwordEncoder;
        this.objectMapper = objectMapper;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        seedDemoUsers();
        seedDemoIntakeCase();
    }

    private void seedDemoUsers() {
        if (userRepository.findByEmail("doctor@medikiosk.org").isEmpty()) {
            User doctor = new User();
            doctor.setEmail("doctor@medikiosk.org");
            doctor.setPasswordHash(passwordEncoder.encode("Doctor@12345"));
            doctor.setFirstName("Sarah");
            doctor.setLastName("Jenkins");
            doctor.setRole(UserRole.DOCTOR);
            doctor.setStatus(UserStatus.ACTIVE);
            userRepository.save(doctor);
            log.info("Seeded demo doctor account: doctor@medikiosk.org / Doctor@12345");
        }

        if (userRepository.findByEmail("admin@medikiosk.org").isEmpty()) {
            User admin = new User();
            admin.setEmail("admin@medikiosk.org");
            admin.setPasswordHash(passwordEncoder.encode("Admin@12345"));
            admin.setFirstName("System");
            admin.setLastName("Administrator");
            admin.setRole(UserRole.ADMIN);
            admin.setStatus(UserStatus.ACTIVE);
            userRepository.save(admin);
            log.info("Seeded demo admin account: admin@medikiosk.org / Admin@12345");
        }

        if (userRepository.findByEmail("patient@medikiosk.org").isEmpty()) {
            User patient = new User();
            patient.setEmail("patient@medikiosk.org");
            patient.setPasswordHash(passwordEncoder.encode("Patient@12345"));
            patient.setFirstName("Rohan");
            patient.setLastName("Verma");
            patient.setRole(UserRole.PATIENT);
            patient.setStatus(UserStatus.ACTIVE);
            userRepository.save(patient);
            log.info("Seeded demo patient account: patient@medikiosk.org / Patient@12345");
        }
    }

    private void seedDemoIntakeCase() {
        if (intakeSessionRepository.count() > 0) {
            return;
        }

        try {
            Optional<User> patientUserOpt = userRepository.findByEmail("patient@medikiosk.org");
            User patientUser = patientUserOpt.orElse(null);

            // 1. Create Patient
            Patient patient = new Patient();
            patient.setFirstName("Rohan");
            patient.setLastName("Verma");
            patient.setDateOfBirth(LocalDate.of(1989, 5, 12));
            patient.setGender("MALE");
            patient.setPhone("+919876543210");
            patient.setEmail("patient@medikiosk.org");
            patient.setPatientIdentifier("MK-DEMO-001");
            if (patientUser != null) {
                patient.setUserId(patientUser.getId());
            }
            patient = patientRepository.save(patient);

            // 2. Create Intake Session
            IntakeSession session = new IntakeSession(patient);
            session.setLanguage("en");
            session.setStatus(IntakeSessionStatus.COMPLETED);
            session.setStartedAt(Instant.now().minus(45, ChronoUnit.MINUTES));
            session.setCompletedAt(Instant.now().minus(5, ChronoUnit.MINUTES));
            session.setExpiresAt(Instant.now().plus(23, ChronoUnit.HOURS));
            session = intakeSessionRepository.save(session);

            // 3. Record General Intake Consent
            Consent consent = new Consent();
            consent.setPatient(patient);
            consent.setSession(session);
            consent.setConsentType(ConsentType.GENERAL_INTAKE);
            consent.setConsentStatus(ConsentStatus.ACCEPTED);
            consent.setConsentVersion("1.0");
            consent.setConsentedAt(Instant.now().minus(40, ChronoUnit.MINUTES));
            consentRepository.save(consent);

            // 4. Record Symptoms
            Symptom s1 = new Symptom();
            s1.setSession(session);
            s1.setName("Throbbing unilateral headache");
            s1.setSeverity(8);
            s1.setDuration("3 days");
            s1.setLocation("Right temporal region");
            s1.setOnset("ACUTE");
            s1.setSource("PATIENT");
            symptomRepository.save(s1);

            Symptom s2 = new Symptom();
            s2.setSession(session);
            s2.setName("Photophobia and nausea");
            s2.setSeverity(5);
            s2.setDuration("2 days");
            s2.setLocation("Generalized");
            s2.setOnset("ACUTE");
            s2.setSource("PATIENT");
            symptomRepository.save(s2);

            // 5. Medical History & Medications
            MedicalHistory mh = new MedicalHistory();
            mh.setSession(session);
            mh.setPatient(patient);
            mh.setCategory("CONDITIONS");
            mh.setDescription("Migraine episodes diagnosed 5 years ago; seasonal allergic rhinitis");
            mh.setSource("PATIENT");
            medicalHistoryRepository.save(mh);

            Medication med = new Medication();
            med.setSession(session);
            med.setPatient(patient);
            med.setMedicationName("Sumatriptan");
            med.setDosage("50mg");
            med.setFrequency("As needed at migraine onset");
            med.setDuration("5 years");
            med.setSource("PATIENT");
            medicationRepository.save(med);

            // 6. Intake Answer (Chief Complaint)
            List<Question> questions = questionRepository.findAll();
            Question ccQ = questions.stream()
                    .filter(q -> "CHIEF_COMPLAINT".equalsIgnoreCase(q.getCategory()) || "Q_CHIEF_COMPLAINT".equalsIgnoreCase(q.getQuestionCode()))
                    .findFirst()
                    .orElse(null);

            if (ccQ != null) {
                Answer ans = new Answer();
                ans.setSession(session);
                ans.setQuestion(ccQ);
                ans.setAnswerText("Severe throbbing right-sided headache with nausea and light sensitivity for the past 3 days.");
                ans.setInputMethod("TEXT");
                ans.setAnsweredAt(Instant.now().minus(35, ChronoUnit.MINUTES));
                answerRepository.save(ans);
            }

            // 7. Deterministic Triage Result
            TriageResult triage = new TriageResult();
            triage.setSession(session);
            triage.setPriority(TriagePriority.PRIORITY);
            triage.setExplanation("Severe unilateral headache accompanied by photophobia and persistent nausea.");
            triage.setTriggeredRules("[{\"ruleId\":\"R-NEURO-02\",\"name\":\"Acute Migraine Flare with Visual/Autonomic Symptoms\",\"priority\":\"PRIORITY\"}]");
            triageResultRepository.save(triage);

            // 8. AI Pre-Consult Summary (AI_GENERATED, ready for doctor review & sign-off)
            ClinicalSummaryDto summaryDto = new ClinicalSummaryDto();
            summaryDto.setSessionId(session.getId());
            summaryDto.setPatientId(patient.getId());
            summaryDto.setChiefComplaint("Severe throbbing right-sided headache with nausea and light sensitivity for 3 days.");

            StructuredSymptomDto sym1 = new StructuredSymptomDto("Throbbing unilateral headache", "3 days", 8, "Right temporal region", "ACUTE", "CONTINUOUS", "PATIENT");
            StructuredSymptomDto sym2 = new StructuredSymptomDto("Photophobia and nausea", "2 days", 5, "Generalized", "ACUTE", "INTERMITTENT", "PATIENT");
            summaryDto.setSymptoms(List.of(sym1, sym2));

            StructuredHistoryDto hist = new StructuredHistoryDto();
            hist.setCategory("CONDITIONS");
            hist.setDescription("Migraine episodes diagnosed 5 years ago; seasonal allergic rhinitis");
            hist.setSource("PATIENT");
            summaryDto.setMedicalHistory(List.of(hist));

            StructuredMedicationDto medDto = new StructuredMedicationDto();
            medDto.setMedicationName("Sumatriptan");
            medDto.setDosage("50mg");
            medDto.setFrequency("As needed at migraine onset");
            medDto.setDuration("5 years");
            medDto.setSource("PATIENT");
            summaryDto.setMedications(List.of(medDto));

            TriageSummaryDto triageDto = new TriageSummaryDto();
            triageDto.setPriority(TriagePriority.PRIORITY);
            triageDto.setExplanation("Severe unilateral headache accompanied by photophobia and persistent nausea.");
            triageDto.setTriggeredRules(List.of(Map.of("ruleId", "R-NEURO-02", "name", "Acute Migraine Flare", "priority", "PRIORITY")));
            summaryDto.setTriage(triageDto);

            summaryDto.setHumanReadableSummary(
                    "35-year-old male presenting with acute exacerbation of severe right-sided throbbing headache persisting for 3 days, accompanied by photophobia and nausea. Known medical history includes episodic migraines for 5 years managed with Sumatriptan 50mg PRN. Deterministic clinical triage assigned PRIORITY due to severe neurological discomfort."
            );
            summaryDto.setStatus(ClinicalSummaryStatus.AI_GENERATED);
            summaryDto.setVersion(1);

            ClinicalSummary summary = new ClinicalSummary();
            summary.setSession(session);
            summary.setPatient(patient);
            summary.setSummaryText(summaryDto.getHumanReadableSummary());
            summary.setSummaryData(objectMapper.writeValueAsString(summaryDto));
            summary.setStatus(ClinicalSummaryStatus.AI_GENERATED);
            summary.setVersion(1);
            clinicalSummaryRepository.save(summary);

            log.info("Seeded rich demonstration case for patient Rohan Verma (Session: {}, Status: AI_GENERATED, Triage: PRIORITY)", session.getId());
        } catch (Exception e) {
            log.warn("Could not complete demo clinical case seeding: {}", e.getMessage());
        }
    }
}
