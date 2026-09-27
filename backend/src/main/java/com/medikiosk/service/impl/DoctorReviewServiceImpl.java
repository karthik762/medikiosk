package com.medikiosk.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.medikiosk.dto.ayurveda.AyurvedaHistoryResponse;
import com.medikiosk.dto.clinical.ClinicalSummaryDto;
import com.medikiosk.dto.clinical.DoctorVerificationDto;
import com.medikiosk.dto.clinical.StructuredAllergyDto;
import com.medikiosk.dto.clinical.StructuredHistoryDto;
import com.medikiosk.dto.clinical.StructuredMedicationDto;
import com.medikiosk.dto.clinical.StructuredSymptomDto;
import com.medikiosk.dto.clinical.TriageSummaryDto;
import com.medikiosk.dto.doctor.DoctorAmendmentRequest;
import com.medikiosk.dto.doctor.DoctorQueueItemDto;
import com.medikiosk.dto.doctor.DoctorReviewDetailDto;
import com.medikiosk.dto.doctor.DoctorReviewHistoryDto;
import com.medikiosk.dto.doctor.DoctorVerificationRequest;
import com.medikiosk.dto.doctor.OriginalPatientDataDto;
import com.medikiosk.dto.document.DocumentUploadResponse;
import com.medikiosk.entity.Allergy;
import com.medikiosk.entity.Answer;
import com.medikiosk.entity.AuditLog;
import com.medikiosk.entity.ClinicalSummary;
import com.medikiosk.entity.DoctorReview;
import com.medikiosk.entity.IntakeSession;
import com.medikiosk.entity.MedicalHistory;
import com.medikiosk.entity.Medication;
import com.medikiosk.entity.Patient;
import com.medikiosk.entity.Question;
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
import com.medikiosk.entity.IntegrationRecord;
import com.medikiosk.dto.clinical.DualCodedConceptDto;
import com.medikiosk.dto.doctor.DoctorInteroperabilityStatusDto;
import com.medikiosk.abdm.adapter.AbdmAdapter;
import com.medikiosk.fhir.FhirMappingService;
import com.medikiosk.dto.fhir.FhirExportSummaryDto;
import com.medikiosk.healthlake.adapter.HealthLakeAdapter;
import com.medikiosk.his.adapter.HisAdapter;
import com.medikiosk.exception.ApiException;
import com.medikiosk.exception.ResourceNotFoundException;
import com.medikiosk.repository.AllergyRepository;
import com.medikiosk.repository.AnswerRepository;
import com.medikiosk.repository.AuditLogRepository;
import com.medikiosk.repository.AyurvedaHistoryRepository;
import com.medikiosk.repository.ClinicalSummaryRepository;
import com.medikiosk.repository.DoctorReviewRepository;
import com.medikiosk.repository.IntakeSessionRepository;
import com.medikiosk.repository.IntegrationRecordRepository;
import com.medikiosk.repository.MedicalHistoryRepository;
import com.medikiosk.repository.MedicationRepository;
import com.medikiosk.repository.PatientRepository;
import com.medikiosk.repository.SymptomRepository;
import com.medikiosk.repository.TriageResultRepository;
import com.medikiosk.service.AyurvedaService;
import com.medikiosk.service.DoctorReviewService;
import com.medikiosk.service.DocumentProcessingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.time.Period;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Implementation of DoctorReviewService for doctor queue management and clinical review workstation.
 */
@Service
public class DoctorReviewServiceImpl implements DoctorReviewService {

    private static final Logger log = LoggerFactory.getLogger(DoctorReviewServiceImpl.class);

    private final IntakeSessionRepository intakeSessionRepository;
    private final PatientRepository patientRepository;
    private final ClinicalSummaryRepository clinicalSummaryRepository;
    private final DoctorReviewRepository doctorReviewRepository;
    private final TriageResultRepository triageResultRepository;
    private final AnswerRepository answerRepository;
    private final SymptomRepository symptomRepository;
    private final MedicalHistoryRepository medicalHistoryRepository;
    private final MedicationRepository medicationRepository;
    private final AllergyRepository allergyRepository;
    private final DocumentProcessingService documentProcessingService;
    private final AyurvedaService ayurvedaService;
    private final AyurvedaHistoryRepository ayurvedaHistoryRepository;
    private final AuditLogRepository auditLogRepository;
    private final IntegrationRecordRepository integrationRecordRepository;
    private final FhirMappingService fhirMappingService;
    private final AbdmAdapter abdmAdapter;
    private final HisAdapter hisAdapter;
    private final HealthLakeAdapter healthLakeAdapter;
    private final ObjectMapper objectMapper;

    @Autowired
    @org.springframework.context.annotation.Lazy
    private DoctorReviewService self;

    public DoctorReviewServiceImpl(
            IntakeSessionRepository intakeSessionRepository,
            PatientRepository patientRepository,
            ClinicalSummaryRepository clinicalSummaryRepository,
            DoctorReviewRepository doctorReviewRepository,
            TriageResultRepository triageResultRepository,
            AnswerRepository answerRepository,
            SymptomRepository symptomRepository,
            MedicalHistoryRepository medicalHistoryRepository,
            MedicationRepository medicationRepository,
            AllergyRepository allergyRepository,
            DocumentProcessingService documentProcessingService,
            AyurvedaService ayurvedaService,
            AyurvedaHistoryRepository ayurvedaHistoryRepository,
            AuditLogRepository auditLogRepository,
            IntegrationRecordRepository integrationRecordRepository,
            @Autowired(required = false) FhirMappingService fhirMappingService,
            @Autowired(required = false) AbdmAdapter abdmAdapter,
            @Autowired(required = false) HisAdapter hisAdapter,
            @Autowired(required = false) HealthLakeAdapter healthLakeAdapter,
            ObjectMapper objectMapper) {
        this.intakeSessionRepository = intakeSessionRepository;
        this.patientRepository = patientRepository;
        this.clinicalSummaryRepository = clinicalSummaryRepository;
        this.doctorReviewRepository = doctorReviewRepository;
        this.triageResultRepository = triageResultRepository;
        this.answerRepository = answerRepository;
        this.symptomRepository = symptomRepository;
        this.medicalHistoryRepository = medicalHistoryRepository;
        this.medicationRepository = medicationRepository;
        this.allergyRepository = allergyRepository;
        this.documentProcessingService = documentProcessingService;
        this.ayurvedaService = ayurvedaService;
        this.ayurvedaHistoryRepository = ayurvedaHistoryRepository;
        this.auditLogRepository = auditLogRepository;
        this.integrationRecordRepository = integrationRecordRepository;
        this.fhirMappingService = fhirMappingService;
        this.abdmAdapter = abdmAdapter;
        this.hisAdapter = hisAdapter;
        this.healthLakeAdapter = healthLakeAdapter;
        this.objectMapper = objectMapper;
    }

    @Override
    @Transactional(readOnly = true)
    public List<DoctorQueueItemDto> getConsultationQueue(TriagePriority priorityFilter, ClinicalSummaryStatus statusFilter, User doctor) {
        validateDoctorAccess(doctor);

        List<IntakeSession> sessions = intakeSessionRepository.findAll();
        List<DoctorQueueItemDto> queueItems = new ArrayList<>();

        for (IntakeSession session : sessions) {
            Patient patient = session.getPatient();
            Optional<TriageResult> triageOpt = triageResultRepository.findBySessionId(session.getId());
            Optional<ClinicalSummary> summaryOpt = clinicalSummaryRepository.findBySessionId(session.getId());

            TriagePriority triagePriority = triageOpt.map(TriageResult::getPriority).orElse(null);
            ClinicalSummaryStatus summaryStatus = summaryOpt.map(ClinicalSummary::getStatus).orElse(null);

            // Filtering
            if (priorityFilter != null && triagePriority != priorityFilter) {
                continue;
            }
            if (statusFilter != null && summaryStatus != statusFilter) {
                continue;
            }

            DoctorQueueItemDto item = new DoctorQueueItemDto();
            item.setSessionId(session.getId());
            item.setPatientId(patient.getId());
            item.setPatientIdentifier(patient.getPatientIdentifier());

            // Privacy-compliant patient display (e.g., Jane D. or J. Doe)
            item.setPatientName(formatPrivacyName(patient.getFirstName(), patient.getLastName()));
            if (patient.getDateOfBirth() != null) {
                item.setPatientAge(Period.between(patient.getDateOfBirth(), LocalDate.now()).getYears());
            }
            item.setPatientGender(patient.getGender());
            item.setSessionStatus(session.getStatus());
            item.setSessionCreatedAt(session.getCreatedAt());
            item.setSessionUpdatedAt(session.getUpdatedAt());

            item.setTriagePriority(triagePriority);
            item.setTriageReason(triageOpt.map(TriageResult::getExplanation).orElse(null));
            item.setSummaryStatus(summaryStatus);

            boolean isUrgent = triagePriority == TriagePriority.URGENT;
            item.setUrgent(isUrgent);

            // Derive clinical review status
            if (summaryStatus == ClinicalSummaryStatus.VERIFIED) {
                item.setReviewStatus("VERIFIED");
            } else if (summaryStatus == ClinicalSummaryStatus.DOCTOR_REVIEWED) {
                item.setReviewStatus("IN_REVIEW");
            } else if (summaryStatus == ClinicalSummaryStatus.AI_GENERATED) {
                item.setReviewStatus("READY_FOR_REVIEW");
            } else {
                item.setReviewStatus("PENDING_INTAKE");
            }

            // Extract chief complaint from answers or summary
            item.setChiefComplaint(extractChiefComplaint(session, summaryOpt.orElse(null)));

            queueItems.add(item);
        }

        // Deterministic queue sorting: Severity first, then newest first
        queueItems.sort(Comparator.comparingInt(this::getPriorityWeight)
                .thenComparing(DoctorQueueItemDto::getSessionCreatedAt, Comparator.nullsLast(Comparator.reverseOrder())));

        return queueItems;
    }

    @Override
    @Transactional
    public DoctorReviewDetailDto getCaseReviewDetails(UUID sessionId, User doctor) {
        validateDoctorAccess(doctor);

        IntakeSession session = intakeSessionRepository.findById(sessionId)
                .orElseThrow(() -> new ResourceNotFoundException("IntakeSession", "id", sessionId));

        Patient patient = session.getPatient();
        DoctorReviewDetailDto detail = new DoctorReviewDetailDto();

        detail.setSessionId(session.getId());
        detail.setPatientId(patient.getId());
        detail.setPatientIdentifier(patient.getPatientIdentifier());
        detail.setPatientName(patient.getFirstName() + " " + (patient.getLastName() != null ? patient.getLastName() : ""));
        detail.setDateOfBirth(patient.getDateOfBirth());
        if (patient.getDateOfBirth() != null) {
            detail.setAge(Period.between(patient.getDateOfBirth(), LocalDate.now()).getYears());
        }
        detail.setGender(patient.getGender());
        detail.setPhone(maskPhone(patient.getPhone()));
        detail.setSessionStatus(session.getStatus());
        detail.setSessionCreatedAt(session.getCreatedAt());
        detail.setSessionUpdatedAt(session.getUpdatedAt());

        // 1. Triage Information (Deterministic Rule Engine)
        Optional<TriageResult> triageOpt = triageResultRepository.findBySessionId(sessionId);
        if (triageOpt.isPresent()) {
            TriageResult tr = triageOpt.get();
            TriageSummaryDto triageDto = new TriageSummaryDto();
            triageDto.setPriority(tr.getPriority());
            triageDto.setExplanation(tr.getExplanation());
            triageDto.setSource("RULE_ENGINE");
            if (tr.getTriggeredRules() != null && !tr.getTriggeredRules().isBlank()) {
                try {
                    List<Map<String, Object>> rules = objectMapper.readValue(tr.getTriggeredRules(), new TypeReference<>() {});
                    triageDto.setTriggeredRules(rules);
                } catch (Exception ignored) {
                }
            }
            detail.setTriage(triageDto);
        }

        // 2. Clinical Summary (AI Pre-Consult Summary)
        Optional<ClinicalSummary> summaryOpt = clinicalSummaryRepository.findBySessionId(sessionId);
        if (summaryOpt.isPresent()) {
            ClinicalSummary summary = summaryOpt.get();
            detail.setCurrentVersion(summary.getVersion());
            detail.setCurrentStatus(summary.getStatus());
            detail.setVerified(summary.getStatus() == ClinicalSummaryStatus.VERIFIED);
            detail.setAiSummaryAvailable(true);

            if (summary.getSummaryData() != null) {
                try {
                    ClinicalSummaryDto summaryDto = objectMapper.readValue(summary.getSummaryData(), ClinicalSummaryDto.class);
                    detail.setClinicalSummary(summaryDto);
                } catch (Exception e) {
                    log.error("Failed to deserialize clinical summary data for session {}", sessionId, e);
                }
            }

            // Review history
            List<DoctorReview> reviews = doctorReviewRepository.findBySummaryIdOrderByCreatedAtDesc(summary.getId());
            List<DoctorReviewHistoryDto> reviewHistoryDtos = new ArrayList<>();
            for (DoctorReview rev : reviews) {
                DoctorReviewHistoryDto rh = new DoctorReviewHistoryDto();
                rh.setId(rev.getId());
                rh.setAction(rev.getAction());
                if (rev.getDoctor() != null) {
                    rh.setDoctorName(rev.getDoctor().getFirstName() + " " + rev.getDoctor().getLastName());
                    rh.setDoctorEmail(rev.getDoctor().getEmail());
                }
                rh.setSummaryVersion(rev.getSummaryVersion());
                rh.setComments(rev.getComments());
                rh.setAmendments(rev.getAmendments());
                rh.setCreatedAt(rev.getCreatedAt());
                rh.setVerifiedAt(rev.getVerifiedAt());
                reviewHistoryDtos.add(rh);
            }
            detail.setReviewHistory(reviewHistoryDtos);
        } else {
            detail.setAiSummaryAvailable(false);
            detail.setCurrentVersion(1);
            detail.setCurrentStatus(null);
            detail.setVerified(false);
        }

        // 3. Original Patient Source Data (Immutable intake)
        OriginalPatientDataDto patientData = buildOriginalPatientData(session, patient);
        detail.setOriginalPatientData(patientData);

        // 4. Ayurveda Profile
        if (ayurvedaHistoryRepository.findBySessionId(sessionId).isPresent()) {
            try {
                AyurvedaHistoryResponse ayurveda = ayurvedaService.getAyurvedaHistory(sessionId, doctor);
                detail.setAyurvedaHistory(ayurveda);
            } catch (Exception e) {
                log.debug("Could not retrieve Ayurveda history for session {}", sessionId, e);
            }
        }

        // 5. Medical Documents & OCR Extractions
        try {
            List<DocumentUploadResponse> docs = documentProcessingService.getSessionDocuments(sessionId, doctor);
            detail.setDocuments(docs);
        } catch (Exception e) {
            log.debug("No documents found for session {}", sessionId);
        }

        // Audit Event
        logAuditEvent(doctor.getId(), "DOCTOR_REVIEW_OPENED", "IntakeSession", sessionId.toString(),
                Map.of("sessionId", sessionId.toString(), "patientId", patient.getId().toString()));

        return detail;
    }

    @Override
    @Transactional
    public ClinicalSummaryDto amendClinicalSummary(UUID sessionId, DoctorAmendmentRequest request, User doctor) {
        validateDoctorAccess(doctor);

        IntakeSession session = intakeSessionRepository.findById(sessionId)
                .orElseThrow(() -> new ResourceNotFoundException("IntakeSession", "id", sessionId));

        ClinicalSummary summary = clinicalSummaryRepository.findBySessionId(sessionId)
                .orElseGet(() -> createInitialSummary(session, doctor));

        if (summary.getStatus() == ClinicalSummaryStatus.VERIFIED) {
            throw new ApiException(
                    "Verified clinical summaries cannot be amended. Verification must remain explicit and auditable.",
                    HttpStatus.CONFLICT
            );
        }

        // Optimistic Concurrency Control
        if (request.getExpectedVersion() == null || !summary.getVersion().equals(request.getExpectedVersion())) {
            throw new ApiException(
                    "Conflict: The clinical summary has been modified by another clinician (Current: v" +
                            summary.getVersion() + ", Submitted expected: v" + request.getExpectedVersion() +
                            "). Please refresh the page before submitting amendments.",
                    HttpStatus.CONFLICT
            );
        }

        // Archive original AI summary snapshot if not yet preserved
        if (summary.getOriginalAiData() == null && summary.getSummaryData() != null) {
            summary.setOriginalAiData(summary.getSummaryData());
        }

        ClinicalSummaryDto amendedDto = request.getAmendedSummary();
        int newVersion = summary.getVersion() + 1;

        // Update DoctorVerification metadata in summary
        DoctorVerificationDto verificationDto = amendedDto.getDoctorVerification();
        if (verificationDto == null) {
            verificationDto = new DoctorVerificationDto();
        }
        verificationDto.setStatus("AMENDED");
        verificationDto.setVerifiedBy(doctor.getId());
        verificationDto.setVerifiedByName(doctor.getFirstName() + " " + doctor.getLastName());
        verificationDto.setVerifiedAt(Instant.now());
        verificationDto.setComments(request.getClinicalNotes());
        amendedDto.setDoctorVerification(verificationDto);

        // Update summary entity
        try {
            String updatedJson = objectMapper.writeValueAsString(amendedDto);
            summary.setSummaryData(updatedJson);
            summary.setSummaryText(amendedDto.getChiefComplaint());
            summary.setVersion(newVersion);
            summary.setStatus(ClinicalSummaryStatus.DOCTOR_REVIEWED);
            summary.setGeneratedBy("DOCTOR");
            clinicalSummaryRepository.save(summary);

            // Record DoctorReview audit entry
            DoctorReview review = new DoctorReview();
            review.setSummary(summary);
            review.setDoctor(doctor);
            review.setAction(DoctorReviewAction.EDITED);
            review.setSummaryVersion(newVersion);
            review.setComments(request.getClinicalNotes());
            review.setAmendments(updatedJson);
            doctorReviewRepository.save(review);

            // Audit log
            logAuditEvent(doctor.getId(), "DOCTOR_SUMMARY_EDITED", "ClinicalSummary", summary.getId().toString(),
                    Map.of("sessionId", sessionId.toString(), "newVersion", newVersion, "action", "EDITED"));

        } catch (Exception e) {
            log.error("Failed to save doctor amendments for summary {}", summary.getId(), e);
            throw new ApiException("Failed to record doctor amendments", HttpStatus.INTERNAL_SERVER_ERROR);
        }

        amendedDto.setId(summary.getId());
        amendedDto.setSessionId(sessionId);
        amendedDto.setPatientId(session.getPatient().getId());
        amendedDto.setStatus(ClinicalSummaryStatus.DOCTOR_REVIEWED);
        amendedDto.setVersion(newVersion);
        return amendedDto;
    }

    @Override
    @Transactional
    public ClinicalSummaryDto verifyClinicalSummary(UUID sessionId, DoctorVerificationRequest request, User doctor) {
        validateDoctorAccess(doctor);

        if (request.getConfirmUnderstood() == null || !request.getConfirmUnderstood()) {
            throw new ApiException("Doctor must explicitly confirm verification responsibility", HttpStatus.BAD_REQUEST);
        }

        IntakeSession session = intakeSessionRepository.findById(sessionId)
                .orElseThrow(() -> new ResourceNotFoundException("IntakeSession", "id", sessionId));

        ClinicalSummary summary = clinicalSummaryRepository.findBySessionId(sessionId)
                .orElseGet(() -> createInitialSummary(session, doctor));

        // Optimistic Concurrency Control
        if (request.getExpectedVersion() == null || !summary.getVersion().equals(request.getExpectedVersion())) {
            throw new ApiException(
                    "Conflict: The clinical summary has been modified by another clinician (Current: v" +
                            summary.getVersion() + ", Submitted expected: v" + request.getExpectedVersion() +
                            "). Please refresh the page before verifying.",
                    HttpStatus.CONFLICT
            );
        }

        // Archive original AI summary snapshot if not yet preserved
        if (summary.getOriginalAiData() == null && summary.getSummaryData() != null) {
            summary.setOriginalAiData(summary.getSummaryData());
        }

        ClinicalSummaryDto summaryDto;
        try {
            summaryDto = objectMapper.readValue(summary.getSummaryData(), ClinicalSummaryDto.class);
        } catch (Exception e) {
            throw new ApiException("Invalid summary payload", HttpStatus.INTERNAL_SERVER_ERROR);
        }

        int newVersion = summary.getVersion() + 1;
        Instant now = Instant.now();

        // Update DoctorVerification metadata
        DoctorVerificationDto verificationDto = summaryDto.getDoctorVerification();
        if (verificationDto == null) {
            verificationDto = new DoctorVerificationDto();
        }
        verificationDto.setStatus("VERIFIED");
        verificationDto.setVerifiedBy(doctor.getId());
        verificationDto.setVerifiedByName(doctor.getFirstName() + " " + doctor.getLastName());
        verificationDto.setVerifiedAt(now);
        verificationDto.setComments(request.getClinicalNotes());
        summaryDto.setDoctorVerification(verificationDto);
        summaryDto.setStatus(ClinicalSummaryStatus.VERIFIED);
        
        // Stage 5C: Mark all summary findings as clinician verified
        markFieldsAsVerified(summaryDto);

        // Stage 5C: Lock IntakeSession
        session.setStatus(IntakeSessionStatus.COMPLETED);
        intakeSessionRepository.save(session);

        try {
            String updatedJson = objectMapper.writeValueAsString(summaryDto);
            summary.setSummaryData(updatedJson);
            summary.setVersion(newVersion);
            summary.setStatus(ClinicalSummaryStatus.VERIFIED);
            clinicalSummaryRepository.save(summary);

            // Record DoctorReview audit entry
            DoctorReview review = new DoctorReview();
            review.setSummary(summary);
            review.setDoctor(doctor);
            review.setAction(DoctorReviewAction.VERIFIED);
            review.setSummaryVersion(newVersion);
            review.setComments(request.getClinicalNotes());
            review.setVerifiedAt(now);
            doctorReviewRepository.save(review);

            // Audit log
            logAuditEvent(doctor.getId(), "DOCTOR_SUMMARY_VERIFIED", "ClinicalSummary", summary.getId().toString(),
                    Map.of("sessionId", sessionId.toString(), "newVersion", newVersion, "action", "VERIFIED"));

            // Stage 5C: Automated Interoperability Sync across FHIR R4, ABDM, HIS, HealthLake
            try {
                if (self != null) {
                    self.syncInteroperability(sessionId, doctor);
                } else {
                    syncInteroperability(sessionId, doctor);
                }
            } catch (Exception interopEx) {
                log.warn("Non-fatal interoperability sync warning for session {}: {}", sessionId, interopEx.getMessage());
            }

        } catch (Exception e) {
            log.error("Failed to verify clinical summary {}", summary.getId(), e);
            throw new ApiException("Failed to record clinical verification", HttpStatus.INTERNAL_SERVER_ERROR);
        }

        summaryDto.setId(summary.getId());
        summaryDto.setSessionId(sessionId);
        summaryDto.setPatientId(session.getPatient().getId());
        summaryDto.setVersion(newVersion);
        return summaryDto;
    }

    private void validateDoctorAccess(User user) {
        if (user == null || (user.getRole() != UserRole.DOCTOR && user.getRole() != UserRole.ADMIN)) {
            throw new ApiException("Access denied: Requires DOCTOR or ADMIN role", HttpStatus.FORBIDDEN);
        }
    }

    private int getPriorityWeight(DoctorQueueItemDto item) {
        if (item.getTriagePriority() == null) return 3;
        return switch (item.getTriagePriority()) {
            case URGENT -> 0;
            case PRIORITY -> 1;
            case STABLE -> 2;
        };
    }

    private String formatPrivacyName(String firstName, String lastName) {
        if (firstName == null && lastName == null) return "Unknown Patient";
        if (lastName == null || lastName.isBlank()) return firstName;
        if (firstName == null || firstName.isBlank()) return lastName;
        return firstName.charAt(0) + ". " + lastName;
    }

    private String maskPhone(String phone) {
        if (phone == null || phone.length() < 4) return "XXXX";
        return phone.substring(0, Math.min(3, phone.length())) + "****" + phone.substring(phone.length() - 2);
    }

    private String extractChiefComplaint(IntakeSession session, ClinicalSummary summary) {
        if (summary != null && summary.getSummaryText() != null && !summary.getSummaryText().isBlank()) {
            return summary.getSummaryText();
        }
        List<Answer> answers = answerRepository.findBySessionId(session.getId());
        for (Answer ans : answers) {
            if (ans.getQuestion() != null && isChiefComplaintQuestion(ans.getQuestion())) {
                return ans.getAnswerText();
            }
        }
        return "Not stated";
    }

    private boolean isChiefComplaintQuestion(Question q) {
        if (q == null) return false;
        return "CHIEF_COMPLAINT".equalsIgnoreCase(q.getCategory())
                || "CHIEF_COMPLAINT".equalsIgnoreCase(q.getQuestionCode())
                || "Q_PRIMARY_COMPLAINT".equalsIgnoreCase(q.getQuestionCode())
                || "Q_CHIEF_COMPLAINT".equalsIgnoreCase(q.getQuestionCode());
    }

    private OriginalPatientDataDto buildOriginalPatientData(IntakeSession session, Patient patient) {
        OriginalPatientDataDto dto = new OriginalPatientDataDto();

        // 1. Raw answers
        List<Answer> answers = answerRepository.findBySessionId(session.getId());
        List<OriginalPatientDataDto.PatientAnswerRecordDto> ansDtos = new ArrayList<>();
        String chiefComplaint = null;

        for (Answer ans : answers) {
            String qCode = ans.getQuestion() != null ? ans.getQuestion().getQuestionCode() : "Q";
            String qText = ans.getQuestion() != null ? ans.getQuestion().getQuestionText() : "";
            ansDtos.add(new OriginalPatientDataDto.PatientAnswerRecordDto(
                    ans.getQuestion() != null ? ans.getQuestion().getId() : null,
                    qCode,
                    qText,
                    ans.getAnswerText(),
                    ans.getInputMethod() != null ? ans.getInputMethod() : "TEXT",
                    ans.getOriginalTranscription(),
                    ans.getAnsweredAt()
            ));
            if (ans.getQuestion() != null && isChiefComplaintQuestion(ans.getQuestion())) {
                chiefComplaint = ans.getAnswerText();
            }
        }
        dto.setIntakeAnswers(ansDtos);
        dto.setChiefComplaint(chiefComplaint != null ? chiefComplaint : "No chief complaint recorded in answers");

        // 2. Symptoms
        List<Symptom> symptoms = symptomRepository.findBySessionId(session.getId());
        List<StructuredSymptomDto> symptomDtos = new ArrayList<>();
        for (Symptom s : symptoms) {
            StructuredSymptomDto sd = new StructuredSymptomDto();
            sd.setName(s.getName());
            sd.setSeverity(s.getSeverity());
            sd.setDuration(s.getDuration());
            sd.setOnset(s.getOnset());
            sd.setLocation(s.getLocation());
            sd.setFrequency(s.getFrequency());
            sd.setSource(s.getSource());
            symptomDtos.add(sd);
        }
        dto.setSymptoms(symptomDtos);

        // 3. Medical history (Session & Patient union)
        List<MedicalHistory> histories = new ArrayList<>(medicalHistoryRepository.findBySessionId(session.getId()));
        Set<UUID> existingHIds = histories.stream().map(MedicalHistory::getId).collect(Collectors.toSet());
        for (MedicalHistory ph : medicalHistoryRepository.findByPatientId(patient.getId())) {
            if (ph.getId() != null && !existingHIds.contains(ph.getId())) {
                histories.add(ph);
            }
        }
        List<StructuredHistoryDto> historyDtos = new ArrayList<>();
        for (MedicalHistory mh : histories) {
            StructuredHistoryDto hd = new StructuredHistoryDto();
            hd.setCategory(mh.getCategory());
            hd.setDescription(mh.getDescription());
            hd.setSource(mh.getSource());
            historyDtos.add(hd);
        }
        dto.setMedicalHistory(historyDtos);

        // 4. Medications
        List<Medication> medications = new ArrayList<>(medicationRepository.findBySessionId(session.getId()));
        Set<UUID> existingMIds = medications.stream().map(Medication::getId).collect(Collectors.toSet());
        for (Medication pm : medicationRepository.findByPatientId(patient.getId())) {
            if (pm.getId() != null && !existingMIds.contains(pm.getId())) {
                medications.add(pm);
            }
        }
        List<StructuredMedicationDto> medDtos = new ArrayList<>();
        for (Medication m : medications) {
            StructuredMedicationDto md = new StructuredMedicationDto();
            md.setMedicationName(m.getMedicationName());
            md.setDosage(m.getDosage());
            md.setFrequency(m.getFrequency());
            md.setDuration(m.getDuration());
            md.setSource(m.getSource());
            medDtos.add(md);
        }
        dto.setMedications(medDtos);

        // 5. Allergies
        List<Allergy> allergies = new ArrayList<>(allergyRepository.findBySessionId(session.getId()));
        Set<UUID> existingAIds = allergies.stream().map(Allergy::getId).collect(Collectors.toSet());
        for (Allergy pa : allergyRepository.findByPatientId(patient.getId())) {
            if (pa.getId() != null && !existingAIds.contains(pa.getId())) {
                allergies.add(pa);
            }
        }
        List<StructuredAllergyDto> allergyDtos = new ArrayList<>();
        for (Allergy a : allergies) {
            StructuredAllergyDto ad = new StructuredAllergyDto();
            ad.setAllergen(a.getAllergen());
            ad.setReaction(a.getReaction());
            ad.setSeverity(a.getSeverity());
            ad.setSource(a.getSource());
            allergyDtos.add(ad);
        }
        dto.setAllergies(allergyDtos);

        return dto;
    }

    private ClinicalSummary createInitialSummary(IntakeSession session, User doctor) {
        ClinicalSummary summary = new ClinicalSummary();
        summary.setSession(session);
        summary.setPatient(session.getPatient());
        summary.setVersion(1);
        summary.setStatus(ClinicalSummaryStatus.AI_GENERATED);
        summary.setGeneratedBy("SYSTEM");

        ClinicalSummaryDto dto = new ClinicalSummaryDto();
        dto.setSessionId(session.getId());
        dto.setPatientId(session.getPatient().getId());
        dto.setChiefComplaint(extractChiefComplaint(session, null));
        dto.setStatus(ClinicalSummaryStatus.AI_GENERATED);
        dto.setVersion(1);

        try {
            summary.setSummaryData(objectMapper.writeValueAsString(dto));
            summary.setSummaryText(dto.getChiefComplaint());
        } catch (Exception e) {
            summary.setSummaryData("{}");
            summary.setSummaryText("");
        }
        return clinicalSummaryRepository.save(summary);
    }

    private void logAuditEvent(UUID userId, String action, String entityType, String entityId, Map<String, Object> metadata) {
        try {
            String metadataJson = metadata != null ? objectMapper.writeValueAsString(metadata) : "{}";
            UUID entUuid = null;
            try {
                if (entityId != null) entUuid = UUID.fromString(entityId);
            } catch (Exception ignored) {
            }
            AuditLog logEntry = new AuditLog(userId, action, entityType, entUuid, metadataJson);
            auditLogRepository.save(logEntry);
        } catch (Exception e) {
            log.warn("Failed to write audit log event {} for entity {}", action, entityId, e);
        }
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public DoctorInteroperabilityStatusDto syncInteroperability(UUID sessionId, User doctor) {
        validateDoctorAccess(doctor);
        IntakeSession session = intakeSessionRepository.findById(sessionId)
                .orElseThrow(() -> new ResourceNotFoundException("IntakeSession", "id", sessionId));
        Patient patient = session.getPatient();

        DoctorInteroperabilityStatusDto statusDto = new DoctorInteroperabilityStatusDto();
        statusDto.setSessionId(sessionId);
        statusDto.setPatientId(patient.getId());
        statusDto.setSessionLocked(session.getStatus() == IntakeSessionStatus.COMPLETED);

        ClinicalSummary summary = clinicalSummaryRepository.findBySessionId(sessionId).orElse(null);
        statusDto.setClinicalSummaryStatus(summary != null ? summary.getStatus().name() : "NONE");

        String summaryText = summary != null && summary.getSummaryText() != null ? summary.getSummaryText() : "Patient Clinical Summary";

        // 1. FHIR R4 Bundle Export
        String fhirBundleJson = "{}";
        try {
            if (fhirMappingService != null) {
                FhirExportSummaryDto fhirExport = fhirMappingService.exportClinicalBundle(sessionId, doctor.getId());
                statusDto.setFhirStatus("SUCCESS");
                statusDto.setFhirBundleId(fhirExport.getBundleId());
                fhirBundleJson = fhirExport.getFhirBundleJson();
                saveIntegrationRecord(IntegrationType.HIS, "FHIR_R4_BUNDLE_SYNC", patient, session, IntegrationStatus.SUCCESS, fhirExport.getBundleId(), null, fhirBundleJson);
            } else {
                statusDto.setFhirStatus("SKIPPED");
            }
        } catch (Exception e) {
            log.error("FHIR Sync failed for session {}", sessionId, e);
            statusDto.setFhirStatus("FAILED");
            saveIntegrationRecord(IntegrationType.HIS, "FHIR_R4_BUNDLE_SYNC", patient, session, IntegrationStatus.FAILED, null, e.getMessage(), null);
        }

        // 2. ABDM Adapter Sync
        try {
            if (abdmAdapter != null && abdmAdapter.isAvailable()) {
                String abdmTxId = abdmAdapter.linkRecord(doctor.getId(), sessionId, fhirBundleJson);
                if (abdmTxId != null) {
                    statusDto.setAbdmStatus("SUCCESS");
                    statusDto.setAbdmTransactionId(abdmTxId);
                    saveIntegrationRecord(IntegrationType.ABDM, "ABHA_RECORD_LINK", patient, session, IntegrationStatus.SUCCESS, abdmTxId, null, "{\"transactionId\":\"" + abdmTxId + "\"}");
                } else {
                    statusDto.setAbdmStatus("FAILED");
                    saveIntegrationRecord(IntegrationType.ABDM, "ABHA_RECORD_LINK", patient, session, IntegrationStatus.FAILED, null, "ABDM exchange returned failure or missing consent", null);
                }
            } else {
                statusDto.setAbdmStatus("SKIPPED");
            }
        } catch (Exception e) {
            log.error("ABDM Sync failed for session {}", sessionId, e);
            statusDto.setAbdmStatus("FAILED");
            saveIntegrationRecord(IntegrationType.ABDM, "ABHA_RECORD_LINK", patient, session, IntegrationStatus.FAILED, null, e.getMessage(), null);
        }

        // 3. HIS Adapter Sync
        try {
            if (hisAdapter != null && hisAdapter.isAvailable()) {
                String encRef = hisAdapter.exportToIntakeQueue(doctor.getId(), sessionId, summaryText);
                if (encRef != null) {
                    statusDto.setHisStatus("SUCCESS");
                    statusDto.setHisEncounterRef(encRef);
                    saveIntegrationRecord(IntegrationType.HIS, "HIS_ENCOUNTER_EXPORT", patient, session, IntegrationStatus.SUCCESS, encRef, null, "{\"encounterRef\":\"" + encRef + "\"}");
                } else {
                    statusDto.setHisStatus("FAILED");
                    saveIntegrationRecord(IntegrationType.HIS, "HIS_ENCOUNTER_EXPORT", patient, session, IntegrationStatus.FAILED, null, "HIS export returned failure", null);
                }
            } else {
                statusDto.setHisStatus("SKIPPED");
            }
        } catch (Exception e) {
            log.error("HIS Sync failed for session {}", sessionId, e);
            statusDto.setHisStatus("FAILED");
            saveIntegrationRecord(IntegrationType.HIS, "HIS_ENCOUNTER_EXPORT", patient, session, IntegrationStatus.FAILED, null, e.getMessage(), null);
        }

        // 4. AWS HealthLake Adapter Sync
        try {
            if (healthLakeAdapter != null && healthLakeAdapter.isAvailable()) {
                String hlRef = healthLakeAdapter.createResource("Bundle", fhirBundleJson);
                statusDto.setHealthLakeStatus("SUCCESS");
                statusDto.setHealthLakeResourceRef(hlRef);
                saveIntegrationRecord(IntegrationType.HEALTHLAKE, "HEALTHLAKE_BUNDLE_EXPORT", patient, session, IntegrationStatus.SUCCESS, hlRef, null, "{\"resourceRef\":\"" + hlRef + "\"}");
            } else {
                statusDto.setHealthLakeStatus("SKIPPED");
            }
        } catch (Exception e) {
            log.error("HealthLake Sync failed for session {}", sessionId, e);
            statusDto.setHealthLakeStatus("FAILED");
            saveIntegrationRecord(IntegrationType.HEALTHLAKE, "HEALTHLAKE_BUNDLE_EXPORT", patient, session, IntegrationStatus.FAILED, null, e.getMessage(), null);
        }

        statusDto.setLastSyncedAt(Instant.now());
        statusDto.setAdapterStatuses(Map.of(
                "FHIR", statusDto.getFhirStatus(),
                "ABDM", statusDto.getAbdmStatus() != null ? statusDto.getAbdmStatus() : "NONE",
                "HIS", statusDto.getHisStatus() != null ? statusDto.getHisStatus() : "NONE",
                "HealthLake", statusDto.getHealthLakeStatus() != null ? statusDto.getHealthLakeStatus() : "NONE"
        ));

        logAuditEvent(doctor.getId(), "DOCTOR_INTEROP_SYNC_COMPLETED", "IntakeSession", sessionId.toString(),
                Map.of("sessionId", sessionId.toString(), "fhir", statusDto.getFhirStatus()));

        return statusDto;
    }

    @Override
    @Transactional(readOnly = true)
    public DoctorInteroperabilityStatusDto getInteroperabilityStatus(UUID sessionId, User doctor) {
        validateDoctorAccess(doctor);
        IntakeSession session = intakeSessionRepository.findById(sessionId)
                .orElseThrow(() -> new ResourceNotFoundException("IntakeSession", "id", sessionId));
        Patient patient = session.getPatient();

        DoctorInteroperabilityStatusDto statusDto = new DoctorInteroperabilityStatusDto();
        statusDto.setSessionId(sessionId);
        statusDto.setPatientId(patient.getId());
        statusDto.setSessionLocked(session.getStatus() == IntakeSessionStatus.COMPLETED);

        ClinicalSummary summary = clinicalSummaryRepository.findBySessionId(sessionId).orElse(null);
        statusDto.setClinicalSummaryStatus(summary != null ? summary.getStatus().name() : "NONE");

        List<IntegrationRecord> records = integrationRecordRepository.findBySessionId(sessionId);
        Instant latestSync = null;

        for (IntegrationRecord rec : records) {
            if (latestSync == null || (rec.getAttemptedAt() != null && rec.getAttemptedAt().isAfter(latestSync))) {
                latestSync = rec.getAttemptedAt();
            }
            if (rec.getIntegrationType() == IntegrationType.ABDM) {
                statusDto.setAbdmStatus(rec.getStatus().name());
                statusDto.setAbdmTransactionId(rec.getExternalReference());
            } else if (rec.getIntegrationType() == IntegrationType.HIS) {
                if ("FHIR_R4_BUNDLE_SYNC".equals(rec.getOperation())) {
                    statusDto.setFhirStatus(rec.getStatus().name());
                    statusDto.setFhirBundleId(rec.getExternalReference());
                } else {
                    statusDto.setHisStatus(rec.getStatus().name());
                    statusDto.setHisEncounterRef(rec.getExternalReference());
                }
            } else if (rec.getIntegrationType() == IntegrationType.HEALTHLAKE) {
                statusDto.setHealthLakeStatus(rec.getStatus().name());
                statusDto.setHealthLakeResourceRef(rec.getExternalReference());
            }
        }

        if (statusDto.getFhirStatus() == null) statusDto.setFhirStatus("NOT_SYNCED");
        if (statusDto.getAbdmStatus() == null) statusDto.setAbdmStatus("NOT_SYNCED");
        if (statusDto.getHisStatus() == null) statusDto.setHisStatus("NOT_SYNCED");
        if (statusDto.getHealthLakeStatus() == null) statusDto.setHealthLakeStatus("NOT_SYNCED");

        statusDto.setLastSyncedAt(latestSync);
        statusDto.setAdapterStatuses(Map.of(
                "FHIR", statusDto.getFhirStatus(),
                "ABDM", statusDto.getAbdmStatus(),
                "HIS", statusDto.getHisStatus(),
                "HealthLake", statusDto.getHealthLakeStatus()
        ));

        return statusDto;
    }

    private void markFieldsAsVerified(ClinicalSummaryDto summaryDto) {
        if (summaryDto == null) return;
        if (summaryDto.getSymptoms() != null) {
            for (StructuredSymptomDto s : summaryDto.getSymptoms()) {
                s.setSource("CLINICIAN_VERIFIED");
                s.setStatus("CONFIRMED");
                s.setVerified(true);
                s.setConfidence(1.0);
            }
        }
        if (summaryDto.getMedicalHistory() != null) {
            for (StructuredHistoryDto h : summaryDto.getMedicalHistory()) {
                h.setSource("CLINICIAN_VERIFIED");
                h.setStatus("CONFIRMED");
                h.setVerified(true);
                h.setConfidence(1.0);
            }
        }
        if (summaryDto.getMedications() != null) {
            for (StructuredMedicationDto m : summaryDto.getMedications()) {
                m.setSource("CLINICIAN_VERIFIED");
                m.setStatus("CONFIRMED");
                m.setVerified(true);
                m.setConfidence(1.0);
            }
        }
        if (summaryDto.getAllergies() != null) {
            for (StructuredAllergyDto a : summaryDto.getAllergies()) {
                a.setSource("CLINICIAN_VERIFIED");
                a.setStatus("CONFIRMED");
                a.setVerified(true);
                a.setConfidence(1.0);
            }
        }
        if (summaryDto.getAyurveda() != null) {
            summaryDto.getAyurveda().setSource("CLINICIAN_VERIFIED");
            summaryDto.getAyurveda().setVerified(true);
            summaryDto.getAyurveda().setConfidence(1.0);
        }
        if (summaryDto.getDualCodedConcepts() != null) {
            for (DualCodedConceptDto dcc : summaryDto.getDualCodedConcepts()) {
                dcc.setSource("CLINICIAN_VERIFIED");
                dcc.setStatus("CONFIRMED");
                dcc.setVerified(true);
                dcc.setConfidence(1.0);
            }
        }
    }

    private void saveIntegrationRecord(IntegrationType type, String operation, Patient patient, IntakeSession session, IntegrationStatus status, String extRef, String errorMsg, String payload) {
        try {
            IntegrationRecord rec = new IntegrationRecord(type, operation, patient, session);
            rec.setStatus(status);
            rec.setExternalReference(extRef);
            rec.setErrorMessage(errorMsg);
            rec.setResponsePayload(payload);
            rec.setCompletedAt(Instant.now());
            rec.setIdempotencyKey(type.name() + "-" + session.getId() + "-" + System.currentTimeMillis());
            integrationRecordRepository.save(rec);
        } catch (Exception e) {
            log.warn("Failed to save integration record for {}", type, e);
        }
    }
}
