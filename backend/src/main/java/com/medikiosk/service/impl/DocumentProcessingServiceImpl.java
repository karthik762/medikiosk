package com.medikiosk.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.medikiosk.document.DocumentService;
import com.medikiosk.document.storage.DocumentStorageProvider;
import com.medikiosk.document.util.DocumentValidator;
import com.medikiosk.dto.document.DocumentUploadResponse;
import com.medikiosk.dto.document.ExtractionResultDto;
import com.medikiosk.entity.AuditLog;
import com.medikiosk.entity.Consent;
import com.medikiosk.entity.DocumentExtraction;
import com.medikiosk.entity.IntakeSession;
import com.medikiosk.entity.MedicalDocument;
import com.medikiosk.entity.Patient;
import com.medikiosk.entity.User;
import com.medikiosk.entity.enums.ConsentStatus;
import com.medikiosk.entity.enums.ConsentType;
import com.medikiosk.entity.enums.DocumentProcessingStatus;
import com.medikiosk.entity.enums.DocumentType;
import com.medikiosk.entity.enums.ExtractionStatus;
import com.medikiosk.entity.enums.IntakeSessionStatus;
import com.medikiosk.entity.enums.UserRole;
import com.medikiosk.exception.ApiException;
import com.medikiosk.exception.ResourceNotFoundException;
import com.medikiosk.ocr.OcrService;
import com.medikiosk.ocr.provider.OcrProvider;
import com.medikiosk.ocr.provider.OcrResult;
import com.medikiosk.repository.AuditLogRepository;
import com.medikiosk.repository.ConsentRepository;
import com.medikiosk.repository.DocumentExtractionRepository;
import com.medikiosk.repository.IntakeSessionRepository;
import com.medikiosk.repository.MedicalDocumentRepository;
import com.medikiosk.repository.PatientRepository;
import com.medikiosk.service.DocumentProcessingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Service
public class DocumentProcessingServiceImpl implements DocumentProcessingService, DocumentService, OcrService {

    private static final Logger log = LoggerFactory.getLogger(DocumentProcessingServiceImpl.class);

    private final MedicalDocumentRepository medicalDocumentRepository;
    private final DocumentExtractionRepository documentExtractionRepository;
    private final IntakeSessionRepository intakeSessionRepository;
    private final ConsentRepository consentRepository;
    private final PatientRepository patientRepository;
    private final AuditLogRepository auditLogRepository;
    private final DocumentStorageProvider documentStorageProvider;
    private final OcrProvider ocrProvider;
    private final ObjectMapper objectMapper;

    public DocumentProcessingServiceImpl(
            MedicalDocumentRepository medicalDocumentRepository,
            DocumentExtractionRepository documentExtractionRepository,
            IntakeSessionRepository intakeSessionRepository,
            ConsentRepository consentRepository,
            PatientRepository patientRepository,
            AuditLogRepository auditLogRepository,
            DocumentStorageProvider documentStorageProvider,
            OcrProvider ocrProvider,
            ObjectMapper objectMapper) {
        this.medicalDocumentRepository = medicalDocumentRepository;
        this.documentExtractionRepository = documentExtractionRepository;
        this.intakeSessionRepository = intakeSessionRepository;
        this.consentRepository = consentRepository;
        this.patientRepository = patientRepository;
        this.auditLogRepository = auditLogRepository;
        this.documentStorageProvider = documentStorageProvider;
        this.ocrProvider = ocrProvider;
        this.objectMapper = objectMapper;
    }

    @Override
    @Transactional
    public DocumentUploadResponse uploadAndProcessDocument(
            UUID sessionId,
            DocumentType documentType,
            MultipartFile file,
            User authenticatedUser) {

        IntakeSession session = getSessionOrThrow(sessionId);
        validateSessionAccess(session, authenticatedUser);
        verifyConsentGiven(sessionId);
        verifySessionModifiable(session);

        // 1. Server-side validation
        DocumentValidator.validateDocument(file, DocumentValidator.DEFAULT_MAX_DOCUMENT_SIZE_BYTES);

        byte[] fileBytes;
        try {
            fileBytes = file.getBytes();
        } catch (IOException e) {
            log.error("Failed to read uploaded document bytes for session {}", sessionId, e);
            throw new ApiException("Failed to read document payload", HttpStatus.BAD_REQUEST);
        }

        String safeFileName = DocumentValidator.sanitizeFilename(file.getOriginalFilename());
        String contentType = file.getContentType();
        String fileHash = DocumentValidator.calculateSha256(fileBytes);
        // 2. Persist MedicalDocument metadata first so ID is generated by JPA
        MedicalDocument document = new MedicalDocument();
        document.setPatient(session.getPatient());
        document.setSession(session);
        document.setDocumentType(documentType != null ? documentType : DocumentType.OTHER);
        document.setFileName(safeFileName);
        document.setFileType(contentType);
        document.setFileSize(file.getSize());
        document.setFileHash(fileHash);
        document.setProcessingStatus(DocumentProcessingStatus.PROCESSING);
        document.setStorageReference("pending");

        MedicalDocument savedDoc = medicalDocumentRepository.save(document);
        UUID documentId = savedDoc.getId();

        // 3. Persist source document binary through storage abstraction
        String storageRef = documentStorageProvider.storeDocument(documentId, safeFileName, fileBytes, contentType);
        savedDoc.setStorageReference(storageRef);
        savedDoc = medicalDocumentRepository.save(savedDoc);

        log.info("Persisted medical document {} for session {} (type: {}, size: {})", savedDoc.getId(), sessionId, savedDoc.getDocumentType(), savedDoc.getFileSize());

        // Audit upload
        createAuditLog(authenticatedUser, "DOCUMENT_UPLOADED", "MedicalDocument", savedDoc.getId(),
                "{\"sessionId\":\"" + sessionId + "\",\"fileName\":\"" + safeFileName + "\",\"fileSize\":" + file.getSize() + "}");
        createAuditLog(authenticatedUser, "DOCUMENT_PROCESSING_STARTED", "MedicalDocument", savedDoc.getId(),
                "{\"engine\":\"" + ocrProvider.getEngineName() + "\"}");

        // 4. Invoke OCR provider abstraction
        OcrResult ocrResult;
        try {
            ocrResult = ocrProvider.processDocument(
                    savedDoc.getId(),
                    savedDoc.getDocumentType(),
                    safeFileName,
                    fileBytes,
                    contentType
            );
        } catch (Exception e) {
            log.error("OCR provider threw unhandled exception for document {}", savedDoc.getId(), e);
            ocrResult = new OcrResult();
            ocrResult.setStatus(ExtractionStatus.FAILED);
            ocrResult.setErrorMessage("OCR engine execution failure: " + e.getMessage());
            ocrResult.setUncertaintyFlags(List.of("OCR_PROCESSING_FAILED", "DOCTOR_INSPECTION_REQUIRED"));
        }

        // 5. Store extraction and update document processing status
        if (ocrResult.getStatus() == ExtractionStatus.FAILED) {
            savedDoc.setProcessingStatus(DocumentProcessingStatus.FAILED);
            savedDoc.setProcessedAt(Instant.now());
            medicalDocumentRepository.save(savedDoc);
            createAuditLog(authenticatedUser, "DOCUMENT_PROCESSING_FAILED", "MedicalDocument", savedDoc.getId(),
                    "{\"error\":\"" + ocrResult.getErrorMessage() + "\"}");
            return mapToResponse(savedDoc, null);
        }

        DocumentExtraction extraction = new DocumentExtraction();
        extraction.setDocument(savedDoc);
        extraction.setProcessingEngine(ocrProvider.getEngineName());
        extraction.setConfidenceScore(ocrResult.getConfidence());
        extraction.setExtractedText(ocrResult.getRawText());

        try {
            Map<String, Object> dataPayload = new HashMap<>(ocrResult.getExtractedEntities());
            if (ocrResult.getUncertaintyFlags() != null && !ocrResult.getUncertaintyFlags().isEmpty()) {
                dataPayload.put("uncertaintyFlags", ocrResult.getUncertaintyFlags());
            }
            if (ocrResult.getErrorMessage() != null) {
                dataPayload.put("error", ocrResult.getErrorMessage());
            }
            extraction.setExtractionData(objectMapper.writeValueAsString(dataPayload));
        } catch (Exception e) {
            log.error("Failed to serialize extraction data for document {}", savedDoc.getId(), e);
            extraction.setExtractionData("{}");
        }

        if (ocrResult.getStatus() == ExtractionStatus.LOW_CONFIDENCE || (ocrResult.getConfidence() != null && ocrResult.getConfidence().compareTo(new BigDecimal("0.7500")) < 0)) {
            savedDoc.setProcessingStatus(DocumentProcessingStatus.REVIEW_REQUIRED);
            extraction.setStatus(ExtractionStatus.LOW_CONFIDENCE);
            createAuditLog(authenticatedUser, "DOCUMENT_PROCESSED", "MedicalDocument", savedDoc.getId(),
                    "{\"status\":\"REVIEW_REQUIRED\",\"confidence\":" + ocrResult.getConfidence() + "}");
        } else {
            savedDoc.setProcessingStatus(DocumentProcessingStatus.COMPLETED);
            extraction.setStatus(ExtractionStatus.SUCCESS);
            createAuditLog(authenticatedUser, "DOCUMENT_PROCESSED", "MedicalDocument", savedDoc.getId(),
                    "{\"status\":\"COMPLETED\",\"confidence\":" + ocrResult.getConfidence() + "}");
        }

        savedDoc.setProcessedAt(Instant.now());
        medicalDocumentRepository.save(savedDoc);
        DocumentExtraction savedExtraction = documentExtractionRepository.save(extraction);

        return mapToResponse(savedDoc, savedExtraction);
    }

    @Override
    @Transactional(readOnly = true)
    public List<DocumentUploadResponse> getSessionDocuments(UUID sessionId, User authenticatedUser) {
        IntakeSession session = getSessionOrThrow(sessionId);
        validateSessionAccess(session, authenticatedUser);

        List<MedicalDocument> docs = medicalDocumentRepository.findBySessionId(sessionId);
        List<DocumentUploadResponse> responses = new ArrayList<>();
        for (MedicalDocument doc : docs) {
            DocumentExtraction latestExtraction = getLatestExtraction(doc.getId());
            responses.add(mapToResponse(doc, latestExtraction));
        }
        return responses;
    }

    @Override
    @Transactional(readOnly = true)
    public DocumentUploadResponse getDocumentDetails(UUID sessionId, UUID documentId, User authenticatedUser) {
        IntakeSession session = getSessionOrThrow(sessionId);
        validateSessionAccess(session, authenticatedUser);

        MedicalDocument doc = medicalDocumentRepository.findById(documentId)
                .orElseThrow(() -> new ResourceNotFoundException("Document not found with id: " + documentId));

        if (doc.getSession() == null || !doc.getSession().getId().equals(sessionId)) {
            throw new ApiException("Document does not belong to the specified session", HttpStatus.BAD_REQUEST);
        }

        DocumentExtraction extraction = getLatestExtraction(documentId);
        return mapToResponse(doc, extraction);
    }

    @Override
    @Transactional
    public byte[] getDocumentBinary(UUID sessionId, UUID documentId, User authenticatedUser) {
        IntakeSession session = getSessionOrThrow(sessionId);
        validateSessionAccess(session, authenticatedUser);

        MedicalDocument doc = medicalDocumentRepository.findById(documentId)
                .orElseThrow(() -> new ResourceNotFoundException("Document not found with id: " + documentId));

        if (doc.getSession() == null || !doc.getSession().getId().equals(sessionId)) {
            throw new ApiException("Document does not belong to the specified session", HttpStatus.BAD_REQUEST);
        }

        byte[] binary = documentStorageProvider.retrieveDocument(doc.getStorageReference());
        createAuditLog(authenticatedUser, "DOCUMENT_ACCESSED", "MedicalDocument", documentId, "{\"sessionId\":\"" + sessionId + "\"}");
        return binary;
    }

    @Override
    @Transactional(readOnly = true)
    public DocumentUploadResponse getDocumentDetailsById(UUID documentId, User authenticatedUser) {
        MedicalDocument doc = medicalDocumentRepository.findById(documentId)
                .orElseThrow(() -> new ResourceNotFoundException("Document not found with id: " + documentId));

        validateDocumentAccess(doc, authenticatedUser);
        DocumentExtraction extraction = getLatestExtraction(documentId);
        return mapToResponse(doc, extraction);
    }

    @Override
    @Transactional
    public byte[] getDocumentBinaryById(UUID documentId, User authenticatedUser) {
        MedicalDocument doc = medicalDocumentRepository.findById(documentId)
                .orElseThrow(() -> new ResourceNotFoundException("Document not found with id: " + documentId));

        validateDocumentAccess(doc, authenticatedUser);
        byte[] binary = documentStorageProvider.retrieveDocument(doc.getStorageReference());
        createAuditLog(authenticatedUser, "DOCUMENT_ACCESSED", "MedicalDocument", documentId, "{\"direct\":true}");
        return binary;
    }

    // --- Phase 1 Architectural Interface Implementations ---

    @Override
    @Transactional
    public UUID uploadDocument(UUID patientId, String fileName, String contentType, byte[] fileBytes) {
        Patient patient = patientRepository.findById(patientId)
                .orElseThrow(() -> new ResourceNotFoundException("Patient not found with id: " + patientId));

        String safeName = DocumentValidator.sanitizeFilename(fileName);

        MedicalDocument doc = new MedicalDocument();
        doc.setPatient(patient);
        doc.setDocumentType(DocumentType.OTHER);
        doc.setFileName(safeName);
        doc.setFileType(contentType);
        doc.setFileSize((long) fileBytes.length);
        doc.setFileHash(DocumentValidator.calculateSha256(fileBytes));
        doc.setProcessingStatus(DocumentProcessingStatus.UPLOADED);
        doc.setStorageReference("pending");

        MedicalDocument saved = medicalDocumentRepository.save(doc);
        String storageRef = documentStorageProvider.storeDocument(saved.getId(), safeName, fileBytes, contentType);
        saved.setStorageReference(storageRef);
        return medicalDocumentRepository.save(saved).getId();
    }

    @Override
    @Transactional(readOnly = true)
    public byte[] getDocumentContent(UUID documentId) {
        MedicalDocument doc = medicalDocumentRepository.findById(documentId)
                .orElseThrow(() -> new ResourceNotFoundException("Document not found with id: " + documentId));
        return documentStorageProvider.retrieveDocument(doc.getStorageReference());
    }

    @Override
    @Transactional
    public Map<String, Object> extractDocumentText(UUID documentId, byte[] fileBytes) {
        MedicalDocument doc = medicalDocumentRepository.findById(documentId).orElse(null);
        DocumentType type = doc != null ? doc.getDocumentType() : DocumentType.OTHER;
        String fileName = doc != null ? doc.getFileName() : "document.bin";
        String contentType = doc != null ? doc.getFileType() : "application/octet-stream";

        OcrResult result = ocrProvider.processDocument(documentId, type, fileName, fileBytes, contentType);

        Map<String, Object> responseMap = new HashMap<>();
        responseMap.put("documentId", documentId);
        responseMap.put("rawText", result.getRawText());
        responseMap.put("confidence", result.getConfidence());
        responseMap.put("engine", result.getEngine());
        responseMap.put("status", result.getStatus().name());
        responseMap.put("entities", result.getExtractedEntities());
        return responseMap;
    }

    // --- Internal Helpers ---

    private IntakeSession getSessionOrThrow(UUID sessionId) {
        return intakeSessionRepository.findById(sessionId)
                .orElseThrow(() -> new ResourceNotFoundException("Intake session not found with id: " + sessionId));
    }

    private void validateSessionAccess(IntakeSession session, User user) {
        if (user == null) {
            throw new ApiException("Authentication required", HttpStatus.UNAUTHORIZED);
        }
        if (user.getRole() == UserRole.PATIENT) {
            Patient patient = session.getPatient();
            if (patient.getUserId() == null || !patient.getUserId().equals(user.getId())) {
                log.warn("Cross-patient access denied: User {} tried accessing session {}", user.getId(), session.getId());
                throw new ApiException("Access denied: You do not own this intake session", HttpStatus.FORBIDDEN);
            }
        }
    }

    private void validateDocumentAccess(MedicalDocument doc, User user) {
        if (user == null) {
            throw new ApiException("Authentication required", HttpStatus.UNAUTHORIZED);
        }
        if (user.getRole() == UserRole.PATIENT) {
            Patient patient = doc.getPatient();
            if (patient.getUserId() == null || !patient.getUserId().equals(user.getId())) {
                log.warn("Cross-patient access denied: User {} tried accessing document {}", user.getId(), doc.getId());
                throw new ApiException("Access denied: You do not own this document", HttpStatus.FORBIDDEN);
            }
        }
    }

    private void verifySessionModifiable(IntakeSession session) {
        if (session.getStatus() == IntakeSessionStatus.COMPLETED) {
            throw new ApiException("Intake session is already completed and cannot be modified", HttpStatus.CONFLICT);
        }
        if (session.getStatus() == IntakeSessionStatus.EXPIRED) {
            throw new ApiException("Intake session is expired", HttpStatus.CONFLICT);
        }
    }

    private void verifyConsentGiven(UUID sessionId) {
        Optional<Consent> consentOpt = consentRepository
                .findFirstBySessionIdAndConsentTypeOrderByConsentedAtDesc(sessionId, ConsentType.GENERAL_INTAKE);
        if (consentOpt.isEmpty() || consentOpt.get().getConsentStatus() != ConsentStatus.ACCEPTED) {
            log.warn("Document intake operation blocked: Valid accepted GENERAL_INTAKE consent missing for session {}", sessionId);
            throw new ApiException("Clinical intake cannot proceed without valid consent", HttpStatus.BAD_REQUEST);
        }
    }

    private DocumentExtraction getLatestExtraction(UUID documentId) {
        List<DocumentExtraction> extractions = documentExtractionRepository.findByDocumentId(documentId);
        return extractions.isEmpty() ? null : extractions.get(extractions.size() - 1);
    }

    private void createAuditLog(User user, String action, String entityType, UUID entityId, String metadata) {
        try {
            AuditLog auditLog = new AuditLog(
                    user != null ? user.getId() : null,
                    action,
                    entityType,
                    entityId,
                    metadata
            );
            auditLogRepository.save(auditLog);
        } catch (Exception e) {
            log.warn("Failed to create audit log for action {} on entity {}", action, entityId, e);
        }
    }

    private DocumentUploadResponse mapToResponse(MedicalDocument doc, DocumentExtraction extraction) {
        DocumentUploadResponse response = new DocumentUploadResponse();
        response.setId(doc.getId());
        response.setPatientId(doc.getPatient().getId());
        if (doc.getSession() != null) {
            response.setSessionId(doc.getSession().getId());
        }
        response.setDocumentType(doc.getDocumentType());
        response.setFileName(doc.getFileName());
        response.setFileType(doc.getFileType());
        response.setFileSize(doc.getFileSize());
        response.setFileHash(doc.getFileHash());
        response.setProcessingStatus(doc.getProcessingStatus());
        response.setUploadedAt(doc.getUploadedAt());
        response.setProcessedAt(doc.getProcessedAt());

        if (extraction != null) {
            ExtractionResultDto extDto = new ExtractionResultDto();
            extDto.setId(extraction.getId());
            extDto.setDocumentId(doc.getId());
            extDto.setExtractedText(extraction.getExtractedText());
            extDto.setConfidenceScore(extraction.getConfidenceScore());
            extDto.setProcessingEngine(extraction.getProcessingEngine());
            extDto.setIsMock(ocrProvider.isMock());
            extDto.setStatus(extraction.getStatus());
            extDto.setCreatedAt(extraction.getCreatedAt());

            if (extraction.getExtractionData() != null) {
                try {
                    Map<String, Object> parsed = objectMapper.readValue(
                            extraction.getExtractionData(),
                            new TypeReference<Map<String, Object>>() {}
                    );
                    extDto.setExtractionData(parsed);

                    Object flags = parsed.get("uncertaintyFlags");
                    if (flags instanceof List<?> list) {
                        List<String> stringFlags = new ArrayList<>();
                        for (Object item : list) {
                            if (item != null) stringFlags.add(item.toString());
                        }
                        extDto.setUncertaintyFlags(stringFlags);
                    }
                } catch (Exception e) {
                    log.warn("Could not deserialize extractionData for extraction {}", extraction.getId(), e);
                }
            }
            response.setExtraction(extDto);
        }

        return response;
    }
}
