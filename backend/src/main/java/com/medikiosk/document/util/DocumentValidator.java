package com.medikiosk.document.util;

import com.medikiosk.exception.ApiException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.Set;

/**
 * Server-side validation utility for uploaded medical documents.
 * Validates file presence, size limits, allowed MIME types, magic byte signatures, and filename sanitization.
 * Conforms to docs/SECURITY.md and docs/DATABASE.md.
 */
public final class DocumentValidator {

    private static final Logger log = LoggerFactory.getLogger(DocumentValidator.class);

    public static final long DEFAULT_MAX_DOCUMENT_SIZE_BYTES = 10L * 1024L * 1024L; // 10MB

    private static final Set<String> ALLOWED_MIME_TYPES = Set.of(
            "application/pdf",
            "image/jpeg",
            "image/jpg",
            "image/png",
            "image/webp"
    );

    private DocumentValidator() {
        // Prevent instantiation
    }

    /**
     * Validates an uploaded document file for presence, size, MIME type, and magic bytes.
     *
     * @param file         Multipart file.
     * @param maxSizeBytes Maximum permitted size in bytes.
     */
    public static void validateDocument(MultipartFile file, long maxSizeBytes) {
        if (file == null || file.isEmpty()) {
            throw new ApiException("Document file is required and cannot be empty", HttpStatus.BAD_REQUEST);
        }

        if (file.getSize() > maxSizeBytes) {
            log.warn("Document rejected: File size {} exceeds max limit of {}", file.getSize(), maxSizeBytes);
            throw new ApiException("Document exceeds maximum allowed upload size (10MB)", HttpStatus.BAD_REQUEST);
        }

        String rawContentType = file.getContentType();
        if (rawContentType == null || !isAllowedMimeType(rawContentType)) {
            log.warn("Document rejected: Unsupported Content-Type '{}'", rawContentType);
            throw new ApiException("Unsupported document format: " + rawContentType + ". Supported formats: PDF, JPEG, PNG, WebP.", HttpStatus.BAD_REQUEST);
        }

        // Verify magic bytes
        validateMagicBytes(file);
    }

    /**
     * Sanitizes original filename to prevent path traversal or unsafe file system behavior.
     */
    public static String sanitizeFilename(String filename) {
        if (filename == null || filename.isBlank()) {
            return "document.bin";
        }
        // Extract base filename without directories
        String baseName = Paths.get(filename).getFileName().toString();
        // Remove suspicious characters
        String cleaned = baseName.replaceAll("[^a-zA-Z0-9._-]", "_");
        if (cleaned.isBlank()) {
            return "document.bin";
        }
        return cleaned;
    }

    /**
     * Computes the SHA-256 hash of the binary file content for integrity tracking.
     */
    public static String calculateSha256(byte[] content) {
        if (content == null || content.length == 0) {
            return null;
        }
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(content);
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException e) {
            log.error("SHA-256 digest algorithm not available", e);
            throw new ApiException("Failed to compute document integrity hash", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    private static boolean isAllowedMimeType(String contentType) {
        String cleanMime = contentType.toLowerCase().split(";")[0].trim();
        return ALLOWED_MIME_TYPES.contains(cleanMime);
    }

    private static void validateMagicBytes(MultipartFile file) {
        byte[] header = new byte[32];
        int bytesRead;
        try (InputStream is = file.getInputStream()) {
            bytesRead = is.read(header);
        } catch (IOException e) {
            log.error("Failed to read document header for magic byte verification", e);
            throw new ApiException("Failed to inspect document file header", HttpStatus.BAD_REQUEST);
        }

        if (bytesRead < 4) {
            throw new ApiException("Document file is corrupted or too short", HttpStatus.BAD_REQUEST);
        }

        if (!matchesDocumentSignature(header, bytesRead)) {
            log.warn("Document upload rejected: Magic bytes signature does not match allowed types");
            throw new ApiException("Invalid document format: File signature does not match supported PDF or Image types", HttpStatus.BAD_REQUEST);
        }
    }

    private static boolean matchesDocumentSignature(byte[] header, int bytesRead) {
        // 1. PDF: starts with "%PDF-" (0x25, 0x50, 0x44, 0x46, 0x2D)
        if (bytesRead >= 5 &&
                header[0] == 0x25 &&
                header[1] == 0x50 &&
                header[2] == 0x44 &&
                header[3] == 0x46 &&
                header[4] == 0x2D) {
            return true;
        }

        // 2. JPEG: starts with 0xFF, 0xD8, 0xFF
        if (bytesRead >= 3 &&
                (header[0] & 0xFF) == 0xFF &&
                (header[1] & 0xFF) == 0xD8 &&
                (header[2] & 0xFF) == 0xFF) {
            return true;
        }

        // 3. PNG: starts with 0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A
        if (bytesRead >= 8 &&
                (header[0] & 0xFF) == 0x89 &&
                header[1] == 'P' &&
                header[2] == 'N' &&
                header[3] == 'G' &&
                header[4] == 0x0D &&
                header[5] == 0x0A &&
                header[6] == 0x1A &&
                header[7] == 0x0A) {
            return true;
        }

        // 4. WebP: RIFF (bytes 0-3) and WEBP (bytes 8-11)
        if (bytesRead >= 12 &&
                header[0] == 'R' && header[1] == 'I' && header[2] == 'F' && header[3] == 'F' &&
                header[8] == 'W' && header[9] == 'E' && header[10] == 'B' && header[11] == 'P') {
            return true;
        }

        return false;
    }
}
