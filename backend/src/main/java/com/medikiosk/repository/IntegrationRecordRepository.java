package com.medikiosk.repository;

import com.medikiosk.entity.IntegrationRecord;
import com.medikiosk.entity.enums.IntegrationStatus;
import com.medikiosk.entity.enums.IntegrationType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface IntegrationRecordRepository extends JpaRepository<IntegrationRecord, UUID> {
    List<IntegrationRecord> findByPatientId(UUID patientId);
    List<IntegrationRecord> findBySessionId(UUID sessionId);
    List<IntegrationRecord> findBySessionIdAndIntegrationType(UUID sessionId, IntegrationType integrationType);
    Optional<IntegrationRecord> findFirstBySessionIdAndIntegrationTypeOrderByAttemptedAtDesc(UUID sessionId, IntegrationType integrationType);
    Optional<IntegrationRecord> findByIdempotencyKey(String idempotencyKey);
    List<IntegrationRecord> findByIntegrationTypeAndStatus(IntegrationType integrationType, IntegrationStatus status);
    List<IntegrationRecord> findByStatus(IntegrationStatus status);
    long countByIntegrationType(IntegrationType integrationType);
}
