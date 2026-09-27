package com.medikiosk.ocr;

import com.medikiosk.entity.enums.DocumentType;
import com.medikiosk.entity.enums.ExtractionStatus;
import com.medikiosk.ocr.config.OcrConfig;
import com.medikiosk.ocr.config.OcrProperties;
import com.medikiosk.ocr.provider.AwsTextractOcrProvider;
import com.medikiosk.ocr.provider.MockOcrProvider;
import com.medikiosk.ocr.provider.OcrProvider;
import com.medikiosk.ocr.provider.OcrResult;
import com.medikiosk.ocr.provider.TesseractOcrProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import javax.imageio.ImageIO;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Phase 3A — Real OCR Provider Integration & Configuration Tests")
class OcrProviderIntegrationTest {

    private OcrProperties properties;

    @BeforeEach
    void setUp() {
        properties = new OcrProperties();
        properties.setProvider("tesseract");
        properties.setConfidenceThreshold(new BigDecimal("0.7500"));
    }

    @Test
    @DisplayName("1. TesseractOcrProvider returns isMock() == false and correct engine name")
    void testTesseractProviderIsReal() {
        OcrProvider provider = new TesseractOcrProvider(properties);
        assertFalse(provider.isMock(), "Production TesseractOcrProvider must return isMock() == false");
        assertEquals("TESSERACT_OCR_PROVIDER", provider.getEngineName());
    }

    @Test
    @DisplayName("2. AwsTextractOcrProvider returns isMock() == false and correct engine name")
    void testAwsTextractProviderIsReal() {
        OcrProvider provider = new AwsTextractOcrProvider(properties);
        assertFalse(provider.isMock(), "Production AwsTextractOcrProvider must return isMock() == false");
        assertEquals("AWS_TEXTRACT_OCR_PROVIDER", provider.getEngineName());
    }

    @Test
    @DisplayName("3. OcrConfig instantiates correct OcrProvider bean based on properties")
    void testOcrConfigSelection() {
        OcrConfig config = new OcrConfig();
        
        properties.setProvider("tesseract");
        OcrProvider tesseract = config.tesseractOcrProvider(properties);
        assertFalse(tesseract.isMock());
        assertEquals("TESSERACT_OCR_PROVIDER", tesseract.getEngineName());

        properties.setProvider("textract");
        OcrProvider textract = config.awsTextractOcrProvider(properties);
        assertFalse(textract.isMock());
        assertEquals("AWS_TEXTRACT_OCR_PROVIDER", textract.getEngineName());

        OcrProvider mock = config.mockOcrProvider();
        assertTrue(mock.isMock());
        assertEquals("MOCK_OCR_PROVIDER", mock.getEngineName());
    }

    @Test
    @DisplayName("4. TesseractOcrProvider handles empty document bytes safely with FAILED status")
    void testTesseractEmptyBytes() {
        OcrProvider provider = new TesseractOcrProvider(properties);
        OcrResult result = provider.processDocument(UUID.randomUUID(), DocumentType.PRESCRIPTION, "test.png", new byte[0], "image/png");

        assertEquals(ExtractionStatus.FAILED, result.getStatus());
        assertEquals(BigDecimal.ZERO, result.getConfidence());
        assertTrue(result.getErrorMessage().contains("0 bytes"));
        assertFalse(result.isMock());
    }

    @Test
    @DisplayName("5. TesseractOcrProvider handles unsupported file formats safely")
    void testTesseractUnsupportedFormat() {
        OcrProvider provider = new TesseractOcrProvider(properties);
        OcrResult result = provider.processDocument(UUID.randomUUID(), DocumentType.OTHER, "document.exe", new byte[]{1, 2, 3, 4}, "application/x-msdownload");

        assertEquals(ExtractionStatus.FAILED, result.getStatus());
        assertEquals(BigDecimal.ZERO, result.getConfidence());
        assertTrue(result.getErrorMessage().contains("Unsupported file format"));
    }

    @Test
    @DisplayName("6. TesseractOcrProvider performs image preprocessing and returns structured result")
    void testTesseractImageProcessing() throws Exception {
        OcrProvider provider = new TesseractOcrProvider(properties);

        // Generate a synthetic test image with text drawing
        BufferedImage img = new BufferedImage(400, 100, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = img.createGraphics();
        g.setColor(Color.WHITE);
        g.fillRect(0, 0, 400, 100);
        g.setColor(Color.BLACK);
        g.drawString("Rx: Paracetamol 500mg - Take 1 tablet daily", 20, 50);
        g.dispose();

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(img, "png", baos);
        byte[] imageBytes = baos.toByteArray();

        OcrResult result = provider.processDocument(UUID.randomUUID(), DocumentType.PRESCRIPTION, "sample_rx.png", imageBytes, "image/png");

        assertNotNull(result);
        assertFalse(result.isMock());
        assertNotNull(result.getStatus());
        assertNotNull(result.getConfidence());
        assertEquals("TESSERACT_OCR_PROVIDER", result.getEngine());
    }

    @Test
    @DisplayName("7. AwsTextractOcrProvider handles missing credentials with explicit FAILED status")
    void testAwsTextractMissingCredentials() {
        properties.setAwsAccessKey("");
        properties.setAwsSecretKey("");
        OcrProvider provider = new AwsTextractOcrProvider(properties);

        OcrResult result = provider.processDocument(UUID.randomUUID(), DocumentType.LAB_REPORT, "lab.pdf", new byte[]{1, 2, 3}, "application/pdf");

        assertEquals(ExtractionStatus.FAILED, result.getStatus());
        assertTrue(result.getErrorMessage().contains("AWS Textract credentials"));
    }

    @Test
    @DisplayName("8. MockOcrProvider remains available for deterministic offline tests")
    void testMockOcrProviderAvailable() {
        OcrProvider mock = new MockOcrProvider();
        assertTrue(mock.isMock());

        OcrResult result = mock.processDocument(UUID.randomUUID(), DocumentType.PRESCRIPTION, "rx.pdf", new byte[]{1}, "application/pdf");
        assertTrue(result.isMock());
        assertEquals(ExtractionStatus.SUCCESS, result.getStatus());
        assertTrue(result.getRawText().contains("Metformin"));
    }
}
