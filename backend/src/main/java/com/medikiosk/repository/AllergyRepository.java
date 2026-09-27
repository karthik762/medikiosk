package com.medikiosk.repository;

import com.medikiosk.entity.Allergy;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface AllergyRepository extends JpaRepository<Allergy, UUID> {
    List<Allergy> findByPatientId(UUID patientId);
    List<Allergy> findBySessionId(UUID sessionId);
    List<Allergy> findByPatientIdAndVerifiedTrue(UUID patientId);
}
