package com.medikiosk.abdm.service;

import com.medikiosk.abdm.dto.AbdmConsentRequestDto;
import com.medikiosk.abdm.dto.AbdmConsentResponseDto;
import com.medikiosk.abdm.dto.AbdmExchangeRequestDto;
import com.medikiosk.abdm.dto.AbdmExchangeResponseDto;
import com.medikiosk.abdm.dto.AbdmStatusDto;
import com.medikiosk.abdm.dto.AbhaVerificationResponseDto;
import com.medikiosk.abdm.dto.IntegrationRecordDto;

import java.util.List;
import java.util.UUID;

/**
 * Service interface for ABDM and ABHA integration workflows.
 * Conforms to docs/ABDM_SPEC.md, docs/SECURITY.md, and Phase 12 requirements.
 */
public interface AbdmService {

    /**
     * Retrieves current ABDM adapter status, configured operating mode, and gateway connectivity.
     */
    AbdmStatusDto getStatus();

    /**
     * Verifies an ABHA identifier for a patient.
     */
    AbhaVerificationResponseDto verifyPatientAbha(UUID patientId, String abhaId, UUID authenticatedUserId);

    /**
     * Records or updates patient consent for ABDM health information exchange.
     */
    AbdmConsentResponseDto grantConsent(UUID sessionId, AbdmConsentRequestDto request, UUID authenticatedUserId);

    /**
     * Revokes patient consent for ABDM health data sharing.
     */
    AbdmConsentResponseDto revokeConsent(UUID sessionId, UUID authenticatedUserId);

    /**
     * Validates and transmits a doctor-verified FHIR clinical document bundle to the ABDM gateway.
     */
    AbdmExchangeResponseDto exchangeClinicalData(UUID sessionId, AbdmExchangeRequestDto request, UUID authenticatedDoctorId);

    /**
     * Retrieves the history of ABDM integration operations for a session.
     */
    List<IntegrationRecordDto> getSessionIntegrationHistory(UUID sessionId, UUID authenticatedUserId);
}
