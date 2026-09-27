package com.medikiosk.ocr.provider;

import com.medikiosk.entity.enums.ExtractionStatus;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Result model representing the outcome of an OCR document text extraction.
 * Conforms to docs/AI_SPEC.md and docs/DATABASE.md.
 */
public class OcrResult {

    private String rawText;
    private BigDecimal confidence;
    private String engine;
    private boolean isMock;
    private ExtractionStatus status = ExtractionStatus.SUCCESS;
    private Map<String, Object> extractedEntities = new HashMap<>();
    private List<String> uncertaintyFlags = new ArrayList<>();
    private String errorMessage;

    public OcrResult() {
    }

    public OcrResult(String rawText, BigDecimal confidence, String engine, boolean isMock, ExtractionStatus status) {
        this.rawText = rawText;
        this.confidence = confidence;
        this.engine = engine;
        this.isMock = isMock;
        this.status = status;
    }

    public String getRawText() {
        return rawText;
    }

    public void setRawText(String rawText) {
        this.rawText = rawText;
    }

    public BigDecimal getConfidence() {
        return confidence;
    }

    public void setConfidence(BigDecimal confidence) {
        this.confidence = confidence;
    }

    public String getEngine() {
        return engine;
    }

    public void setEngine(String engine) {
        this.engine = engine;
    }

    public boolean isMock() {
        return isMock;
    }

    public void setMock(boolean mock) {
        isMock = mock;
    }

    public ExtractionStatus getStatus() {
        return status;
    }

    public void setStatus(ExtractionStatus status) {
        this.status = status;
    }

    public Map<String, Object> getExtractedEntities() {
        return extractedEntities;
    }

    public void setExtractedEntities(Map<String, Object> extractedEntities) {
        this.extractedEntities = extractedEntities;
    }

    public List<String> getUncertaintyFlags() {
        return uncertaintyFlags;
    }

    public void setUncertaintyFlags(List<String> uncertaintyFlags) {
        this.uncertaintyFlags = uncertaintyFlags;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }
}
