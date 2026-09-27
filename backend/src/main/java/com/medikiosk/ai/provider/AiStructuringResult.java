package com.medikiosk.ai.provider;

import com.medikiosk.dto.clinical.ClinicalSummaryDto;

/**
 * Result produced by an AI clinical structuring provider before safety validation.
 */
public class AiStructuringResult {

    private boolean success;
    private ClinicalSummaryDto summaryDto;
    private String rawOutputJson;
    private String errorMessage;
    private String modelIdentifier;
    private boolean mock;

    public AiStructuringResult() {
    }

    public static AiStructuringResult success(ClinicalSummaryDto summaryDto, String rawOutputJson, String modelIdentifier, boolean mock) {
        AiStructuringResult result = new AiStructuringResult();
        result.success = true;
        result.summaryDto = summaryDto;
        result.rawOutputJson = rawOutputJson;
        result.modelIdentifier = modelIdentifier;
        result.mock = mock;
        return result;
    }

    public static AiStructuringResult failure(String errorMessage, String modelIdentifier, boolean mock) {
        AiStructuringResult result = new AiStructuringResult();
        result.success = false;
        result.errorMessage = errorMessage;
        result.modelIdentifier = modelIdentifier;
        result.mock = mock;
        return result;
    }

    public boolean isSuccess() {
        return success;
    }

    public ClinicalSummaryDto getSummaryDto() {
        return summaryDto;
    }

    public void setSummaryDto(ClinicalSummaryDto summaryDto) {
        this.summaryDto = summaryDto;
    }

    public String getRawOutputJson() {
        return rawOutputJson;
    }

    public void setRawOutputJson(String rawOutputJson) {
        this.rawOutputJson = rawOutputJson;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public String getModelIdentifier() {
        return modelIdentifier;
    }

    public void setModelIdentifier(String modelIdentifier) {
        this.modelIdentifier = modelIdentifier;
    }

    public boolean isMock() {
        return mock;
    }

    public void setMock(boolean mock) {
        this.mock = mock;
    }
}
