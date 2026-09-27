package com.medikiosk.clinical.nlp;

/**
 * Provider interface for extracting structured clinical entities, concepts, and assertion states
 * from raw unstructured patient statements or transcribed audio.
 */
public interface ClinicalNlpProvider {

    /**
     * Extracts clinical entities with assertion status (AFFIRMED/NEGATED) and normalized concepts.
     *
     * @param request Extraction request containing text and metadata
     * @return Extracted entities and categorized concept codes
     */
    ClinicalExtractionResult extractEntities(ClinicalExtractionRequest request);
}
