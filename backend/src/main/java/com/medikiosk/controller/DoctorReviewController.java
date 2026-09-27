package com.medikiosk.controller;

import com.medikiosk.dto.ApiResponse;
import com.medikiosk.dto.clinical.ClinicalSummaryDto;
import com.medikiosk.dto.doctor.DoctorAmendmentRequest;
import com.medikiosk.dto.doctor.DoctorQueueItemDto;
import com.medikiosk.dto.doctor.DoctorReviewDetailDto;
import com.medikiosk.dto.doctor.DoctorVerificationRequest;
import com.medikiosk.entity.User;
import com.medikiosk.entity.enums.ClinicalSummaryStatus;
import com.medikiosk.entity.enums.TriagePriority;
import com.medikiosk.exception.ApiException;
import com.medikiosk.service.DoctorReviewService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

import com.medikiosk.dto.doctor.DoctorInteroperabilityStatusDto;

/**
 * Controller exposing doctor-facing endpoints for the consultation queue,
 * clinical review workstation, amendments, formal verification, and multi-system interoperability sync.
 * Strictly enforces server-side ROLE_DOCTOR or ROLE_ADMIN authorization.
 */
@RestController
@RequestMapping("/api/doctor")
public class DoctorReviewController {

    private final DoctorReviewService doctorReviewService;

    public DoctorReviewController(DoctorReviewService doctorReviewService) {
        this.doctorReviewService = doctorReviewService;
    }

    /**
     * Retrieves the consultation queue ordered by triage priority and creation time.
     */
    @GetMapping("/queue")
    public ResponseEntity<ApiResponse<List<DoctorQueueItemDto>>> getConsultationQueue(
            @RequestParam(value = "priority", required = false) TriagePriority priority,
            @RequestParam(value = "status", required = false) ClinicalSummaryStatus status,
            Authentication authentication) {

        User doctor = getUserFromAuth(authentication);
        List<DoctorQueueItemDto> queue = doctorReviewService.getConsultationQueue(priority, status, doctor);
        return ResponseEntity.ok(ApiResponse.success(queue, "Consultation queue retrieved successfully"));
    }

    /**
     * Retrieves the comprehensive clinical review workstation payload for an intake session.
     */
    @GetMapping("/sessions/{sessionId}/review")
    public ResponseEntity<ApiResponse<DoctorReviewDetailDto>> getCaseReviewDetails(
            @PathVariable UUID sessionId,
            Authentication authentication) {

        User doctor = getUserFromAuth(authentication);
        DoctorReviewDetailDto detail = doctorReviewService.getCaseReviewDetails(sessionId, doctor);
        return ResponseEntity.ok(ApiResponse.success(detail, "Case review details retrieved successfully"));
    }

    /**
     * Records physician amendments to the pre-consult summary.
     * Enforces optimistic concurrency control.
     */
    @PutMapping("/sessions/{sessionId}/summary/amend")
    public ResponseEntity<ApiResponse<ClinicalSummaryDto>> amendSummary(
            @PathVariable UUID sessionId,
            @Valid @RequestBody DoctorAmendmentRequest request,
            Authentication authentication) {

        User doctor = getUserFromAuth(authentication);
        ClinicalSummaryDto updated = doctorReviewService.amendClinicalSummary(sessionId, request, doctor);
        return ResponseEntity.ok(ApiResponse.success(updated, "Clinical summary amended successfully"));
    }

    /**
     * Marks the clinical summary as formally doctor verified.
     */
    @PostMapping("/sessions/{sessionId}/summary/verify")
    public ResponseEntity<ApiResponse<ClinicalSummaryDto>> verifySummary(
            @PathVariable UUID sessionId,
            @Valid @RequestBody DoctorVerificationRequest request,
            Authentication authentication) {

        User doctor = getUserFromAuth(authentication);
        ClinicalSummaryDto verified = doctorReviewService.verifyClinicalSummary(sessionId, request, doctor);
        return ResponseEntity.ok(ApiResponse.success(verified, "Clinical summary verified successfully"));
    }

    /**
     * Retrieves multi-system interoperability sync status for an intake session.
     */
    @GetMapping("/sessions/{sessionId}/integrations")
    public ResponseEntity<ApiResponse<DoctorInteroperabilityStatusDto>> getInteroperabilityStatus(
            @PathVariable UUID sessionId,
            Authentication authentication) {

        User doctor = getUserFromAuth(authentication);
        DoctorInteroperabilityStatusDto status = doctorReviewService.getInteroperabilityStatus(sessionId, doctor);
        return ResponseEntity.ok(ApiResponse.success(status, "Interoperability status retrieved successfully"));
    }

    /**
     * Manually triggers multi-system interoperability synchronization across FHIR R4, ABDM, HIS, and AWS HealthLake.
     */
    @PostMapping("/sessions/{sessionId}/sync-integrations")
    public ResponseEntity<ApiResponse<DoctorInteroperabilityStatusDto>> syncInteroperability(
            @PathVariable UUID sessionId,
            Authentication authentication) {

        User doctor = getUserFromAuth(authentication);
        DoctorInteroperabilityStatusDto synced = doctorReviewService.syncInteroperability(sessionId, doctor);
        return ResponseEntity.ok(ApiResponse.success(synced, "Interoperability synchronization executed successfully"));
    }

    private User getUserFromAuth(Authentication authentication) {
        if (authentication == null || !(authentication.getPrincipal() instanceof User)) {
            throw new ApiException("Authentication required", HttpStatus.UNAUTHORIZED);
        }
        return (User) authentication.getPrincipal();
    }
}
