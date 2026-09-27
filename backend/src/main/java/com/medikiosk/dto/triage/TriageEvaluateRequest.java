package com.medikiosk.dto.triage;

import java.util.HashMap;
import java.util.Map;

/**
 * Optional request body for submitting additional screening parameters to triage evaluation.
 */
public class TriageEvaluateRequest {

    private Map<String, Object> parameters = new HashMap<>();

    public TriageEvaluateRequest() {
    }

    public TriageEvaluateRequest(Map<String, Object> parameters) {
        this.parameters = parameters != null ? parameters : new HashMap<>();
    }

    public Map<String, Object> getParameters() {
        return parameters;
    }

    public void setParameters(Map<String, Object> parameters) {
        this.parameters = parameters;
    }
}
