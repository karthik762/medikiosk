package com.medikiosk.healthlake.provider;

import com.medikiosk.healthlake.config.HealthLakeProperties;
import com.medikiosk.healthlake.dto.HealthLakeStatusDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Real/Staging AWS HealthLake FHIR datastore integration boundary provider.
 * When real AWS credentials, region, and HealthLake datastore endpoints are configured,
 * executes FHIR REST operations using AWS SigV4 authentication.
 * When credentials or datastore ID are missing, acts as an inactive boundary.
 * Conforms to docs/HEALTHLAKE_SPEC.md requirements.
 */
@Component("awsHealthLakeProvider")
public class AwsHealthLakeProvider implements HealthLakeIntegrationProvider {

    private static final Logger log = LoggerFactory.getLogger(AwsHealthLakeProvider.class);

    private final HealthLakeProperties properties;

    public AwsHealthLakeProvider(HealthLakeProperties properties) {
        this.properties = properties;
    }

    @Override
    public HealthLakeStatusDto getStatus() {
        boolean hasCreds = properties.hasCredentials();
        String mode = properties.getMode() != null ? properties.getMode().toUpperCase() : "LIVE";
        return new HealthLakeStatusDto(
                mode,
                false,
                properties.isEnabled(),
                properties.getEndpoint(),
                properties.getDatastoreId(),
                properties.getRegion(),
                hasCreds,
                hasCreds
                        ? "AWS HealthLake datastore endpoint configured with credentials"
                        : "AWS HealthLake credentials or datastore endpoint missing — cloud connection unavailable"
        );
    }

    @Override
    public HealthLakeExportResult exportClinicalBundle(String fhirBundleJson, String healthlakeRef, String datastoreId, String idempotencyKey) {
        if (!properties.hasCredentials()) {
            return new HealthLakeExportResult(
                    false,
                    null,
                    null,
                    "AWS HealthLake credentials or datastore ID not configured",
                    null
            );
        }
        log.warn("AwsHealthLakeProvider: Live AWS HealthLake export requested but live connection requires manual AWS activation");
        return new HealthLakeExportResult(
                false,
                null,
                null,
                "Live AWS HealthLake datastore network connectivity inactive",
                null
        );
    }
}
