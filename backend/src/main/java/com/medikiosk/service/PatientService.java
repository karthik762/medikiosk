package com.medikiosk.service;

import com.medikiosk.dto.intake.PatientRequest;
import com.medikiosk.dto.intake.PatientResponse;
import com.medikiosk.entity.User;

import java.util.UUID;

/**
 * Service interface for patient registration and identity management.
 */
public interface PatientService {

    /**
     * Registers a new patient.
     *
     * @param request Patient registration details
     * @param authenticatedUser The currently authenticated user context
     * @return Created PatientResponse DTO
     */
    PatientResponse registerPatient(PatientRequest request, User authenticatedUser);

    /**
     * Retrieves patient profile by unique primary ID.
     *
     * @param patientId Unique patient UUID
     * @param authenticatedUser The currently authenticated user context
     * @return PatientResponse DTO
     */
    PatientResponse getPatient(UUID patientId, User authenticatedUser);

    /**
     * Retrieves patient profile by unique patient identifier.
     *
     * @param patientIdentifier Human-readable unique patient identifier
     * @param authenticatedUser The currently authenticated user context
     * @return PatientResponse DTO
     */
    PatientResponse getPatientByIdentifier(String patientIdentifier, User authenticatedUser);
}
