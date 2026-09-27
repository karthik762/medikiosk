package com.medikiosk.controller;

import com.medikiosk.dto.ApiResponse;
import com.medikiosk.dto.intake.AllergyRequest;
import com.medikiosk.dto.intake.AllergyResponse;
import com.medikiosk.dto.intake.AnswerRequest;
import com.medikiosk.dto.intake.AnswerResponse;
import com.medikiosk.dto.intake.ChiefComplaintRequest;
import com.medikiosk.dto.intake.ChiefComplaintResponse;
import com.medikiosk.dto.intake.ConsentRequest;
import com.medikiosk.dto.intake.ConsentResponse;
import com.medikiosk.dto.intake.IntakeSessionRequest;
import com.medikiosk.dto.intake.IntakeSessionResponse;
import com.medikiosk.dto.intake.MedicalHistoryRequest;
import com.medikiosk.dto.intake.MedicalHistoryResponse;
import com.medikiosk.dto.intake.MedicationRequest;
import com.medikiosk.dto.intake.MedicationResponse;
import com.medikiosk.dto.intake.QuestionResponse;
import com.medikiosk.dto.intake.SymptomRequest;
import com.medikiosk.dto.intake.SymptomResponse;
import com.medikiosk.entity.User;
import com.medikiosk.service.PatientIntakeService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/intake/sessions")
public class IntakeController {

    private final PatientIntakeService patientIntakeService;
    private final com.medikiosk.clinical.adaptive.AdaptiveQuestionService adaptiveQuestionService;

    public IntakeController(
            PatientIntakeService patientIntakeService,
            com.medikiosk.clinical.adaptive.AdaptiveQuestionService adaptiveQuestionService) {
        this.patientIntakeService = patientIntakeService;
        this.adaptiveQuestionService = adaptiveQuestionService;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<IntakeSessionResponse>> createSession(
            @Valid @RequestBody IntakeSessionRequest request,
            Authentication authentication) {
        User user = getUserFromAuth(authentication);
        IntakeSessionResponse response = patientIntakeService.createSession(request, user);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "Intake session created successfully"));
    }

    @GetMapping("/{sessionId}")
    public ResponseEntity<ApiResponse<IntakeSessionResponse>> getSession(
            @PathVariable UUID sessionId,
            Authentication authentication) {
        User user = getUserFromAuth(authentication);
        IntakeSessionResponse response = patientIntakeService.getSession(sessionId, user);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping("/{sessionId}/consent")
    public ResponseEntity<ApiResponse<ConsentResponse>> recordConsent(
            @PathVariable UUID sessionId,
            @Valid @RequestBody ConsentRequest request,
            Authentication authentication) {
        User user = getUserFromAuth(authentication);
        ConsentResponse response = patientIntakeService.recordConsent(sessionId, request, user);
        return ResponseEntity.ok(ApiResponse.success(response, "Consent recorded successfully"));
    }

    @PostMapping("/{sessionId}/complaint")
    public ResponseEntity<ApiResponse<ChiefComplaintResponse>> recordChiefComplaint(
            @PathVariable UUID sessionId,
            @Valid @RequestBody ChiefComplaintRequest request,
            Authentication authentication) {
        User user = getUserFromAuth(authentication);
        ChiefComplaintResponse response = patientIntakeService.recordChiefComplaint(sessionId, request, user);
        return ResponseEntity.ok(ApiResponse.success(response, "Chief complaint recorded successfully"));
    }

    @PostMapping("/{sessionId}/symptoms")
    public ResponseEntity<ApiResponse<SymptomResponse>> recordSymptom(
            @PathVariable UUID sessionId,
            @Valid @RequestBody SymptomRequest request,
            Authentication authentication) {
        User user = getUserFromAuth(authentication);
        SymptomResponse response = patientIntakeService.recordSymptom(sessionId, request, user);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "Symptom recorded successfully"));
    }

    @PostMapping("/{sessionId}/medical-history")
    public ResponseEntity<ApiResponse<MedicalHistoryResponse>> recordMedicalHistory(
            @PathVariable UUID sessionId,
            @Valid @RequestBody MedicalHistoryRequest request,
            Authentication authentication) {
        User user = getUserFromAuth(authentication);
        MedicalHistoryResponse response = patientIntakeService.recordMedicalHistory(sessionId, request, user);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "Medical history recorded successfully"));
    }

    @PostMapping("/{sessionId}/medications")
    public ResponseEntity<ApiResponse<MedicationResponse>> recordMedication(
            @PathVariable UUID sessionId,
            @Valid @RequestBody MedicationRequest request,
            Authentication authentication) {
        User user = getUserFromAuth(authentication);
        MedicationResponse response = patientIntakeService.recordMedication(sessionId, request, user);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "Medication recorded successfully"));
    }

    @PostMapping("/{sessionId}/allergies")
    public ResponseEntity<ApiResponse<AllergyResponse>> recordAllergy(
            @PathVariable UUID sessionId,
            @Valid @RequestBody AllergyRequest request,
            Authentication authentication) {
        User user = getUserFromAuth(authentication);
        AllergyResponse response = patientIntakeService.recordAllergy(sessionId, request, user);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "Allergy recorded successfully"));
    }

    @GetMapping("/{sessionId}/questions")
    public ResponseEntity<ApiResponse<List<QuestionResponse>>> getApplicableQuestions(
            @PathVariable UUID sessionId,
            Authentication authentication) {
        User user = getUserFromAuth(authentication);
        List<QuestionResponse> response = patientIntakeService.getApplicableQuestions(sessionId, user);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping("/{sessionId}/answers")
    public ResponseEntity<ApiResponse<AnswerResponse>> recordAnswer(
            @PathVariable UUID sessionId,
            @Valid @RequestBody AnswerRequest request,
            Authentication authentication) {
        User user = getUserFromAuth(authentication);
        AnswerResponse response = patientIntakeService.recordAnswer(sessionId, request, user);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "Answer recorded successfully"));
    }

    @GetMapping("/{sessionId}/next-question")
    public ResponseEntity<ApiResponse<com.medikiosk.clinical.adaptive.NextQuestionResponse>> getNextQuestion(
            @PathVariable UUID sessionId,
            Authentication authentication) {
        User user = getUserFromAuth(authentication);
        com.medikiosk.clinical.adaptive.NextQuestionResponse response = adaptiveQuestionService.getNextQuestion(sessionId, user);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/{sessionId}/clinical-state")
    public ResponseEntity<ApiResponse<com.medikiosk.clinical.state.ClinicalState>> getClinicalState(
            @PathVariable UUID sessionId,
            Authentication authentication) {
        User user = getUserFromAuth(authentication);
        com.medikiosk.clinical.state.ClinicalState response = adaptiveQuestionService.getClinicalState(sessionId, user);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping("/{sessionId}/complete")
    public ResponseEntity<ApiResponse<IntakeSessionResponse>> completeSession(
            @PathVariable UUID sessionId,
            Authentication authentication) {
        User user = getUserFromAuth(authentication);
        IntakeSessionResponse response = patientIntakeService.completeSession(sessionId, user);
        return ResponseEntity.ok(ApiResponse.success(response, "Intake session completed successfully"));
    }

    private User getUserFromAuth(Authentication authentication) {
        if (authentication != null && authentication.getPrincipal() instanceof User user) {
            return user;
        }
        return null;
    }
}
