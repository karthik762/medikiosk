package com.medikiosk.fhir;

import com.medikiosk.dto.fhir.FhirExportSummaryDto;
import com.medikiosk.dto.fhir.FhirValidationResponseDto;
import com.medikiosk.fhir.model.FhirBundle;
import com.medikiosk.fhir.model.FhirEncounter;
import com.medikiosk.fhir.model.FhirPatient;

import java.util.UUID;

/**
 * Service for mapping MediKiosk internal relational clinical entities to FHIR R4 resources,
 * validating compliance with FHIR standards and invariants, and exporting verified clinical summaries.
 * Conforms to docs/FHIR_MAPPING.md and Phase 11 requirements.
 */
public interface FhirMappingService {

    /**
     * Builds structured FHIR Patient resource.
     */
    FhirPatient buildPatientResource(UUID patientId);

    /**
     * Generates a FHIR Patient resource JSON representation.
     *
     * @param patientId Unique internal patient identifier.
     * @return FHIR JSON string.
     */
    String toFhirPatient(UUID patientId);

    /**
     * Builds structured FHIR Encounter resource.
     */
    FhirEncounter buildEncounterResource(UUID intakeSessionId);

    /**
     * Generates a FHIR Encounter resource JSON representation for an intake session.
     *
     * @param intakeSessionId Unique intake session identifier.
     * @return FHIR JSON string.
     */
    String toFhirEncounter(UUID intakeSessionId);

    /**
     * Builds structured FHIR Bundle resource containing Composition and all referenced clinical resources.
     */
    FhirBundle buildClinicalBundle(UUID intakeSessionId);

    /**
     * Generates a FHIR Composition / Bundle JSON string for the intake session.
     *
     * @param intakeSessionId Unique intake session identifier.
     * @return FHIR Bundle JSON string.
     */
    String toFhirClinicalBundle(UUID intakeSessionId);

    /**
     * Generates, validates, and stores FHIR Bundle export with audit logging and integration record tracking.
     *
     * @param intakeSessionId Unique intake session identifier.
     * @param requestingUserId Authenticated user requesting the export.
     * @return Summary of exported FHIR bundle.
     */
    FhirExportSummaryDto exportClinicalBundle(UUID intakeSessionId, UUID requestingUserId);

    /**
     * Retrieves stored export summary for an intake session.
     *
     * @param intakeSessionId Unique intake session identifier.
     * @return Stored export summary or newly generated preview if not yet exported.
     */
    FhirExportSummaryDto getStoredExportSummary(UUID intakeSessionId);

    /**
     * Performs FHIR validation against session clinical resources and bundle.
     *
     * @param intakeSessionId Unique intake session identifier.
     * @return Validation details and errors/warnings.
     */
    FhirValidationResponseDto validateBundleForSession(UUID intakeSessionId);
}
