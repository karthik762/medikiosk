package com.medikiosk;

import com.medikiosk.entity.Allergy;
import com.medikiosk.entity.Answer;
import com.medikiosk.entity.AuditLog;
import com.medikiosk.entity.AyurvedaHistory;
import com.medikiosk.entity.ClinicalSummary;
import com.medikiosk.entity.Consent;
import com.medikiosk.entity.DoctorReview;
import com.medikiosk.entity.DocumentExtraction;
import com.medikiosk.entity.FhirMapping;
import com.medikiosk.entity.IntakeSession;
import com.medikiosk.entity.IntegrationRecord;
import com.medikiosk.entity.MedicalDocument;
import com.medikiosk.entity.MedicalHistory;
import com.medikiosk.entity.Medication;
import com.medikiosk.entity.Notification;
import com.medikiosk.entity.Patient;
import com.medikiosk.entity.Question;
import com.medikiosk.entity.QuestionCondition;
import com.medikiosk.entity.Symptom;
import com.medikiosk.entity.TriageResult;
import com.medikiosk.entity.User;
import com.medikiosk.entity.enums.ClinicalSummaryStatus;
import com.medikiosk.entity.enums.ConsentStatus;
import com.medikiosk.entity.enums.DoctorReviewAction;
import com.medikiosk.entity.enums.DocumentProcessingStatus;
import com.medikiosk.entity.enums.ExtractionStatus;
import com.medikiosk.entity.enums.IntakeSessionStatus;
import com.medikiosk.entity.enums.IntegrationStatus;
import com.medikiosk.entity.enums.IntegrationType;
import com.medikiosk.entity.enums.TriagePriority;
import com.medikiosk.entity.enums.UserRole;
import com.medikiosk.entity.enums.UserStatus;
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
import com.medikiosk.repository.NotificationRepository;
import com.medikiosk.repository.PatientRepository;
import com.medikiosk.repository.QuestionConditionRepository;
import com.medikiosk.repository.QuestionRepository;
import com.medikiosk.repository.SymptomRepository;
import com.medikiosk.repository.TriageResultRepository;
import com.medikiosk.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@ActiveProfiles("test")
public class DatabaseDomainIntegrationTest {

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
    private QuestionConditionRepository questionConditionRepository;

    @Autowired
    private AnswerRepository answerRepository;

    @Autowired
    private SymptomRepository symptomRepository;

    @Autowired
    private TriageResultRepository triageResultRepository;

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
    private DocumentExtractionRepository documentExtractionRepository;

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
    private NotificationRepository notificationRepository;

    @Test
    @Transactional
    void testCompleteDomainEntityLifecycleAndRelationships() {
        // 1. Persist User (Doctor)
        User doctor = new User("dr.sharma@hospital.org", "hashed_pwd_123", "Priya", "Sharma", UserRole.DOCTOR, UserStatus.ACTIVE);
        doctor = userRepository.save(doctor);
        assertThat(doctor.getId()).isNotNull();

        // 2. Persist Patient
        Patient patient = new Patient("MK-PAT-2026-0001", "Aarav", "Patel");
        patient.setDateOfBirth(LocalDate.of(1988, 5, 20));
        patient.setGender("male");
        patient.setPhone("+919876543210");
        patient.setEmail("aarav.patel@example.com");
        patient.setAbhaId("91-1234-5678-9012");
        patient = patientRepository.save(patient);
        assertThat(patient.getId()).isNotNull();

        // 3. Persist Intake Session
        IntakeSession session = new IntakeSession(patient);
        session.setLanguage("hi");
        session.setStatus(IntakeSessionStatus.IN_PROGRESS);
        session = intakeSessionRepository.save(session);
        assertThat(session.getId()).isNotNull();
        assertThat(session.getPatient().getId()).isEqualTo(patient.getId());

        // 4. Persist Consent
        Consent consent = new Consent(patient, session, ConsentStatus.ACCEPTED, "CLINICAL_INTAKE", "v1.0");
        consent = consentRepository.save(consent);
        assertThat(consent.getId()).isNotNull();
        assertThat(consent.getConsentStatus()).isEqualTo(ConsentStatus.ACCEPTED);

        // 5. Persist Question, Condition & Answer
        Question q1 = new Question("Q_CHIEF_COMPLAINT", "What brings you in today?", "TEXT", "CHIEF_COMPLAINT");
        q1 = questionRepository.save(q1);

        Question q2 = new Question("Q_CHEST_PAIN_SEVERITY", "How severe is your chest pain?", "SCALE", "SYMPTOMS");
        q2 = questionRepository.save(q2);

        QuestionCondition condition = new QuestionCondition(q1, q2, "CONTAINS_SYMPTOM", "chest pain", "SHOW");
        condition = questionConditionRepository.save(condition);
        assertThat(condition.getId()).isNotNull();

        Answer answer = new Answer(session, q1, "Severe chest heaviness since morning", "VOICE");
        answer.setOriginalTranscription("Severe chest heaviness since morning");
        answer.setConfidenceScore(new BigDecimal("0.9850"));
        answer = answerRepository.save(answer);
        assertThat(answer.getId()).isNotNull();

        // 6. Persist Symptom
        Symptom symptom = new Symptom(session, "Chest tightness", 8, "4 hours");
        symptom.setLocation("Substernal");
        symptom.setSource("PATIENT");
        symptom = symptomRepository.save(symptom);
        assertThat(symptom.getId()).isNotNull();

        // 7. Persist Rule-based Triage Result
        TriageResult triage = new TriageResult(session, TriagePriority.URGENT, "Red flag triggered: severe substernal chest discomfort");
        triage.setTriggeredRules("{\"ruleId\": \"RF-CV-001\", \"severity\": \"URGENT\"}");
        triage = triageResultRepository.save(triage);
        assertThat(triage.getId()).isNotNull();
        assertThat(triage.getPriority()).isEqualTo(TriagePriority.URGENT);

        // 8. Persist Medical History, Medication, and Allergy
        MedicalHistory medHistory = new MedicalHistory(patient, "Previous conditions", "Hypertension diagnosed 2020", "PATIENT");
        medHistory.setSession(session);
        medHistory = medicalHistoryRepository.save(medHistory);
        assertThat(medHistory.getId()).isNotNull();

        Medication med = new Medication(patient, "Amlodipine", "5mg", "Once daily");
        med.setSession(session);
        med = medicationRepository.save(med);
        assertThat(med.getId()).isNotNull();

        Allergy allergy = new Allergy(patient, "Penicillin", "Anaphylaxis, cutaneous rash", "HIGH");
        allergy.setSession(session);
        allergy = allergyRepository.save(allergy);
        assertThat(allergy.getId()).isNotNull();

        // 9. Persist Ayurveda History
        AyurvedaHistory ayur = new AyurvedaHistory(patient, session);
        ayur.setPrakritiData("{\"primary\": \"Vata\", \"secondary\": \"Pitta\"}");
        ayur.setNidanaData("{\"factors\": [\"irregular sleep\", \"excessive dry food\"]}");
        ayur.setParikshaData("{\"ashtavidha\": {\"nadi\": \"Vata-Pitta\", \"jihwa\": \"sama\"}}");
        ayur = ayurvedaHistoryRepository.save(ayur);
        assertThat(ayur.getId()).isNotNull();

        // 10. Persist Medical Document and OCR Extraction
        MedicalDocument doc = new MedicalDocument(patient, "PRESCRIPTION", "prescription_2026.pdf", "s3://medikiosk-docs/p1/prescription_2026.pdf");
        doc.setSession(session);
        doc.setProcessingStatus(DocumentProcessingStatus.COMPLETED);
        doc = medicalDocumentRepository.save(doc);
        assertThat(doc.getId()).isNotNull();

        DocumentExtraction extraction = new DocumentExtraction(doc, "Rx: Tab Amlodipine 5mg OD", new BigDecimal("0.9400"));
        extraction.setExtractionData("{\"medications\": [\"Amlodipine 5mg\"]}");
        extraction.setStatus(ExtractionStatus.SUCCESS);
        extraction = documentExtractionRepository.save(extraction);
        assertThat(extraction.getId()).isNotNull();

        // 11. Persist Clinical Summary and Doctor Review
        ClinicalSummary summary = new ClinicalSummary(patient, session, "Pre-consultation intake summary draft", ClinicalSummaryStatus.AI_GENERATED);
        summary.setSummaryData("{\"chiefComplaint\": \"Chest tightness\", \"triage\": \"URGENT\"}");
        summary = clinicalSummaryRepository.save(summary);
        assertThat(summary.getId()).isNotNull();

        DoctorReview review = new DoctorReview(summary, doctor, DoctorReviewAction.VERIFIED, 1, "Reviewed and confirmed urgent cardiology referral");
        review = doctorReviewRepository.save(review);
        assertThat(review.getId()).isNotNull();

        // 12. Persist FHIR Mapping
        FhirMapping fhirPatient = new FhirMapping("Patient", patient.getId(), "Patient", "FHIR-PAT-" + patient.getId());
        fhirPatient = fhirMappingRepository.save(fhirPatient);
        assertThat(fhirPatient.getId()).isNotNull();

        // 13. Persist Integration Record (ABDM / HIS / HealthLake)
        IntegrationRecord abdmRecord = new IntegrationRecord(IntegrationType.ABDM, "SYNC", patient, session);
        abdmRecord.setExternalReference("ABDM-TXN-98431");
        abdmRecord.setStatus(IntegrationStatus.SUCCESS);
        abdmRecord = integrationRecordRepository.save(abdmRecord);
        assertThat(abdmRecord.getId()).isNotNull();

        // 14. Persist Audit Log
        AuditLog audit = new AuditLog(doctor.getId(), "SUMMARY_VERIFIED", "ClinicalSummary", summary.getId(), "{\"verifiedBy\": \"Dr. Priya Sharma\"}");
        audit = auditLogRepository.save(audit);
        assertThat(audit.getId()).isNotNull();

        // 15. Persist Notification
        Notification notif = new Notification(doctor, "URGENT_TRIAGE", "Urgent Patient Intake", "Patient MK-PAT-2026-0001 triaged URGENT", "HIGH");
        notif = notificationRepository.save(notif);
        assertThat(notif.getId()).isNotNull();

        // 16. Verify Relational Queries
        List<IntakeSession> patientSessions = intakeSessionRepository.findByPatientId(patient.getId());
        assertThat(patientSessions).hasSize(1);

        List<Symptom> sessionSymptoms = symptomRepository.findBySessionId(session.getId());
        assertThat(sessionSymptoms).hasSize(1);
        assertThat(sessionSymptoms.get(0).getName()).isEqualTo("Chest tightness");

        Optional<TriageResult> sessionTriage = triageResultRepository.findBySessionId(session.getId());
        assertThat(sessionTriage).isPresent();
        assertThat(sessionTriage.get().getPriority()).isEqualTo(TriagePriority.URGENT);

        Optional<ClinicalSummary> sessionSummary = clinicalSummaryRepository.findBySessionId(session.getId());
        assertThat(sessionSummary).isPresent();
        assertThat(sessionSummary.get().getStatus()).isEqualTo(ClinicalSummaryStatus.AI_GENERATED);

        List<DoctorReview> summaryReviews = doctorReviewRepository.findBySummaryId(summary.getId());
        assertThat(summaryReviews).hasSize(1);
        assertThat(summaryReviews.get(0).getAction()).isEqualTo(DoctorReviewAction.VERIFIED);
    }

    @Test
    void testUniquePatientIdentifierConstraint() {
        Patient p1 = new Patient("MK-UNIQUE-PAT-01", "Ravi", "Kumar");
        patientRepository.saveAndFlush(p1);

        Patient p2 = new Patient("MK-UNIQUE-PAT-01", "Duplicate", "Identifier");
        assertThatThrownBy(() -> patientRepository.saveAndFlush(p2))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void testUniqueUserEmailConstraint() {
        User u1 = new User("unique.doc@hospital.org", "pwd_hash", "Neha", "Gupta", UserRole.DOCTOR, UserStatus.ACTIVE);
        userRepository.saveAndFlush(u1);

        User u2 = new User("unique.doc@hospital.org", "another_hash", "Duplicate", "Email", UserRole.DOCTOR, UserStatus.ACTIVE);
        assertThatThrownBy(() -> userRepository.saveAndFlush(u2))
                .isInstanceOf(DataIntegrityViolationException.class);
    }
}
