package com.medikiosk.ocr.config;

import com.medikiosk.ocr.provider.AwsTextractOcrProvider;
import com.medikiosk.ocr.provider.MockOcrProvider;
import com.medikiosk.ocr.provider.OcrProvider;
import com.medikiosk.ocr.provider.TesseractOcrProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

/**
 * Spring Configuration for OCR provider selection.
 * Selects real OCR provider (Tesseract / Textract) for production and MockOcrProvider for test suites.
 */
@Configuration
public class OcrConfig {

    private static final Logger log = LoggerFactory.getLogger(OcrConfig.class);

    @Bean
    @Primary
    @ConditionalOnProperty(name = "medikiosk.ocr.provider", havingValue = "tesseract", matchIfMissing = true)
    public OcrProvider tesseractOcrProvider(OcrProperties properties) {
        log.info("Configuring production Tesseract OCR Provider (medikiosk.ocr.provider=tesseract)");
        return new TesseractOcrProvider(properties);
    }

    @Bean
    @Primary
    @ConditionalOnProperty(name = "medikiosk.ocr.provider", havingValue = "textract")
    public OcrProvider awsTextractOcrProvider(OcrProperties properties) {
        log.info("Configuring AWS Textract OCR Provider (medikiosk.ocr.provider=textract)");
        return new AwsTextractOcrProvider(properties);
    }

    @Bean
    @Primary
    @ConditionalOnProperty(name = "medikiosk.ocr.provider", havingValue = "mock")
    public OcrProvider mockOcrProvider() {
        log.info("Configuring Mock OCR Provider (medikiosk.ocr.provider=mock)");
        return new MockOcrProvider();
    }
}
