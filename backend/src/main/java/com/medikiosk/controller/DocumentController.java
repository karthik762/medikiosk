package com.medikiosk.controller;

import com.medikiosk.dto.ApiResponse;
import com.medikiosk.dto.document.DocumentUploadResponse;
import com.medikiosk.entity.User;
import com.medikiosk.entity.enums.DocumentType;
import com.medikiosk.service.DocumentProcessingService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

/**
 * Controller exposing document upload, retrieval, and secure binary streaming endpoints.
 * Conforms to docs/PRODUCT_SPEC.md, docs/API_SPEC.md, and docs/SECURITY.md.
 */
@RestController
public class DocumentController {

    private final DocumentProcessingService documentProcessingService;

    public DocumentController(DocumentProcessingService documentProcessingService) {
        this.documentProcessingService = documentProcessingService;
    }

    @PostMapping(value = "/api/intake/sessions/{sessionId}/documents", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<DocumentUploadResponse>> uploadDocument(
            @PathVariable UUID sessionId,
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "documentType", required = false) DocumentType documentType,
            Authentication authentication) {

        User user = getUserFromAuth(authentication);
        DocumentUploadResponse response = documentProcessingService.uploadAndProcessDocument(sessionId, documentType, file, user);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "Document uploaded and processed successfully"));
    }

    @GetMapping("/api/intake/sessions/{sessionId}/documents")
    public ResponseEntity<ApiResponse<List<DocumentUploadResponse>>> getSessionDocuments(
            @PathVariable UUID sessionId,
            Authentication authentication) {

        User user = getUserFromAuth(authentication);
        List<DocumentUploadResponse> response = documentProcessingService.getSessionDocuments(sessionId, user);
        return ResponseEntity.ok(ApiResponse.success(response, "Session documents retrieved successfully"));
    }

    @GetMapping("/api/intake/sessions/{sessionId}/documents/{documentId}")
    public ResponseEntity<ApiResponse<DocumentUploadResponse>> getDocumentDetails(
            @PathVariable UUID sessionId,
            @PathVariable UUID documentId,
            Authentication authentication) {

        User user = getUserFromAuth(authentication);
        DocumentUploadResponse response = documentProcessingService.getDocumentDetails(sessionId, documentId, user);
        return ResponseEntity.ok(ApiResponse.success(response, "Document details retrieved successfully"));
    }

    @GetMapping("/api/intake/sessions/{sessionId}/documents/{documentId}/file")
    public ResponseEntity<byte[]> getDocumentFile(
            @PathVariable UUID sessionId,
            @PathVariable UUID documentId,
            Authentication authentication) {

        User user = getUserFromAuth(authentication);
        DocumentUploadResponse details = documentProcessingService.getDocumentDetails(sessionId, documentId, user);
        byte[] binary = documentProcessingService.getDocumentBinary(sessionId, documentId, user);

        MediaType mediaType = MediaType.APPLICATION_OCTET_STREAM;
        if (details.getFileType() != null) {
            try {
                mediaType = MediaType.parseMediaType(details.getFileType());
            } catch (Exception ignored) {
            }
        }

        return ResponseEntity.ok()
                .contentType(mediaType)
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + details.getFileName() + "\"")
                .body(binary);
    }

    @GetMapping("/api/documents/{documentId}")
    public ResponseEntity<ApiResponse<DocumentUploadResponse>> getDocumentDetailsDirect(
            @PathVariable UUID documentId,
            Authentication authentication) {

        User user = getUserFromAuth(authentication);
        DocumentUploadResponse response = documentProcessingService.getDocumentDetailsById(documentId, user);
        return ResponseEntity.ok(ApiResponse.success(response, "Document details retrieved successfully"));
    }

    @GetMapping("/api/documents/{documentId}/file")
    public ResponseEntity<byte[]> getDocumentFileDirect(
            @PathVariable UUID documentId,
            Authentication authentication) {

        User user = getUserFromAuth(authentication);
        DocumentUploadResponse details = documentProcessingService.getDocumentDetailsById(documentId, user);
        byte[] binary = documentProcessingService.getDocumentBinaryById(documentId, user);

        MediaType mediaType = MediaType.APPLICATION_OCTET_STREAM;
        if (details.getFileType() != null) {
            try {
                mediaType = MediaType.parseMediaType(details.getFileType());
            } catch (Exception ignored) {
            }
        }

        return ResponseEntity.ok()
                .contentType(mediaType)
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + details.getFileName() + "\"")
                .body(binary);
    }

    private User getUserFromAuth(Authentication authentication) {
        if (authentication != null && authentication.getPrincipal() instanceof User user) {
            return user;
        }
        return null;
    }
}
