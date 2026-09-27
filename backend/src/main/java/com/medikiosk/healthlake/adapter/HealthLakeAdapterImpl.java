package com.medikiosk.healthlake.adapter;

import com.medikiosk.healthlake.config.HealthLakeProperties;
import com.medikiosk.healthlake.provider.HealthLakeExportResult;
import com.medikiosk.healthlake.provider.HealthLakeIntegrationProvider;
import com.medikiosk.healthlake.service.HealthLakeService;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Architectural adapter implementation for AWS HealthLake FHIR datastore integration.
 * Conforms to docs/HEALTHLAKE_SPEC.md requirements.
 */
@Component
public class HealthLakeAdapterImpl implements HealthLakeAdapter {

    private final HealthLakeProperties properties;
    private final HealthLakeService healthLakeService;
    private final HealthLakeIntegrationProvider mockProvider;
    private final HealthLakeIntegrationProvider awsProvider;

    public HealthLakeAdapterImpl(
            HealthLakeProperties properties,
            HealthLakeService healthLakeService,
            @Qualifier("mockHealthLakeProvider") HealthLakeIntegrationProvider mockProvider,
            @Qualifier("awsHealthLakeProvider") HealthLakeIntegrationProvider awsProvider) {
        this.properties = properties;
        this.healthLakeService = healthLakeService;
        this.mockProvider = mockProvider;
        this.awsProvider = awsProvider;
    }

    @Override
    public boolean isAvailable() {
        return healthLakeService.getStatus().isEnabled();
    }

    @Override
    public String createResource(String resourceType, String fhirResourceJson) {
        HealthLakeIntegrationProvider activeProvider = (!properties.isMock() && properties.hasCredentials())
                ? awsProvider : mockProvider;

        HealthLakeExportResult result = activeProvider.exportClinicalBundle(
                fhirResourceJson,
                null,
                properties.getDatastoreId(),
                "HL-ADAPTER-" + UUID.randomUUID()
        );

        return result.externalReference();
    }
}
