package com.medikiosk.his.controller;

import com.medikiosk.abdm.dto.IntegrationRecordDto;
import com.medikiosk.dto.ApiResponse;
import com.medikiosk.entity.User;
import com.medikiosk.entity.enums.UserRole;
import com.medikiosk.exception.ApiException;
import com.medikiosk.his.dto.HisExportRequestDto;
import com.medikiosk.his.dto.HisExportResponseDto;
import com.medikiosk.his.dto.HisLinkPatientRequestDto;
import com.medikiosk.his.dto.HisLinkPatientResponseDto;
import com.medikiosk.his.dto.HisPatientSearchResponseDto;
import com.medikiosk.his.dto.HisStatusDto;
import com.medikiosk.his.service.HisService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

/**
 * REST controller for Hospital Information System (HIS) integration endpoints.
 * Conforms to docs/HIS_SPEC.md, docs/API_SPEC.md, and docs/SECURITY.md.
 */
@RestController
@RequestMapping("/api/his")
public class HisController {

    private final HisService hisService;

    public HisController(HisService hisService) {
        this.hisService = hisService;
    }

    /**
     * Checks HIS adapter operating mode, provider configuration, and hospital connectivity.
     */
    @GetMapping("/status")
    public ResponseEntity<ApiResponse<HisStatusDto>> getStatus() {
        HisStatusDto status = hisService.getStatus();
        return ResponseEntity.ok(ApiResponse.success(status, "HIS status retrieved successfully"));
    }

    /**
     * Transmits doctor-verified FHIR clinical document bundle to hospital intake queue.
     * Strictly restricted to DOCTOR and ADMIN roles.
     */
    @PostMapping("/sessions/{sessionId}/export")
    public ResponseEntity<ApiResponse<HisExportResponseDto>> exportClinicalData(
            @PathVariable UUID sessionId,
            @RequestBody(required = false) HisExportRequestDto request,
            Authentication authentication) {

        User user = getUserFromAuth(authentication);
        checkDoctorOrAdmin(user);

        HisExportResponseDto response = hisService.exportClinicalData(sessionId, request, user.getId());
        if (response.getStatus() == com.medikiosk.entity.enums.IntegrationStatus.FAILED && response.getErrorMessage() != null && response.getErrorMessage().contains("rejected")) {
            throw new ApiException(response.getErrorMessage(), HttpStatus.PRECONDITION_FAILED);
        }
        HttpStatus status = response.getStatus() != null && response.getStatus().name().equals("SUCCESS")
                ? HttpStatus.OK : HttpStatus.BAD_GATEWAY;

        return ResponseEntity.status(status).body(ApiResponse.success(response, response.getMessage()));
    }

    /**
     * Retrieves session HIS integration history and transaction records.
     */
    @GetMapping("/sessions/{sessionId}/status")
    public ResponseEntity<ApiResponse<List<IntegrationRecordDto>>> getSessionStatus(
            @PathVariable UUID sessionId,
            Authentication authentication) {

        User user = getUserFromAuth(authentication);
        List<IntegrationRecordDto> history = hisService.getSessionIntegrationHistory(sessionId, user.getId());
        return ResponseEntity.ok(ApiResponse.success(history, "HIS transaction history retrieved successfully"));
    }

    /**
     * Searches hospital Master Patient Index (MPI) for existing patient records.
     * Restricted to DOCTOR, STAFF, and ADMIN roles.
     */
    @GetMapping("/patients/search")
    public ResponseEntity<ApiResponse<HisPatientSearchResponseDto>> searchHospitalPatients(
            @RequestParam(required = false, defaultValue = "") String query,
            @RequestParam(required = false, defaultValue = "ALL") String searchType,
            Authentication authentication) {

        User user = getUserFromAuth(authentication);
        checkClinicalStaffOrAdmin(user);

        HisPatientSearchResponseDto response = hisService.searchHospitalPatients(query, searchType, user.getId());
        return ResponseEntity.ok(ApiResponse.success(response, response.getMessage()));
    }

    /**
     * Associates a hospital HIS MRN / ID with an existing MediKiosk patient record.
     * Restricted to DOCTOR, STAFF, and ADMIN roles.
     */
    @PostMapping("/patients/{patientId}/link")
    public ResponseEntity<ApiResponse<HisLinkPatientResponseDto>> linkPatientHisId(
            @PathVariable UUID patientId,
            @Valid @RequestBody HisLinkPatientRequestDto request,
            Authentication authentication) {

        User user = getUserFromAuth(authentication);
        checkClinicalStaffOrAdmin(user);

        HisLinkPatientResponseDto response = hisService.linkPatientHisId(patientId, request, user.getId());
        return ResponseEntity.ok(ApiResponse.success(response, response.getMessage()));
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

    private void checkClinicalStaffOrAdmin(User user) {
        if (user.getRole() != UserRole.DOCTOR && user.getRole() != UserRole.ADMIN) {
            throw new ApiException("Access restricted to clinical staff and administrators", HttpStatus.FORBIDDEN);
        }
    }
}
