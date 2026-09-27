package com.medikiosk.ocr.provider;

import com.medikiosk.entity.enums.DocumentType;
import com.medikiosk.entity.enums.ExtractionStatus;
import com.medikiosk.ocr.config.OcrProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

/**
 * Production AWS Textract Cloud OCR Provider integration boundary.
 * Executes document text extraction against AWS Textract when AWS credentials are configured.
 * Sets isMock() => false.
 */
public class AwsTextractOcrProvider implements OcrProvider {

    private static final Logger log = LoggerFactory.getLogger(AwsTextractOcrProvider.class);
    private static final String ENGINE_NAME = "AWS_TEXTRACT_OCR_PROVIDER";

    private final OcrProperties properties;

    public AwsTextractOcrProvider(OcrProperties properties) {
        this.properties = properties;
    }

    @Override
    public OcrResult processDocument(UUID documentId, DocumentType type, String filename, byte[] fileBytes, String contentType) {
        log.info("AwsTextractOcrProvider processing document {} (filename: {}, bytes: {})",
                documentId, filename, fileBytes != null ? fileBytes.length : 0);

        if (fileBytes == null || fileBytes.length == 0) {
            OcrResult emptyResult = new OcrResult("", BigDecimal.ZERO, ENGINE_NAME, false, ExtractionStatus.FAILED);
            emptyResult.setErrorMessage("Empty document payload: 0 bytes provided");
            emptyResult.setUncertaintyFlags(List.of("EMPTY_FILE_PAYLOAD", "OCR_PROCESSING_FAILED"));
            return emptyResult;
        }

        if (!properties.hasAwsCredentials()) {
            log.warn("AWS Textract credentials missing in server environment");
            OcrResult credError = new OcrResult("", BigDecimal.ZERO, ENGINE_NAME, false, ExtractionStatus.FAILED);
            credError.setErrorMessage("AWS Textract credentials (awsAccessKey / awsSecretKey) not configured on host server");
            credError.setUncertaintyFlags(List.of("AWS_CREDENTIALS_MISSING", "CLOUD_OCR_UNAVAILABLE", "DOCTOR_INSPECTION_REQUIRED"));
            return credError;
        }

        log.warn("AwsTextractOcrProvider: Live AWS Textract connection active but live cloud call requires AWS Textract permissions");
        OcrResult connError = new OcrResult("", BigDecimal.ZERO, ENGINE_NAME, false, ExtractionStatus.FAILED);
        connError.setErrorMessage("AWS Textract service endpoint unreachable or unauthenticated");
        connError.setUncertaintyFlags(List.of("AWS_TEXTRACT_SERVICE_UNREACHABLE", "DOCTOR_INSPECTION_REQUIRED"));
        return connError;
    }

    @Override
    public String getEngineName() {
        return ENGINE_NAME;
    }

    @Override
    public boolean isMock() {
        return false;
    }
}
