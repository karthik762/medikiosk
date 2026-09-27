package com.medikiosk.healthlake.provider;

import com.medikiosk.healthlake.dto.HealthLakeStatusDto;

/**
 * Common abstraction for AWS HealthLake FHIR datastore integration providers.
 * Conforms to docs/HEALTHLAKE_SPEC.md requirements.
 */
public interface HealthLakeIntegrationProvider {

    /**
     * Checks provider connection status, health, and configured operating mode.
     */
    HealthLakeStatusDto getStatus();

    /**
     * Transmits a doctor-verified FHIR clinical document bundle to AWS HealthLake.
     *
     * @param fhirBundleJson FHIR R4 Document Bundle JSON.
     * @param healthlakeRef  Existing HealthLake Patient reference/ID (if known).
     * @param datastoreId    Target HealthLake datastore ID.
     * @param idempotencyKey Key to prevent duplicate transmissions.
     * @return Result of the transmission.
     */
    HealthLakeExportResult exportClinicalBundle(String fhirBundleJson, String healthlakeRef, String datastoreId, String idempotencyKey);
}
