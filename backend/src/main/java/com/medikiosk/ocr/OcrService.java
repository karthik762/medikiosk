package com.medikiosk.ocr;

import java.util.Map;
import java.util.UUID;

/**
 * Architectural interface for Optical Character Recognition (OCR) and document extraction.
 * Extracts text and structured entities from medical prescriptions and lab reports.
 */
public interface OcrService {

    /**
     * Extracts text and structured medical entities from an uploaded document.
     *
     * @param documentId Document identifier.
     * @param fileBytes  Binary content of the document.
     * @return Map containing raw extracted text and identified clinical entities.
     */
    Map<String, Object> extractDocumentText(UUID documentId, byte[] fileBytes);
}
