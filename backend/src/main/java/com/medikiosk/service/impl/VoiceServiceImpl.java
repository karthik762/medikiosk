package com.medikiosk.service.impl;

import com.medikiosk.dto.voice.SpeechSynthesisRequest;
import com.medikiosk.dto.voice.TranscriptionResponse;
import com.medikiosk.entity.Consent;
import com.medikiosk.entity.enums.ConsentStatus;
import com.medikiosk.entity.enums.ConsentType;
import com.medikiosk.entity.IntakeSession;
import com.medikiosk.entity.enums.IntakeSessionStatus;
import com.medikiosk.entity.Patient;
import com.medikiosk.entity.User;
import com.medikiosk.entity.enums.UserRole;
import com.medikiosk.exception.ApiException;
import com.medikiosk.exception.ResourceNotFoundException;
import com.medikiosk.repository.ConsentRepository;
import com.medikiosk.repository.IntakeSessionRepository;
import com.medikiosk.service.VoiceService;
import com.medikiosk.voice.provider.AudioSynthesisResult;
import com.medikiosk.voice.provider.SpeechToTextProvider;
import com.medikiosk.voice.provider.TextToSpeechProvider;
import com.medikiosk.voice.provider.TranscriptionResult;
import com.medikiosk.voice.util.AudioValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.Optional;
import java.util.UUID;

/**
 * Service implementation for voice transcription (STT) and speech synthesis (TTS).
 * Conforms to docs/API_SPEC.md, docs/FEATURES.md (F-005, F-006, F-007), and docs/SECURITY.md.
 * Ensures data minimization: raw audio is ephemeral and never permanently persisted.
 */
@Service
@Transactional(readOnly = true)
public class VoiceServiceImpl implements VoiceService {

    private static final Logger log = LoggerFactory.getLogger(VoiceServiceImpl.class);

    private final IntakeSessionRepository intakeSessionRepository;
    private final ConsentRepository consentRepository;
    private final SpeechToTextProvider speechToTextProvider;
    private final TextToSpeechProvider textToSpeechProvider;
    private final long maxAudioSizeBytes;

    public VoiceServiceImpl(
            IntakeSessionRepository intakeSessionRepository,
            ConsentRepository consentRepository,
            SpeechToTextProvider speechToTextProvider,
            TextToSpeechProvider textToSpeechProvider,
            @Value("${app.voice.max-audio-size-bytes:10485760}") long maxAudioSizeBytes) {
        this.intakeSessionRepository = intakeSessionRepository;
        this.consentRepository = consentRepository;
        this.speechToTextProvider = speechToTextProvider;
        this.textToSpeechProvider = textToSpeechProvider;
        this.maxAudioSizeBytes = maxAudioSizeBytes;
    }

    @Override
    public TranscriptionResponse transcribeAudio(UUID sessionId, MultipartFile audioFile, String language, User authenticatedUser) {
        if (sessionId == null) {
            throw new ApiException("Session ID is required for audio transcription", HttpStatus.BAD_REQUEST);
        }

        IntakeSession session = intakeSessionRepository.findById(sessionId)
                .orElseThrow(() -> new ResourceNotFoundException("Intake session not found with id: " + sessionId));

        validateSessionAccess(session, authenticatedUser);
        verifySessionModifiable(session);
        verifyConsentGiven(sessionId);

        // Validate audio format, size, mime type, and magic bytes
        AudioValidator.validateAudio(audioFile, maxAudioSizeBytes);

        // Resolve target language: request param -> session language -> "en"
        String resolvedLanguage = resolveLanguage(language, session.getLanguage());

        // Process audio ephemerally without permanent storage
        Path tempAudioPath = null;
        try {
            tempAudioPath = Files.createTempFile("medikiosk_voice_", ".tmp");
            audioFile.transferTo(tempAudioPath.toFile());
            byte[] audioBytes = Files.readAllBytes(tempAudioPath);

            log.info("Processing voice transcription for session {} (size: {} bytes, language: {}, provider: {})",
                    sessionId, audioBytes.length, resolvedLanguage, speechToTextProvider.getProviderName());

            TranscriptionResult result = speechToTextProvider.transcribe(
                    audioBytes, audioFile.getContentType(), resolvedLanguage, Collections.emptyMap()
            );

            if (result.getStatus() == TranscriptionResult.Status.FAILED) {
                log.warn("STT provider returned failure for session {}: {}", sessionId, result.getErrorMessage());
                throw new ApiException("Speech-to-text processing failed: " +
                        (result.getErrorMessage() != null ? result.getErrorMessage() : "Provider unavailable"),
                        HttpStatus.SERVICE_UNAVAILABLE);
            }

            if (result.getStatus() == TranscriptionResult.Status.NO_SPEECH_DETECTED) {
                log.info("No speech detected for session {}", sessionId);
                return new TranscriptionResponse(
                        sessionId,
                        "",
                        java.math.BigDecimal.ZERO,
                        result.getLanguage(),
                        "NO_SPEECH_DETECTED",
                        result.isMock(),
                        speechToTextProvider.getProviderName(),
                        result.isMock() ? "MOCK" : "REAL"
                );
            }

            return new TranscriptionResponse(
                    sessionId,
                    result.getTranscript(),
                    result.getConfidence(),
                    result.getLanguage(),
                    "SUCCESS",
                    result.isMock(),
                    speechToTextProvider.getProviderName(),
                    result.isMock() ? "MOCK" : "REAL"
            );
        } catch (IOException e) {
            log.error("Failed to read audio file during transcription", e);
            throw new ApiException("Failed to process audio stream", HttpStatus.INTERNAL_SERVER_ERROR);
        } finally {
            if (tempAudioPath != null) {
                try {
                    Files.deleteIfExists(tempAudioPath);
                } catch (IOException ignored) {
                }
            }
        }
    }

    @Override
    public AudioSynthesisResult synthesizeSpeech(UUID sessionId, SpeechSynthesisRequest request, User authenticatedUser) {
        if (request == null || request.getText() == null || request.getText().trim().isEmpty()) {
            throw new ApiException("Text to synthesize is required and cannot be blank", HttpStatus.BAD_REQUEST);
        }

        String textToSynthesize = request.getText().trim();
        if (textToSynthesize.length() > 1000) {
            throw new ApiException("Text to synthesize exceeds maximum allowed length of 1000 characters", HttpStatus.BAD_REQUEST);
        }

        String defaultLanguage = "en";
        if (sessionId != null) {
            IntakeSession session = intakeSessionRepository.findById(sessionId)
                    .orElseThrow(() -> new ResourceNotFoundException("Intake session not found with id: " + sessionId));
            validateSessionAccess(session, authenticatedUser);
            if (session.getLanguage() != null && !session.getLanguage().isBlank()) {
                defaultLanguage = session.getLanguage();
            }
        }

        String resolvedLanguage = resolveLanguage(request.getLanguage(), defaultLanguage);

        log.info("Processing speech synthesis (length: {} chars, language: {}, provider: {})",
                textToSynthesize.length(), resolvedLanguage, textToSpeechProvider.getProviderName());

        AudioSynthesisResult result = textToSpeechProvider.synthesize(
                textToSynthesize, resolvedLanguage, Collections.emptyMap()
        );

        if (result.getStatus() == AudioSynthesisResult.Status.FAILED) {
            log.warn("TTS provider returned failure: {}", result.getErrorMessage());
            throw new ApiException("Speech synthesis failed: " +
                    (result.getErrorMessage() != null ? result.getErrorMessage() : "Provider unavailable"),
                    HttpStatus.SERVICE_UNAVAILABLE);
        }

        return result;
    }

    private String resolveLanguage(String requestedLanguage, String sessionLanguage) {
        if (requestedLanguage != null && !requestedLanguage.trim().isEmpty()) {
            return requestedLanguage.trim();
        }
        if (sessionLanguage != null && !sessionLanguage.trim().isEmpty()) {
            return sessionLanguage.trim();
        }
        return "en";
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
            log.warn("Voice intake operation blocked: Valid accepted GENERAL_INTAKE consent missing for session {}", sessionId);
            throw new ApiException("Clinical intake cannot proceed without valid consent", HttpStatus.BAD_REQUEST);
        }
    }
}
