package com.medikiosk.controller;

import com.medikiosk.dto.ApiResponse;
import com.medikiosk.dto.voice.SpeechSynthesisRequest;
import com.medikiosk.dto.voice.TranscriptionResponse;
import com.medikiosk.entity.User;
import com.medikiosk.service.VoiceService;
import com.medikiosk.voice.provider.AudioSynthesisResult;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

/**
 * Controller for voice interactions: speech-to-text transcription and text-to-speech synthesis.
 * Conforms to docs/API_SPEC.md and docs/FEATURES.md (F-005, F-006, F-007).
 * Voice is purely an input method; it does not make clinical inferences or diagnoses.
 */
@RestController
@RequestMapping("/api/voice")
public class VoiceController {

    private final VoiceService voiceService;

    public VoiceController(VoiceService voiceService) {
        this.voiceService = voiceService;
    }

    /**
     * Transcribes patient audio input for an active intake session.
     * Audio is validated and processed ephemerally. The transcript is returned
     * for patient review and must be confirmed before saving.
     */
    @PostMapping(value = "/transcribe", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAnyRole('PATIENT', 'DOCTOR', 'ADMIN')")
    public ResponseEntity<ApiResponse<TranscriptionResponse>> transcribeAudio(
            @RequestParam("sessionId") UUID sessionId,
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "language", required = false) String language,
            Authentication authentication) {
        User user = getUserFromAuth(authentication);
        TranscriptionResponse response = voiceService.transcribeAudio(sessionId, file, language, user);
        return ResponseEntity.ok(ApiResponse.success(response, "Audio transcribed successfully. Please confirm transcript before saving."));
    }

    /**
     * Alias endpoint supporting session-scoped URL routing.
     */
    @PostMapping(value = "/sessions/{sessionId}/transcribe", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAnyRole('PATIENT', 'DOCTOR', 'ADMIN')")
    public ResponseEntity<ApiResponse<TranscriptionResponse>> transcribeAudioSessionScoped(
            @PathVariable("sessionId") UUID sessionId,
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "language", required = false) String language,
            Authentication authentication) {
        User user = getUserFromAuth(authentication);
        TranscriptionResponse response = voiceService.transcribeAudio(sessionId, file, language, user);
        return ResponseEntity.ok(ApiResponse.success(response, "Audio transcribed successfully. Please confirm transcript before saving."));
    }

    /**
     * Synthesizes approved UI or question text into spoken audio.
     * Does NOT generate autonomous clinical advice.
     */
    @PostMapping(value = "/synthesize", consumes = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasAnyRole('PATIENT', 'DOCTOR', 'ADMIN')")
    public ResponseEntity<byte[]> synthesizeSpeech(
            @Valid @RequestBody SpeechSynthesisRequest request,
            Authentication authentication) {
        User user = getUserFromAuth(authentication);
        AudioSynthesisResult result = voiceService.synthesizeSpeech(request.getSessionId(), request, user);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType(result.getContentType()));
        headers.setContentLength(result.getAudioData().length);
        headers.set(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"speech_prompt.wav\"");
        headers.set(HttpHeaders.CACHE_CONTROL, "no-cache, no-store, must-revalidate");

        return new ResponseEntity<>(result.getAudioData(), headers, HttpStatus.OK);
    }

    private User getUserFromAuth(Authentication authentication) {
        if (authentication != null && authentication.getPrincipal() instanceof User user) {
            return user;
        }
        return null;
    }
}
