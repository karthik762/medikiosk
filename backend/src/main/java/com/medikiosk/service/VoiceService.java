package com.medikiosk.service;

import com.medikiosk.dto.voice.SpeechSynthesisRequest;
import com.medikiosk.dto.voice.TranscriptionResponse;
import com.medikiosk.entity.User;
import com.medikiosk.voice.provider.AudioSynthesisResult;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

/**
 * Service interface for voice input processing (STT) and speech synthesis (TTS).
 * Conforms to docs/API_SPEC.md and docs/FEATURES.md (F-005, F-006, F-007).
 * Voice is purely an input method; it does NOT make clinical conclusions or diagnoses.
 */
public interface VoiceService {

    /**
     * Transcribes patient audio input for an active intake session.
     * Audio is validated, processed ephemerally, and discarded.
     *
     * @param sessionId         Intake session ID.
     * @param audioFile         Uploaded audio file.
     * @param language          Optional language code (e.g. en, hi, te).
     * @param authenticatedUser The calling authenticated user.
     * @return Transcription response with text, confidence, language, and status.
     */
    TranscriptionResponse transcribeAudio(UUID sessionId, MultipartFile audioFile, String language, User authenticatedUser);

    /**
     * Synthesizes approved UI or question text into spoken audio.
     *
     * @param sessionId         Optional intake session ID.
     * @param request           Speech synthesis request with text and language.
     * @param authenticatedUser The calling authenticated user.
     * @return Audio synthesis result with audio bytes and content type.
     */
    AudioSynthesisResult synthesizeSpeech(UUID sessionId, SpeechSynthesisRequest request, User authenticatedUser);
}
