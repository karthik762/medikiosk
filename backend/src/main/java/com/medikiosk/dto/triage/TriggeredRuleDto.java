package com.medikiosk.dto.triage;

import com.medikiosk.entity.enums.TriagePriority;

/**
 * Detailed trace record for a triggered triage screening rule.
 * Conforms to docs/TRIAGE_RULES.md (Sections 6, 12, 27).
 */
public class TriggeredRuleDto {

    private String ruleId;
    private String version;
    private String name;
    private TriagePriority priority;
    private String reason;

    public TriggeredRuleDto() {
    }

    public TriggeredRuleDto(String ruleId, String version, String name, TriagePriority priority, String reason) {
        this.ruleId = ruleId;
        this.version = version;
        this.name = name;
        this.priority = priority;
        this.reason = reason;
    }

    public String getRuleId() {
        return ruleId;
    }

    public void setRuleId(String ruleId) {
        this.ruleId = ruleId;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public TriagePriority getPriority() {
        return priority;
    }

    public void setPriority(TriagePriority priority) {
        this.priority = priority;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }
}
