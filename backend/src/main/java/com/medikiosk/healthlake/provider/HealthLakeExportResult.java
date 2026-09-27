package com.medikiosk.healthlake.provider;

/**
 * Result of transmitting a clinical FHIR bundle to an AWS HealthLake FHIR datastore.
 */
public record HealthLakeExportResult(
        boolean success,
        String transactionId,
        String externalReference,
        String errorMessage,
        String responsePayload
) {
}
