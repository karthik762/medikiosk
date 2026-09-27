package com.medikiosk.ai.safety;

public class AiSafetyViolationException extends RuntimeException {

    private final String violationCategory;

    public AiSafetyViolationException(String violationCategory, String message) {
        super(message);
        this.violationCategory = violationCategory;
    }

    public String getViolationCategory() {
        return violationCategory;
    }
}
