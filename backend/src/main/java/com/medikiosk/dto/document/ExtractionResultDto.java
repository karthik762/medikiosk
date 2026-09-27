package com.medikiosk.dto.document;

import com.medikiosk.entity.enums.ExtractionStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * DTO representing structured OCR and document extraction results.
 */
public class ExtractionResultDto {

    private UUID id;
    private UUID documentId;
    private String extractedText;
    private Map<String, Object> extractionData;
    private BigDecimal confidenceScore;
    private String processingEngine;
    private Boolean isMock;
    private ExtractionStatus status;
    private List<String> uncertaintyFlags;
    private Instant createdAt;

    public ExtractionResultDto() {
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getDocumentId() {
        return documentId;
    }

    public void setDocumentId(UUID documentId) {
        this.documentId = documentId;
    }

    public String getExtractedText() {
        return extractedText;
    }

    public void setExtractedText(String extractedText) {
        this.extractedText = extractedText;
    }

    public Map<String, Object> getExtractionData() {
        return extractionData;
    }

    public void setExtractionData(Map<String, Object> extractionData) {
        this.extractionData = extractionData;
    }

    public BigDecimal getConfidenceScore() {
        return confidenceScore;
    }

    public void setConfidenceScore(BigDecimal confidenceScore) {
        this.confidenceScore = confidenceScore;
    }

    public String getProcessingEngine() {
        return processingEngine;
    }

    public void setProcessingEngine(String processingEngine) {
        this.processingEngine = processingEngine;
    }

    public Boolean getIsMock() {
        return isMock;
    }

    public void setIsMock(Boolean isMock) {
        this.isMock = isMock;
    }

    public ExtractionStatus getStatus() {
        return status;
    }

    public void setStatus(ExtractionStatus status) {
        this.status = status;
    }

    public List<String> getUncertaintyFlags() {
        return uncertaintyFlags;
    }

    public void setUncertaintyFlags(List<String> uncertaintyFlags) {
        this.uncertaintyFlags = uncertaintyFlags;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }
}
