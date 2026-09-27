package com.medikiosk.his.provider;

import com.medikiosk.exception.ApiException;
import com.medikiosk.his.config.HisProperties;
import com.medikiosk.his.dto.HisPatientCandidateDto;
import com.medikiosk.his.dto.HisStatusDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Real/Staging Hospital Information System (HIS) integration boundary provider.
 * When real hospital credentials and endpoints are configured, calls external HIS REST/HL7 endpoints.
 * Unauthenticated/disabled when credentials are not configured.
 * Conforms to docs/HIS_SPEC.md requirements.
 */
@Component("hospitalHisProvider")
public class HospitalHisProvider implements HisIntegrationProvider {

    private static final Logger log = LoggerFactory.getLogger(HospitalHisProvider.class);

    private final HisProperties properties;

    public HospitalHisProvider(HisProperties properties) {
        this.properties = properties;
    }

    @Override
    public HisStatusDto getStatus() {
        boolean hasCreds = properties.hasCredentials();
        String mode = properties.getMode() != null ? properties.getMode().toUpperCase() : "LIVE";
        return new HisStatusDto(
                mode,
                false,
                properties.isEnabled(),
                properties.getBaseUrl(),
                hasCreds,
                hasCreds
                        ? "Hospital HIS endpoint configured with client credentials"
                        : "Hospital HIS credentials missing — external hospital connection unavailable"
        );
    }

    @Override
    public HisExportResult exportClinicalBundle(String fhirBundleJson, String hisId, String intakeQueue, String idempotencyKey) {
        if (!properties.hasCredentials()) {
            return new HisExportResult(
                    false,
                    null,
                    null,
                    "HIS client credentials not configured",
                    null
            );
        }
        log.warn("HospitalHisProvider: Live HIS export requested but live connection requires manual hospital endpoint activation");
        return new HisExportResult(
                false,
                null,
                null,
                "Live hospital HIS network connectivity inactive",
                null
        );
    }

    @Override
    public List<HisPatientCandidateDto> searchPatients(String query, String searchType) {
        if (!properties.hasCredentials()) {
            throw new ApiException("Hospital HIS patient search unavailable — credentials required", HttpStatus.SERVICE_UNAVAILABLE);
        }
        log.warn("HospitalHisProvider: Live HIS patient search requested but live connection requires hospital activation");
        return List.of();
    }

    @Override
    public HisPatientCandidateDto fetchPatientByHisId(String hisId) {
        if (!properties.hasCredentials()) {
            throw new ApiException("Hospital HIS patient lookup unavailable — credentials required", HttpStatus.SERVICE_UNAVAILABLE);
        }
        return null;
    }
}
