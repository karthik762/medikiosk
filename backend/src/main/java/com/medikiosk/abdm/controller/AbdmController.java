package com.medikiosk.abdm.controller;

import com.medikiosk.abdm.dto.AbdmConsentRequestDto;
import com.medikiosk.abdm.dto.AbdmConsentResponseDto;
import com.medikiosk.abdm.dto.AbdmExchangeRequestDto;
import com.medikiosk.abdm.dto.AbdmExchangeResponseDto;
import com.medikiosk.abdm.dto.AbdmStatusDto;
import com.medikiosk.abdm.dto.AbhaVerificationRequestDto;
import com.medikiosk.abdm.dto.AbhaVerificationResponseDto;
import com.medikiosk.abdm.service.AbdmService;
import com.medikiosk.dto.ApiResponse;
import com.medikiosk.entity.IntegrationRecord;
import com.medikiosk.entity.User;
import com.medikiosk.entity.enums.UserRole;
import com.medikiosk.exception.ApiException;
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

/**
 * REST controller for Ayushman Bharat Digital Mission (ABDM) integration endpoints.
 * Conforms to docs/ABDM_SPEC.md, docs/API_SPEC.md, and docs/SECURITY.md.
 */
@RestController
@RequestMapping("/api/abdm")
public class AbdmController {

    private final AbdmService abdmService;

    public AbdmController(AbdmService abdmService) {
        this.abdmService = abdmService;
    }

    /**
     * Checks ABDM adapter operating mode and gateway connectivity.
     */
    @GetMapping("/status")
    public ResponseEntity<ApiResponse<AbdmStatusDto>> getStatus() {
        AbdmStatusDto status = abdmService.getStatus();
        return ResponseEntity.ok(ApiResponse.success(status, "ABDM status retrieved successfully"));
    }

    /**
     * Verifies patient ABHA number or address.
     */
    @PostMapping("/patients/{patientId}/abha/verify")
    public ResponseEntity<ApiResponse<AbhaVerificationResponseDto>> verifyPatientAbha(
            @PathVariable UUID patientId,
            @Valid @RequestBody AbhaVerificationRequestDto request,
            Authentication authentication) {

        User user = getUserFromAuth(authentication);
        AbhaVerificationResponseDto response = abdmService.verifyPatientAbha(patientId, request.getAbhaId(), user.getId());
        return ResponseEntity.ok(ApiResponse.success(response, response.getMessage()));
    }

    /**
     * Grants patient consent for ABDM data sharing.
     */
    @PostMapping("/sessions/{sessionId}/consent")
    public ResponseEntity<ApiResponse<AbdmConsentResponseDto>> grantConsent(
            @PathVariable UUID sessionId,
            @RequestBody(required = false) AbdmConsentRequestDto request,
            Authentication authentication) {

        User user = getUserFromAuth(authentication);
        AbdmConsentResponseDto response = abdmService.grantConsent(sessionId, request, user.getId());
        return ResponseEntity.ok(ApiResponse.success(response, response.getMessage()));
    }

    /**
     * Revokes patient consent for ABDM data sharing.
     */
    @PostMapping("/sessions/{sessionId}/consent/revoke")
    public ResponseEntity<ApiResponse<AbdmConsentResponseDto>> revokeConsent(
            @PathVariable UUID sessionId,
            Authentication authentication) {

        User user = getUserFromAuth(authentication);
        AbdmConsentResponseDto response = abdmService.revokeConsent(sessionId, user.getId());
        return ResponseEntity.ok(ApiResponse.success(response, response.getMessage()));
    }

    /**
     * Transmits doctor-verified FHIR clinical document bundle to ABDM gateway.
     * Restricted to DOCTOR and ADMIN roles.
     */
    @PostMapping("/sessions/{sessionId}/exchange")
    public ResponseEntity<ApiResponse<AbdmExchangeResponseDto>> exchangeClinicalData(
            @PathVariable UUID sessionId,
            @RequestBody(required = false) AbdmExchangeRequestDto request,
            Authentication authentication) {

        User user = getUserFromAuth(authentication);
        checkDoctorOrAdmin(user);

        AbdmExchangeResponseDto response = abdmService.exchangeClinicalData(sessionId, request, user.getId());
        if (response.getStatus() == com.medikiosk.entity.enums.IntegrationStatus.FAILED && response.getErrorMessage() != null && response.getErrorMessage().contains("rejected")) {
            throw new ApiException(response.getErrorMessage(), HttpStatus.PRECONDITION_FAILED);
        }
        HttpStatus status = response.getStatus() != null && response.getStatus().name().equals("SUCCESS")
                ? HttpStatus.OK : HttpStatus.BAD_GATEWAY;

        return ResponseEntity.status(status).body(ApiResponse.success(response, response.getMessage()));
    }

    /**
     * Retrieves session ABDM integration history and transaction records.
     */
    @GetMapping("/sessions/{sessionId}/status")
    public ResponseEntity<ApiResponse<List<com.medikiosk.abdm.dto.IntegrationRecordDto>>> getSessionStatus(
            @PathVariable UUID sessionId,
            Authentication authentication) {

        User user = getUserFromAuth(authentication);
        List<com.medikiosk.abdm.dto.IntegrationRecordDto> history = abdmService.getSessionIntegrationHistory(sessionId, user.getId());
        return ResponseEntity.ok(ApiResponse.success(history, "ABDM transaction history retrieved successfully"));
    }

    private User getUserFromAuth(Authentication authentication) {
        if (authentication == null || !(authentication.getPrincipal() instanceof User)) {
            throw new ApiException("Authentication required", HttpStatus.UNAUTHORIZED);
        }
        return (User) authentication.getPrincipal();
    }

    private void checkDoctorOrAdmin(User user) {
        if (user.getRole() != UserRole.DOCTOR && user.getRole() != UserRole.ADMIN) {
            throw new ApiException("Access restricted to clinical staff and administrators", HttpStatus.FORBIDDEN);
        }
    }
}
