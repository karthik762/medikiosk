package com.medikiosk.healthlake.adapter;

import java.util.UUID;

/**
 * Architectural adapter boundary for AWS HealthLake FHIR datastore integration.
 * Conforms to docs/HEALTHLAKE_SPEC.md requirements.
 */
public interface HealthLakeAdapter {

    /**
     * Checks if AWS HealthLake connectivity is configured and accessible.
     *
     * @return true if available, false otherwise.
     */
    boolean isAvailable();

    /**
     * Stores a FHIR resource in the target AWS HealthLake datastore.
     *
     * @param resourceType     FHIR resource type (e.g., "Patient", "Encounter", "Composition").
     * @param fhirResourceJson Serialized FHIR resource JSON.
     * @return Amazon Resource Name (ARN) or resource ID in HealthLake.
     */
    String createResource(String resourceType, String fhirResourceJson);
}
