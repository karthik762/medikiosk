package com.medikiosk.clinical;

import com.medikiosk.clinical.knowledge.ClinicalKnowledgeCatalog;
import com.medikiosk.clinical.knowledge.ClinicalKnowledgeCatalog.IntentMetadata;
import com.medikiosk.clinical.knowledge.ClinicalKnowledgeCatalog.Provenance;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ClinicalKnowledgeTest {

    @Test
    @DisplayName("Verify Clinical Knowledge Catalog contains required information-gathering intents")
    void testCatalogContainsRequiredIntents() {
        List<IntentMetadata> catalog = ClinicalKnowledgeCatalog.getCatalog();
        assertNotNull(catalog);
        assertFalse(catalog.isEmpty());

        String[] requiredIntents = {
                "SYMPTOM_CHARACTER", "SYMPTOM_ONSET", "SYMPTOM_DURATION", "SYMPTOM_SEVERITY",
                "SYMPTOM_LOCATION", "ASSOCIATED_SYMPTOMS", "AGGRAVATING_FACTORS", "RELIEVING_FACTORS",
                "MEDICATION_STATUS", "ALLERGY_STATUS", "PAST_HISTORY", "FAMILY_HISTORY",
                "SOCIAL_HISTORY", "LIFESTYLE", "DOCUMENT_CONFIRMATION"
        };

        for (String intentCode : requiredIntents) {
            IntentMetadata metadata = ClinicalKnowledgeCatalog.findByCode(intentCode);
            assertNotNull(metadata, "Intent code " + intentCode + " must exist in ClinicalKnowledgeCatalog");
            assertNotNull(metadata.getDescription());
            assertNotNull(metadata.getClinicalCategory());
            assertNotNull(metadata.getProvenance());
        }
    }

    @Test
    @DisplayName("Verify intent provenance types are assigned correctly without disease diagnosis rules")
    void testIntentProvenanceAssignments() {
        IntentMetadata characterMeta = ClinicalKnowledgeCatalog.findByCode("SYMPTOM_CHARACTER");
        assertEquals(Provenance.CLINICIAN_CURATED, characterMeta.getProvenance());
        assertFalse(characterMeta.isSafetyCritical());

        IntentMetadata severityMeta = ClinicalKnowledgeCatalog.findByCode("SYMPTOM_SEVERITY");
        assertTrue(severityMeta.isSafetyCritical());

        IntentMetadata medMeta = ClinicalKnowledgeCatalog.findByCode("MEDICATION_STATUS");
        assertEquals(Provenance.SYNTHETIC_EVALUATION, medMeta.getProvenance());
    }
}
