package com.medikiosk.clinical.state;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

/**
 * Encapsulates the aggregate clinical state of an active intake session,
 * tracking extracted concepts, provenance, missing information, and detected conflicts.
 */
public class ClinicalState {

    private UUID sessionId;
    private UUID patientId;
    private String patientIdentifier;
    private String patientName;
    private String chiefComplaint;
    private String language = "en";

    private final List<ClinicalFact> facts = new ArrayList<>();
    private final List<String> symptoms = new ArrayList<>();
    private final List<String> medicalHistories = new ArrayList<>();
    private final List<String> medications = new ArrayList<>();
    private final List<String> allergies = new ArrayList<>();
    private final Set<String> answeredQuestionCodes = new HashSet<>();
    private final Map<String, String> answersByQuestionCode = new HashMap<>();

    private final List<String> redFlagSignals = new ArrayList<>();
    private final List<String> missingInformation = new ArrayList<>();
    private final List<ClinicalConflict> conflicts = new ArrayList<>();

    private boolean ayurvedaCompleted;
    private int documentCount;
    private String triagePriority = "NORMAL";
    private double overallConfidence = 1.0;

    public ClinicalState() {
    }

    public ClinicalState(UUID sessionId, UUID patientId, String patientIdentifier) {
        this.sessionId = sessionId;
        this.patientId = patientId;
        this.patientIdentifier = patientIdentifier;
    }

    public void addFact(ClinicalFact fact) {
        if (fact != null) {
            facts.add(fact);
        }
    }

    public boolean hasAffirmedConcept(String conceptCode) {
        if (conceptCode == null) return false;
        return facts.stream()
                .anyMatch(f -> conceptCode.equalsIgnoreCase(f.getConceptCode()) && !f.isNegated());
    }

    public boolean hasNegatedConcept(String conceptCode) {
        if (conceptCode == null) return false;
        return facts.stream()
                .anyMatch(f -> conceptCode.equalsIgnoreCase(f.getConceptCode()) && f.isNegated());
    }

    public Optional<ClinicalFact> getFact(String conceptCode) {
        if (conceptCode == null) return Optional.empty();
        return facts.stream()
                .filter(f -> conceptCode.equalsIgnoreCase(f.getConceptCode()))
                .findFirst();
    }

    public boolean hasFact(String conceptCode) {
        return getFact(conceptCode).isPresent();
    }

    public void recordAnswer(String questionCode, String answerValue) {
        if (questionCode != null) {
            answeredQuestionCodes.add(questionCode);
            if (answerValue != null) {
                answersByQuestionCode.put(questionCode, answerValue);
            }
        }
    }

    public boolean isQuestionAnswered(String questionCode) {
        return questionCode != null && answeredQuestionCodes.contains(questionCode);
    }

    public String getAnswer(String questionCode) {
        return answersByQuestionCode.get(questionCode);
    }

    public void addConflict(ClinicalConflict conflict) {
        if (conflict != null) {
            conflicts.add(conflict);
        }
    }

    public void addRedFlagSignal(String signal) {
        if (signal != null && !redFlagSignals.contains(signal)) {
            redFlagSignals.add(signal);
        }
    }

    public void addMissingInfo(String info) {
        if (info != null && !missingInformation.contains(info)) {
            missingInformation.add(info);
        }
    }

    // Getters and Setters
    public UUID getSessionId() {
        return sessionId;
    }

    public void setSessionId(UUID sessionId) {
        this.sessionId = sessionId;
    }

    public UUID getPatientId() {
        return patientId;
    }

    public void setPatientId(UUID patientId) {
        this.patientId = patientId;
    }

    public String getPatientIdentifier() {
        return patientIdentifier;
    }

    public void setPatientIdentifier(String patientIdentifier) {
        this.patientIdentifier = patientIdentifier;
    }

    public String getPatientName() {
        return patientName;
    }

    public void setPatientName(String patientName) {
        this.patientName = patientName;
    }

    public String getChiefComplaint() {
        return chiefComplaint;
    }

    public void setChiefComplaint(String chiefComplaint) {
        this.chiefComplaint = chiefComplaint;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public List<ClinicalFact> getFacts() {
        return facts;
    }

    public List<String> getSymptoms() {
        return symptoms;
    }

    public List<String> getMedicalHistories() {
        return medicalHistories;
    }

    public List<String> getMedications() {
        return medications;
    }

    public List<String> getAllergies() {
        return allergies;
    }

    public Set<String> getAnsweredQuestionCodes() {
        return answeredQuestionCodes;
    }

    public Map<String, String> getAnswersByQuestionCode() {
        return answersByQuestionCode;
    }

    public List<String> getRedFlagSignals() {
        return redFlagSignals;
    }

    public List<String> getMissingInformation() {
        return missingInformation;
    }

    public List<ClinicalConflict> getConflicts() {
        return conflicts;
    }

    public boolean isAyurvedaCompleted() {
        return ayurvedaCompleted;
    }

    public void setAyurvedaCompleted(boolean ayurvedaCompleted) {
        this.ayurvedaCompleted = ayurvedaCompleted;
    }

    public int getDocumentCount() {
        return documentCount;
    }

    public void setDocumentCount(int documentCount) {
        this.documentCount = documentCount;
    }

    public String getTriagePriority() {
        return triagePriority;
    }

    public void setTriagePriority(String triagePriority) {
        this.triagePriority = triagePriority;
    }

    public double getOverallConfidence() {
        return overallConfidence;
    }

    public void addFactWithCorrection(ClinicalFact fact) {
        if (fact == null) return;
        
        // Search for existing fact to check for correction or contradiction
        Optional<ClinicalFact> existingOpt = facts.stream()
                .filter(existing -> isSameConceptOrExpression(existing, fact))
                .findFirst();

        if (existingOpt.isPresent()) {
            ClinicalFact existing = existingOpt.get();
            if (existing.isNegated() != fact.isNegated() 
                    || !equalsIgnoreNull(existing.getAnatomicalSite(), fact.getAnatomicalSite())
                    || !equalsIgnoreNull(existing.getSensation(), fact.getSensation())
                    || !equalsIgnoreNull(existing.getLaterality(), fact.getLaterality())
                    || "PATIENT_CORRECTED".equalsIgnoreCase(fact.getExtractionMethod())
                    || "PATIENT_CORRECTION".equalsIgnoreCase(fact.getExtractionMethod())) {
                
                // Set verification status to PATIENT_CORRECTED
                fact.setVerificationStatus("PATIENT_CORRECTED");
                existing.setVerificationStatus("SUPERSEDED");

                // Record conflict if opposite negation without explicit correction prompt
                if (existing.isNegated() != fact.isNegated()) {
                    addConflict(new ClinicalConflict(
                            fact.getNormalizedConcept() != null ? fact.getNormalizedConcept() : fact.getPatientExpression(),
                            "Contradiction between turn facts",
                            existing.getSourceType() != null ? existing.getSourceType() : "PATIENT_REPORTED",
                            existing.isNegated() ? "ABSENT" : "PRESENT",
                            fact.getSourceType() != null ? fact.getSourceType() : "PATIENT_REPORTED",
                            fact.isNegated() ? "ABSENT" : "PRESENT"
                    ));
                    fact.setContradictory(true);
                }
            }
        }
        
        facts.add(fact);
    }

    private boolean isSameConceptOrExpression(ClinicalFact f1, ClinicalFact f2) {
        if (f1 == null || f2 == null) return false;
        if (f1.getConceptCode() != null && !"UNMAPPED".equalsIgnoreCase(f1.getConceptCode()) 
                && f1.getConceptCode().equalsIgnoreCase(f2.getConceptCode())) {
            return true;
        }
        if (f1.getPatientExpression() != null && f2.getPatientExpression() != null
                && f1.getPatientExpression().equalsIgnoreCase(f2.getPatientExpression())) {
            return true;
        }
        if (f1.getAnatomicalSite() != null && f2.getAnatomicalSite() != null
                && f1.getAnatomicalSite().equalsIgnoreCase(f2.getAnatomicalSite())
                && f1.getSensation() != null && f2.getSensation() != null
                && f1.getSensation().equalsIgnoreCase(f2.getSensation())) {
            return true;
        }
        return false;
    }

    private boolean equalsIgnoreNull(String s1, String s2) {
        if (s1 == null && s2 == null) return true;
        if (s1 == null || s2 == null) return false;
        return s1.equalsIgnoreCase(s2);
    }

    public List<ClinicalFact> getAffirmedFacts() {
        return facts.stream().filter(f -> !f.isNegated()).toList();
    }

    public List<ClinicalFact> getNegatedFacts() {
        return facts.stream().filter(ClinicalFact::isNegated).toList();
    }

    public List<ClinicalFact> getFactsByAnatomicalSite(String site) {
        if (site == null) return List.of();
        return facts.stream()
                .filter(f -> site.equalsIgnoreCase(f.getAnatomicalSite()))
                .toList();
    }

    public List<ClinicalFact> getFactsBySensation(String sensation) {
        if (sensation == null) return List.of();
        return facts.stream()
                .filter(f -> sensation.equalsIgnoreCase(f.getSensation()))
                .toList();
    }

    public List<ClinicalFact> getUnmappedFacts() {
        return facts.stream().filter(ClinicalFact::isUnmapped).toList();
    }

    public List<ClinicalFact> getAmbiguousFacts() {
        return facts.stream().filter(ClinicalFact::isAmbiguous).toList();
    }

    public List<ClinicalFact> getContradictoryFacts() {
        return facts.stream().filter(ClinicalFact::isContradictory).toList();
    }

    public String getAnswerText(String questionCode) {
        return getAnswer(questionCode);
    }
}
