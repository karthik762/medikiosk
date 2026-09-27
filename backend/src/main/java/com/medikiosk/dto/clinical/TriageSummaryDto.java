package com.medikiosk.dto.clinical;

import com.medikiosk.entity.enums.TriagePriority;

import java.util.List;
import java.util.Map;

/**
 * Summarized deterministic triage results.
 * Preserves deterministic rule-engine outputs verbatim without LLM override.
 */
public class TriageSummaryDto {

    private TriagePriority priority;
    private String explanation;
    private List<Map<String, Object>> triggeredRules;
    private String source = "RULE_ENGINE";

    public TriageSummaryDto() {
    }

    public TriageSummaryDto(TriagePriority priority, String explanation, List<Map<String, Object>> triggeredRules) {
        this.priority = priority;
        this.explanation = explanation;
        this.triggeredRules = triggeredRules;
        this.source = "RULE_ENGINE";
    }

    public TriagePriority getPriority() {
        return priority;
    }

    public void setPriority(TriagePriority priority) {
        this.priority = priority;
    }

    public String getExplanation() {
        return explanation;
    }

    public void setExplanation(String explanation) {
        this.explanation = explanation;
    }

    public List<Map<String, Object>> getTriggeredRules() {
        return triggeredRules;
    }

    public void setTriggeredRules(List<Map<String, Object>> triggeredRules) {
        this.triggeredRules = triggeredRules;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }
}
