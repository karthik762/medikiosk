package com.medikiosk.repository;

import com.medikiosk.entity.AyurvedaHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface AyurvedaHistoryRepository extends JpaRepository<AyurvedaHistory, UUID> {
    Optional<AyurvedaHistory> findBySessionId(UUID sessionId);
    List<AyurvedaHistory> findByPatientId(UUID patientId);
}
