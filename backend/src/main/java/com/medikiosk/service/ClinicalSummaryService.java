package com.medikiosk.service;

import com.medikiosk.dto.clinical.ClinicalSummaryDto;
import com.medikiosk.entity.User;

import java.util.UUID;

/**
 * Service orchestrating AI clinical structuring and pre-consultation summary generation.
 * Conforms to docs/AI_SPEC.md and docs/PRODUCT_SPEC.md.
 */
public interface ClinicalSummaryService {

    /**
     * Ingests intake session data, triggers AI structuring, validates safety, and persists the clinical summary.
     *
     * @param sessionId   Unique intake session identifier.
     * @param currentUser Authenticated user initiating generation.
     * @return Generated and validated clinical summary DTO.
     */
    ClinicalSummaryDto generateSummary(UUID sessionId, User currentUser);

    /**
     * Retrieves the clinical summary associated with an intake session.
     *
     * @param sessionId   Unique intake session identifier.
     * @param currentUser Authenticated user requesting access.
     * @return Existing clinical summary DTO.
     */
    ClinicalSummaryDto getSummaryBySessionId(UUID sessionId, User currentUser);

    /**
     * Retrieves a clinical summary by its direct identifier.
     *
     * @param summaryId   Unique summary identifier.
     * @param currentUser Authenticated user requesting access.
     * @return Existing clinical summary DTO.
     */
    ClinicalSummaryDto getSummaryById(UUID summaryId, User currentUser);

    /**
     * Retrieves the latest clinical summary associated with a patient identifier.
     *
     * @param patientId   Unique patient identifier.
     * @param currentUser Authenticated user requesting access.
     * @return Existing clinical summary DTO.
     */
    ClinicalSummaryDto getSummaryByPatientId(UUID patientId, User currentUser);
}
