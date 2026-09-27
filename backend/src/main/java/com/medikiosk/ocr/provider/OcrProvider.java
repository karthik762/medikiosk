package com.medikiosk.ocr.provider;

import com.medikiosk.entity.enums.DocumentType;

import java.util.UUID;

/**
 * Pluggable OCR provider abstraction for processing and extracting medical text from documents.
 * Conforms to docs/PRODUCT_SPEC.md and docs/AI_SPEC.md.
 */
public interface OcrProvider {

    /**
     * Processes document content and produces extracted OCR text and deterministic structures.
     *
     * @param documentId  Unique document identifier.
     * @param type        Classified document type.
     * @param filename    Original filename.
     * @param fileBytes   Binary document content.
     * @param contentType MIME type of the document.
     * @return OCR processing and entity extraction result.
     */
    OcrResult processDocument(UUID documentId, DocumentType type, String filename, byte[] fileBytes, String contentType);

    /**
     * Gets the identifier name of the OCR provider engine.
     */
    String getEngineName();

    /**
     * Whether this provider is a mock / test provider.
     */
    boolean isMock();
}
