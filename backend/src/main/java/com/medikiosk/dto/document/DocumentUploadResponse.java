package com.medikiosk.dto.document;

import com.medikiosk.entity.enums.DocumentProcessingStatus;
import com.medikiosk.entity.enums.DocumentType;

import java.time.Instant;
import java.util.UUID;

/**
 * Response DTO representing uploaded document metadata and extraction state.
 */
public class DocumentUploadResponse {

    private UUID id;
    private UUID patientId;
    private UUID sessionId;
    private DocumentType documentType;
    private String fileName;
    private String fileType;
    private Long fileSize;
    private String fileHash;
    private DocumentProcessingStatus processingStatus;
    private Instant uploadedAt;
    private Instant processedAt;
    private ExtractionResultDto extraction;

    public DocumentUploadResponse() {
    }

    public UUID getId() {
        return id;
    }

    public UUID getDocumentId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public void setDocumentId(UUID documentId) {
        this.id = documentId;
    }

    public UUID getPatientId() {
        return patientId;
    }

    public void setPatientId(UUID patientId) {
        this.patientId = patientId;
    }

    public UUID getSessionId() {
        return sessionId;
    }

    public void setSessionId(UUID sessionId) {
        this.sessionId = sessionId;
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

    public String getFileType() {
        return fileType;
    }

    public void setFileType(String fileType) {
        this.fileType = fileType;
    }

    public Long getFileSize() {
        return fileSize;
    }

    public void setFileSize(Long fileSize) {
        this.fileSize = fileSize;
    }

    public String getFileHash() {
        return fileHash;
    }

    public void setFileHash(String fileHash) {
        this.fileHash = fileHash;
    }

    public DocumentProcessingStatus getProcessingStatus() {
        return processingStatus;
    }

    public void setProcessingStatus(DocumentProcessingStatus processingStatus) {
        this.processingStatus = processingStatus;
    }

    public Instant getUploadedAt() {
        return uploadedAt;
    }

    public void setUploadedAt(Instant uploadedAt) {
        this.uploadedAt = uploadedAt;
    }

    public Instant getProcessedAt() {
        return processedAt;
    }

    public void setProcessedAt(Instant processedAt) {
        this.processedAt = processedAt;
    }

    public ExtractionResultDto getExtraction() {
        return extraction;
    }

    public void setExtraction(ExtractionResultDto extraction) {
        this.extraction = extraction;
    }
}
