package com.medikiosk.clinical.reasoning;

/**
 * Encapsulates a candidate clinical question intent recommended by the reasoning engine.
 */
public class ClinicalQuestionIntent {

    private String intentCode;
    private int priority;
    private String reason;
    private boolean safetyCritical;

    public ClinicalQuestionIntent() {
    }

    public ClinicalQuestionIntent(String intentCode, int priority, String reason, boolean safetyCritical) {
        this.intentCode = intentCode;
        this.priority = priority;
        this.reason = reason;
        this.safetyCritical = safetyCritical;
    }

    public String getIntentCode() {
        return intentCode;
    }

    public void setIntentCode(String intentCode) {
        this.intentCode = intentCode;
    }

    public int getPriority() {
        return priority;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public boolean isSafetyCritical() {
        return safetyCritical;
    }

    public void setSafetyCritical(boolean safetyCritical) {
        this.safetyCritical = safetyCritical;
    }
}
