package com.medikiosk.healthlake.provider;

import com.medikiosk.healthlake.config.HealthLakeProperties;
import com.medikiosk.healthlake.dto.HealthLakeStatusDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Deterministic mock AWS HealthLake integration provider.
 * Used for local development, simulations, and automated tests.
 * Conforms to docs/HEALTHLAKE_SPEC.md and Phase 14 requirements.
 * Never connects to AWS HealthLake and explicitly identifies itself as MOCK with connected=false.
 */
@Component("mockHealthLakeProvider")
public class MockHealthLakeProvider implements HealthLakeIntegrationProvider {

    private static final Logger log = LoggerFactory.getLogger(MockHealthLakeProvider.class);

    private final HealthLakeProperties properties;

    public MockHealthLakeProvider(HealthLakeProperties properties) {
        this.properties = properties;
    }

    @Override
    public HealthLakeStatusDto getStatus() {
        return new HealthLakeStatusDto(
                "MOCK",
                true,
                properties.isEnabled(),
                properties.getEndpoint() != null ? properties.getEndpoint() : "mock://healthlake.ap-south-1.amazonaws.com/datastore/mock-ds/r4/",
                properties.getDatastoreId() != null ? properties.getDatastoreId() : "mock-healthlake-datastore",
                properties.getRegion() != null ? properties.getRegion() : "ap-south-1",
                false, // Explicit invariant: Never claim live AWS HealthLake connection
                "AWS HealthLake Mock Mode active. External live connection: NOT CONNECTED."
        );
    }

    @Override
    public HealthLakeExportResult exportClinicalBundle(String fhirBundleJson, String healthlakeRef, String datastoreId, String idempotencyKey) {
        String effectiveDatastore = (datastoreId != null && !datastoreId.isBlank())
                ? datastoreId
                : (properties.getDatastoreId() != null ? properties.getDatastoreId() : "mock-healthlake-datastore");

        log.info("Mock HealthLake Provider: Exporting FHIR bundle to datastore {} (healthlakeRef: {}, idempotency: {})",
                effectiveDatastore, healthlakeRef, idempotencyKey);

        if (fhirBundleJson == null || fhirBundleJson.isBlank()) {
            return new HealthLakeExportResult(
                    false,
                    null,
                    null,
                    "Empty FHIR bundle payload",
                    null
            );
        }

        // Test error simulation triggers
        if ((fhirBundleJson.contains("TRIGGER_TIMEOUT"))
                || (idempotencyKey != null && idempotencyKey.contains("TIMEOUT"))) {
            return new HealthLakeExportResult(
                    false,
                    null,
                    null,
                    "Simulated AWS HealthLake timeout after 5000ms",
                    null
            );
        }

        if ((fhirBundleJson.contains("TRIGGER_SERVER_ERROR"))
                || (idempotencyKey != null && idempotencyKey.contains("SERVER_ERROR"))) {
            return new HealthLakeExportResult(
                    false,
                    null,
                    null,
                    "Simulated AWS HealthLake 500 Internal Server Error (ServiceUnavailableException)",
                    null
            );
        }

        String txnId = "MOCK-HL-TXN-" + UUID.randomUUID();
        String externalRef = "HL-BUNDLE-" + UUID.randomUUID();
        String effectivePatientRef = (healthlakeRef != null && !healthlakeRef.isBlank())
                ? healthlakeRef
                : "HL-PAT-" + UUID.randomUUID().toString().substring(0, 8);

        String payload = String.format(
                "{\"resourceType\":\"Bundle\",\"type\":\"transaction-response\",\"id\":\"%s\",\"datastoreId\":\"%s\",\"patientRef\":\"%s\",\"status\":\"200 OK\",\"mode\":\"MOCK\"}",
                externalRef, effectiveDatastore, effectivePatientRef
        );

        return new HealthLakeExportResult(
                true,
                txnId,
                externalRef,
                null,
                payload
        );
    }
}
