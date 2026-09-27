package com.medikiosk.repository;

import com.medikiosk.entity.Answer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface AnswerRepository extends JpaRepository<Answer, UUID> {
    List<Answer> findBySessionId(UUID sessionId);
    List<Answer> findBySessionIdAndQuestionId(UUID sessionId, UUID questionId);
}
