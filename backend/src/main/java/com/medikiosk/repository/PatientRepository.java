package com.medikiosk.repository;

import com.medikiosk.entity.Patient;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface PatientRepository extends JpaRepository<Patient, UUID> {
    Optional<Patient> findByPatientIdentifier(String patientIdentifier);
    Optional<Patient> findByPhone(String phone);
    Optional<Patient> findByAbhaId(String abhaId);
    boolean existsByPatientIdentifier(String patientIdentifier);
}
