package com.medikiosk.repository;

import com.medikiosk.entity.ClinicalSummary;
import com.medikiosk.entity.enums.ClinicalSummaryStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ClinicalSummaryRepository extends JpaRepository<ClinicalSummary, UUID> {
    Optional<ClinicalSummary> findBySessionId(UUID sessionId);
    List<ClinicalSummary> findByPatientId(UUID patientId);
    List<ClinicalSummary> findByStatus(ClinicalSummaryStatus status);
    long countByStatus(ClinicalSummaryStatus status);
}
