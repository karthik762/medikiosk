package com.medikiosk.controller;

import com.medikiosk.dto.ApiResponse;
import com.medikiosk.dto.clinical.ClinicalSummaryDto;
import com.medikiosk.entity.User;
import com.medikiosk.service.ClinicalSummaryService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

/**
 * REST controller exposing endpoints for generating and retrieving AI clinical pre-consult summaries.
 * Conforms to docs/AI_SPEC.md, docs/API_SPEC.md, and docs/SECURITY.md.
 */
@RestController
public class ClinicalSummaryController {

    private final ClinicalSummaryService clinicalSummaryService;

    public ClinicalSummaryController(ClinicalSummaryService clinicalSummaryService) {
        this.clinicalSummaryService = clinicalSummaryService;
    }

    @PostMapping({"/api/intake/sessions/{sessionId}/clinical-summary/generate", "/api/sessions/{sessionId}/clinical-summary/generate"})
    public ResponseEntity<ApiResponse<ClinicalSummaryDto>> generateClinicalSummary(
            @PathVariable UUID sessionId,
            Authentication authentication) {

        User user = getUserFromAuth(authentication);
        ClinicalSummaryDto summary = clinicalSummaryService.generateSummary(sessionId, user);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(summary, "Clinical summary generated successfully"));
    }

    @GetMapping({"/api/intake/sessions/{sessionId}/clinical-summary", "/api/sessions/{sessionId}/clinical-summary"})
    public ResponseEntity<ApiResponse<ClinicalSummaryDto>> getSessionClinicalSummary(
            @PathVariable UUID sessionId,
            Authentication authentication) {

        User user = getUserFromAuth(authentication);
        ClinicalSummaryDto summary = clinicalSummaryService.getSummaryBySessionId(sessionId, user);
        return ResponseEntity.ok(ApiResponse.success(summary, "Clinical summary retrieved successfully"));
    }

    @GetMapping("/api/clinical-summaries/{summaryId}")
    public ResponseEntity<ApiResponse<ClinicalSummaryDto>> getClinicalSummaryById(
            @PathVariable UUID summaryId,
            Authentication authentication) {

        User user = getUserFromAuth(authentication);
        ClinicalSummaryDto summary = clinicalSummaryService.getSummaryById(summaryId, user);
        return ResponseEntity.ok(ApiResponse.success(summary, "Clinical summary retrieved successfully"));
    }

    @GetMapping({"/api/clinical-summary/{patientId}", "/api/patients/{patientId}/clinical-summary"})
    public ResponseEntity<ApiResponse<ClinicalSummaryDto>> getPatientClinicalSummary(
            @PathVariable UUID patientId,
            Authentication authentication) {

        User user = getUserFromAuth(authentication);
        ClinicalSummaryDto summary = clinicalSummaryService.getSummaryByPatientId(patientId, user);
        return ResponseEntity.ok(ApiResponse.success(summary, "Clinical summary retrieved successfully"));
    }

    private User getUserFromAuth(Authentication authentication) {
        if (authentication != null && authentication.getPrincipal() instanceof User user) {
            return user;
        }
        return null;
    }
}
