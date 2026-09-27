package com.medikiosk.service;

import com.medikiosk.dto.document.DocumentUploadResponse;
import com.medikiosk.entity.User;
import com.medikiosk.entity.enums.DocumentType;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

/**
 * Service contract for document intake, validation, secure storage, and OCR extraction.
 * Conforms to docs/PRODUCT_SPEC.md, docs/AI_SPEC.md, and docs/SECURITY.md.
 */
public interface DocumentProcessingService {

    /**
     * Validates, stores, and performs OCR extraction on an uploaded medical document for an intake session.
     */
    DocumentUploadResponse uploadAndProcessDocument(UUID sessionId, DocumentType documentType, MultipartFile file, User authenticatedUser);

    /**
     * Retrieves all documents associated with an intake session.
     */
    List<DocumentUploadResponse> getSessionDocuments(UUID sessionId, User authenticatedUser);

    /**
     * Retrieves details and extraction results for a specific document within a session.
     */
    DocumentUploadResponse getDocumentDetails(UUID sessionId, UUID documentId, User authenticatedUser);

    /**
     * Retrieves the binary content of a stored source document within a session.
     */
    byte[] getDocumentBinary(UUID sessionId, UUID documentId, User authenticatedUser);

    /**
     * Direct retrieval of document details by documentId (e.g. for doctor review).
     */
    DocumentUploadResponse getDocumentDetailsById(UUID documentId, User authenticatedUser);

    /**
     * Direct retrieval of document binary by documentId (e.g. for doctor review).
     */
    byte[] getDocumentBinaryById(UUID documentId, User authenticatedUser);
}
