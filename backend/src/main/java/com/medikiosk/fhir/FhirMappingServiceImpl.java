package com.medikiosk.fhir;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.medikiosk.dto.fhir.FhirExportSummaryDto;
import com.medikiosk.dto.fhir.FhirValidationResponseDto;
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
import com.medikiosk.entity.enums.IntegrationStatus;
import com.medikiosk.entity.enums.IntegrationType;
import com.medikiosk.entity.enums.IntakeSessionStatus;
import com.medikiosk.exception.ApiException;
import com.medikiosk.exception.ResourceNotFoundException;
import com.medikiosk.fhir.model.FhirAddress;
import com.medikiosk.fhir.model.FhirAllergyIntolerance;
import com.medikiosk.fhir.model.FhirAttachment;
import com.medikiosk.fhir.model.FhirBundle;
import com.medikiosk.fhir.model.FhirCodeableConcept;
import com.medikiosk.fhir.model.FhirCoding;
import com.medikiosk.fhir.model.FhirComposition;
import com.medikiosk.fhir.model.FhirCondition;
import com.medikiosk.fhir.model.FhirConsent;
import com.medikiosk.fhir.model.FhirContactPoint;
import com.medikiosk.fhir.model.FhirDocumentReference;
import com.medikiosk.fhir.model.FhirEncounter;
import com.medikiosk.fhir.model.FhirHumanName;
import com.medikiosk.fhir.model.FhirIdentifier;
import com.medikiosk.fhir.model.FhirMedicationStatement;
import com.medikiosk.fhir.model.FhirObservation;
import com.medikiosk.fhir.model.FhirPatient;
import com.medikiosk.fhir.model.FhirPeriod;
import com.medikiosk.fhir.model.FhirProvenance;
import com.medikiosk.fhir.model.FhirReference;
import com.medikiosk.fhir.model.FhirResource;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Service
public class FhirMappingServiceImpl implements FhirMappingService {

    private static final Logger log = LoggerFactory.getLogger(FhirMappingServiceImpl.class);

    private final PatientRepository patientRepository;
    private final IntakeSessionRepository intakeSessionRepository;
    private final ConsentRepository consentRepository;
    private final SymptomRepository symptomRepository;
    private final MedicalHistoryRepository medicalHistoryRepository;
    private final MedicationRepository medicationRepository;
    private final AllergyRepository allergyRepository;
    private final AyurvedaHistoryRepository ayurvedaHistoryRepository;
    private final MedicalDocumentRepository medicalDocumentRepository;
    private final TriageResultRepository triageResultRepository;
    private final ClinicalSummaryRepository clinicalSummaryRepository;
    private final DoctorReviewRepository doctorReviewRepository;
    private final FhirMappingRepository fhirMappingRepository;
    private final IntegrationRecordRepository integrationRecordRepository;
    private final AuditLogRepository auditLogRepository;
    private final FhirValidationService fhirValidationService;
    private final ObjectMapper objectMapper;

    public FhirMappingServiceImpl(PatientRepository patientRepository,
                                   IntakeSessionRepository intakeSessionRepository,
                                   ConsentRepository consentRepository,
                                   SymptomRepository symptomRepository,
                                   MedicalHistoryRepository medicalHistoryRepository,
                                   MedicationRepository medicationRepository,
                                   AllergyRepository allergyRepository,
                                   AyurvedaHistoryRepository ayurvedaHistoryRepository,
                                   MedicalDocumentRepository medicalDocumentRepository,
                                   TriageResultRepository triageResultRepository,
                                   ClinicalSummaryRepository clinicalSummaryRepository,
                                   DoctorReviewRepository doctorReviewRepository,
                                   FhirMappingRepository fhirMappingRepository,
                                   IntegrationRecordRepository integrationRecordRepository,
                                   AuditLogRepository auditLogRepository,
                                   FhirValidationService fhirValidationService,
                                   ObjectMapper objectMapper) {
        this.patientRepository = patientRepository;
        this.intakeSessionRepository = intakeSessionRepository;
        this.consentRepository = consentRepository;
        this.symptomRepository = symptomRepository;
        this.medicalHistoryRepository = medicalHistoryRepository;
        this.medicationRepository = medicationRepository;
        this.allergyRepository = allergyRepository;
        this.ayurvedaHistoryRepository = ayurvedaHistoryRepository;
        this.medicalDocumentRepository = medicalDocumentRepository;
        this.triageResultRepository = triageResultRepository;
        this.clinicalSummaryRepository = clinicalSummaryRepository;
        this.doctorReviewRepository = doctorReviewRepository;
        this.fhirMappingRepository = fhirMappingRepository;
        this.integrationRecordRepository = integrationRecordRepository;
        this.auditLogRepository = auditLogRepository;
        this.fhirValidationService = fhirValidationService;
        this.objectMapper = objectMapper;
    }

    @Override
    @Transactional(readOnly = true)
    public FhirPatient buildPatientResource(UUID patientId) {
        Patient patient = patientRepository.findById(patientId)
                .orElseThrow(() -> new ResourceNotFoundException("Patient", "id", patientId));

        FhirPatient fhirPatient = new FhirPatient(patient.getId().toString());
        fhirPatient.setActive(true);

        // Identifiers
        fhirPatient.addIdentifier(new FhirIdentifier("official", "https://medikiosk.local/fhir/patient-id", patient.getPatientIdentifier()));
        if (patient.getAbhaId() != null && !patient.getAbhaId().isBlank()) {
            fhirPatient.addIdentifier(new FhirIdentifier("secondary", "https://abdm.gov.in/abha", patient.getAbhaId()));
        }
        if (patient.getHisId() != null && !patient.getHisId().isBlank()) {
            fhirPatient.addIdentifier(new FhirIdentifier("secondary", "https://medikiosk.local/fhir/his-id", patient.getHisId()));
        }
        if (patient.getHealthlakeRef() != null && !patient.getHealthlakeRef().isBlank()) {
            fhirPatient.addIdentifier(new FhirIdentifier("secondary", "https://healthlake.amazonaws.com/fhir/id", patient.getHealthlakeRef()));
        }

        // Name
        String firstName = patient.getFirstName() != null ? patient.getFirstName() : "";
        String lastName = patient.getLastName() != null ? patient.getLastName() : "";
        String fullName = (firstName + " " + lastName).trim();
        FhirHumanName name = new FhirHumanName("official", fullName);
        name.setFamily(lastName);
        if (!firstName.isBlank()) {
            List<String> givens = new ArrayList<>();
            givens.add(firstName);
            name.setGiven(givens);
        }
        fhirPatient.addName(name);

        // Telecom
        if (patient.getPhone() != null && !patient.getPhone().isBlank()) {
            fhirPatient.addTelecom(new FhirContactPoint("phone", patient.getPhone(), "mobile"));
        }
        if (patient.getEmail() != null && !patient.getEmail().isBlank()) {
            fhirPatient.addTelecom(new FhirContactPoint("email", patient.getEmail(), "home"));
        }

        // Gender & DOB
        if (patient.getGender() != null) {
            fhirPatient.setGender(patient.getGender().toLowerCase());
        } else {
            fhirPatient.setGender("unknown");
        }
        if (patient.getDateOfBirth() != null) {
            fhirPatient.setBirthDate(patient.getDateOfBirth().toString());
        }

        return fhirPatient;
    }

    @Override
    @Transactional(readOnly = true)
    public String toFhirPatient(UUID patientId) {
        FhirPatient patient = buildPatientResource(patientId);
        try {
            return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(patient);
        } catch (JsonProcessingException e) {
            throw new ApiException("Failed to serialize FHIR Patient", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public FhirEncounter buildEncounterResource(UUID intakeSessionId) {
        IntakeSession session = intakeSessionRepository.findById(intakeSessionId)
                .orElseThrow(() -> new ResourceNotFoundException("IntakeSession", "id", intakeSessionId));

        FhirEncounter encounter = new FhirEncounter(session.getId().toString());
        encounter.setStatus(session.getStatus() == IntakeSessionStatus.COMPLETED ? "finished" : "in-progress");
        encounter.setClass_(new FhirCoding("http://terminology.hl7.org/CodeSystem/v3-ActCode", "AMB", "ambulatory"));
        encounter.setSubject(new FhirReference("Patient/" + session.getPatient().getId(), 
                (session.getPatient().getFirstName() + " " + session.getPatient().getLastName()).trim()));

        FhirPeriod period = new FhirPeriod();
        if (session.getStartedAt() != null) {
            period.setStart(session.getStartedAt().toString());
        } else if (session.getCreatedAt() != null) {
            period.setStart(session.getCreatedAt().toString());
        }
        if (session.getCompletedAt() != null) {
            period.setEnd(session.getCompletedAt().toString());
        }
        encounter.setPeriod(period);

        // ReasonCodes from symptoms
        List<Symptom> symptoms = symptomRepository.findBySessionId(intakeSessionId);
        for (Symptom s : symptoms) {
            encounter.addReasonCode(new FhirCodeableConcept(s.getName()));
        }

        return encounter;
    }

    @Override
    @Transactional(readOnly = true)
    public String toFhirEncounter(UUID intakeSessionId) {
        FhirEncounter encounter = buildEncounterResource(intakeSessionId);
        try {
            return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(encounter);
        } catch (JsonProcessingException e) {
            throw new ApiException("Failed to serialize FHIR Encounter", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public FhirBundle buildClinicalBundle(UUID intakeSessionId) {
        IntakeSession session = intakeSessionRepository.findById(intakeSessionId)
                .orElseThrow(() -> new ResourceNotFoundException("IntakeSession", "id", intakeSessionId));
        Patient patient = session.getPatient();

        // Check verification state
        Optional<ClinicalSummary> summaryOpt = clinicalSummaryRepository.findBySessionId(intakeSessionId);
        boolean isVerified = summaryOpt.isPresent() && summaryOpt.get().getStatus() == ClinicalSummaryStatus.VERIFIED;
        ClinicalSummary summary = summaryOpt.orElse(null);

        Optional<DoctorReview> latestReviewOpt = Optional.empty();
        if (summary != null) {
            latestReviewOpt = doctorReviewRepository.findFirstBySummaryIdOrderByCreatedAtDesc(summary.getId());
        }

        String compositionId = summary != null ? summary.getId().toString() : UUID.randomUUID().toString();
        String bundleId = UUID.randomUUID().toString();

        FhirBundle bundle = new FhirBundle(bundleId);
        bundle.setType("document");
        bundle.setTimestamp(Instant.now().toString());
        bundle.setIdentifier(new FhirIdentifier("https://medikiosk.local/fhir/bundles", "bundle-" + session.getId()));

        // 1. Build Composition (First entry in document bundle)
        FhirComposition composition = new FhirComposition(compositionId);
        composition.setStatus(isVerified ? "final" : "preliminary");
        composition.setType(new FhirCodeableConcept("http://loinc.org", "34117-2", "History and physical note", "Pre-Consultation Clinical Intake Summary"));
        composition.setTitle("Pre-Consultation Clinical Intake Summary");
        composition.setDate(isVerified && summary.getUpdatedAt() != null ? summary.getUpdatedAt().toString() : Instant.now().toString());
        composition.setSubject(new FhirReference("Patient/" + patient.getId(), (patient.getFirstName() + " " + patient.getLastName()).trim()));
        composition.setEncounter(new FhirReference("Encounter/" + session.getId()));

        // Author
        if (isVerified && latestReviewOpt.isPresent() && latestReviewOpt.get().getDoctor() != null) {
            User doctor = latestReviewOpt.get().getDoctor();
            String docName = "Dr. " + (doctor.getFirstName() != null ? doctor.getFirstName() : "") + " " + (doctor.getLastName() != null ? doctor.getLastName() : "");
            composition.addAuthor(new FhirReference("Practitioner/" + doctor.getId(), docName.trim()));
        } else {
            composition.addAuthor(new FhirReference("Device/medikiosk-ai-engine", "MediKiosk Clinical Structuring Engine"));
        }

        // We will collect sections and bundle entries
        List<FhirResource> subsequentResources = new ArrayList<>();

        // Patient resource
        FhirPatient fhirPatient = buildPatientResource(patient.getId());
        subsequentResources.add(fhirPatient);

        // Encounter resource
        FhirEncounter fhirEncounter = buildEncounterResource(session.getId());
        subsequentResources.add(fhirEncounter);

        // Consent resources — emit every distinct consent type for the session (do not conflate)
        List<Consent> consents = consentRepository.findAllBySessionId(intakeSessionId);
        for (Consent c : consents) {
            FhirConsent fhirConsent = new FhirConsent(c.getId().toString());
            fhirConsent.setStatus(c.getConsentStatus() == ConsentStatus.ACCEPTED && c.getRevokedAt() == null ? "active" : "rejected");
            fhirConsent.setScope(new FhirCodeableConcept("http://terminology.hl7.org/CodeSystem/consentscope", "patient-privacy", "Privacy Consent"));
            fhirConsent.setPatient(new FhirReference("Patient/" + patient.getId()));
            fhirConsent.setDateTime(c.getConsentedAt() != null ? c.getConsentedAt().toString() : (c.getCreatedAt() != null ? c.getCreatedAt().toString() : Instant.now().toString()));
            fhirConsent.setPolicy(new FhirConsent.ConsentPolicy("https://medikiosk.local/consent-policy/" +
                    (c.getConsentType() != null ? c.getConsentType() : "v1")));
            fhirConsent.setProvision(new FhirConsent.ConsentProvision(
                    c.getConsentStatus() == ConsentStatus.ACCEPTED && c.getRevokedAt() == null ? "permit" : "deny"));
            subsequentResources.add(fhirConsent);
        }

        // Section 1: Chief Complaints & Symptoms (Observations)
        FhirComposition.CompositionSection symptomsSection = new FhirComposition.CompositionSection();
        symptomsSection.setTitle("Chief Complaints & Symptoms");
        symptomsSection.setCode(new FhirCodeableConcept("http://loinc.org", "10154-3", "Chief complaint", "Chief Complaints & Symptoms"));
        StringBuilder symptomsNarrative = new StringBuilder("<div xmlns=\"http://www.w3.org/1999/xhtml\"><ul>");

        List<Symptom> symptoms = symptomRepository.findBySessionId(intakeSessionId);
        for (Symptom s : symptoms) {
            FhirObservation obs = new FhirObservation(s.getId().toString());
            obs.setStatus(isVerified ? "final" : "preliminary");
            obs.addCategory(new FhirCodeableConcept("http://terminology.hl7.org/CodeSystem/observation-category", "exam", "Exam"));
            // NO fake codes: symptom name in text
            obs.setCode(new FhirCodeableConcept(s.getName()));
            obs.setSubject(new FhirReference("Patient/" + patient.getId()));
            obs.setEncounter(new FhirReference("Encounter/" + session.getId()));
            obs.setEffectiveDateTime(s.getCreatedAt() != null ? s.getCreatedAt().toString() : Instant.now().toString());
            if (s.getSeverity() != null) {
                obs.setValueString("Severity: " + s.getSeverity() + "/10");
            }
            if (s.getDuration() != null) {
                obs.addComponent(new FhirCodeableConcept("Duration"), s.getDuration());
            }
            if (s.getLocation() != null) {
                obs.addComponent(new FhirCodeableConcept("Body Site / Location"), s.getLocation());
            }
            if (s.getOnset() != null) {
                obs.addComponent(new FhirCodeableConcept("Onset"), s.getOnset());
            }
            if (s.getSource() != null) {
                obs.addNote("Source: " + s.getSource());
            }

            subsequentResources.add(obs);
            symptomsSection.addEntry(new FhirReference("Observation/" + obs.getId(), s.getName()));
            symptomsNarrative.append("<li><strong>").append(s.getName()).append("</strong>");
            if (s.getSeverity() != null) {
                symptomsNarrative.append(" - Severity: ").append(s.getSeverity()).append("/10");
            }
            if (s.getDuration() != null) {
                symptomsNarrative.append(" (").append(s.getDuration()).append(")");
            }
            symptomsNarrative.append("</li>");
        }
        symptomsNarrative.append("</ul></div>");
        symptomsSection.setText(new FhirComposition.SectionText("generated", symptomsNarrative.toString()));
        composition.addSection(symptomsSection);

        // Section 2: Deterministic Triage Result (Observation)
        Optional<TriageResult> triageOpt = triageResultRepository.findBySessionId(intakeSessionId);
        if (triageOpt.isPresent()) {
            TriageResult tr = triageOpt.get();
            FhirObservation triageObs = new FhirObservation(tr.getId().toString());
            triageObs.setStatus("final");
            triageObs.addCategory(new FhirCodeableConcept("http://terminology.hl7.org/CodeSystem/observation-category", "survey", "Survey"));
            triageObs.setCode(new FhirCodeableConcept("https://medikiosk.local/fhir/CodeSystem/triage", "triage-category", "Triage Priority"));
            triageObs.setValueString(tr.getPriority().name());
            triageObs.setSubject(new FhirReference("Patient/" + patient.getId()));
            triageObs.setEncounter(new FhirReference("Encounter/" + session.getId()));
            triageObs.setEffectiveDateTime(tr.getCreatedAt() != null ? tr.getCreatedAt().toString() : Instant.now().toString());

            // Extensions for Rule Engine preservation
            triageObs.addExtension("https://medikiosk.local/fhir/StructureDefinition/triage-source", "RULE_ENGINE");
            triageObs.addExtension("https://medikiosk.local/fhir/StructureDefinition/triggered-rules", 
                    tr.getTriggeredRules() != null ? tr.getTriggeredRules() : "NONE");

            if (tr.getExplanation() != null) {
                triageObs.addNote(tr.getExplanation());
            }

            subsequentResources.add(triageObs);

            FhirComposition.CompositionSection triageSection = new FhirComposition.CompositionSection();
            triageSection.setTitle("Deterministic Triage Result");
            triageSection.setCode(new FhirCodeableConcept("Triage Assessment"));
            triageSection.addEntry(new FhirReference("Observation/" + triageObs.getId(), "Triage Priority: " + tr.getPriority().name()));
            triageSection.setText(new FhirComposition.SectionText("generated", 
                    "<div xmlns=\"http://www.w3.org/1999/xhtml\"><p><strong>Priority: </strong>" 
                            + tr.getPriority().name() + "</p><p><strong>Source: </strong>Deterministic Rule Engine</p></div>"));
            composition.addSection(triageSection);
        }

        // Section 3: Past Medical History (Conditions)
        List<MedicalHistory> histories = medicalHistoryRepository.findBySessionId(intakeSessionId);
        if (histories.isEmpty()) {
            histories = medicalHistoryRepository.findByPatientId(patient.getId());
        }
        if (!histories.isEmpty()) {
            FhirComposition.CompositionSection historySection = new FhirComposition.CompositionSection();
            historySection.setTitle("Past Medical History");
            historySection.setCode(new FhirCodeableConcept("http://loinc.org", "11348-0", "History of Past illness", "Past Medical History"));
            StringBuilder historyNarrative = new StringBuilder("<div xmlns=\"http://www.w3.org/1999/xhtml\"><ul>");

            for (MedicalHistory h : histories) {
                FhirCondition cond = new FhirCondition(h.getId().toString());
                cond.setClinicalStatus(new FhirCodeableConcept("http://terminology.hl7.org/CodeSystem/condition-clinical", "active", "Active"));
                cond.setVerificationStatus(new FhirCodeableConcept("http://terminology.hl7.org/CodeSystem/condition-ver-status", "confirmed", "Confirmed"));
                cond.addCategory(new FhirCodeableConcept("http://terminology.hl7.org/CodeSystem/condition-category", "problem-list-item", "Problem List Item"));
                // NO fake codes: description in text, empty coding list
                cond.setCode(new FhirCodeableConcept(h.getDescription()));
                cond.setSubject(new FhirReference("Patient/" + patient.getId()));
                cond.setEncounter(new FhirReference("Encounter/" + session.getId()));
                cond.setRecordedDate(h.getCreatedAt() != null ? h.getCreatedAt().toString() : Instant.now().toString());
                if (h.getCategory() != null) {
                    cond.addNote("Category: " + h.getCategory());
                }

                subsequentResources.add(cond);
                historySection.addEntry(new FhirReference("Condition/" + cond.getId(), h.getDescription()));
                historyNarrative.append("<li>").append(h.getDescription()).append(" [").append(h.getCategory()).append("]</li>");
            }
            historyNarrative.append("</ul></div>");
            historySection.setText(new FhirComposition.SectionText("generated", historyNarrative.toString()));
            composition.addSection(historySection);
        }

        // Section 4: Current Medications (MedicationStatements)
        List<Medication> medications = medicationRepository.findBySessionId(intakeSessionId);
        if (medications.isEmpty()) {
            medications = medicationRepository.findByPatientId(patient.getId());
        }
        if (!medications.isEmpty()) {
            FhirComposition.CompositionSection medSection = new FhirComposition.CompositionSection();
            medSection.setTitle("Medications");
            medSection.setCode(new FhirCodeableConcept("http://loinc.org", "10160-0", "History of Medication use", "Medications"));
            StringBuilder medNarrative = new StringBuilder("<div xmlns=\"http://www.w3.org/1999/xhtml\"><ul>");

            for (Medication m : medications) {
                FhirMedicationStatement medStmt = new FhirMedicationStatement(m.getId().toString());
                medStmt.setStatus("active");
                // NO fake codes
                medStmt.setMedicationCodeableConcept(new FhirCodeableConcept(m.getMedicationName()));
                medStmt.setSubject(new FhirReference("Patient/" + patient.getId()));
                medStmt.setContext(new FhirReference("Encounter/" + session.getId()));
                String dosageText = (m.getDosage() != null ? m.getDosage() : "") + 
                        (m.getFrequency() != null ? " " + m.getFrequency() : "");
                if (!dosageText.isBlank()) {
                    medStmt.addDosage(dosageText.trim());
                }
                if (m.getSource() != null) {
                    medStmt.addNote("Source: " + m.getSource());
                }

                subsequentResources.add(medStmt);
                medSection.addEntry(new FhirReference("MedicationStatement/" + medStmt.getId(), m.getMedicationName()));
                medNarrative.append("<li>").append(m.getMedicationName()).append(" ").append(dosageText).append("</li>");
            }
            medNarrative.append("</ul></div>");
            medSection.setText(new FhirComposition.SectionText("generated", medNarrative.toString()));
            composition.addSection(medSection);
        }

        // Section 5: Allergies & Adverse Reactions (AllergyIntolerances)
        List<Allergy> allergies = allergyRepository.findBySessionId(intakeSessionId);
        if (allergies.isEmpty()) {
            allergies = allergyRepository.findByPatientId(patient.getId());
        }
        if (!allergies.isEmpty()) {
            FhirComposition.CompositionSection allergySection = new FhirComposition.CompositionSection();
            allergySection.setTitle("Allergies & Adverse Reactions");
            allergySection.setCode(new FhirCodeableConcept("http://loinc.org", "48765-2", "Allergies and adverse reactions", "Allergies"));
            StringBuilder allergyNarrative = new StringBuilder("<div xmlns=\"http://www.w3.org/1999/xhtml\"><ul>");

            for (Allergy a : allergies) {
                FhirAllergyIntolerance allergyIntol = new FhirAllergyIntolerance(a.getId().toString());
                allergyIntol.setClinicalStatus(new FhirCodeableConcept("http://terminology.hl7.org/CodeSystem/allergyintolerance-clinical", "active", "Active"));
                allergyIntol.setVerificationStatus(new FhirCodeableConcept("http://terminology.hl7.org/CodeSystem/allergyintolerance-verification", "confirmed", "Confirmed"));
                allergyIntol.setType("allergy");
                allergyIntol.setCode(new FhirCodeableConcept(a.getAllergen()));
                allergyIntol.setPatient(new FhirReference("Patient/" + patient.getId()));
                allergyIntol.setEncounter(new FhirReference("Encounter/" + session.getId()));
                allergyIntol.setRecordedDate(a.getCreatedAt() != null ? a.getCreatedAt().toString() : Instant.now().toString());

                String sev = a.getSeverity() != null ? a.getSeverity().toLowerCase() : "mild";
                if ("severe".equalsIgnoreCase(sev) || "high".equalsIgnoreCase(sev)) {
                    allergyIntol.setCriticality("high");
                } else {
                    allergyIntol.setCriticality("low");
                }
                allergyIntol.addReaction(a.getReaction(), sev);

                subsequentResources.add(allergyIntol);
                allergySection.addEntry(new FhirReference("AllergyIntolerance/" + allergyIntol.getId(), a.getAllergen()));
                allergyNarrative.append("<li>").append(a.getAllergen()).append(" - Reaction: ").append(a.getReaction()).append(" (").append(sev).append(")</li>");
            }
            allergyNarrative.append("</ul></div>");
            allergySection.setText(new FhirComposition.SectionText("generated", allergyNarrative.toString()));
            composition.addSection(allergySection);
        }

        // Section 6: Ayurveda History (Observations)
        Optional<AyurvedaHistory> ayurvedaOpt = ayurvedaHistoryRepository.findBySessionId(intakeSessionId);
        if (ayurvedaOpt.isPresent()) {
            AyurvedaHistory ay = ayurvedaOpt.get();
            FhirComposition.CompositionSection aySection = new FhirComposition.CompositionSection();
            aySection.setTitle("Ayurveda Clinical Findings");
            aySection.setCode(new FhirCodeableConcept("Ayurveda Assessment"));
            StringBuilder ayNarrative = new StringBuilder("<div xmlns=\"http://www.w3.org/1999/xhtml\"><p><strong>Ayurveda Findings:</strong></p><ul>");

            FhirObservation ayObs = new FhirObservation(ay.getId().toString());
            ayObs.setStatus(isVerified ? "final" : "preliminary");
            ayObs.addCategory(new FhirCodeableConcept("http://terminology.hl7.org/CodeSystem/observation-category", "survey", "Survey"));
            ayObs.setCode(new FhirCodeableConcept("Ayurveda Clinical Profile"));
            ayObs.setSubject(new FhirReference("Patient/" + patient.getId()));
            ayObs.setEncounter(new FhirReference("Encounter/" + session.getId()));
            ayObs.setEffectiveDateTime(ay.getCreatedAt() != null ? ay.getCreatedAt().toString() : Instant.now().toString());

            if (ay.getPrakritiData() != null) {
                ayObs.addComponent(new FhirCodeableConcept("Prakriti Assessment"), ay.getPrakritiData());
                ayNarrative.append("<li>Prakriti: ").append(ay.getPrakritiData()).append("</li>");
            }
            if (ay.getNidanaData() != null) {
                ayObs.addComponent(new FhirCodeableConcept("Nidana Factors"), ay.getNidanaData());
                ayNarrative.append("<li>Nidana: ").append(ay.getNidanaData()).append("</li>");
            }
            if (ay.getParikshaData() != null) {
                ayObs.addComponent(new FhirCodeableConcept("Pariksha Examination"), ay.getParikshaData());
                ayNarrative.append("<li>Pariksha: ").append(ay.getParikshaData()).append("</li>");
            }
            if (ay.getTerminologyData() != null) {
                ayObs.addComponent(new FhirCodeableConcept("Standardized Terminology"), ay.getTerminologyData());
            }

            subsequentResources.add(ayObs);
            aySection.addEntry(new FhirReference("Observation/" + ayObs.getId(), "Ayurveda Clinical Profile"));
            ayNarrative.append("</ul></div>");
            aySection.setText(new FhirComposition.SectionText("generated", ayNarrative.toString()));
            composition.addSection(aySection);
        }

        // Section 7: Medical Documents & OCR Findings (DocumentReferences)
        List<MedicalDocument> documents = medicalDocumentRepository.findBySessionId(intakeSessionId);
        if (!documents.isEmpty()) {
            FhirComposition.CompositionSection docSection = new FhirComposition.CompositionSection();
            docSection.setTitle("Medical Documents & Attached Records");
            docSection.setCode(new FhirCodeableConcept("http://loinc.org", "55107-7", "Addendum Document", "Medical Documents"));
            StringBuilder docNarrative = new StringBuilder("<div xmlns=\"http://www.w3.org/1999/xhtml\"><ul>");

            for (MedicalDocument doc : documents) {
                FhirDocumentReference docRef = new FhirDocumentReference(doc.getId().toString());
                docRef.setStatus("current");
                docRef.setDocStatus("final");
                docRef.setType(new FhirCodeableConcept(doc.getDocumentType() != null ? doc.getDocumentType().name() : "Medical Record"));
                docRef.setSubject(new FhirReference("Patient/" + patient.getId()));
                docRef.setDate(doc.getUploadedAt() != null ? doc.getUploadedAt().toString() : Instant.now().toString());
                docRef.setDescription(doc.getFileName());

                // REST URL, never a local filesystem path!
                String restDownloadUrl = "/api/intake/sessions/" + session.getId() + "/documents/" + doc.getId() + "/download";
                FhirAttachment attachment = new FhirAttachment(doc.getFileType(), restDownloadUrl, doc.getFileName(), doc.getFileSize());
                docRef.addAttachment(attachment);

                subsequentResources.add(docRef);
                docSection.addEntry(new FhirReference("DocumentReference/" + docRef.getId(), doc.getFileName()));
                docNarrative.append("<li>").append(doc.getFileName()).append(" (").append(doc.getDocumentType()).append(")</li>");
            }
            docNarrative.append("</ul></div>");
            docSection.setText(new FhirComposition.SectionText("generated", docNarrative.toString()));
            composition.addSection(docSection);
        }

        // Section 8: Doctor Review & Verification Notes
        FhirComposition.CompositionSection reviewSection = new FhirComposition.CompositionSection();
        reviewSection.setTitle("Doctor Clinical Review & Verification");
        reviewSection.setCode(new FhirCodeableConcept("Clinical Verification Status"));
        if (isVerified) {
            String comments = (latestReviewOpt.isPresent() && latestReviewOpt.get().getComments() != null) 
                    ? latestReviewOpt.get().getComments() : "Verified by consulting physician.";
            reviewSection.setText(new FhirComposition.SectionText("generated", 
                    "<div xmlns=\"http://www.w3.org/1999/xhtml\"><p><strong>Status: </strong>VERIFIED</p><p><strong>Doctor Notes: </strong>" 
                            + comments + "</p></div>"));
        } else {
            reviewSection.setText(new FhirComposition.SectionText("generated", 
                    "<div xmlns=\"http://www.w3.org/1999/xhtml\"><p><strong>Status: </strong>PRELIMINARY (Awaiting Doctor Review & Verification)</p></div>"));
        }
        composition.addSection(reviewSection);

        // Provenance Resource
        FhirProvenance provenance = new FhirProvenance(UUID.randomUUID().toString());
        provenance.addTarget(new FhirReference("Composition/" + composition.getId()));
        provenance.setRecorded(Instant.now().toString());
        provenance.setActivity(new FhirCodeableConcept("http://terminology.hl7.org/CodeSystem/v3-DataOperation", "CREATE", "Create"));

        // Agent: Patient
        provenance.addAgent(new FhirProvenance.ProvenanceAgent("Patient", new FhirReference("Patient/" + patient.getId())));
        // Agent: AI Engine
        provenance.addAgent(new FhirProvenance.ProvenanceAgent("AI Structuring Engine", new FhirReference("Device/medikiosk-ai-engine", "MediKiosk AI")));
        // Agent: Rule Engine
        provenance.addAgent(new FhirProvenance.ProvenanceAgent("Deterministic Triage Engine", new FhirReference("Device/medikiosk-rule-engine", "MediKiosk Triage")));
        // Agent: Doctor if verified
        if (isVerified && latestReviewOpt.isPresent() && latestReviewOpt.get().getDoctor() != null) {
            User doctor = latestReviewOpt.get().getDoctor();
            provenance.addAgent(new FhirProvenance.ProvenanceAgent("Legal Authenticator", 
                    new FhirReference("Practitioner/" + doctor.getId(), "Dr. " + doctor.getFirstName() + " " + doctor.getLastName())));
        }
        subsequentResources.add(provenance);

        // Now put Composition as the FIRST entry in Bundle
        bundle.addEntry("urn:uuid:" + composition.getId(), composition);

        // Put all subsequent resources in Bundle
        for (FhirResource res : subsequentResources) {
            bundle.addEntry("urn:uuid:" + res.getId(), res);
        }

        return bundle;
    }

    @Override
    @Transactional(readOnly = true)
    public String toFhirClinicalBundle(UUID intakeSessionId) {
        FhirBundle bundle = buildClinicalBundle(intakeSessionId);
        try {
            return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(bundle);
        } catch (JsonProcessingException e) {
            throw new ApiException("Failed to serialize FHIR Clinical Bundle", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    @Transactional
    public FhirExportSummaryDto exportClinicalBundle(UUID intakeSessionId, UUID requestingUserId) {
        IntakeSession session = intakeSessionRepository.findById(intakeSessionId)
                .orElseThrow(() -> new ResourceNotFoundException("IntakeSession", "id", intakeSessionId));

        FhirBundle bundle = buildClinicalBundle(intakeSessionId);
        FhirValidationResponseDto validation = fhirValidationService.validateBundle(bundle);

        String bundleJson;
        try {
            bundleJson = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(bundle);
        } catch (JsonProcessingException e) {
            throw new ApiException("Failed to serialize FHIR Bundle", HttpStatus.INTERNAL_SERVER_ERROR);
        }

        // Clean previous mappings for this session
        fhirMappingRepository.deleteBySessionId(intakeSessionId);

        // Map and persist individual resources to fhir_mappings
        Map<String, Integer> counts = new HashMap<>();
        if (bundle.getEntry() != null) {
            for (var entry : bundle.getEntry()) {
                FhirResource res = entry.getResource();
                if (res != null) {
                    counts.put(res.getResourceType(), counts.getOrDefault(res.getResourceType(), 0) + 1);

                    String resJson = "";
                    try {
                        resJson = objectMapper.writeValueAsString(res);
                    } catch (JsonProcessingException ignored) {}

                    UUID entityId = null;
                    try {
                        entityId = UUID.fromString(res.getId());
                    } catch (Exception ignored) {}

                    FhirMapping mapping = new FhirMapping(
                            session.getId(),
                            res.getResourceType(),
                            entityId != null ? entityId : session.getId(),
                            res.getResourceType(),
                            res.getId(),
                            resJson,
                            validation.isValid() ? "VALID" : "WARNING"
                    );
                    fhirMappingRepository.save(mapping);
                }
            }
        }

        // Persist IntegrationRecord for audit / tracking boundary
        IntegrationRecord ir = new IntegrationRecord(
                IntegrationType.HIS,
                "FHIR_EXPORT",
                session.getPatient(),
                session
        );
        ir.setStatus(IntegrationStatus.PENDING);
        ir.setErrorMessage("Adapter not implemented in Phase 11: Future adapter required for live transmission");
        integrationRecordRepository.save(ir);

        // Persist AuditLog
        AuditLog audit = new AuditLog(
                requestingUserId,
                "FHIR_BUNDLE_EXPORTED",
                "FhirBundle",
                session.getId(),
                "{\"bundleId\":\"" + bundle.getId() + "\",\"totalResources\":" + bundle.getTotal() + ",\"valid\":" + validation.isValid() + "}"
        );
        auditLogRepository.save(audit);

        // Build return DTO
        Optional<ClinicalSummary> summaryOpt = clinicalSummaryRepository.findBySessionId(intakeSessionId);
        boolean isVerified = summaryOpt.isPresent() && summaryOpt.get().getStatus() == ClinicalSummaryStatus.VERIFIED;

        FhirExportSummaryDto dto = new FhirExportSummaryDto();
        dto.setSessionId(session.getId());
        dto.setPatientId(session.getPatient().getId());
        dto.setBundleId(bundle.getId());
        dto.setBundleType(bundle.getType());
        dto.setTotalResources(bundle.getTotal() != null ? bundle.getTotal() : 0);
        dto.setResourceTypeCounts(counts);
        dto.setVerificationStatus(isVerified ? "VERIFIED" : "UNVERIFIED");
        dto.setCompositionStatus(isVerified ? "final" : "preliminary");
        dto.setValidationStatus(validation.isValid() ? "VALID" : "WARNING");
        dto.setExportedAt(Instant.now());
        dto.setFhirBundleJson(bundleJson);

        return dto;
    }

    @Override
    @Transactional(readOnly = true)
    public FhirExportSummaryDto getStoredExportSummary(UUID intakeSessionId) {
        IntakeSession session = intakeSessionRepository.findById(intakeSessionId)
                .orElseThrow(() -> new ResourceNotFoundException("IntakeSession", "id", intakeSessionId));

        List<FhirMapping> mappings = fhirMappingRepository.findBySessionId(intakeSessionId);
        if (mappings.isEmpty()) {
            // Generate live representation preview
            FhirBundle bundle = buildClinicalBundle(intakeSessionId);
            FhirValidationResponseDto validation = fhirValidationService.validateBundle(bundle);
            String bundleJson;
            try {
                bundleJson = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(bundle);
            } catch (JsonProcessingException e) {
                throw new ApiException("Failed to serialize FHIR Bundle", HttpStatus.INTERNAL_SERVER_ERROR);
            }

            Map<String, Integer> counts = new HashMap<>();
            if (bundle.getEntry() != null) {
                for (var e : bundle.getEntry()) {
                    if (e.getResource() != null) {
                        counts.put(e.getResource().getResourceType(), counts.getOrDefault(e.getResource().getResourceType(), 0) + 1);
                    }
                }
            }

            Optional<ClinicalSummary> summaryOpt = clinicalSummaryRepository.findBySessionId(intakeSessionId);
            boolean isVerified = summaryOpt.isPresent() && summaryOpt.get().getStatus() == ClinicalSummaryStatus.VERIFIED;

            FhirExportSummaryDto dto = new FhirExportSummaryDto();
            dto.setSessionId(session.getId());
            dto.setPatientId(session.getPatient().getId());
            dto.setBundleId(bundle.getId());
            dto.setBundleType(bundle.getType());
            dto.setTotalResources(bundle.getTotal() != null ? bundle.getTotal() : 0);
            dto.setResourceTypeCounts(counts);
            dto.setVerificationStatus(isVerified ? "VERIFIED" : "UNVERIFIED");
            dto.setCompositionStatus(isVerified ? "final" : "preliminary");
            dto.setValidationStatus(validation.isValid() ? "VALID" : "WARNING");
            dto.setExportedAt(Instant.now());
            dto.setFhirBundleJson(bundleJson);
            return dto;
        }

        // Build from stored mappings
        Map<String, Integer> counts = new HashMap<>();
        String bundleJson = null;
        String validationStatus = "VALID";
        for (FhirMapping m : mappings) {
            counts.put(m.getFhirResourceType(), counts.getOrDefault(m.getFhirResourceType(), 0) + 1);
            if ("INVALID".equals(m.getValidationStatus())) {
                validationStatus = "INVALID";
            }
        }

        // Find stored bundle or reconstruct
        FhirBundle bundle = buildClinicalBundle(intakeSessionId);
        try {
            bundleJson = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(bundle);
        } catch (JsonProcessingException ignored) {}

        Optional<ClinicalSummary> summaryOpt = clinicalSummaryRepository.findBySessionId(intakeSessionId);
        boolean isVerified = summaryOpt.isPresent() && summaryOpt.get().getStatus() == ClinicalSummaryStatus.VERIFIED;

        FhirExportSummaryDto dto = new FhirExportSummaryDto();
        dto.setSessionId(session.getId());
        dto.setPatientId(session.getPatient().getId());
        dto.setBundleId(bundle.getId());
        dto.setBundleType("document");
        dto.setTotalResources(mappings.size());
        dto.setResourceTypeCounts(counts);
        dto.setVerificationStatus(isVerified ? "VERIFIED" : "UNVERIFIED");
        dto.setCompositionStatus(isVerified ? "final" : "preliminary");
        dto.setValidationStatus(validationStatus);
        dto.setExportedAt(mappings.get(0).getCreatedAt());
        dto.setFhirBundleJson(bundleJson);
        return dto;
    }

    @Override
    @Transactional(readOnly = true)
    public FhirValidationResponseDto validateBundleForSession(UUID intakeSessionId) {
        FhirBundle bundle = buildClinicalBundle(intakeSessionId);
        return fhirValidationService.validateBundle(bundle);
    }
}
