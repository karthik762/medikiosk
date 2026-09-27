package com.medikiosk.repository;

import com.medikiosk.entity.Symptom;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface SymptomRepository extends JpaRepository<Symptom, UUID> {
    List<Symptom> findBySessionId(UUID sessionId);
    List<Symptom> findBySessionIdAndVerifiedTrue(UUID sessionId);
}
