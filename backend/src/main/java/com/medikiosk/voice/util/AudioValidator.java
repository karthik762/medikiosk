package com.medikiosk.voice.util;

import com.medikiosk.exception.ApiException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Set;

/**
 * Validates audio file uploads for presence, size, MIME type, and magic bytes.
 * Prevents arbitrary, non-audio or malicious file uploads.
 */
public final class AudioValidator {

    private static final Logger log = LoggerFactory.getLogger(AudioValidator.class);

    public static final long DEFAULT_MAX_AUDIO_SIZE_BYTES = 10L * 1024L * 1024L; // 10MB

    private static final Set<String> ALLOWED_MIME_TYPES = Set.of(
            "audio/wav",
            "audio/wave",
            "audio/x-wav",
            "audio/webm",
            "audio/mpeg",
            "audio/mp3",
            "audio/ogg",
            "audio/mp4",
            "audio/m4a",
            "audio/x-m4a",
            "audio/aac",
            "audio/x-aac"
    );

    private AudioValidator() {
        // Prevent instantiation
    }

    /**
     * Validates an uploaded audio file.
     *
     * @param file             Uploaded multipart file.
     * @param maxSizeBytes     Maximum allowed size in bytes.
     * @throws ApiException if validation fails with HTTP 400.
     */
    public static void validateAudio(MultipartFile file, long maxSizeBytes) {
        if (file == null || file.isEmpty()) {
            throw new ApiException("Audio file is required and cannot be empty", HttpStatus.BAD_REQUEST);
        }

        if (file.getSize() > maxSizeBytes) {
            log.warn("Audio upload rejected: File size {} exceeds limit {}", file.getSize(), maxSizeBytes);
            throw new ApiException("Audio file exceeds maximum allowed upload size (10MB)", HttpStatus.BAD_REQUEST);
        }

        String contentType = file.getContentType();
        if (contentType == null || !isAllowedMimeType(contentType)) {
            log.warn("Audio upload rejected: Unsupported Content-Type '{}'", contentType);
            throw new ApiException("Unsupported audio format: " + contentType + ". Supported formats: WAV, WebM, MP3, OGG, MP4/M4A, AAC.", HttpStatus.BAD_REQUEST);
        }

        // Server-side magic bytes validation (do not trust client MIME type alone)
        validateMagicBytes(file);
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
            log.error("Failed to read audio file header", e);
            throw new ApiException("Failed to inspect audio file header", HttpStatus.BAD_REQUEST);
        }

        if (bytesRead < 4) {
            throw new ApiException("Audio file is malformed or too short", HttpStatus.BAD_REQUEST);
        }

        if (!matchesAudioSignature(header, bytesRead)) {
            log.warn("Audio upload rejected: Magic bytes do not match supported audio signature");
            throw new ApiException("Unsupported or invalid audio format: File signature does not match supported audio types", HttpStatus.BAD_REQUEST);
        }
    }

    private static boolean matchesAudioSignature(byte[] header, int bytesRead) {
        // 1. WAV: starts with "RIFF" (bytes 0-3) and "WAVE" (bytes 8-11)
        if (bytesRead >= 12 &&
                header[0] == 'R' && header[1] == 'I' && header[2] == 'F' && header[3] == 'F' &&
                header[8] == 'W' && header[9] == 'A' && header[10] == 'V' && header[11] == 'E') {
            return true;
        }

        // 2. WebM / Matroska: EBML header 0x1A 0x45 0xDF 0xA3
        if (bytesRead >= 4 &&
                (header[0] & 0xFF) == 0x1A &&
                (header[1] & 0xFF) == 0x45 &&
                (header[2] & 0xFF) == 0xDF &&
                (header[3] & 0xFF) == 0xA3) {
            return true;
        }

        // 3. MP3 with ID3v2 tag: "ID3" (0x49 0x44 0x33)
        if (bytesRead >= 3 &&
                header[0] == 'I' && header[1] == 'D' && header[2] == '3') {
            return true;
        }

        // 4. MP3 frame sync: 0xFF 0xFB, 0xFF 0xF3, 0xFF 0xF2, or 0xFF 0xFA
        if (bytesRead >= 2 &&
                (header[0] & 0xFF) == 0xFF &&
                ((header[1] & 0xFF) >= 0xE0)) {
            return true;
        }

        // 5. OGG: "OggS" (0x4F 0x67 0x67 0x53)
        if (bytesRead >= 4 &&
                header[0] == 'O' && header[1] == 'g' && header[2] == 'g' && header[3] == 'S') {
            return true;
        }

        // 6. MP4/M4A: contains "ftyp" in bytes 4-7
        if (bytesRead >= 8 &&
                header[4] == 'f' && header[5] == 't' && header[6] == 'y' && header[7] == 'p') {
            return true;
        }

        // 7. AAC ADTS: 0xFF 0xF1 or 0xFF 0xF9
        if (bytesRead >= 2 &&
                (header[0] & 0xFF) == 0xFF &&
                ((header[1] & 0xF6) == 0xF0)) {
            return true;
        }

        return false;
    }
}
