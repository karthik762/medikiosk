package com.medikiosk.dto.clinical;

import com.medikiosk.entity.enums.DocumentType;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Extracted document findings preserving documentId, OCR confidence, and uncertainty flags.
 */
public class DocumentFindingDto {

    private UUID documentId;
    private DocumentType documentType;
    private String fileName;
    private BigDecimal confidenceScore;
    private String processingEngine;
    private List<String> uncertaintyFlags;
    private Map<String, Object> extractionData;
    private String extractedTextPreview;

    public DocumentFindingDto() {
    }

    public DocumentFindingDto(UUID documentId, DocumentType documentType, String fileName, BigDecimal confidenceScore, String processingEngine, List<String> uncertaintyFlags, Map<String, Object> extractionData, String extractedTextPreview) {
        this.documentId = documentId;
        this.documentType = documentType;
        this.fileName = fileName;
        this.confidenceScore = confidenceScore;
        this.processingEngine = processingEngine;
        this.uncertaintyFlags = uncertaintyFlags;
        this.extractionData = extractionData;
        this.extractedTextPreview = extractedTextPreview;
    }

    public UUID getDocumentId() {
        return documentId;
    }

    public void setDocumentId(UUID documentId) {
        this.documentId = documentId;
    }

    public DocumentType getDocumentType() {
        return documentType;
    }

    public void setDocumentType(DocumentType documentType) {
        this.documentType = documentType;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
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

    public List<String> getUncertaintyFlags() {
        return uncertaintyFlags;
    }

    public void setUncertaintyFlags(List<String> uncertaintyFlags) {
        this.uncertaintyFlags = uncertaintyFlags;
    }

    public Map<String, Object> getExtractionData() {
        return extractionData;
    }

    public void setExtractionData(Map<String, Object> extractionData) {
        this.extractionData = extractionData;
    }

    public String getExtractedTextPreview() {
        return extractedTextPreview;
    }

    public void setExtractedTextPreview(String extractedTextPreview) {
        this.extractedTextPreview = extractedTextPreview;
    }
}
