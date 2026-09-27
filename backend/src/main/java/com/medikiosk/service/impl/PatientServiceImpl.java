package com.medikiosk.service.impl;

import com.medikiosk.dto.intake.PatientRequest;
import com.medikiosk.dto.intake.PatientResponse;
import com.medikiosk.entity.Patient;
import com.medikiosk.entity.User;
import com.medikiosk.entity.enums.UserRole;
import com.medikiosk.exception.ApiException;
import com.medikiosk.exception.ResourceNotFoundException;
import com.medikiosk.repository.PatientRepository;
import com.medikiosk.service.PatientService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.UUID;

@Service
public class PatientServiceImpl implements PatientService {

    private static final Logger log = LoggerFactory.getLogger(PatientServiceImpl.class);

    private final PatientRepository patientRepository;

    public PatientServiceImpl(PatientRepository patientRepository) {
        this.patientRepository = patientRepository;
    }

    @Override
    @Transactional
    public PatientResponse registerPatient(PatientRequest request, User authenticatedUser) {
        String identifier = request.getPatientIdentifier();
        if (StringUtils.hasText(identifier)) {
            identifier = identifier.trim();
            if (patientRepository.existsByPatientIdentifier(identifier)) {
                log.warn("Registration rejected: Duplicate patient identifier {}", identifier);
                throw new ApiException("Patient with identifier " + identifier + " already exists", HttpStatus.CONFLICT);
            }
        } else {
            identifier = "MK-PAT-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        }

        Patient patient = new Patient(identifier, request.getFirstName().trim(), request.getLastName().trim());
        patient.setDateOfBirth(request.getDateOfBirth());
        patient.setGender(request.getGender());
        patient.setPhone(request.getPhone());
        patient.setEmail(request.getEmail());

        if (authenticatedUser != null && authenticatedUser.getRole() == UserRole.PATIENT) {
            patient.setUserId(authenticatedUser.getId());
        }

        Patient saved = patientRepository.save(patient);
        log.info("Patient registered successfully with identifier: {}", saved.getPatientIdentifier());

        return mapToResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public PatientResponse getPatient(UUID patientId, User authenticatedUser) {
        Patient patient = patientRepository.findById(patientId)
                .orElseThrow(() -> new ResourceNotFoundException("Patient not found with id: " + patientId));

        validatePatientAccess(patient, authenticatedUser);
        return mapToResponse(patient);
    }

    @Override
    @Transactional(readOnly = true)
    public PatientResponse getPatientByIdentifier(String patientIdentifier, User authenticatedUser) {
        Patient patient = patientRepository.findByPatientIdentifier(patientIdentifier)
                .orElseThrow(() -> new ResourceNotFoundException("Patient not found with identifier: " + patientIdentifier));

        validatePatientAccess(patient, authenticatedUser);
        return mapToResponse(patient);
    }

    private void validatePatientAccess(Patient patient, User authenticatedUser) {
        if (authenticatedUser == null) {
            throw new ApiException("Authentication required", HttpStatus.UNAUTHORIZED);
        }
        if (authenticatedUser.getRole() == UserRole.PATIENT) {
            if (patient.getUserId() == null || !patient.getUserId().equals(authenticatedUser.getId())) {
                log.warn("Patient access denied: User {} tried accessing patient {}", authenticatedUser.getId(), patient.getId());
                throw new ApiException("Access denied: You do not own this patient record", HttpStatus.FORBIDDEN);
            }
        }
    }

    private PatientResponse mapToResponse(Patient patient) {
        return new PatientResponse(
                patient.getId(),
                patient.getPatientIdentifier(),
                patient.getFirstName(),
                patient.getLastName(),
                patient.getDateOfBirth(),
                patient.getGender(),
                patient.getPhone(),
                patient.getEmail(),
                patient.getUserId()
        );
    }
}
