package com.medikiosk.his.provider;

import com.medikiosk.his.config.HisProperties;
import com.medikiosk.his.dto.HisPatientCandidateDto;
import com.medikiosk.his.dto.HisStatusDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

/**
 * Deterministic mock Hospital Information System (HIS) integration provider.
 * Used for local development, simulations, and automated tests.
 * Conforms to docs/HIS_SPEC.md and Phase 13 requirements.
 * Never connects to external hospital networks and explicitly identifies itself as MOCK.
 */
@Component("mockHisProvider")
public class MockHisProvider implements HisIntegrationProvider {

    private static final Logger log = LoggerFactory.getLogger(MockHisProvider.class);

    private final HisProperties properties;

    public MockHisProvider(HisProperties properties) {
        this.properties = properties;
    }

    @Override
    public HisStatusDto getStatus() {
        return new HisStatusDto(
                "MOCK",
                true,
                properties.isEnabled(),
                properties.getBaseUrl() != null ? properties.getBaseUrl() : "mock://localhost/his-api",
                false, // Explicit invariant: Never claim live hospital connection
                "HIS Mock Mode active. External live connection: NOT CONNECTED."
        );
    }

    @Override
    public HisExportResult exportClinicalBundle(String fhirBundleJson, String hisId, String intakeQueue, String idempotencyKey) {
        log.info("Mock HIS Provider: Transmitting FHIR clinical bundle to queue {} (hisId: {}, idempotency: {})",
                intakeQueue, hisId, idempotencyKey);

        if (fhirBundleJson == null || fhirBundleJson.isBlank()) {
            return new HisExportResult(
                    false,
                    null,
                    null,
                    "Empty FHIR bundle payload",
                    null
            );
        }

        // Test error simulation triggers
        if ((fhirBundleJson != null && fhirBundleJson.contains("TRIGGER_TIMEOUT"))
                || (intakeQueue != null && intakeQueue.contains("TIMEOUT"))
                || (idempotencyKey != null && idempotencyKey.contains("TIMEOUT"))) {
            return new HisExportResult(
                    false,
                    null,
                    null,
                    "Simulated HIS gateway timeout after 5000ms",
                    null
            );
        }

        if ((fhirBundleJson != null && fhirBundleJson.contains("TRIGGER_SERVER_ERROR"))
                || (intakeQueue != null && intakeQueue.contains("SERVER_ERROR"))
                || (idempotencyKey != null && idempotencyKey.contains("SERVER_ERROR"))) {
            return new HisExportResult(
                    false,
                    null,
                    null,
                    "Simulated HIS remote gateway 500 Internal Server Error",
                    null
            );
        }

        String txnId = "MOCK-HIS-TXN-" + UUID.randomUUID();
        String externalRef = "HIS-ENC-" + UUID.randomUUID();
        String effectiveHisId = (hisId != null && !hisId.isBlank()) ? hisId : "NEW-TEMP-" + UUID.randomUUID().toString().substring(0, 8);
        String payload = String.format(
                "{\"status\":\"ACCEPTED\",\"mode\":\"MOCK\",\"transactionId\":\"%s\",\"encounterRef\":\"%s\",\"hisId\":\"%s\",\"queue\":\"%s\"}",
                txnId, externalRef, effectiveHisId, intakeQueue != null ? intakeQueue : "OPD_INTAKE"
        );

        return new HisExportResult(
                true,
                txnId,
                externalRef,
                null,
                payload
        );
    }

    @Override
    public List<HisPatientCandidateDto> searchPatients(String query, String searchType) {
        log.info("Mock HIS Provider: Searching patients (query: {}, type: {})", query, searchType);

        if (query == null || query.isBlank() || query.equalsIgnoreCase("EMPTY") || query.equalsIgnoreCase("NOT_FOUND")) {
            return List.of();
        }

        List<HisPatientCandidateDto> candidates = getDeterministicCandidates();
        String q = query.trim().toLowerCase(Locale.ROOT);

        List<HisPatientCandidateDto> matched = new ArrayList<>();
        for (HisPatientCandidateDto candidate : candidates) {
            boolean match = false;
            if (candidate.getHisId().toLowerCase(Locale.ROOT).contains(q)) {
                match = true;
            } else if (candidate.getDisplayName().toLowerCase(Locale.ROOT).contains(q)) {
                match = true;
            } else if (candidate.getPhone() != null && candidate.getPhone().contains(q)) {
                match = true;
            }
            if (match) {
                matched.add(candidate);
            }
        }

        // If specific test query and no match found in canned data, create a deterministic synthetic match
        if (matched.isEmpty() && !q.contains("unknown") && !q.contains("none")) {
            matched.add(HisPatientCandidateDto.builder()
                    .hisId("HIS-MRN-" + Math.abs(query.hashCode() % 100000))
                    .displayName("Synthetic Mock Patient (" + query + ")")
                    .phone("9876543210")
                    .gender("MALE")
                    .dateOfBirth(LocalDate.of(1988, 5, 20))
                    .department("General Medicine")
                    .roomOrWard("OPD Room 101")
                    .build());
        }

        return matched;
    }

    @Override
    public HisPatientCandidateDto fetchPatientByHisId(String hisId) {
        log.info("Mock HIS Provider: Fetching patient by HIS ID: {}", hisId);

        if (hisId == null || hisId.isBlank() || hisId.equalsIgnoreCase("NOT_FOUND")) {
            return null;
        }

        for (HisPatientCandidateDto candidate : getDeterministicCandidates()) {
            if (candidate.getHisId().equalsIgnoreCase(hisId.trim())) {
                return candidate;
            }
        }

        // Synthetic candidate if starts with HIS-MRN
        if (hisId.toUpperCase(Locale.ROOT).startsWith("HIS-MRN-")) {
            return HisPatientCandidateDto.builder()
                    .hisId(hisId.trim().toUpperCase(Locale.ROOT))
                    .displayName("Hospital Patient " + hisId)
                    .phone("9876543210")
                    .gender("OTHER")
                    .dateOfBirth(LocalDate.of(1990, 1, 1))
                    .department("General Medicine")
                    .roomOrWard("Outpatient Clinic")
                    .build();
        }

        return null;
    }

    private List<HisPatientCandidateDto> getDeterministicCandidates() {
        return List.of(
                HisPatientCandidateDto.builder()
                        .hisId("HIS-MRN-90210")
                        .displayName("Aarav Sharma")
                        .phone("9876543210")
                        .gender("MALE")
                        .dateOfBirth(LocalDate.of(1985, 4, 12))
                        .department("Cardiology")
                        .roomOrWard("Room 302")
                        .build(),
                HisPatientCandidateDto.builder()
                        .hisId("HIS-MRN-84521")
                        .displayName("Priya Patel")
                        .phone("9123456780")
                        .gender("FEMALE")
                        .dateOfBirth(LocalDate.of(1992, 8, 25))
                        .department("Internal Medicine")
                        .roomOrWard("OPD Block B")
                        .build(),
                HisPatientCandidateDto.builder()
                        .hisId("HIS-MRN-77312")
                        .displayName("Rahul Verma")
                        .phone("9988776655")
                        .gender("MALE")
                        .dateOfBirth(LocalDate.of(1978, 11, 3))
                        .department("Orthopedics")
                        .roomOrWard("Ward 4")
                        .build()
        );
    }
}
