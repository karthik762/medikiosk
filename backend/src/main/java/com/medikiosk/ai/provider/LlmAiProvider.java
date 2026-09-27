package com.medikiosk.ai.provider;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.medikiosk.ai.gateway.AiModelGateway;
import com.medikiosk.ai.prompt.PromptTemplates;
import com.medikiosk.dto.clinical.ClinicalSummaryDto;
import com.medikiosk.dto.clinical.DoctorVerificationDto;
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
import org.springframework.beans.factory.annotation.Value;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Real LLM-backed AI Provider implementing clinical pre-consult summary structuring
 * using an external OpenAI/OpenRouter-compatible model gateway.
 * Enforces strict non-fabrication, deterministic triage preservation, and privacy safeguards.
 */
public class LlmAiProvider implements AiProvider {

    private static final Logger log = LoggerFactory.getLogger(LlmAiProvider.class);
    private static final String PROVIDER_NAME = "REAL_LLM_PROVIDER";

    private final AiModelGateway aiModelGateway;
    private final ObjectMapper objectMapper;

    @Value("${medikiosk.ai.model:google/gemini-2.0-flash-001}")
    private String modelIdentifier;

    public LlmAiProvider(AiModelGateway aiModelGateway, ObjectMapper objectMapper) {
        this.aiModelGateway = aiModelGateway;
        this.objectMapper = objectMapper != null ? objectMapper.copy().findAndRegisterModules() : new ObjectMapper().findAndRegisterModules();
    }

    @Override
    public String getProviderName() {
        return PROVIDER_NAME;
    }

    @Override
    public String getModelIdentifier() {
        return modelIdentifier != null ? modelIdentifier : "llm-clinical-structuring-v1";
    }

    @Override
    public boolean isMock() {
        return false;
    }

    @Override
    public AiStructuringResult generateClinicalSummary(AiStructuringContext context) {
        try {
            // Build minimized, privacy-aware context payload for prompt
            String userPromptJson = buildUserPrompt(context);

            String systemPrompt = PromptTemplates.CLINICAL_SUMMARY_SYSTEM + "\n" +
                    """
                    STRICT NON-FABRICATION AND SAFETY CONSTRAINTS:
                    1. Use ONLY facts explicitly supplied in the intake payload. Do NOT infer, guess, or fabricate clinical facts.
                    2. If a field (e.g. chief complaint, duration, severity, allergies) is missing, mark it in missingInformation.
                    3. Do NOT make definitive medical diagnoses or prescribe medications.
                    4. Information extracted from document OCR must be clearly flagged as requiring doctor verification.
                    5. Keep conventional medical history separate from Ayurveda intake data.
                    6. The deterministic triage priority MUST NOT be modified or overridden.
                    7. Output must strictly conform to the ClinicalSummaryDto JSON structure.
                    """;

            ClinicalSummaryDto dto = aiModelGateway.generateStructured(systemPrompt, userPromptJson, ClinicalSummaryDto.class);

            if (dto == null) {
                return AiStructuringResult.failure("LLM model returned null output", getModelIdentifier(), false);
            }

            // Post-process to guarantee non-negotiable systemic invariants
            dto.setPatientId(context.getPatient().getId());
            dto.setSessionId(context.getSession().getId());
            dto.setStatus(ClinicalSummaryStatus.AI_GENERATED);
            dto.setVersion(1);

            // Force Doctor Verification to PENDING
            dto.setDoctorVerification(new DoctorVerificationDto("PENDING", null, null, null, null));

            // Force Deterministic Triage from Ground Truth Context
            if (context.getTriageResult() != null) {
                TriageResult tr = context.getTriageResult();
                List<Map<String, Object>> rules = new ArrayList<>();
                if (tr.getTriggeredRules() != null && !tr.getTriggeredRules().isBlank()) {
                    try {
                        rules = objectMapper.readValue(tr.getTriggeredRules(), new TypeReference<List<Map<String, Object>>>() {});
                    } catch (Exception ignored) {}
                }
                dto.setTriage(new TriageSummaryDto(tr.getPriority(), tr.getExplanation(), rules));
            }

            // Set metadata
            dto.setMetadata(new SummaryMetadataDto(PROVIDER_NAME, getModelIdentifier(), false, Instant.now(), 1));

            // Generate human readable summary if missing or brief
            if (dto.getHumanReadableSummary() == null || dto.getHumanReadableSummary().isBlank()) {
                dto.setHumanReadableSummary(buildHumanReadableSummary(context, dto));
            }

            String rawJson = objectMapper.writeValueAsString(dto);
            return AiStructuringResult.success(dto, rawJson, getModelIdentifier(), false);

        } catch (Exception ex) {
            log.error("Real LLM provider failed to generate clinical summary", ex);
            return AiStructuringResult.failure("LLM structuring error: " + ex.getMessage(), getModelIdentifier(), false);
        }
    }

    private String buildUserPrompt(AiStructuringContext context) throws Exception {
        Map<String, Object> payload = new HashMap<>();

        // Patient demographics (minimal - no sensitive auth credentials/JWTs)
        if (context.getPatient() != null) {
            Map<String, Object> patientMap = new HashMap<>();
            patientMap.put("gender", context.getPatient().getGender());
            patientMap.put("dateOfBirth", context.getPatient().getDateOfBirth() != null ? context.getPatient().getDateOfBirth().toString() : null);
            payload.put("patient", patientMap);
        }

        // Answers / Chief Complaint
        List<Map<String, String>> answersList = new ArrayList<>();
        if (context.getAnswers() != null) {
            for (Answer ans : context.getAnswers()) {
                if (ans.getQuestion() != null) {
                    Map<String, String> am = new HashMap<>();
                    am.put("code", ans.getQuestion().getQuestionCode() != null ? ans.getQuestion().getQuestionCode() : "");
                    am.put("text", ans.getAnswerText() != null ? ans.getAnswerText() : "");
                    answersList.add(am);
                }
            }
        }
        payload.put("answers", answersList);

        // Symptoms
        List<Map<String, Object>> symptomsList = new ArrayList<>();
        if (context.getSymptoms() != null) {
            for (Symptom s : context.getSymptoms()) {
                Map<String, Object> sm = new HashMap<>();
                sm.put("name", s.getName());
                sm.put("duration", s.getDuration());
                sm.put("severity", s.getSeverity());
                sm.put("location", s.getLocation());
                sm.put("onset", s.getOnset());
                sm.put("frequency", s.getFrequency());
                symptomsList.add(sm);
            }
        }
        payload.put("symptoms", symptomsList);

        // Medical History
        List<Map<String, Object>> historyList = new ArrayList<>();
        if (context.getMedicalHistories() != null) {
            for (MedicalHistory mh : context.getMedicalHistories()) {
                Map<String, Object> hm = new HashMap<>();
                hm.put("category", mh.getCategory() != null ? mh.getCategory() : "");
                hm.put("description", mh.getDescription() != null ? mh.getDescription() : "");
                historyList.add(hm);
            }
        }
        payload.put("medicalHistory", historyList);

        // Medications
        List<Map<String, Object>> medList = new ArrayList<>();
        if (context.getMedications() != null) {
            for (Medication m : context.getMedications()) {
                Map<String, Object> mm = new HashMap<>();
                mm.put("name", m.getMedicationName());
                mm.put("dosage", m.getDosage());
                mm.put("frequency", m.getFrequency());
                medList.add(mm);
            }
        }
        payload.put("medications", medList);

        // Allergies
        List<Map<String, Object>> allergyList = new ArrayList<>();
        if (context.getAllergies() != null) {
            for (Allergy a : context.getAllergies()) {
                Map<String, Object> alm = new HashMap<>();
                alm.put("allergen", a.getAllergen() != null ? a.getAllergen() : "");
                alm.put("reaction", a.getReaction() != null ? a.getReaction() : "");
                alm.put("severity", a.getSeverity() != null ? a.getSeverity() : "");
                allergyList.add(alm);
            }
        }
        payload.put("allergies", allergyList);

        // OCR Extracted Documents
        List<Map<String, Object>> docList = new ArrayList<>();
        if (context.getDocuments() != null) {
            for (MedicalDocument doc : context.getDocuments()) {
                DocumentExtraction ext = context.getExtractions() != null ? context.getExtractions().get(doc) : null;
                Map<String, Object> dm = new HashMap<>();
                dm.put("fileName", doc.getFileName());
                dm.put("documentType", doc.getDocumentType());
                if (ext != null) {
                    dm.put("ocrEngine", ext.getProcessingEngine());
                    dm.put("extractedText", ext.getExtractedText());
                }
                docList.add(dm);
            }
        }
        payload.put("documents", docList);

        // Ayurveda Intake Data
        if (context.getAyurvedaHistory() != null) {
            AyurvedaHistory ah = context.getAyurvedaHistory();
            Map<String, Object> ayurMap = new HashMap<>();
            ayurMap.put("prakritiData", ah.getPrakritiData());
            ayurMap.put("nidanaData", ah.getNidanaData());
            ayurMap.put("parikshaData", ah.getParikshaData());
            payload.put("ayurvedaHistory", ayurMap);
        }

        // Triage Result
        if (context.getTriageResult() != null) {
            TriageResult tr = context.getTriageResult();
            Map<String, Object> tm = new HashMap<>();
            tm.put("priority", tr.getPriority() != null ? tr.getPriority().name() : "");
            tm.put("explanation", tr.getExplanation() != null ? tr.getExplanation() : "");
            payload.put("triage", tm);
        }

        return objectMapper.writeValueAsString(payload);
    }

    private String buildHumanReadableSummary(AiStructuringContext context, ClinicalSummaryDto dto) {
        StringBuilder sb = new StringBuilder();
        sb.append("PRE-CONSULTATION SUMMARY (AI-GENERATED — DOCTOR REVIEW REQUIRED)\n");
        sb.append("===============================================================\n");
        if (context.getPatient() != null) {
            sb.append("Patient: ").append(context.getPatient().getFirstName()).append(" ").append(context.getPatient().getLastName()).append("\n");
        }
        sb.append("Chief Complaint: ").append(dto.getChiefComplaint() != null ? dto.getChiefComplaint() : "Not provided").append("\n\n");

        sb.append("Reported Symptoms:\n");
        if (dto.getSymptoms() == null || dto.getSymptoms().isEmpty()) {
            sb.append("- None reported\n");
        } else {
            for (StructuredSymptomDto s : dto.getSymptoms()) {
                sb.append("- ").append(s.getName());
                if (s.getDuration() != null) sb.append(" (duration: ").append(s.getDuration()).append(")");
                if (s.getSeverity() != null) sb.append(" [severity: ").append(s.getSeverity()).append("/10]");
                sb.append(" [Source: ").append(s.getSource() != null ? s.getSource() : "PATIENT_RESPONSE").append("]\n");
            }
        }

        sb.append("\nMedications:\n");
        if (dto.getMedications() == null || dto.getMedications().isEmpty()) {
            sb.append("- None reported\n");
        } else {
            for (StructuredMedicationDto m : dto.getMedications()) {
                sb.append("- ").append(m.getMedicationName());
                if (m.getDosage() != null) sb.append(" ").append(m.getDosage());
                if (m.getFrequency() != null) sb.append(" ").append(m.getFrequency());
                sb.append(" [Source: ").append(m.getSource() != null ? m.getSource() : "PATIENT_RESPONSE").append("]");
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

        sb.append("\nSafety Notice: MediKiosk is not an autonomous diagnostic system. All findings require clinician review and verification.");
        return sb.toString();
    }
}
