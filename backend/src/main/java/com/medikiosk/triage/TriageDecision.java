package com.medikiosk.triage;

import com.medikiosk.dto.triage.TriggeredRuleDto;
import com.medikiosk.entity.enums.TriagePriority;

import java.util.ArrayList;
import java.util.List;

/**
 * Deterministic triage evaluation decision produced by TriageEngine before persistence.
 * Conforms to docs/TRIAGE_RULES.md (Sections 4, 11, 12, 19).
 */
public class TriageDecision {

    private final TriagePriority priority;
    private final List<TriggeredRuleDto> triggeredRules;
    private final List<String> reasons;
    private final String explanation;
    private final String patientInstruction;

    public TriageDecision(TriagePriority priority,
                          List<TriggeredRuleDto> triggeredRules,
                          List<String> reasons,
                          String explanation,
                          String patientInstruction) {
        this.priority = priority;
        this.triggeredRules = triggeredRules != null ? triggeredRules : new ArrayList<>();
        this.reasons = reasons != null ? reasons : new ArrayList<>();
        this.explanation = explanation;
        this.patientInstruction = patientInstruction;
    }

    public TriagePriority getPriority() {
        return priority;
    }

    public List<TriggeredRuleDto> getTriggeredRules() {
        return triggeredRules;
    }

    public List<String> getReasons() {
        return reasons;
    }

    public String getExplanation() {
        return explanation;
    }

    public String getPatientInstruction() {
        return patientInstruction;
    }
}
