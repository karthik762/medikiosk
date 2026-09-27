package com.medikiosk.document;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.medikiosk.document.storage.DocumentStorageProvider;
import com.medikiosk.dto.intake.ConsentRequest;
import com.medikiosk.dto.intake.IntakeSessionRequest;
import com.medikiosk.dto.intake.PatientRequest;
import com.medikiosk.entity.AuditLog;
import com.medikiosk.entity.DocumentExtraction;
import com.medikiosk.entity.IntakeSession;
import com.medikiosk.entity.MedicalDocument;
import com.medikiosk.entity.Patient;
import com.medikiosk.entity.User;
import com.medikiosk.entity.enums.ConsentStatus;
import com.medikiosk.entity.enums.DocumentProcessingStatus;
import com.medikiosk.entity.enums.DocumentType;
import com.medikiosk.entity.enums.IntakeSessionStatus;
import com.medikiosk.entity.enums.UserRole;
import com.medikiosk.entity.enums.UserStatus;
import com.medikiosk.ocr.OcrService;
import com.medikiosk.repository.AuditLogRepository;
import com.medikiosk.repository.ConsentRepository;
import com.medikiosk.repository.DocumentExtractionRepository;
import com.medikiosk.repository.IntakeSessionRepository;
import com.medikiosk.repository.MedicalDocumentRepository;
import com.medikiosk.repository.PatientRepository;
import com.medikiosk.repository.UserRepository;
import com.medikiosk.security.JwtTokenProvider;
import com.medikiosk.service.DocumentProcessingService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class DocumentIntelligenceIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PatientRepository patientRepository;

    @Autowired
    private IntakeSessionRepository intakeSessionRepository;

    @Autowired
    private ConsentRepository consentRepository;

    @Autowired
    private MedicalDocumentRepository medicalDocumentRepository;

    @Autowired
    private DocumentExtractionRepository documentExtractionRepository;

    @Autowired
    private AuditLogRepository auditLogRepository;

    @Autowired
    private DocumentStorageProvider storageProvider;

    @Autowired
    private DocumentProcessingService documentProcessingService;

    @Autowired
    private DocumentService legacyDocumentService;

    @Autowired
    private OcrService legacyOcrService;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private ObjectMapper objectMapper;

    private User patientUserA;
    private String tokenPatientA;
    private User patientUserB;
    private String tokenPatientB;
    private User doctorUser;
    private String tokenDoctor;

    // Helper byte arrays with authentic magic bytes
    private static final byte[] VALID_PDF_BYTES = "%PDF-1.4\n1 0 obj\n<<>>\nendobj\ntrailer\n<<>>\n%%EOF".getBytes(StandardCharsets.US_ASCII);
    private static final byte[] VALID_PNG_BYTES = new byte[]{(byte) 0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A, 0x00, 0x00, 0x00, 0x0D, 0x49, 0x48, 0x44, 0x52};
    private static final byte[] VALID_JPEG_BYTES = new byte[]{(byte) 0xFF, (byte) 0xD8, (byte) 0xFF, (byte) 0xE0, 0x00, 0x10, 0x4A, 0x46, 0x49, 0x46, 0x00, 0x01};

    @BeforeEach
    void setUp() {
        documentExtractionRepository.deleteAll();
        medicalDocumentRepository.deleteAll();
        auditLogRepository.deleteAll();
        consentRepository.deleteAll();
        intakeSessionRepository.deleteAll();
        patientRepository.deleteAll();
        userRepository.deleteAll();

        patientUserA = userRepository.save(new User(
                "patientA.docs@test.com",
                passwordEncoder.encode("Pass123!"),
                "Aarav",
                "Sharma",
                UserRole.PATIENT,
                UserStatus.ACTIVE
        ));
        tokenPatientA = jwtTokenProvider.generateToken(patientUserA);

        patientUserB = userRepository.save(new User(
                "patientB.docs@test.com",
                passwordEncoder.encode("Pass123!"),
                "Priya",
                "Patel",
                UserRole.PATIENT,
                UserStatus.ACTIVE
        ));
        tokenPatientB = jwtTokenProvider.generateToken(patientUserB);

        doctorUser = userRepository.save(new User(
                "doctor.docs@test.com",
                passwordEncoder.encode("Doctor123!"),
                "Dr. Vikram",
                "Seth",
                UserRole.DOCTOR,
                UserStatus.ACTIVE
        ));
        tokenDoctor = jwtTokenProvider.generateToken(doctorUser);
    }

    private Patient createPatient(User user, String identifier) throws Exception {
        PatientRequest request = new PatientRequest("Test", "Patient", LocalDate.of(1990, 1, 1), "MALE", "+919876543210", user.getEmail());
        request.setPatientIdentifier(identifier);
        String token = user.equals(patientUserA) ? tokenPatientA : tokenPatientB;

        MvcResult result = mockMvc.perform(post("/api/patients")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn();

        JsonNode root = objectMapper.readTree(result.getResponse().getContentAsString());
        UUID patientId = UUID.fromString(root.path("data").path("id").asText());
        return patientRepository.findById(patientId).orElseThrow();
    }

    private IntakeSession createSessionWithConsent(Patient patient, String token) throws Exception {
        IntakeSessionRequest sessionRequest = new IntakeSessionRequest(patient.getId(), "en");
        MvcResult sessionResult = mockMvc.perform(post("/api/intake/sessions")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sessionRequest)))
                .andExpect(status().isCreated())
                .andReturn();

        JsonNode root = objectMapper.readTree(sessionResult.getResponse().getContentAsString());
        UUID sessionId = UUID.fromString(root.path("data").path("id").asText());

        ConsentRequest consentRequest = new ConsentRequest(
                ConsentStatus.ACCEPTED,
                "GENERAL_INTAKE",
                "v1.0",
                "REF-DOC-1"
        );

        mockMvc.perform(post("/api/intake/sessions/" + sessionId + "/consent")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(consentRequest)))
                .andExpect(status().isOk());

        return intakeSessionRepository.findById(sessionId).orElseThrow();
    }

    // ==========================================
    // 1. VALID UPLOAD TESTS
    // ==========================================

    @Test
    @DisplayName("Upload valid prescription PDF - should succeed, trigger OCR, and link extraction")
    void testUploadValidPrescriptionPdf() throws Exception {
        Patient patient = createPatient(patientUserA, "PAT-DOC-001");
        IntakeSession session = createSessionWithConsent(patient, tokenPatientA);

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "rx_metformin.pdf",
                "application/pdf",
                VALID_PDF_BYTES
        );

        MvcResult result = mockMvc.perform(multipart("/api/intake/sessions/{sessionId}/documents", session.getId())
                        .file(file)
                        .param("documentType", "PRESCRIPTION")
                        .header("Authorization", "Bearer " + tokenPatientA))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.id").exists())
                .andExpect(jsonPath("$.data.documentType").value("PRESCRIPTION"))
                .andExpect(jsonPath("$.data.fileName").value("rx_metformin.pdf"))
                .andExpect(jsonPath("$.data.fileSize").value(VALID_PDF_BYTES.length))
                .andExpect(jsonPath("$.data.processingStatus").value("COMPLETED"))
                .andExpect(jsonPath("$.data.fileHash").isNotEmpty())
                .andExpect(jsonPath("$.data.extraction").exists())
                .andExpect(jsonPath("$.data.extraction.status").value("SUCCESS"))
                .andExpect(jsonPath("$.data.extraction.isMock").value(true))
                .andExpect(jsonPath("$.data.extraction.confidenceScore").value(0.945))
                .andReturn();

        JsonNode jsonNode = objectMapper.readTree(result.getResponse().getContentAsString());
        UUID documentId = UUID.fromString(jsonNode.path("data").path("id").asText());

        // Verify DB state
        MedicalDocument savedDoc = medicalDocumentRepository.findById(documentId).orElseThrow();
        assertThat(savedDoc.getDocumentType()).isEqualTo(DocumentType.PRESCRIPTION);
        assertThat(savedDoc.getProcessingStatus()).isEqualTo(DocumentProcessingStatus.COMPLETED);
        assertThat(savedDoc.getFileHash()).isNotEmpty();

        List<DocumentExtraction> extractions = documentExtractionRepository.findByDocumentId(documentId);
        assertThat(extractions).hasSize(1);
        assertThat(extractions.get(0).getStatus().name()).isEqualTo("SUCCESS");
        assertThat(extractions.get(0).getConfidenceScore().doubleValue()).isEqualTo(0.945);
        assertThat(extractions.get(0).getExtractionData()).isNotNull();

        // Verify audit log
        List<AuditLog> auditLogs = auditLogRepository.findByAction("DOCUMENT_UPLOADED");
        assertThat(auditLogs).isNotEmpty();
        List<AuditLog> processedLogs = auditLogRepository.findByAction("DOCUMENT_PROCESSED");
        assertThat(processedLogs).isNotEmpty();
    }

    @Test
    @DisplayName("Upload valid lab report PNG - should extract structured lab tests")
    void testUploadValidLabReportPng() throws Exception {
        Patient patient = createPatient(patientUserA, "PAT-DOC-002");
        IntakeSession session = createSessionWithConsent(patient, tokenPatientA);

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "blood_test_report.png",
                "image/png",
                VALID_PNG_BYTES
        );

        mockMvc.perform(multipart("/api/intake/sessions/{sessionId}/documents", session.getId())
                        .file(file)
                        .param("documentType", "LAB_REPORT")
                        .header("Authorization", "Bearer " + tokenPatientA))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.documentType").value("LAB_REPORT"))
                .andExpect(jsonPath("$.data.processingStatus").value("COMPLETED"))
                .andExpect(jsonPath("$.data.extraction.extractionData.labTests").isArray())
                .andExpect(jsonPath("$.data.extraction.isMock").value(true));
    }

    // ==========================================
    // 2. VALIDATION & SECURITY REJECTION TESTS
    // ==========================================

    @Test
    @DisplayName("Empty file upload should return 400 Bad Request")
    void testUploadEmptyFile() throws Exception {
        Patient patient = createPatient(patientUserA, "PAT-DOC-003");
        IntakeSession session = createSessionWithConsent(patient, tokenPatientA);

        MockMultipartFile emptyFile = new MockMultipartFile(
                "file",
                "empty.pdf",
                "application/pdf",
                new byte[0]
        );

        mockMvc.perform(multipart("/api/intake/sessions/{sessionId}/documents", session.getId())
                        .file(emptyFile)
                        .param("documentType", "PRESCRIPTION")
                        .header("Authorization", "Bearer " + tokenPatientA))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Document file is required and cannot be empty"));
    }

    @Test
    @DisplayName("Unsupported MIME type should return 400 Bad Request")
    void testUploadUnsupportedMimeType() throws Exception {
        Patient patient = createPatient(patientUserA, "PAT-DOC-004");
        IntakeSession session = createSessionWithConsent(patient, tokenPatientA);

        MockMultipartFile txtFile = new MockMultipartFile(
                "file",
                "notes.txt",
                "text/plain",
                "Patient notes here".getBytes(StandardCharsets.UTF_8)
        );

        mockMvc.perform(multipart("/api/intake/sessions/{sessionId}/documents", session.getId())
                        .file(txtFile)
                        .param("documentType", "PRESCRIPTION")
                        .header("Authorization", "Bearer " + tokenPatientA))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Unsupported document format: text/plain. Supported formats: PDF, JPEG, PNG, WebP."));
    }

    @Test
    @DisplayName("Spoofed MIME type with invalid magic bytes should return 400 Bad Request")
    void testUploadSpoofedMimeType() throws Exception {
        Patient patient = createPatient(patientUserA, "PAT-DOC-005");
        IntakeSession session = createSessionWithConsent(patient, tokenPatientA);

        // Claims to be PDF, but header is plain text
        byte[] fakePdfBytes = "THIS IS AN EXECUTABLE NOT A PDF".getBytes(StandardCharsets.UTF_8);

        MockMultipartFile spoofedFile = new MockMultipartFile(
                "file",
                "malicious.pdf",
                "application/pdf",
                fakePdfBytes
        );

        mockMvc.perform(multipart("/api/intake/sessions/{sessionId}/documents", session.getId())
                        .file(spoofedFile)
                        .param("documentType", "PRESCRIPTION")
                        .header("Authorization", "Bearer " + tokenPatientA))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Invalid document format: File signature does not match supported PDF or Image types"));
    }

    @Test
    @DisplayName("File size exceeding 10MB limit should return 400 Bad Request")
    void testUploadExceedsFileSizeLimit() throws Exception {
        Patient patient = createPatient(patientUserA, "PAT-DOC-006");
        IntakeSession session = createSessionWithConsent(patient, tokenPatientA);

        // Header valid, but oversized
        byte[] oversizedBytes = new byte[10 * 1024 * 1024 + 10];
        System.arraycopy(VALID_PDF_BYTES, 0, oversizedBytes, 0, VALID_PDF_BYTES.length);

        MockMultipartFile oversizedFile = new MockMultipartFile(
                "file",
                "giant.pdf",
                "application/pdf",
                oversizedBytes
        );

        mockMvc.perform(multipart("/api/intake/sessions/{sessionId}/documents", session.getId())
                        .file(oversizedFile)
                        .param("documentType", "PRESCRIPTION")
                        .header("Authorization", "Bearer " + tokenPatientA))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Document exceeds maximum allowed upload size (10MB)"));
    }

    @Test
    @DisplayName("Filename with path traversal attempts should be sanitized safely")
    void testFilenamePathTraversalSanitized() throws Exception {
        Patient patient = createPatient(patientUserA, "PAT-DOC-007");
        IntakeSession session = createSessionWithConsent(patient, tokenPatientA);

        MockMultipartFile traversalFile = new MockMultipartFile(
                "file",
                "../../../../etc/passwd_report.pdf",
                "application/pdf",
                VALID_PDF_BYTES
        );

        MvcResult result = mockMvc.perform(multipart("/api/intake/sessions/{sessionId}/documents", session.getId())
                        .file(traversalFile)
                        .param("documentType", "MEDICAL_RECORD")
                        .header("Authorization", "Bearer " + tokenPatientA))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.fileName").value("passwd_report.pdf"))
                .andReturn();

        JsonNode jsonNode = objectMapper.readTree(result.getResponse().getContentAsString());
        UUID docId = UUID.fromString(jsonNode.path("data").path("id").asText());

        MedicalDocument doc = medicalDocumentRepository.findById(docId).orElseThrow();
        assertThat(doc.getFileName()).isEqualTo("passwd_report.pdf");
        assertThat(doc.getStorageReference()).doesNotContain("..");
    }

    // ==========================================
    // 3. LOW CONFIDENCE & FAILED OCR HANDLING
    // ==========================================

    @Test
    @DisplayName("Low confidence OCR document should be marked REVIEW_REQUIRED and LOW_CONFIDENCE")
    void testLowConfidenceOcrProcessing() throws Exception {
        Patient patient = createPatient(patientUserA, "PAT-DOC-008");
        IntakeSession session = createSessionWithConsent(patient, tokenPatientA);

        MockMultipartFile blurryFile = new MockMultipartFile(
                "file",
                "blurry_scan.pdf",
                "application/pdf",
                VALID_PDF_BYTES
        );

        mockMvc.perform(multipart("/api/intake/sessions/{sessionId}/documents", session.getId())
                        .file(blurryFile)
                        .param("documentType", "PRESCRIPTION")
                        .header("Authorization", "Bearer " + tokenPatientA))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.processingStatus").value("REVIEW_REQUIRED"))
                .andExpect(jsonPath("$.data.extraction.status").value("LOW_CONFIDENCE"))
                .andExpect(jsonPath("$.data.extraction.confidenceScore").value(0.45))
                .andExpect(jsonPath("$.data.extraction.uncertaintyFlags").isArray());
    }

    @Test
    @DisplayName("Failed OCR should mark document FAILED but source file remains preserved and downloadable")
    void testFailedOcrPreservesSourceDocument() throws Exception {
        Patient patient = createPatient(patientUserA, "PAT-DOC-009");
        IntakeSession session = createSessionWithConsent(patient, tokenPatientA);

        MockMultipartFile corruptFile = new MockMultipartFile(
                "file",
                "corrupt_damaged.pdf",
                "application/pdf",
                VALID_PDF_BYTES
        );

        MvcResult result = mockMvc.perform(multipart("/api/intake/sessions/{sessionId}/documents", session.getId())
                        .file(corruptFile)
                        .param("documentType", "DISCHARGE_SUMMARY")
                        .header("Authorization", "Bearer " + tokenPatientA))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.processingStatus").value("FAILED"))
                .andExpect(jsonPath("$.data.extraction").doesNotExist())
                .andReturn();

        UUID docId = UUID.fromString(objectMapper.readTree(result.getResponse().getContentAsString()).path("data").path("id").asText());

        // File stream should still be downloadable even if OCR failed!
        mockMvc.perform(get("/api/intake/sessions/{sessionId}/documents/{documentId}/file", session.getId(), docId)
                        .header("Authorization", "Bearer " + tokenPatientA))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Disposition", "inline; filename=\"corrupt_damaged.pdf\""));
    }

    // ==========================================
    // 4. RETRIEVAL & STREAMING TESTS
    // ==========================================

    @Test
    @DisplayName("Patient can retrieve own document metadata and binary stream")
    void testPatientRetrievalOwnDocument() throws Exception {
        Patient patient = createPatient(patientUserA, "PAT-DOC-010");
        IntakeSession session = createSessionWithConsent(patient, tokenPatientA);

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "my_lab.png",
                "image/png",
                VALID_PNG_BYTES
        );

        MvcResult uploadResult = mockMvc.perform(multipart("/api/intake/sessions/{sessionId}/documents", session.getId())
                        .file(file)
                        .param("documentType", "LAB_REPORT")
                        .header("Authorization", "Bearer " + tokenPatientA))
                .andExpect(status().isCreated())
                .andReturn();

        UUID docId = UUID.fromString(objectMapper.readTree(uploadResult.getResponse().getContentAsString()).path("data").path("id").asText());

        // Get document metadata via session route
        mockMvc.perform(get("/api/intake/sessions/{sessionId}/documents/{documentId}", session.getId(), docId)
                        .header("Authorization", "Bearer " + tokenPatientA))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(docId.toString()))
                .andExpect(jsonPath("$.data.fileName").value("my_lab.png"));

        // Get document metadata via direct document route
        mockMvc.perform(get("/api/documents/{documentId}", docId)
                        .header("Authorization", "Bearer " + tokenPatientA))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(docId.toString()));

        // Stream file binary via session route
        MvcResult streamResult = mockMvc.perform(get("/api/intake/sessions/{sessionId}/documents/{documentId}/file", session.getId(), docId)
                        .header("Authorization", "Bearer " + tokenPatientA))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Disposition", "inline; filename=\"my_lab.png\""))
                .andReturn();

        assertThat(streamResult.getResponse().getContentAsByteArray()).isEqualTo(VALID_PNG_BYTES);

        // Stream file binary via direct document route
        MvcResult directStreamResult = mockMvc.perform(get("/api/documents/{documentId}/file", docId)
                        .header("Authorization", "Bearer " + tokenPatientA))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Disposition", "inline; filename=\"my_lab.png\""))
                .andReturn();

        assertThat(directStreamResult.getResponse().getContentAsByteArray()).isEqualTo(VALID_PNG_BYTES);

        // Verify audit log for DOCUMENT_ACCESSED
        List<AuditLog> accessLogs = auditLogRepository.findByAction("DOCUMENT_ACCESSED");
        assertThat(accessLogs).isNotEmpty();
    }

    @Test
    @DisplayName("List documents for intake session returns all uploaded documents")
    void testListDocumentsForSession() throws Exception {
        Patient patient = createPatient(patientUserA, "PAT-DOC-011");
        IntakeSession session = createSessionWithConsent(patient, tokenPatientA);

        MockMultipartFile file1 = new MockMultipartFile("file", "rx1.pdf", "application/pdf", VALID_PDF_BYTES);
        MockMultipartFile file2 = new MockMultipartFile("file", "rx2.pdf", "application/pdf", VALID_PDF_BYTES);

        mockMvc.perform(multipart("/api/intake/sessions/{sessionId}/documents", session.getId())
                .file(file1).param("documentType", "PRESCRIPTION").header("Authorization", "Bearer " + tokenPatientA));

        mockMvc.perform(multipart("/api/intake/sessions/{sessionId}/documents", session.getId())
                .file(file2).param("documentType", "LAB_REPORT").header("Authorization", "Bearer " + tokenPatientA));

        mockMvc.perform(get("/api/intake/sessions/{sessionId}/documents", session.getId())
                        .header("Authorization", "Bearer " + tokenPatientA))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(2));
    }

    // ==========================================
    // 5. SECURITY, AUTHORIZATION & ACCESS CONTROL
    // ==========================================

    @Test
    @DisplayName("Cross-patient upload is forbidden (403)")
    void testCrossPatientUploadForbidden() throws Exception {
        Patient patientA = createPatient(patientUserA, "PAT-DOC-012");
        IntakeSession sessionA = createSessionWithConsent(patientA, tokenPatientA);

        MockMultipartFile file = new MockMultipartFile("file", "rx.pdf", "application/pdf", VALID_PDF_BYTES);

        // Patient B attempts to upload into Patient A's intake session
        mockMvc.perform(multipart("/api/intake/sessions/{sessionId}/documents", sessionA.getId())
                        .file(file)
                        .param("documentType", "PRESCRIPTION")
                        .header("Authorization", "Bearer " + tokenPatientB))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Cross-patient retrieval is forbidden (403)")
    void testCrossPatientRetrievalForbidden() throws Exception {
        Patient patientA = createPatient(patientUserA, "PAT-DOC-013");
        IntakeSession sessionA = createSessionWithConsent(patientA, tokenPatientA);

        MockMultipartFile file = new MockMultipartFile("file", "rx.pdf", "application/pdf", VALID_PDF_BYTES);

        MvcResult uploadResult = mockMvc.perform(multipart("/api/intake/sessions/{sessionId}/documents", sessionA.getId())
                        .file(file)
                        .param("documentType", "PRESCRIPTION")
                        .header("Authorization", "Bearer " + tokenPatientA))
                .andExpect(status().isCreated())
                .andReturn();

        UUID docId = UUID.fromString(objectMapper.readTree(uploadResult.getResponse().getContentAsString()).path("data").path("id").asText());

        // Patient B attempts to retrieve Patient A's document metadata
        mockMvc.perform(get("/api/intake/sessions/{sessionId}/documents/{documentId}", sessionA.getId(), docId)
                        .header("Authorization", "Bearer " + tokenPatientB))
                .andExpect(status().isForbidden());

        // Patient B attempts to download Patient A's file binary
        mockMvc.perform(get("/api/intake/sessions/{sessionId}/documents/{documentId}/file", sessionA.getId(), docId)
                        .header("Authorization", "Bearer " + tokenPatientB))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Unauthenticated document access returns 401")
    void testUnauthenticatedAccessReturns401() throws Exception {
        mockMvc.perform(get("/api/intake/sessions/{sessionId}/documents", UUID.randomUUID()))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Doctor can view patient document and download binary")
    void testDoctorCanAccessPatientDocument() throws Exception {
        Patient patientA = createPatient(patientUserA, "PAT-DOC-014");
        IntakeSession sessionA = createSessionWithConsent(patientA, tokenPatientA);

        MockMultipartFile file = new MockMultipartFile("file", "doctor_review.pdf", "application/pdf", VALID_PDF_BYTES);

        MvcResult uploadResult = mockMvc.perform(multipart("/api/intake/sessions/{sessionId}/documents", sessionA.getId())
                        .file(file)
                        .param("documentType", "PRESCRIPTION")
                        .header("Authorization", "Bearer " + tokenPatientA))
                .andExpect(status().isCreated())
                .andReturn();

        UUID docId = UUID.fromString(objectMapper.readTree(uploadResult.getResponse().getContentAsString()).path("data").path("id").asText());

        // Doctor views metadata
        mockMvc.perform(get("/api/intake/sessions/{sessionId}/documents/{documentId}", sessionA.getId(), docId)
                        .header("Authorization", "Bearer " + tokenDoctor))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(docId.toString()));

        // Doctor streams binary
        mockMvc.perform(get("/api/intake/sessions/{sessionId}/documents/{documentId}/file", sessionA.getId(), docId)
                        .header("Authorization", "Bearer " + tokenDoctor))
                .andExpect(status().isOk());
    }

    // ==========================================
    // 6. SESSION STATE & CONSENT RULES
    // ==========================================

    @Test
    @DisplayName("Upload rejected if session does not have active consent (400)")
    void testUploadWithoutConsentRejected() throws Exception {
        Patient patient = createPatient(patientUserA, "PAT-DOC-015");

        // Create session WITHOUT consent
        IntakeSessionRequest sessionRequest = new IntakeSessionRequest(patient.getId(), "en");
        MvcResult sessionResult = mockMvc.perform(post("/api/intake/sessions")
                        .header("Authorization", "Bearer " + tokenPatientA)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sessionRequest)))
                .andExpect(status().isCreated())
                .andReturn();

        JsonNode sessionJson = objectMapper.readTree(sessionResult.getResponse().getContentAsString());
        UUID sessionId = UUID.fromString(sessionJson.path("data").path("id").asText());

        MockMultipartFile file = new MockMultipartFile("file", "no_consent.pdf", "application/pdf", VALID_PDF_BYTES);

        mockMvc.perform(multipart("/api/intake/sessions/{sessionId}/documents", sessionId)
                        .file(file)
                        .param("documentType", "PRESCRIPTION")
                        .header("Authorization", "Bearer " + tokenPatientA))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Clinical intake cannot proceed without valid consent"));
    }

    @Test
    @DisplayName("Upload rejected if session is COMPLETED (409 Conflict)")
    void testUploadToCompletedSessionRejected() throws Exception {
        Patient patient = createPatient(patientUserA, "PAT-DOC-016");
        IntakeSession session = createSessionWithConsent(patient, tokenPatientA);

        // Mark session completed
        session.setStatus(IntakeSessionStatus.COMPLETED);
        intakeSessionRepository.save(session);

        MockMultipartFile file = new MockMultipartFile("file", "post_complete.pdf", "application/pdf", VALID_PDF_BYTES);

        mockMvc.perform(multipart("/api/intake/sessions/{sessionId}/documents", session.getId())
                        .file(file)
                        .param("documentType", "PRESCRIPTION")
                        .header("Authorization", "Bearer " + tokenPatientA))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("Intake session is already completed and cannot be modified"));
    }

    // ==========================================
    // 7. ARCHITECTURAL / BACKWARD COMPATIBILITY
    // ==========================================

    @Test
    @DisplayName("Phase 1 DocumentService and OcrService interfaces remain fully functional")
    void testArchitecturalInterfacesCompatibility() throws Exception {
        Patient patient = createPatient(patientUserA, "PAT-DOC-017");

        // Invoke legacy DocumentService.uploadDocument
        UUID docId = legacyDocumentService.uploadDocument(patient.getId(), "legacy_test.pdf", "application/pdf", VALID_PDF_BYTES);
        assertThat(docId).isNotNull();

        // Invoke legacy DocumentService.getDocumentContent
        byte[] retrievedBytes = legacyDocumentService.getDocumentContent(docId);
        assertThat(retrievedBytes).isEqualTo(VALID_PDF_BYTES);

        // Invoke legacy OcrService.extractDocumentText
        var ocrResultMap = legacyOcrService.extractDocumentText(docId, VALID_PDF_BYTES);
        assertThat(ocrResultMap).isNotNull();
        assertThat(ocrResultMap.get("status")).isEqualTo("SUCCESS");
    }
}
