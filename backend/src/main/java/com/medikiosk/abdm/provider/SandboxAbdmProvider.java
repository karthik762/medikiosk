package com.medikiosk.abdm.provider;

import com.medikiosk.abdm.config.AbdmProperties;
import com.medikiosk.abdm.dto.AbdmStatusDto;
import com.medikiosk.abdm.dto.AbhaVerificationResponseDto;
import com.medikiosk.entity.enums.AbhaStatus;
import com.medikiosk.exception.ApiException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

/**
 * Standard gateway provider for ABDM Sandbox / Production environments.
 * Disabled and unauthenticated until authentic ABDM credentials are provided.
 * Conforms to docs/ABDM_SPEC.md and Phase 12 requirements.
 */
@Component("sandboxAbdmProvider")
public class SandboxAbdmProvider implements AbdmIntegrationProvider {

    private static final Logger log = LoggerFactory.getLogger(SandboxAbdmProvider.class);

    private final AbdmProperties properties;

    public SandboxAbdmProvider(AbdmProperties properties) {
        this.properties = properties;
    }

    @Override
    public AbdmStatusDto getStatus() {
        boolean hasCreds = properties.hasCredentials();
        String mode = properties.getMode() != null ? properties.getMode().toUpperCase() : "SANDBOX";
        return new AbdmStatusDto(
                mode,
                false,
                properties.isEnabled(),
                properties.getBaseUrl(),
                hasCreds,
                hasCreds
                        ? "ABDM Sandbox configured with client credentials"
                        : "ABDM Sandbox credentials missing — external gateway connection unavailable"
        );
    }

    @Override
    public AbhaVerificationResponseDto verifyAbha(String abhaId) {
        if (!properties.hasCredentials()) {
            throw new ApiException("ABHA verification unavailable — ABDM sandbox credentials required", HttpStatus.SERVICE_UNAVAILABLE);
        }
        // Real ABDM Gateway calls would execute here using authentic OAuth access tokens
        log.warn("Sandbox ABDM Provider: Live gateway call requested but sandbox endpoint requires manual activation");
        return new AbhaVerificationResponseDto(
                false,
                AbhaStatus.UNVERIFIED,
                null,
                "GATEWAY_UNAVAILABLE",
                false,
                "Live ABDM gateway connection inactive",
                null
        );
    }

    @Override
    public String generateConsentArtefact(String abhaId, String purpose, int validMonths) {
        if (!properties.hasCredentials()) {
            throw new ApiException("ABDM consent service unavailable — sandbox credentials required", HttpStatus.SERVICE_UNAVAILABLE);
        }
        throw new ApiException("ABDM sandbox gateway requires active OAuth session", HttpStatus.SERVICE_UNAVAILABLE);
    }

    @Override
    public boolean revokeConsentArtefact(String consentArtefactId) {
        if (!properties.hasCredentials()) {
            throw new ApiException("ABDM consent revocation unavailable — sandbox credentials required", HttpStatus.SERVICE_UNAVAILABLE);
        }
        return false;
    }

    @Override
    public AbdmExchangeResult transmitClinicalBundle(String fhirBundleJson, String abhaId, String consentArtefactId, String idempotencyKey) {
        if (!properties.hasCredentials()) {
            return new AbdmExchangeResult(
                    false,
                    null,
                    null,
                    "ABDM gateway credentials not configured",
                    null
            );
        }
        return new AbdmExchangeResult(
                false,
                null,
                null,
                "ABDM live network connectivity inactive",
                null
        );
    }
}
