package com.medikiosk.repository;

import com.medikiosk.entity.Question;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface QuestionRepository extends JpaRepository<Question, UUID> {
    Optional<Question> findByQuestionCode(String questionCode);
    List<Question> findByCategoryAndActiveTrue(String category);
    List<Question> findByActiveTrue();
    List<Question> findByClinicalConceptCodeAndActiveTrueOrderByPriorityAsc(String clinicalConceptCode);
    List<Question> findBySafetyCriticalTrueAndActiveTrueOrderByPriorityAsc();
}
