package com.medikiosk.ayurveda;

import java.util.Map;
import java.util.UUID;

/**
 * Architectural interface for Ayurveda-integrated clinical intake.
 * Conforms to docs/AYURVEDA_SPEC.md.
 */
public interface AyurvedaIntakeService {

    /**
     * Records Ayurvedic clinical intake parameters for an intake session.
     *
     * @param intakeSessionId Intake session identifier.
     * @param ayurvedaData    Ayurvedic parameters (Agni, Koshtha, Prakriti indicators, habits).
     */
    void saveAyurvedaIntake(UUID intakeSessionId, Map<String, Object> ayurvedaData);

    /**
     * Retrieves Ayurvedic intake parameters for clinical review.
     *
     * @param intakeSessionId Intake session identifier.
     * @return Map of recorded Ayurvedic assessment parameters.
     */
    Map<String, Object> getAyurvedaIntake(UUID intakeSessionId);
}
