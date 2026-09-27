package com.medikiosk.clinical.nlp;

/**
 * Types of clinical entities recognized by the NLP extraction layer.
 */
public enum ClinicalEntityType {
    SYMPTOM,
    DURATION,
    SEVERITY,
    BODY_SITE,
    MEDICATION,
    ALLERGY,
    CONDITION,
    NEGATION,
    TEMPORAL_EXPRESSION
}
