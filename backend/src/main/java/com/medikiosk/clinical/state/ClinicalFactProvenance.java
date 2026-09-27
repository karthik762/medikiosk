package com.medikiosk.clinical.state;

/**
 * Provenance tracking for extracted, provided, or system-derived clinical information.
 */
public enum ClinicalFactProvenance {
    PATIENT_TEXT,
    PATIENT_VOICE,
    DOCUMENT_OCR,
    PATIENT_SELECTED,
    PATIENT_CONFIRMED,
    SYSTEM_DERIVED,
    AI_EXTRACTED
}
