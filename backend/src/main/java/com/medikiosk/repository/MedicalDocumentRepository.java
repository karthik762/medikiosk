package com.medikiosk.repository;

import com.medikiosk.entity.MedicalDocument;
import com.medikiosk.entity.enums.DocumentProcessingStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface MedicalDocumentRepository extends JpaRepository<MedicalDocument, UUID> {
    List<MedicalDocument> findByPatientId(UUID patientId);
    List<MedicalDocument> findBySessionId(UUID sessionId);
    List<MedicalDocument> findByProcessingStatus(DocumentProcessingStatus processingStatus);
}
