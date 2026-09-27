package com.medikiosk.controller;

import com.medikiosk.dto.ApiResponse;
import com.medikiosk.dto.fhir.FhirExportSummaryDto;
import com.medikiosk.dto.fhir.FhirValidationResponseDto;
import com.medikiosk.entity.User;
import com.medikiosk.entity.enums.UserRole;
import com.medikiosk.exception.ApiException;
import com.medikiosk.fhir.FhirMappingService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

/**
 * REST controller providing FHIR R4 interoperability endpoints.
 * Conforms to docs/FHIR_MAPPING.md and docs/API_SPEC.md.
 */
@RestController
@RequestMapping("/api/fhir")
public class FhirController {

    private final FhirMappingService fhirMappingService;

    public FhirController(FhirMappingService fhirMappingService) {
        this.fhirMappingService = fhirMappingService;
    }

    /**
     * Retrieves the complete FHIR R4 Document Bundle JSON for a clinical intake session.
     */
    @GetMapping(value = "/sessions/{sessionId}/bundle", produces = {MediaType.APPLICATION_JSON_VALUE, "application/fhir+json"})
    public ResponseEntity<String> getClinicalBundleJson(
            @PathVariable UUID sessionId,
            Authentication authentication) {

        checkDoctorOrAdmin(authentication);
        String bundleJson = fhirMappingService.toFhirClinicalBundle(sessionId);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_TYPE, "application/fhir+json;charset=UTF-8")
                .body(bundleJson);
    }

    /**
     * Retrieves the stored or preview FHIR export summary for an intake session.
     */
    @GetMapping("/sessions/{sessionId}/export")
    public ResponseEntity<ApiResponse<FhirExportSummaryDto>> getExportSummary(
            @PathVariable UUID sessionId,
            Authentication authentication) {

        checkDoctorOrAdmin(authentication);
        FhirExportSummaryDto summary = fhirMappingService.getStoredExportSummary(sessionId);
        return ResponseEntity.ok(ApiResponse.success(summary, "FHIR export summary retrieved successfully"));
    }

    /**
     * Generates, validates, and stores a verified FHIR R4 export bundle with full audit log and integration records.
     */
    @PostMapping("/sessions/{sessionId}/export")
    public ResponseEntity<ApiResponse<FhirExportSummaryDto>> exportClinicalBundle(
            @PathVariable UUID sessionId,
            Authentication authentication) {

        User user = getUserFromAuth(authentication);
        checkDoctorOrAdmin(authentication);
        FhirExportSummaryDto summary = fhirMappingService.exportClinicalBundle(sessionId, user.getId());
        return ResponseEntity.ok(ApiResponse.success(summary, "FHIR R4 clinical bundle exported successfully"));
    }

    /**
     * Validates session data against FHIR R4 invariants and MediKiosk profile requirements.
     */
    @GetMapping("/sessions/{sessionId}/validate")
    public ResponseEntity<ApiResponse<FhirValidationResponseDto>> validateSession(
            @PathVariable UUID sessionId,
            Authentication authentication) {

        checkDoctorOrAdmin(authentication);
        FhirValidationResponseDto validation = fhirMappingService.validateBundleForSession(sessionId);
        return ResponseEntity.ok(ApiResponse.success(validation, "FHIR validation completed"));
    }

    /**
     * Retrieves individual FHIR R4 Patient representation.
     */
    @GetMapping(value = "/patients/{patientId}", produces = {MediaType.APPLICATION_JSON_VALUE, "application/fhir+json"})
    public ResponseEntity<String> getPatientJson(
            @PathVariable UUID patientId,
            Authentication authentication) {

        checkDoctorOrAdmin(authentication);
        String patientJson = fhirMappingService.toFhirPatient(patientId);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_TYPE, "application/fhir+json;charset=UTF-8")
                .body(patientJson);
    }

    /**
     * Retrieves individual FHIR R4 Encounter representation for an intake session.
     */
    @GetMapping(value = "/sessions/{sessionId}/encounter", produces = {MediaType.APPLICATION_JSON_VALUE, "application/fhir+json"})
    public ResponseEntity<String> getEncounterJson(
            @PathVariable UUID sessionId,
            Authentication authentication) {

        checkDoctorOrAdmin(authentication);
        String encounterJson = fhirMappingService.toFhirEncounter(sessionId);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_TYPE, "application/fhir+json;charset=UTF-8")
                .body(encounterJson);
    }

    private User getUserFromAuth(Authentication authentication) {
        if (authentication == null || !(authentication.getPrincipal() instanceof User)) {
            throw new ApiException("Authentication required", HttpStatus.UNAUTHORIZED);
        }
        return (User) authentication.getPrincipal();
    }

    private void checkDoctorOrAdmin(Authentication authentication) {
        User user = getUserFromAuth(authentication);
        if (user.getRole() != UserRole.DOCTOR && user.getRole() != UserRole.ADMIN) {
            throw new ApiException("Access restricted to clinical staff and administrators", HttpStatus.FORBIDDEN);
        }
    }
}
