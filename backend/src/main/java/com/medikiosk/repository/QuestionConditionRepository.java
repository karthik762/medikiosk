package com.medikiosk.repository;

import com.medikiosk.entity.QuestionCondition;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface QuestionConditionRepository extends JpaRepository<QuestionCondition, UUID> {
    List<QuestionCondition> findByQuestionId(UUID questionId);
    List<QuestionCondition> findByDependentQuestionId(UUID dependentQuestionId);
}
