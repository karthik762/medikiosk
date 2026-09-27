package com.medikiosk.repository;

import com.medikiosk.entity.TriageResult;
import com.medikiosk.entity.enums.TriagePriority;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface TriageResultRepository extends JpaRepository<TriageResult, UUID> {
    Optional<TriageResult> findBySessionId(UUID sessionId);
    List<TriageResult> findByPriority(TriagePriority priority);
}
