package com.medikiosk.ocr.provider;

import com.medikiosk.entity.enums.DocumentType;
import com.medikiosk.entity.enums.ExtractionStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Deterministic Mock OCR provider for local development, test automation, and validation.
 * Explicitly marks isMock = true and produces predictable structured extractions without cloud dependencies.
 */
public class MockOcrProvider implements OcrProvider {

    private static final Logger log = LoggerFactory.getLogger(MockOcrProvider.class);
    private static final String ENGINE_NAME = "MOCK_OCR_PROVIDER";

    @Override
    public OcrResult processDocument(UUID documentId, DocumentType type, String filename, byte[] fileBytes, String contentType) {
        log.info("MockOcrProvider processing document {} (name: {}, type: {}, bytes: {})", documentId, filename, type, fileBytes != null ? fileBytes.length : 0);

        String lowerName = filename != null ? filename.toLowerCase() : "";

        // 1. Simulated failure trigger
        if (lowerName.contains("fail") || lowerName.contains("corrupt") || lowerName.contains("error")) {
            OcrResult failure = new OcrResult("", BigDecimal.ZERO, ENGINE_NAME, true, ExtractionStatus.FAILED);
            failure.setErrorMessage("Simulated OCR engine failure: unreadable or corrupted document bytes");
            failure.setUncertaintyFlags(List.of("OCR_PROCESSING_FAILED", "UNREADABLE_DOCUMENT", "DOCTOR_INSPECTION_REQUIRED"));
            return failure;
        }

        // 2. Simulated low-confidence / poor handwriting trigger
        if (lowerName.contains("low_confidence") || lowerName.contains("poor_handwriting") || lowerName.contains("handwriting") || lowerName.contains("blurry")) {
            OcrResult lowConf = new OcrResult();
            lowConf.setEngine(ENGINE_NAME);
            lowConf.setMock(true);
            lowConf.setStatus(ExtractionStatus.LOW_CONFIDENCE);
            lowConf.setConfidence(new BigDecimal("0.4500"));
            lowConf.setRawText("Rx: Metf... 50O mg ? once/twice daily ? sig illegible.\nDr. signature unverified.");

            Map<String, Object> entities = new HashMap<>();
            entities.put("facilityName", "Unknown Clinic");
            entities.put("documentDate", "2026-09-01");

            List<Map<String, Object>> meds = new ArrayList<>();
            Map<String, Object> med = new HashMap<>();
            med.put("name", "Metf... [Uncertain]");
            med.put("dosage", "50O mg [Uncertain]");
            med.put("frequency", "illegible");
            med.put("confidence", 0.52);
            med.put("uncertaintyFlag", "POSSIBLE_HANDWRITING_AMBIGUITY");
            meds.add(med);
            entities.put("medications", meds);

            lowConf.setExtractedEntities(entities);
            lowConf.setUncertaintyFlags(List.of(
                    "LOW_OCR_CONFIDENCE",
                    "POOR_HANDWRITING_AMBIGUITY",
                    "DOCTOR_VERIFICATION_REQUIRED"
            ));
            return lowConf;
        }

        // 3. Prescription document type
        if (type == DocumentType.PRESCRIPTION || lowerName.contains("prescription")) {
            OcrResult rx = new OcrResult();
            rx.setEngine(ENGINE_NAME);
            rx.setMock(true);
            rx.setStatus(ExtractionStatus.SUCCESS);
            rx.setConfidence(new BigDecimal("0.9450"));
            rx.setRawText("Rx: City Health Care Clinic\nDate: 2026-09-01\nPatient: Registered Intake\n" +
                    "1. Metformin 500mg Tablet - Twice daily with meals\n" +
                    "2. Atorvastatin 10mg Tablet - Once daily at bedtime\n" +
                    "Doctor: Dr. R. Sharma, MD (Reg: 45892)");

            Map<String, Object> entities = new HashMap<>();
            entities.put("facilityName", "City Health Care Clinic");
            entities.put("documentDate", "2026-09-01");
            entities.put("providerName", "Dr. R. Sharma, MD");

            List<Map<String, Object>> meds = new ArrayList<>();
            Map<String, Object> med1 = new HashMap<>();
            med1.put("name", "Metformin");
            med1.put("dosage", "500 mg");
            med1.put("frequency", "Twice daily with meals");
            med1.put("confidence", 0.96);
            meds.add(med1);

            Map<String, Object> med2 = new HashMap<>();
            med2.put("name", "Atorvastatin");
            med2.put("dosage", "10 mg");
            med2.put("frequency", "Once daily at bedtime");
            med2.put("confidence", 0.93);
            meds.add(med2);

            entities.put("medications", meds);
            rx.setExtractedEntities(entities);
            return rx;
        }

        // 4. Lab report document type
        if (type == DocumentType.LAB_REPORT || lowerName.contains("lab") || lowerName.contains("report")) {
            OcrResult lab = new OcrResult();
            lab.setEngine(ENGINE_NAME);
            lab.setMock(true);
            lab.setStatus(ExtractionStatus.SUCCESS);
            lab.setConfidence(new BigDecimal("0.9600"));
            lab.setRawText("Apex Diagnostic Laboratories\nDate: 2026-08-28\nPathology Test Report\n" +
                    "Fasting Blood Glucose: 110 mg/dL (Reference: 70 - 99 mg/dL)\n" +
                    "HbA1c: 6.2 % (Reference: < 5.7 %)\n" +
                    "Total Cholesterol: 195 mg/dL (Reference: < 200 mg/dL)\n" +
                    "Verified by: Dr. P. Mehta, Pathologist");

            Map<String, Object> entities = new HashMap<>();
            entities.put("facilityName", "Apex Diagnostic Laboratories");
            entities.put("documentDate", "2026-08-28");
            entities.put("providerName", "Dr. P. Mehta, Pathologist");

            List<Map<String, Object>> tests = new ArrayList<>();
            Map<String, Object> test1 = new HashMap<>();
            test1.put("testName", "Fasting Blood Glucose");
            test1.put("value", "110");
            test1.put("unit", "mg/dL");
            test1.put("referenceRange", "70 - 99");
            test1.put("confidence", 0.97);
            tests.add(test1);

            Map<String, Object> test2 = new HashMap<>();
            test2.put("testName", "HbA1c");
            test2.put("value", "6.2");
            test2.put("unit", "%");
            test2.put("referenceRange", "< 5.7");
            test2.put("confidence", 0.96);
            tests.add(test2);

            Map<String, Object> test3 = new HashMap<>();
            test3.put("testName", "Total Cholesterol");
            test3.put("value", "195");
            test3.put("unit", "mg/dL");
            test3.put("referenceRange", "< 200");
            test3.put("confidence", 0.95);
            tests.add(test3);

            entities.put("labTests", tests);
            lab.setExtractedEntities(entities);
            return lab;
        }

        // 5. Default medical document
        OcrResult def = new OcrResult();
        def.setEngine(ENGINE_NAME);
        def.setMock(true);
        def.setStatus(ExtractionStatus.SUCCESS);
        def.setConfidence(new BigDecimal("0.9100"));
        def.setRawText("Clinical Document Attachment\nDate: 2026-08-20\nGeneral medical record observations and follow-up notes.");

        Map<String, Object> entities = new HashMap<>();
        entities.put("documentDate", "2026-08-20");
        def.setExtractedEntities(entities);
        return def;
    }

    @Override
    public String getEngineName() {
        return ENGINE_NAME;
    }

    @Override
    public boolean isMock() {
        return true;
    }
}
