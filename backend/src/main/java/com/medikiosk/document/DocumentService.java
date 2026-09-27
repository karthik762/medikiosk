package com.medikiosk.document;

import java.util.UUID;

/**
 * Architectural interface for managing patient document attachments and records.
 */
public interface DocumentService {

    /**
     * Stores an uploaded patient document for OCR and clinical review.
     *
     * @param patientId   Patient identifier.
     * @param fileName    Original file name.
     * @param contentType MIME type.
     * @param fileBytes   Binary document content.
     * @return Unique stored document identifier.
     */
    UUID uploadDocument(UUID patientId, String fileName, String contentType, byte[] fileBytes);

    /**
     * Retrieves stored document content by identifier.
     *
     * @param documentId Document identifier.
     * @return Binary file content.
     */
    byte[] getDocumentContent(UUID documentId);
}
