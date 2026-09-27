package com.medikiosk.entity;

import com.medikiosk.entity.enums.ExtractionStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * Stores OCR and machine-extracted content from uploaded medical documents.
 */
@Entity
@Table(name = "document_extractions")
public class DocumentExtraction {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "document_id", nullable = false)
    private MedicalDocument document;

    @Column(name = "extracted_text", columnDefinition = "TEXT")
    private String extractedText;

    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(name = "extraction_data")
    private String extractionData;

    @Column(name = "confidence_score", precision = 5, scale = 4)
    private BigDecimal confidenceScore;

    @Column(name = "processing_engine", length = 64)
    private String processingEngine;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 32)
    private ExtractionStatus status = ExtractionStatus.SUCCESS;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = Instant.now();
    }

    public DocumentExtraction() {
    }

    public DocumentExtraction(MedicalDocument document, String extractedText, BigDecimal confidenceScore) {
        this.document = document;
        this.extractedText = extractedText;
        this.confidenceScore = confidenceScore;
        this.status = ExtractionStatus.SUCCESS;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public MedicalDocument getDocument() {
        return document;
    }

    public void setDocument(MedicalDocument document) {
        this.document = document;
    }

    public String getExtractedText() {
        return extractedText;
    }

    public void setExtractedText(String extractedText) {
        this.extractedText = extractedText;
    }

    public String getExtractionData() {
        return extractionData;
    }

    public void setExtractionData(String extractionData) {
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

    public ExtractionStatus getStatus() {
        return status;
    }

    public void setStatus(ExtractionStatus status) {
        this.status = status;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }
}
