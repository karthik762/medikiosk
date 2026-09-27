package com.medikiosk.ai.safety;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.medikiosk.ai.provider.AiStructuringContext;
import com.medikiosk.dto.clinical.ClinicalSummaryDto;
import com.medikiosk.dto.clinical.StructuredMedicationDto;
import com.medikiosk.entity.DocumentExtraction;
import com.medikiosk.entity.MedicalDocument;
import com.medikiosk.entity.Medication;
import com.medikiosk.entity.TriageResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * Deterministic application-layer safety and hallucination validator.
 * Enforces strict non-negotiable rules from docs/AI_SPEC.md and docs/PRODUCT_SPEC.md:
 * - Rejects diagnostic claims
 * - Rejects prescription and treatment recommendations
 * - Rejects unauthorized triage overrides
 * - Rejects doctor verification spoofing
 * - Rejects ungrounded / hallucinated medications
 */
@Component
public class AiSafetyValidator {

    private static final Logger log = LoggerFactory.getLogger(AiSafetyValidator.class);

    private final ObjectMapper objectMapper;

    // Forbidden diagnosis patterns
    private static final Pattern DIAGNOSIS_PATTERN = Pattern.compile(
            "\\b(diagnosed with|diagnosis:|definitive diagnosis|has diabetes|has hypertension|has asthma|likely migraine|confirmed covid|suffering from pneumonia)\\b",
            Pattern.CASE_INSENSITIVE
    );

    // Forbidden treatment / prescribing patterns
    private static final Pattern TREATMENT_PATTERN = Pattern.compile(
            "\\b(prescribe|prescribed to take|recommended treatment|treatment plan:|start taking|take 500\\s?mg|increase dosage|discontinue medication|stop taking)\\b",
            Pattern.CASE_INSENSITIVE
    );

    public AiSafetyValidator(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    /**
     * Validates AI-structured clinical summary against source context and safety boundaries.
     *
     * @param summary Structured pre-consult summary.
     * @param context Source intake and document extractions.
     * @throws AiSafetyViolationException if any safety violation or unsupported hallucination is detected.
     */
    public void validate(ClinicalSummaryDto summary, AiStructuringContext context) {
        if (summary == null) {
            throw new AiSafetyViolationException("NULL_OUTPUT", "AI output is null or empty");
        }

        // 1. Diagnosis Guard
        checkDiagnosisClaims(summary);

        // 2. Treatment & Prescription Guard
        checkTreatmentRecommendations(summary);

        // 3. Triage Override Guard
        checkTriageIntegrity(summary, context.getTriageResult());

        // 4. Doctor Verification Spoofing Guard
        checkVerificationIntegrity(summary);

        // 5. Hallucination Guard (Grounded Medications)
        checkMedicationGrounding(summary, context);
    }

    private void checkDiagnosisClaims(ClinicalSummaryDto summary) {
        if (summary.getHumanReadableSummary() != null && DIAGNOSIS_PATTERN.matcher(summary.getHumanReadableSummary()).find()) {
            throw new AiSafetyViolationException("AUTONOMOUS_DIAGNOSIS_PROHIBITED", "AI output contains autonomous diagnostic claim");
        }
        if (summary.getChiefComplaint() != null && DIAGNOSIS_PATTERN.matcher(summary.getChiefComplaint()).find()) {
            throw new AiSafetyViolationException("AUTONOMOUS_DIAGNOSIS_PROHIBITED", "AI chief complaint contains autonomous diagnostic claim");
        }
    }

    private void checkTreatmentRecommendations(ClinicalSummaryDto summary) {
        if (summary.getHumanReadableSummary() != null && TREATMENT_PATTERN.matcher(summary.getHumanReadableSummary()).find()) {
            throw new AiSafetyViolationException("TREATMENT_RECOMMENDATION_PROHIBITED", "AI output contains treatment or prescription recommendation");
        }
    }

    private void checkTriageIntegrity(ClinicalSummaryDto summary, TriageResult groundTruthTriage) {
        if (groundTruthTriage != null && summary.getTriage() != null) {
            if (summary.getTriage().getPriority() != groundTruthTriage.getPriority()) {
                throw new AiSafetyViolationException("TRIAGE_OVERRIDE_PROHIBITED",
                        "AI attempted to modify deterministic triage priority from " + groundTruthTriage.getPriority() + " to " + summary.getTriage().getPriority());
            }
        }
    }

    private void checkVerificationIntegrity(ClinicalSummaryDto summary) {
        if (summary.getDoctorVerification() != null) {
            String status = summary.getDoctorVerification().getStatus();
            if ("VERIFIED".equalsIgnoreCase(status) || "APPROVED".equalsIgnoreCase(status)) {
                throw new AiSafetyViolationException("VERIFICATION_SPOOFING_PROHIBITED",
                        "AI generated summary cannot mark itself as doctor verified");
            }
        }
    }

    private void checkMedicationGrounding(ClinicalSummaryDto summary, AiStructuringContext context) {
        if (summary.getMedications() == null || summary.getMedications().isEmpty()) {
            return;
        }

        // Collect all grounded medication names from patient intake and OCR extractions
        Set<String> groundedMeds = new HashSet<>();
        if (context.getMedications() != null) {
            for (Medication m : context.getMedications()) {
                if (m.getMedicationName() != null) {
                    groundedMeds.add(m.getMedicationName().trim().toLowerCase(Locale.ROOT));
                }
            }
        }

        if (context.getExtractions() != null) {
            for (DocumentExtraction ext : context.getExtractions().values()) {
                if (ext != null && ext.getExtractionData() != null && !ext.getExtractionData().isBlank()) {
                    try {
                        Map<String, Object> data = objectMapper.readValue(ext.getExtractionData(), new TypeReference<Map<String, Object>>() {});
                        if (data.containsKey("medications") && data.get("medications") instanceof List<?> list) {
                            for (Object item : list) {
                                if (item instanceof Map<?, ?> itemMap && itemMap.containsKey("name")) {
                                    groundedMeds.add(String.valueOf(itemMap.get("name")).trim().toLowerCase(Locale.ROOT));
                                }
                            }
                        }
                    } catch (Exception ignored) {
                    }
                }
            }
        }

        // Check if any summary medication is an invented / ungrounded hallucination
        for (StructuredMedicationDto sm : summary.getMedications()) {
            if (sm.getMedicationName() != null && !sm.getMedicationName().isBlank()) {
                String medName = sm.getMedicationName().trim().toLowerCase(Locale.ROOT);
                // Allow fuzzy or substring check (e.g., "Metformin" in "Metformin 500mg")
                boolean grounded = groundedMeds.stream().anyMatch(g -> medName.contains(g) || g.contains(medName));
                if (!grounded) {
                    log.warn("Hallucination detected: AI output contains medication '{}' not found in intake or documents", sm.getMedicationName());
                    throw new AiSafetyViolationException("HALLUCINATED_ENTITY",
                            "AI output contains ungrounded medication: " + sm.getMedicationName());
                }
            }
        }
    }
}
