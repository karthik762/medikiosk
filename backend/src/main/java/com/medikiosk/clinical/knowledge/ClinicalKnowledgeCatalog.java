package com.medikiosk.clinical.knowledge;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * Controlled clinical intent catalog and knowledge evaluation reference mapping
 * inspired by Synthea, MTS-Dialog, and MedQuAD benchmark reference standards.
 * 
 * Provides information-gathering intent definitions and metadata provenance.
 */
public class ClinicalKnowledgeCatalog {

    public enum Provenance {
        CLINICIAN_CURATED,
        SYNTHETIC_EVALUATION,
        RESEARCH_REFERENCE,
        TERMINOLOGY_REFERENCE
    }

    public static class IntentMetadata {
        private final String intentCode;
        private final String description;
        private final String clinicalCategory;
        private final Provenance provenance;
        private final boolean safetyCritical;

        public IntentMetadata(String intentCode, String description, String clinicalCategory, Provenance provenance, boolean safetyCritical) {
            this.intentCode = intentCode;
            this.description = description;
            this.clinicalCategory = clinicalCategory;
            this.provenance = provenance;
            this.safetyCritical = safetyCritical;
        }

        public String getIntentCode() { return intentCode; }
        public String getDescription() { return description; }
        public String getClinicalCategory() { return clinicalCategory; }
        public Provenance getProvenance() { return provenance; }
        public boolean isSafetyCritical() { return safetyCritical; }
    }

    private static final List<IntentMetadata> CATALOG = Arrays.asList(
            new IntentMetadata("SYMPTOM_CHARACTER", "Qualitative description of chief complaint (e.g. sharp, pressure, burning)", "HPI", Provenance.CLINICIAN_CURATED, false),
            new IntentMetadata("SYMPTOM_ONSET", "Time or circumstance when symptoms began", "HPI", Provenance.CLINICIAN_CURATED, false),
            new IntentMetadata("SYMPTOM_DURATION", "Total timeframe of ongoing symptoms", "HPI", Provenance.CLINICIAN_CURATED, false),
            new IntentMetadata("SYMPTOM_SEVERITY", "Numeric or qualitative pain/distress scale (0-10)", "HPI", Provenance.CLINICIAN_CURATED, true),
            new IntentMetadata("SYMPTOM_LOCATION", "Anatomical position or radiation pattern", "HPI", Provenance.CLINICIAN_CURATED, false),
            new IntentMetadata("ASSOCIATED_SYMPTOMS", "Secondary symptoms occurring alongside chief complaint", "HPI", Provenance.RESEARCH_REFERENCE, true),
            new IntentMetadata("AGGRAVATING_FACTORS", "Activities or triggers exacerbating symptoms", "HPI", Provenance.CLINICIAN_CURATED, false),
            new IntentMetadata("RELIEVING_FACTORS", "Interventions or positions easing symptoms", "HPI", Provenance.CLINICIAN_CURATED, false),
            new IntentMetadata("MEDICATION_STATUS", "Current prescription and OTC drug usage", "PMH", Provenance.SYNTHETIC_EVALUATION, false),
            new IntentMetadata("ALLERGY_STATUS", "Drug, food, or environmental hypersensitivities", "SAFETY", Provenance.CLINICIAN_CURATED, true),
            new IntentMetadata("PAST_HISTORY", "Prior medical conditions, surgeries, or hospitalizations", "PMH", Provenance.RESEARCH_REFERENCE, false),
            new IntentMetadata("FAMILY_HISTORY", "Hereditary or familial disease patterns", "FH", Provenance.SYNTHETIC_EVALUATION, false),
            new IntentMetadata("SOCIAL_HISTORY", "Tobacco, alcohol, occupational or living context", "SH", Provenance.RESEARCH_REFERENCE, false),
            new IntentMetadata("LIFESTYLE", "Dietary, sleep, and physical activity habits", "LIFESTYLE", Provenance.TERMINOLOGY_REFERENCE, false),
            new IntentMetadata("DOCUMENT_CONFIRMATION", "Verification of extracted OCR/document findings", "SAFETY", Provenance.CLINICIAN_CURATED, true)
    );

    public static List<IntentMetadata> getCatalog() {
        return CATALOG;
    }

    public static IntentMetadata findByCode(String code) {
        if (code == null) return null;
        return CATALOG.stream()
                .filter(i -> i.getIntentCode().equalsIgnoreCase(code))
                .findFirst()
                .orElse(null);
    }
}
