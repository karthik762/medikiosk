package com.medikiosk.healthlake.controller;

import com.medikiosk.abdm.dto.IntegrationRecordDto;
import com.medikiosk.dto.ApiResponse;
import com.medikiosk.entity.User;
import com.medikiosk.entity.enums.UserRole;
import com.medikiosk.exception.ApiException;
import com.medikiosk.healthlake.dto.HealthLakeExportRequestDto;
import com.medikiosk.healthlake.dto.HealthLakeExportResponseDto;
import com.medikiosk.healthlake.dto.HealthLakeStatusDto;
import com.medikiosk.healthlake.service.HealthLakeService;
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
 * REST controller for AWS HealthLake integration endpoints.
 * Conforms to docs/HEALTHLAKE_SPEC.md, docs/API_SPEC.md, and docs/SECURITY.md.
 */
@RestController
@RequestMapping("/api/healthlake")
public class HealthLakeController {

    private final HealthLakeService healthLakeService;

    public HealthLakeController(HealthLakeService healthLakeService) {
        this.healthLakeService = healthLakeService;
    }

    /**
     * Checks AWS HealthLake adapter operating mode, provider configuration, and cloud connectivity.
     */
    @GetMapping("/status")
    public ResponseEntity<ApiResponse<HealthLakeStatusDto>> getStatus() {
        HealthLakeStatusDto status = healthLakeService.getStatus();
        return ResponseEntity.ok(ApiResponse.success(status, "AWS HealthLake status retrieved successfully"));
    }

    /**
     * Transmits doctor-verified FHIR clinical document bundle to AWS HealthLake datastore.
     * Strictly restricted to DOCTOR and ADMIN roles.
     */
    @PostMapping("/sessions/{sessionId}/export")
    public ResponseEntity<ApiResponse<HealthLakeExportResponseDto>> exportClinicalData(
            @PathVariable UUID sessionId,
            @RequestBody(required = false) HealthLakeExportRequestDto request,
            Authentication authentication) {

        User user = getUserFromAuth(authentication);
        checkDoctorOrAdmin(user);

        HealthLakeExportResponseDto response = healthLakeService.exportClinicalData(sessionId, request, user.getId());
        if (response.getStatus() == com.medikiosk.entity.enums.IntegrationStatus.FAILED && response.getErrorMessage() != null && response.getErrorMessage().contains("rejected")) {
            throw new ApiException(response.getErrorMessage(), HttpStatus.PRECONDITION_FAILED);
        }
        HttpStatus status = response.getStatus() != null && response.getStatus().name().equals("SUCCESS")
                ? HttpStatus.OK : HttpStatus.BAD_GATEWAY;

        return ResponseEntity.status(status).body(ApiResponse.success(response, response.getMessage()));
    }

    /**
     * Retrieves session AWS HealthLake integration history and transaction records.
     */
    @GetMapping("/sessions/{sessionId}/status")
    public ResponseEntity<ApiResponse<List<IntegrationRecordDto>>> getSessionStatus(
            @PathVariable UUID sessionId,
            Authentication authentication) {

        User user = getUserFromAuth(authentication);
        List<IntegrationRecordDto> history = healthLakeService.getSessionIntegrationHistory(sessionId, user.getId());
        return ResponseEntity.ok(ApiResponse.success(history, "AWS HealthLake transaction history retrieved successfully"));
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
