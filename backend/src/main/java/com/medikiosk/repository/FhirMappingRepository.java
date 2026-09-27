package com.medikiosk.repository;

import com.medikiosk.entity.FhirMapping;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface FhirMappingRepository extends JpaRepository<FhirMapping, UUID> {
    Optional<FhirMapping> findByEntityTypeAndEntityId(String entityType, UUID entityId);
    Optional<FhirMapping> findByFhirResourceTypeAndFhirResourceId(String fhirResourceType, String fhirResourceId);
    List<FhirMapping> findBySyncStatus(String syncStatus);
    List<FhirMapping> findBySessionId(UUID sessionId);
    Optional<FhirMapping> findBySessionIdAndFhirResourceType(UUID sessionId, String fhirResourceType);
    List<FhirMapping> findBySessionIdAndValidationStatus(UUID sessionId, String validationStatus);
    void deleteBySessionId(UUID sessionId);
}
