package com.medikiosk.clinical.reasoning;

import com.medikiosk.clinical.state.ClinicalFact;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Request payload passed to ClinicalReasoningProvider to extract structured context and identify question intents.
 */
public class ClinicalContextRequest {

    private UUID sessionId;
    private String patientStatement;
    private String conversationHistory;
    private String language = "en";
    private List<ClinicalFact> existingFacts = new ArrayList<>();
    private List<String> documentSummaries = new ArrayList<>();

    public ClinicalContextRequest() {
    }

    public ClinicalContextRequest(UUID sessionId, String patientStatement, String conversationHistory, String language) {
        this.sessionId = sessionId;
        this.patientStatement = patientStatement;
        this.conversationHistory = conversationHistory;
        this.language = language != null ? language : "en";
    }

    public UUID getSessionId() {
        return sessionId;
    }

    public void setSessionId(UUID sessionId) {
        this.sessionId = sessionId;
    }

    public String getPatientStatement() {
        return patientStatement;
    }

    public void setPatientStatement(String patientStatement) {
        this.patientStatement = patientStatement;
    }

    public String getConversationHistory() {
        return conversationHistory;
    }

    public void setConversationHistory(String conversationHistory) {
        this.conversationHistory = conversationHistory;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public List<ClinicalFact> getExistingFacts() {
        return existingFacts;
    }

    public void setExistingFacts(List<ClinicalFact> existingFacts) {
        this.existingFacts = existingFacts != null ? existingFacts : new ArrayList<>();
    }

    public List<String> getDocumentSummaries() {
        return documentSummaries;
    }

    public void setDocumentSummaries(List<String> documentSummaries) {
        this.documentSummaries = documentSummaries != null ? documentSummaries : new ArrayList<>();
    }
}
