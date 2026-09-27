package com.medikiosk.ocr.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

/**
 * Configuration properties for MediKiosk OCR document processing engine.
 */
@Component
@ConfigurationProperties(prefix = "medikiosk.ocr")
public class OcrProperties {

    private String provider = "tesseract";
    private String tesseractDataPath = "/usr/share/tesseract-ocr/4.00/tessdata";
    private String language = "eng";
    private BigDecimal confidenceThreshold = new BigDecimal("0.7500");
    private String awsRegion = "ap-south-1";
    private String awsAccessKey;
    private String awsSecretKey;

    public String getProvider() {
        return provider;
    }

    public void setProvider(String provider) {
        this.provider = provider;
    }

    public String getTesseractDataPath() {
        return tesseractDataPath;
    }

    public void setTesseractDataPath(String tesseractDataPath) {
        this.tesseractDataPath = tesseractDataPath;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public BigDecimal getConfidenceThreshold() {
        return confidenceThreshold;
    }

    public void setConfidenceThreshold(BigDecimal confidenceThreshold) {
        this.confidenceThreshold = confidenceThreshold;
    }

    public String getAwsRegion() {
        return awsRegion;
    }

    public void setAwsRegion(String awsRegion) {
        this.awsRegion = awsRegion;
    }

    public String getAwsAccessKey() {
        return awsAccessKey;
    }

    public void setAwsAccessKey(String awsAccessKey) {
        this.awsAccessKey = awsAccessKey;
    }

    public String getAwsSecretKey() {
        return awsSecretKey;
    }

    public void setAwsSecretKey(String awsSecretKey) {
        this.awsSecretKey = awsSecretKey;
    }

    public boolean hasAwsCredentials() {
        return awsAccessKey != null && !awsAccessKey.isBlank() && awsSecretKey != null && !awsSecretKey.isBlank();
    }
}
