package com.medikiosk.repository;

import com.medikiosk.entity.IntakeSession;
import com.medikiosk.entity.enums.IntakeSessionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface IntakeSessionRepository extends JpaRepository<IntakeSession, UUID> {
    List<IntakeSession> findByPatientId(UUID patientId);
    List<IntakeSession> findByStatus(IntakeSessionStatus status);
    List<IntakeSession> findByPatientIdAndStatus(UUID patientId, IntakeSessionStatus status);
    long countByStatus(IntakeSessionStatus status);
}
