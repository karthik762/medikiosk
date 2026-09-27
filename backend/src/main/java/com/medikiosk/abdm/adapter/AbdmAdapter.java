package com.medikiosk.abdm.adapter;

import java.util.UUID;

/**
 * Architectural adapter boundary for Ayushman Bharat Digital Mission (ABDM) integration.
 * Conforms to docs/ABDM_SPEC.md requirements.
 */
public interface AbdmAdapter {

    /**
     * Checks ABDM gateway availability status.
     *
     * @return true if ABDM adapter is reachable, false otherwise.
     */
    boolean isAvailable();

    /**
     * Links a verified clinical record to ABDM using FHIR bundles.
     *
     * @param doctorId        Authenticated doctor/admin user identifier.
     * @param intakeSessionId Unique intake session identifier.
     * @param fhirBundleJson  FHIR bundle payload.
     * @return Transaction or reference identifier from ABDM gateway.
     */
    String linkRecord(UUID doctorId, UUID intakeSessionId, String fhirBundleJson);
}
