package com.medikiosk.dto.triage;

import com.medikiosk.entity.enums.TriagePriority;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Structured response containing triage screening and routing evaluation.
 * Conforms to docs/TRIAGE_RULES.md (Sections 4, 19, 29).
 */
public class TriageResponse {

    private UUID sessionId;
    private TriagePriority priority;
    private String status;
    private List<TriggeredRuleDto> triggeredRules = new ArrayList<>();
    private List<String> reasons = new ArrayList<>();
    private String explanation;
    private String patientInstruction;
    private Instant evaluatedAt;

    public TriageResponse() {
    }

    public TriageResponse(UUID sessionId, TriagePriority priority, String status,
                          List<TriggeredRuleDto> triggeredRules, List<String> reasons,
                          String explanation, String patientInstruction, Instant evaluatedAt) {
        this.sessionId = sessionId;
        this.priority = priority;
        this.status = status;
        this.triggeredRules = triggeredRules != null ? triggeredRules : new ArrayList<>();
        this.reasons = reasons != null ? reasons : new ArrayList<>();
        this.explanation = explanation;
        this.patientInstruction = patientInstruction;
        this.evaluatedAt = evaluatedAt;
    }

    public UUID getSessionId() {
        return sessionId;
    }

    public void setSessionId(UUID sessionId) {
        this.sessionId = sessionId;
    }

    public TriagePriority getPriority() {
        return priority;
    }

    public void setPriority(TriagePriority priority) {
        this.priority = priority;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public List<TriggeredRuleDto> getTriggeredRules() {
        return triggeredRules;
    }

    public void setTriggeredRules(List<TriggeredRuleDto> triggeredRules) {
        this.triggeredRules = triggeredRules;
    }

    public List<String> getReasons() {
        return reasons;
    }

    public void setReasons(List<String> reasons) {
        this.reasons = reasons;
    }

    public String getExplanation() {
        return explanation;
    }

    public void setExplanation(String explanation) {
        this.explanation = explanation;
    }

    public String getPatientInstruction() {
        return patientInstruction;
    }

    public void setPatientInstruction(String patientInstruction) {
        this.patientInstruction = patientInstruction;
    }

    public Instant getEvaluatedAt() {
        return evaluatedAt;
    }

    public void setEvaluatedAt(Instant evaluatedAt) {
        this.evaluatedAt = evaluatedAt;
    }
}
