package com.medikiosk.his.adapter;

import java.util.UUID;

/**
 * Architectural adapter boundary for Hospital Information System (HIS) integration.
 * Conforms to docs/HIS_SPEC.md requirements.
 */
public interface HisAdapter {

    /**
     * Checks if the hospital information system connection is configured and available.
     *
     * @return true if available, false otherwise.
     */
    boolean isAvailable();

    /**
     * Synchronizes verified intake summary to HIS.
     *
     * @param doctorId        Authenticated doctor/admin user identifier.
     * @param intakeSessionId Intake session identifier.
     * @param clinicalSummary Verified clinical summary text/data.
     * @return External reference identifier returned by HIS.
     */
    String exportToIntakeQueue(UUID doctorId, UUID intakeSessionId, String clinicalSummary);
}
