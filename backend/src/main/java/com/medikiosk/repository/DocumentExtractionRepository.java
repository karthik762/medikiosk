package com.medikiosk.repository;

import com.medikiosk.entity.DocumentExtraction;
import com.medikiosk.entity.enums.ExtractionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface DocumentExtractionRepository extends JpaRepository<DocumentExtraction, UUID> {
    List<DocumentExtraction> findByDocumentId(UUID documentId);
    List<DocumentExtraction> findByStatus(ExtractionStatus status);
}
