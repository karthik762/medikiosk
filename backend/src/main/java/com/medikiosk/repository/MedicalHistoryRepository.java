package com.medikiosk.repository;

import com.medikiosk.entity.MedicalHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface MedicalHistoryRepository extends JpaRepository<MedicalHistory, UUID> {
    List<MedicalHistory> findByPatientId(UUID patientId);
    List<MedicalHistory> findBySessionId(UUID sessionId);
    List<MedicalHistory> findByPatientIdAndCategory(UUID patientId, String category);
}
