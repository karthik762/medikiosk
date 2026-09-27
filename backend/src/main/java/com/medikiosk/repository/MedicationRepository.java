package com.medikiosk.repository;

import com.medikiosk.entity.Medication;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface MedicationRepository extends JpaRepository<Medication, UUID> {
    List<Medication> findByPatientId(UUID patientId);
    List<Medication> findBySessionId(UUID sessionId);
    List<Medication> findByPatientIdAndVerifiedTrue(UUID patientId);
}
