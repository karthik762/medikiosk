package com.medikiosk.ai.provider;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.medikiosk.concept.ClinicalConcept;
import com.medikiosk.concept.ConceptRegistry;
import com.medikiosk.dto.clinical.ClinicalCodingDto;
import com.medikiosk.dto.clinical.ClinicalConflictDto;
import com.medikiosk.dto.clinical.ClinicalSummaryDto;
import com.medikiosk.dto.clinical.DoctorVerificationDto;
import com.medikiosk.dto.clinical.DocumentFindingDto;
import com.medikiosk.dto.clinical.DualCodedConceptDto;
import com.medikiosk.dto.clinical.MissingInformationDto;
import com.medikiosk.dto.clinical.PatientInformationDto;
import com.medikiosk.dto.clinical.StructuredAllergyDto;
import com.medikiosk.dto.clinical.StructuredAyurvedaDto;
import com.medikiosk.dto.clinical.StructuredHistoryDto;
import com.medikiosk.dto.clinical.StructuredMedicationDto;
import com.medikiosk.dto.clinical.StructuredSymptomDto;
import com.medikiosk.dto.clinical.SummaryMetadataDto;
import com.medikiosk.dto.clinical.TriageSummaryDto;
import com.medikiosk.entity.Allergy;
import com.medikiosk.entity.Answer;
import com.medikiosk.entity.AyurvedaHistory;
import com.medikiosk.entity.DocumentExtraction;
import com.medikiosk.entity.MedicalDocument;
import com.medikiosk.entity.MedicalHistory;
import com.medikiosk.entity.Medication;
import com.medikiosk.entity.Symptom;
import com.medikiosk.entity.TriageResult;
import com.medikiosk.entity.enums.ClinicalSummaryStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.Period;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Deterministic Mock AI Provider for local development and automated testing.
 * Conforms to docs/AI_SPEC.md (Section 20-27), docs/PRODUCT_SPEC.md, and Stage 5B.
 * Strictly grounds every fact in supplied input without hallucination or autonomous diagnosis.
 */
public class MockAiProvider implements AiProvider {

    private static final Logger log = LoggerFactory.getLogger(MockAiProvider.class);
    private static final String PROVIDER_NAME = "MOCK_AI_PROVIDER";
    private static final String MODEL_IDENTIFIER = "mock-clinical-structuring-v1";

    private final ObjectMapper objectMapper;
    private final ConceptRegistry conceptRegistry;

    public MockAiProvider(ObjectMapper objectMapper) {
        this(objectMapper, new ConceptRegistry());
    }

    public MockAiProvider(ObjectMapper objectMapper, ConceptRegistry conceptRegistry) {
        this.objectMapper = objectMapper;
        this.conceptRegistry = conceptRegistry != null ? conceptRegistry : new ConceptRegistry();
    }

    @Override
    public String getProviderName() {
        return PROVIDER_NAME;
    }

    @Override
    public String getModelIdentifier() {
        return MODEL_IDENTIFIER;
    }

    @Override
    public boolean isMock() {
        return true;
    }

    @Override
    public AiStructuringResult generateClinicalSummary(AiStructuringContext context) {
        try {
            ClinicalSummaryDto dto = new ClinicalSummaryDto();
            dto.setPatientId(context.getPatient().getId());
            dto.setSessionId(context.getSession().getId());
            dto.setStatus(ClinicalSummaryStatus.AI_GENERATED);
            dto.setVersion(1);

            List<MissingInformationDto> missingInfoList = new ArrayList<>();
            List<ClinicalConflictDto> conflictsList = new ArrayList<>();
            List<DualCodedConceptDto> dualCodedConcepts = new ArrayList<>();

            // 0. Patient Information Block
            if (context.getPatient() != null) {
                Integer age = null;
                if (context.getPatient().getDateOfBirth() != null) {
                    age = Period.between(context.getPatient().getDateOfBirth(), LocalDate.now()).getYears();
                }
                PatientInformationDto patientInfo = new PatientInformationDto(
                        context.getPatient().getId(),
                        context.getPatient().getPatientIdentifier(),
                        context.getPatient().getFirstName(),
                        context.getPatient().getLastName(),
                        age,
                        context.getPatient().getGender(),
                        context.getPatient().getPhone(),
                        context.getPatient().getEmail(),
                        context.getSession().getId()
                );
                dto.setPatientInfo(patientInfo);
            }

            // 1. Chief Complaint & Answers
            String chiefComplaint = null;
            if (context.getAnswers() != null) {
                for (Answer ans : context.getAnswers()) {
                    if (ans.getQuestion() != null && "CHIEF_COMPLAINT".equalsIgnoreCase(ans.getQuestion().getQuestionCode())) {
                        chiefComplaint = ans.getAnswerText();
                    }
                }
            }
            if (chiefComplaint == null && context.getSymptoms() != null && !context.getSymptoms().isEmpty()) {
                chiefComplaint = context.getSymptoms().get(0).getName();
            }
            if (chiefComplaint == null) {
                chiefComplaint = "Not provided";
                missingInfoList.add(new MissingInformationDto("chiefComplaint", "Chief complaint was not provided in intake"));
            }
            dto.setChiefComplaint(chiefComplaint);

            // 2. Structured Symptoms & Dual Codings
            List<StructuredSymptomDto> symptomDtos = new ArrayList<>();
            if (context.getSymptoms() != null) {
                for (Symptom s : context.getSymptoms()) {
                    String src = s.getSource() != null ? s.getSource() : "PATIENT_REPORTED";
                    StructuredSymptomDto symDto = new StructuredSymptomDto(
                            s.getName(),
                            s.getDuration(),
                            s.getSeverity(),
                            s.getLocation(),
                            s.getOnset(),
                            s.getFrequency(),
                            src
                    );

                    List<ClinicalCodingDto> codings = conceptRegistry.getCodingsForTerm(s.getName());
                    Optional<ClinicalConcept> conceptOpt = conceptRegistry.findByTerm(s.getName());

                    symDto.setCoding(codings);
                    symDto.setClinicalConceptCode(conceptOpt.map(ClinicalConcept::getCode).orElse("UNMAPPED"));
                    symDto.setConfidence(conceptOpt.isPresent() ? 0.95 : 0.70);
                    symDto.setStatus(s.getSeverity() != null && s.getSeverity() >= 8 ? "SUSPECTED" : "REPORTED");
                    symDto.setVerified(false);

                    symptomDtos.add(symDto);

                    dualCodedConcepts.add(new DualCodedConceptDto(
                            s.getName(),
                            symDto.getClinicalConceptCode(),
                            codings,
                            symDto.getStatus(),
                            symDto.getConfidence(),
                            src,
                            false
                    ));

                    if (s.getSeverity() == null) {
                        missingInfoList.add(new MissingInformationDto("severity", "Severity not recorded for symptom: " + s.getName()));
                    }
                }
            }
            dto.setSymptoms(symptomDtos);

            // 3. Structured Medical History
            List<StructuredHistoryDto> historyDtos = new ArrayList<>();
            if (context.getMedicalHistories() != null) {
                for (MedicalHistory mh : context.getMedicalHistories()) {
                    String src = mh.getSource() != null ? mh.getSource() : "PATIENT_REPORTED";
                    Boolean isVer = Boolean.TRUE.equals(mh.getVerified());
                    StructuredHistoryDto histDto = new StructuredHistoryDto(
                            mh.getCategory(),
                            mh.getDescription(),
                            src,
                            isVer
                    );

                    List<ClinicalCodingDto> codings = conceptRegistry.getCodingsForTerm(mh.getDescription());
                    Optional<ClinicalConcept> conceptOpt = conceptRegistry.findByTerm(mh.getDescription());

                    histDto.setCoding(codings);
                    histDto.setClinicalConceptCode(conceptOpt.map(ClinicalConcept::getCode).orElse("UNMAPPED"));
                    histDto.setConfidence(conceptOpt.isPresent() ? 0.95 : 0.75);
                    histDto.setStatus(isVer ? "CONFIRMED" : "POSSIBLE");

                    historyDtos.add(histDto);

                    dualCodedConcepts.add(new DualCodedConceptDto(
                            mh.getDescription(),
                            histDto.getClinicalConceptCode(),
                            codings,
                            histDto.getStatus(),
                            histDto.getConfidence(),
                            src,
                            isVer
                    ));
                }
            }
            dto.setMedicalHistory(historyDtos);

            // 4. Structured Medications & Document Cross-Check
            List<StructuredMedicationDto> medDtos = new ArrayList<>();
            Map<String, StructuredMedicationDto> medByName = new HashMap<>();

            if (context.getMedications() != null) {
                for (Medication m : context.getMedications()) {
                    String src = m.getSource() != null ? m.getSource() : "PATIENT_REPORTED";
                    StructuredMedicationDto sm = new StructuredMedicationDto(
                            m.getMedicationName(),
                            m.getDosage(),
                            m.getFrequency(),
                            m.getDuration(),
                            src,
                            false,
                            null
                    );
                    sm.setCoding(conceptRegistry.getCodingsForTerm(m.getMedicationName()));
                    sm.setConfidence(0.90);
                    sm.setStatus("REPORTED");
                    sm.setVerified(false);

                    medByName.put(m.getMedicationName().toLowerCase(), sm);
                    medDtos.add(sm);
                }
            }

            // Document findings
            List<DocumentFindingDto> docFindings = new ArrayList<>();
            if (context.getDocuments() != null) {
                for (MedicalDocument doc : context.getDocuments()) {
                    DocumentExtraction ext = context.getExtractions() != null ? context.getExtractions().get(doc) : null;
                    List<String> uncertaintyFlags = new ArrayList<>();
                    Map<String, Object> extData = new HashMap<>();

                    if (ext != null) {
                        if (ext.getConfidenceScore() != null && ext.getConfidenceScore().compareTo(BigDecimal.valueOf(0.75)) < 0) {
                            uncertaintyFlags.add("LOW_CONFIDENCE_OCR");
                            uncertaintyFlags.add("DOCTOR_VERIFICATION_REQUIRED");
                        }
                        if (ext.getExtractionData() != null && !ext.getExtractionData().isBlank()) {
                            try {
                                extData = objectMapper.readValue(ext.getExtractionData(), new TypeReference<Map<String, Object>>() {});
                            } catch (Exception ignored) {
                            }
                        }

                        // Check document medications for conflicts
                        if (extData.containsKey("medications") && extData.get("medications") instanceof List<?> docMeds) {
                            for (Object item : docMeds) {
                                if (item instanceof Map<?, ?> itemMap) {
                                    String name = String.valueOf(itemMap.get("name"));
                                    String dose = itemMap.containsKey("dosage") ? String.valueOf(itemMap.get("dosage")) : null;
                                    String freq = itemMap.containsKey("frequency") ? String.valueOf(itemMap.get("frequency")) : null;

                                    if (name != null && !name.isBlank()) {
                                        StructuredMedicationDto existing = medByName.get(name.toLowerCase());
                                        if (existing != null) {
                                            // Check for dosage conflict
                                            if (dose != null && existing.getDosage() != null && !dose.equalsIgnoreCase(existing.getDosage())) {
                                                existing.setConflictFlag(true);
                                                existing.setVerificationNotes("Patient reported dosage (" + existing.getDosage() + ") differs from OCR (" + dose + "). Verification required.");
                                                conflictsList.add(new ClinicalConflictDto(
                                                        "medication:" + name,
                                                        existing.getDosage(),
                                                        dose,
                                                        "REVIEW_REQUIRED"
                                                ));
                                            }
                                        } else {
                                            // Document-reported medication not previously mentioned by patient
                                            StructuredMedicationDto docMed = new StructuredMedicationDto(
                                                    name,
                                                    dose,
                                                    freq,
                                                    null,
                                                    "OCR_EXTRACTION",
                                                    false,
                                                    "Extracted from source document: " + doc.getFileName()
                                            );
                                            docMed.setCoding(conceptRegistry.getCodingsForTerm(name));
                                            docMed.setConfidence(ext.getConfidenceScore() != null ? ext.getConfidenceScore().doubleValue() : 0.80);
                                            docMed.setStatus("EXTRACTED");
                                            medDtos.add(docMed);
                                        }
                                    }
                                }
                            }
                        }
                    }

                    String textPreview = ext != null && ext.getExtractedText() != null
                            ? (ext.getExtractedText().length() > 200 ? ext.getExtractedText().substring(0, 200) + "..." : ext.getExtractedText())
                            : null;

                    docFindings.add(new DocumentFindingDto(
                            doc.getId(),
                            doc.getDocumentType(),
                            doc.getFileName(),
                            ext != null ? ext.getConfidenceScore() : null,
                            ext != null ? ext.getProcessingEngine() : null,
                            uncertaintyFlags,
                            extData,
                            textPreview
                    ));
                }
            }
            dto.setMedications(medDtos);
            dto.setDocumentFindings(docFindings);

            // 5. Structured Allergies
            List<StructuredAllergyDto> allergyDtos = new ArrayList<>();
            if (context.getAllergies() != null && !context.getAllergies().isEmpty()) {
                for (Allergy a : context.getAllergies()) {
                    String src = a.getSource() != null ? a.getSource() : "PATIENT_REPORTED";
                    StructuredAllergyDto ad = new StructuredAllergyDto(
                            a.getAllergen(),
                            a.getReaction(),
                            a.getSeverity(),
                            src
                    );
                    ad.setCoding(conceptRegistry.getCodingsForTerm(a.getAllergen()));
                    ad.setConfidence(0.90);
                    ad.setStatus("REPORTED");
                    ad.setVerified(false);

                    allergyDtos.add(ad);
                }
            } else {
                missingInfoList.add(new MissingInformationDto("allergies", "No known allergies confirmed or recorded during intake"));
            }
            dto.setAllergies(allergyDtos);

            // 6. Structured Ayurveda Findings (Separated clearly)
            StructuredAyurvedaDto ayurvedaDto = new StructuredAyurvedaDto();
            if (context.getAyurvedaHistory() != null) {
                AyurvedaHistory ah = context.getAyurvedaHistory();
                if (ah.getPrakritiData() != null && !ah.getPrakritiData().isBlank()) {
                    Map<String, Object> prak = objectMapper.readValue(ah.getPrakritiData(), new TypeReference<Map<String, Object>>() {});
                    ayurvedaDto.setPrakritiResponses(prak);
                    if (prak.containsKey("dominantDosha")) {
                        ayurvedaDto.setPrakriti(String.valueOf(prak.get("dominantDosha")));
                    }
                }
                if (ah.getNidanaData() != null && !ah.getNidanaData().isBlank()) {
                    Map<String, Object> nid = objectMapper.readValue(ah.getNidanaData(), new TypeReference<Map<String, Object>>() {});
                    ayurvedaDto.setNidanaResponses(nid);
                    if (nid.containsKey("imbalance")) {
                        ayurvedaDto.setVikriti(String.valueOf(nid.get("imbalance")));
                    }
                }
                if (ah.getParikshaData() != null && !ah.getParikshaData().isBlank()) {
                    Map<String, Object> par = objectMapper.readValue(ah.getParikshaData(), new TypeReference<Map<String, Object>>() {});
                    ayurvedaDto.setParikshaResponses(par);
                    if (par.containsKey("agni")) {
                        ayurvedaDto.setAgni(String.valueOf(par.get("agni")));
                    }
                }
                ayurvedaDto.setSource("PATIENT_REPORTED");
                ayurvedaDto.setConfidence(0.85);
                ayurvedaDto.setVerified(false);

                List<String> obs = new ArrayList<>();
                obs.add("Ayurvedic case-taking data structured; clinician evaluation required for Prakriti/Vikriti assessment.");
                ayurvedaDto.setDocumentedObservations(obs);
            }
            dto.setAyurveda(ayurvedaDto);

            // 7. Deterministic Triage Result (Copied verbatim)
            if (context.getTriageResult() != null) {
                TriageResult tr = context.getTriageResult();
                List<Map<String, Object>> rules = new ArrayList<>();
                if (tr.getTriggeredRules() != null && !tr.getTriggeredRules().isBlank()) {
                    try {
                        rules = objectMapper.readValue(tr.getTriggeredRules(), new TypeReference<List<Map<String, Object>>>() {});
                    } catch (Exception ignored) {
                    }
                }
                dto.setTriage(new TriageSummaryDto(tr.getPriority(), tr.getExplanation(), rules));
            }

            // 8. Dual Coded Concepts & Missing Info
            dto.setDualCodedConcepts(dualCodedConcepts);
            dto.setMissingInformation(missingInfoList);
            dto.setConflicts(conflictsList);

            // 9. Doctor Verification Status
            dto.setDoctorVerification(new DoctorVerificationDto("PENDING", null, null, null, null));

            // 10. Metadata
            dto.setMetadata(new SummaryMetadataDto(PROVIDER_NAME, MODEL_IDENTIFIER, true, Instant.now(), 1));

            // 11. Human readable summary text
            StringBuilder sb = new StringBuilder();
            sb.append("PRE-CONSULTATION CLINICAL SUMMARY (AI-GENERATED — DOCTOR REVIEW REQUIRED)\n");
            sb.append("=========================================================================\n");
            sb.append("Patient: ").append(context.getPatient().getFirstName()).append(" ").append(context.getPatient().getLastName()).append("\n");
            sb.append("Chief Complaint: ").append(chiefComplaint).append("\n\n");

            sb.append("Reported Symptoms:\n");
            if (symptomDtos.isEmpty()) {
                sb.append("- None reported\n");
            } else {
                for (StructuredSymptomDto s : symptomDtos) {
                    sb.append("- ").append(s.getName());
                    if (s.getDuration() != null) sb.append(" (duration: ").append(s.getDuration()).append(")");
                    if (s.getSeverity() != null) sb.append(" [severity: ").append(s.getSeverity()).append("/10]");
                    sb.append(" [Source: ").append(s.getSource()).append(", Confidence: ").append(s.getConfidence()).append("]\n");
                }
            }

            sb.append("\nMedications:\n");
            if (medDtos.isEmpty()) {
                sb.append("- None reported\n");
            } else {
                for (StructuredMedicationDto m : medDtos) {
                    sb.append("- ").append(m.getMedicationName());
                    if (m.getDosage() != null) sb.append(" ").append(m.getDosage());
                    if (m.getFrequency() != null) sb.append(" ").append(m.getFrequency());
                    sb.append(" [Source: ").append(m.getSource()).append("]");
                    if (Boolean.TRUE.equals(m.getConflictFlag())) {
                        sb.append(" *DISCREPANCY: ").append(m.getVerificationNotes()).append("*");
                    }
                    sb.append("\n");
                }
            }

            if (dto.getTriage() != null) {
                sb.append("\nTriage Routing: ").append(dto.getTriage().getPriority())
                        .append(" (Source: ").append(dto.getTriage().getSource()).append(")\n");
                if (dto.getTriage().getExplanation() != null) {
                    sb.append("Notes: ").append(dto.getTriage().getExplanation()).append("\n");
                }
            }

            if (!missingInfoList.isEmpty()) {
                sb.append("\nMissing / Unrecorded Intake Information:\n");
                for (MissingInformationDto mi : missingInfoList) {
                    sb.append("- ").append(mi.getField()).append(": ").append(mi.getReason()).append("\n");
                }
            }

            sb.append("\nSafety Notice: MediKiosk is not an autonomous diagnostic system. All findings require clinician review and verification.");
            dto.setHumanReadableSummary(sb.toString());

            String rawJson = objectMapper.writeValueAsString(dto);
            return AiStructuringResult.success(dto, rawJson, MODEL_IDENTIFIER, true);

        } catch (Exception ex) {
            log.error("Failed to generate mock clinical summary", ex);
            return AiStructuringResult.failure("Failed to generate clinical summary: " + ex.getMessage(), MODEL_IDENTIFIER, true);
        }
    }
}
