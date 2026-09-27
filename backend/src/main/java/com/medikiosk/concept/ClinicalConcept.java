package com.medikiosk.concept;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Controlled clinical concept representation for intake normalization and adaptive questioning.
 */
public class ClinicalConcept {

    private final String code;
    private final String displayName;
    private final ClinicalConceptCategory category;
    private final String description;
    private final boolean safetyCritical;
    private final Set<String> synonyms;
    private String icdCode;
    private String icdDisplay;
    private String namasteCode;
    private String namasteDisplay;

    public ClinicalConcept(String code, String displayName, ClinicalConceptCategory category, String description, boolean safetyCritical) {
        this.code = code;
        this.displayName = displayName;
        this.category = category;
        this.description = description;
        this.safetyCritical = safetyCritical;
        this.synonyms = new HashSet<>();
    }

    public ClinicalConcept withIcdCoding(String code, String display) {
        this.icdCode = code;
        this.icdDisplay = display;
        return this;
    }

    public ClinicalConcept withNamasteCoding(String code, String display) {
        this.namasteCode = code;
        this.namasteDisplay = display;
        return this;
    }

    public ClinicalConcept addSynonym(String synonym) {
        if (synonym != null && !synonym.trim().isEmpty()) {
            this.synonyms.add(synonym.trim().toLowerCase());
        }
        return this;
    }

    public String getCode() {
        return code;
    }

    public String getDisplayName() {
        return displayName;
    }

    public ClinicalConceptCategory getCategory() {
        return category;
    }

    public String getDescription() {
        return description;
    }

    public boolean isSafetyCritical() {
        return safetyCritical;
    }

    public Set<String> getSynonyms() {
        return Collections.unmodifiableSet(synonyms);
    }

    public String getIcdCode() {
        return icdCode;
    }

    public String getIcdDisplay() {
        return icdDisplay;
    }

    public String getNamasteCode() {
        return namasteCode;
    }

    public String getNamasteDisplay() {
        return namasteDisplay;
    }
}
