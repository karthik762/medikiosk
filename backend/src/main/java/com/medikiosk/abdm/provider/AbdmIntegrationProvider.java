package com.medikiosk.abdm.provider;

import com.medikiosk.abdm.dto.AbdmStatusDto;
import com.medikiosk.abdm.dto.AbhaVerificationResponseDto;

/**
 * Common abstraction for ABDM Gateway interactions.
 * Conforms to docs/ABDM_SPEC.md.
 */
public interface AbdmIntegrationProvider {

    /**
     * Checks provider connection, health, and configured operating mode.
     */
    AbdmStatusDto getStatus();

    /**
     * Verifies an ABHA number or address against the provider.
     */
    AbhaVerificationResponseDto verifyAbha(String abhaId);

    /**
     * Creates an external ABDM consent artefact reference.
     */
    String generateConsentArtefact(String abhaId, String purpose, int validMonths);

    /**
     * Revokes an existing ABDM consent artefact reference.
     */
    boolean revokeConsentArtefact(String consentArtefactId);

    /**
     * Transmits a doctor-verified FHIR clinical document bundle to the ABDM gateway.
     */
    AbdmExchangeResult transmitClinicalBundle(String fhirBundleJson, String abhaId, String consentArtefactId, String idempotencyKey);
}
