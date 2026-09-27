package com.medikiosk.repository;

import com.medikiosk.entity.SystemMetadata;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository interface for accessing system metadata records.
 */
@Repository
public interface SystemMetadataRepository extends JpaRepository<SystemMetadata, String> {
}
