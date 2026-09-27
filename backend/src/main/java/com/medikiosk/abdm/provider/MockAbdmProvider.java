package com.medikiosk.abdm.provider;

import com.medikiosk.abdm.config.AbdmProperties;
import com.medikiosk.abdm.dto.AbdmStatusDto;
import com.medikiosk.abdm.dto.AbhaVerificationResponseDto;
import com.medikiosk.entity.enums.AbhaStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.UUID;
import java.util.regex.Pattern;

/**
 * Deterministic mock ABDM integration provider for local development, simulation, and automated testing.
 * Conforms to docs/ABDM_SPEC.md and Phase 12 requirements.
 * Never connects to external networks and explicitly identifies itself as MOCK.
 */
@Component("mockAbdmProvider")
public class MockAbdmProvider implements AbdmIntegrationProvider {

    private static final Logger log = LoggerFactory.getLogger(MockAbdmProvider.class);

    // Standard ABHA 14-digit format (XX-XXXX-XXXX-XXXX or 14 consecutive digits) or ABHA address format (name@abdm)
    private static final Pattern ABHA_NUMBER_PATTERN = Pattern.compile("^(\\d{2}-\\d{4}-\\d{4}-\\d{4}|\\d{14})$");
    private static final Pattern ABHA_ADDRESS_PATTERN = Pattern.compile("^[a-zA-Z0-9._-]+@[a-zA-Z0-9.-]+$");

    private final AbdmProperties properties;

    public MockAbdmProvider(AbdmProperties properties) {
        this.properties = properties;
    }

    @Override
    public AbdmStatusDto getStatus() {
        return new AbdmStatusDto(
                "MOCK",
                true,
                properties.isEnabled(),
                "mock://localhost/abdm-gateway",
                false, // Never claim live connected
                "ABDM Mock Mode active. External live connection: NOT CONNECTED."
        );
    }

    @Override
    public AbhaVerificationResponseDto verifyAbha(String abhaId) {
        log.info("Mock ABDM Provider: Verifying ABHA ID/Address");

        if (abhaId == null || abhaId.isBlank()) {
            return new AbhaVerificationResponseDto(
                    false,
                    AbhaStatus.NOT_PROVIDED,
                    null,
                    "MOCK_VERIFIER",
                    true,
                    "ABHA identifier not provided",
                    null
            );
        }

        String clean = abhaId.trim();

        // Simulated test scenarios
        if (clean.startsWith("FAIL") || clean.startsWith("00-") || clean.startsWith("000000")) {
            return new AbhaVerificationResponseDto(
                    false,
                    AbhaStatus.UNVERIFIED,
                    maskAbha(clean),
                    "MOCK_VERIFIER",
                    true,
                    "Simulated ABHA verification failure: record not found in national registry",
                    null
            );
        }

        if (clean.startsWith("TIMEOUT")) {
            return new AbhaVerificationResponseDto(
                    false,
                    AbhaStatus.UNVERIFIED,
                    maskAbha(clean),
                    "MOCK_VERIFIER",
                    true,
                    "Simulated ABDM gateway timeout (504)",
                    null
            );
        }

        if (clean.startsWith("SERVER_ERROR")) {
            return new AbhaVerificationResponseDto(
                    false,
                    AbhaStatus.UNVERIFIED,
                    maskAbha(clean),
                    "MOCK_VERIFIER",
                    true,
                    "Simulated ABDM remote gateway failure (500)",
                    null
            );
        }

        // Format validation
        boolean isValidNumber = ABHA_NUMBER_PATTERN.matcher(clean).matches();
        boolean isValidAddress = ABHA_ADDRESS_PATTERN.matcher(clean).matches();

        if (isValidNumber || isValidAddress) {
            String masked = maskAbha(clean);
            return new AbhaVerificationResponseDto(
                    true,
                    AbhaStatus.VERIFIED,
                    masked,
                    "MOCK_DETERMINISTIC_OTP",
                    true,
                    "ABHA verified successfully (MOCK mode — synthetic test verification only)",
                    Instant.now()
            );
        }

        return new AbhaVerificationResponseDto(
                false,
                AbhaStatus.UNVERIFIED,
                maskAbha(clean),
                "MOCK_VERIFIER",
                true,
                "Invalid ABHA format. Expected 14-digit number (XX-XXXX-XXXX-XXXX) or ABHA address (name@abdm).",
                null
        );
    }

    @Override
    public String generateConsentArtefact(String abhaId, String purpose, int validMonths) {
        log.info("Mock ABDM Provider: Generating consent artefact");
        return "MOCK-ARTEFACT-" + UUID.randomUUID();
    }

    @Override
    public boolean revokeConsentArtefact(String consentArtefactId) {
        log.info("Mock ABDM Provider: Revoking consent artefact {}", consentArtefactId);
        return true;
    }

    @Override
    public AbdmExchangeResult transmitClinicalBundle(String fhirBundleJson, String abhaId, String consentArtefactId, String idempotencyKey) {
        log.info("Mock ABDM Provider: Transmitting FHIR clinical bundle (idempotency: {})", idempotencyKey);

        if (fhirBundleJson == null || fhirBundleJson.isBlank()) {
            return new AbdmExchangeResult(
                    false,
                    null,
                    null,
                    "Empty FHIR bundle payload",
                    null
            );
        }

        // Simulated error triggers for test suite
        if (fhirBundleJson.contains("TRIGGER_TIMEOUT")) {
            return new AbdmExchangeResult(
                    false,
                    null,
                    null,
                    "Simulated ABDM gateway timeout after 5000ms",
                    null
            );
        }

        if (fhirBundleJson.contains("TRIGGER_SERVER_ERROR")) {
            return new AbdmExchangeResult(
                    false,
                    null,
                    null,
                    "Simulated ABDM remote gateway 500 Internal Server Error",
                    null
            );
        }

        String txnId = "MOCK-TXN-" + UUID.randomUUID();
        String externalRef = "MOCK-CARE-CONTEXT-" + UUID.randomUUID();
        String payload = String.format("{\"status\":\"ACCEPTED\",\"mode\":\"MOCK\",\"transactionId\":\"%s\",\"artefact\":\"%s\"}", txnId, consentArtefactId);

        return new AbdmExchangeResult(
                true,
                txnId,
                externalRef,
                null,
                payload
        );
    }

    private String maskAbha(String abha) {
        if (abha == null || abha.isBlank()) return "****";
        if (abha.length() <= 4) return "****";
        if (abha.contains("@")) {
            int atIndex = abha.indexOf('@');
            return abha.substring(0, Math.min(2, atIndex)) + "***" + abha.substring(atIndex);
        }
        return "XX-XXXX-XXXX-" + abha.substring(abha.length() - 4);
    }
}
