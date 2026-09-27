package com.medikiosk.clinical.reasoning;

import java.util.ArrayList;
import java.util.List;

/**
 * Result returned by ClinicalReasoningProvider detailing extracted facts, identified uncertainties,
 * detected conflicts, and recommended question intents.
 */
public class ClinicalContextResult {

    private List<ClinicalFactExtraction> extractedFacts = new ArrayList<>();
    private List<ClinicalUncertainty> uncertainties = new ArrayList<>();
    private List<String> contradictions = new ArrayList<>();
    private List<ClinicalQuestionIntent> recommendedIntents = new ArrayList<>();
    private String modelVersion = "mock-reasoning-v1";
    private double confidence = 1.0;

    public ClinicalContextResult() {
    }

    public void addFact(ClinicalFactExtraction fact) {
        if (fact != null) {
            extractedFacts.add(fact);
        }
    }

    public void addUncertainty(ClinicalUncertainty uncertainty) {
        if (uncertainty != null) {
            uncertainties.add(uncertainty);
        }
    }

    public void addContradiction(String contradiction) {
        if (contradiction != null) {
            contradictions.add(contradiction);
        }
    }

    public void addIntent(ClinicalQuestionIntent intent) {
        if (intent != null) {
            recommendedIntents.add(intent);
        }
    }

    // Getters and Setters
    public List<ClinicalFactExtraction> getExtractedFacts() {
        return extractedFacts;
    }

    public void setExtractedFacts(List<ClinicalFactExtraction> extractedFacts) {
        this.extractedFacts = extractedFacts != null ? extractedFacts : new ArrayList<>();
    }

    public List<ClinicalUncertainty> getUncertainties() {
        return uncertainties;
    }

    public void setUncertainties(List<ClinicalUncertainty> uncertainties) {
        this.uncertainties = uncertainties != null ? uncertainties : new ArrayList<>();
    }

    public List<String> getContradictions() {
        return contradictions;
    }

    public void setContradictions(List<String> contradictions) {
        this.contradictions = contradictions != null ? contradictions : new ArrayList<>();
    }

    public List<ClinicalQuestionIntent> getRecommendedIntents() {
        return recommendedIntents;
    }

    public void setRecommendedIntents(List<ClinicalQuestionIntent> recommendedIntents) {
        this.recommendedIntents = recommendedIntents != null ? recommendedIntents : new ArrayList<>();
    }

    public String getModelVersion() {
        return modelVersion;
    }

    public void setModelVersion(String modelVersion) {
        this.modelVersion = modelVersion;
    }

    public double getConfidence() {
        return confidence;
    }

    public void setConfidence(double confidence) {
        this.confidence = confidence;
    }
}
