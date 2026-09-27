package com.medikiosk.repository;

import com.medikiosk.entity.DoctorReview;
import com.medikiosk.entity.enums.DoctorReviewAction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface DoctorReviewRepository extends JpaRepository<DoctorReview, UUID> {
    List<DoctorReview> findBySummaryId(UUID summaryId);
    List<DoctorReview> findByDoctorId(UUID doctorId);
    List<DoctorReview> findBySummaryIdAndAction(UUID summaryId, DoctorReviewAction action);
    List<DoctorReview> findBySummaryIdOrderByCreatedAtDesc(UUID summaryId);
    Optional<DoctorReview> findFirstBySummaryIdOrderByCreatedAtDesc(UUID summaryId);
}
