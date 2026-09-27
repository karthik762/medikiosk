package com.medikiosk.patient;

import java.util.Map;
import java.util.UUID;

/**
 * Architectural interface for patient registration and identity management.
 */
public interface PatientService {

    /**
     * Registers a new patient or retrieves existing profile.
     *
     * @param patientData Demographic and contact details.
     * @return Unique patient identifier.
     */
    UUID registerPatient(Map<String, Object> patientData);

    /**
     * Retrieves patient profile by identifier.
     *
     * @param patientId Patient identifier.
     * @return Map containing demographic and identification details.
     */
    Map<String, Object> getPatientById(UUID patientId);
}
