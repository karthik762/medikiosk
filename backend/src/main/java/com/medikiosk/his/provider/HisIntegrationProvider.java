package com.medikiosk.his.provider;

import com.medikiosk.his.dto.HisPatientCandidateDto;
import com.medikiosk.his.dto.HisStatusDto;

import java.util.List;

/**
 * Common abstraction for Hospital Information System (HIS) integration providers.
 * Conforms to docs/HIS_SPEC.md requirements.
 */
public interface HisIntegrationProvider {

    /**
     * Checks provider connection, health, and configured operating mode.
     */
    HisStatusDto getStatus();

    /**
     * Transmits a doctor-verified FHIR clinical document bundle to the hospital HIS intake queue.
     *
     * @param fhirBundleJson FHIR R4 Document Bundle JSON.
     * @param hisId          Hospital Patient MRN / ID (if known).
     * @param intakeQueue    Designated intake queue (e.g., OPD, TRIAGE, EMERGENCY).
     * @param idempotencyKey Key to prevent duplicate transmissions.
     * @return Result of the transmission.
     */
    HisExportResult exportClinicalBundle(String fhirBundleJson, String hisId, String intakeQueue, String idempotencyKey);

    /**
     * Searches the hospital master patient index for patient candidates by name, phone, or MRN.
     *
     * @param query      Search query term.
     * @param searchType Search field (e.g. PHONE, NAME, MRN).
     * @return List of matching patient candidates.
     */
    List<HisPatientCandidateDto> searchPatients(String query, String searchType);

    /**
     * Fetches details of a hospital patient by their HIS MRN / ID.
     *
     * @param hisId Hospital patient identifier.
     * @return Patient candidate details, or null if not found.
     */
    HisPatientCandidateDto fetchPatientByHisId(String hisId);
}
