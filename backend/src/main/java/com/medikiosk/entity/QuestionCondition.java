package com.medikiosk.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

/**
 * Defines adaptive conditional branching logic between questions.
 */
@Entity
@Table(name = "question_conditions")
public class QuestionCondition {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "question_id", nullable = false)
    private Question question;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "dependent_question_id", nullable = false)
    private Question dependentQuestion;

    @Column(name = "condition_type", nullable = false, length = 64)
    private String conditionType;

    @Column(name = "condition_value", columnDefinition = "TEXT")
    private String conditionValue;

    @Column(name = "action", nullable = false, length = 32)
    private String action;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = Instant.now();
    }

    public QuestionCondition() {
    }

    public QuestionCondition(Question question, Question dependentQuestion, String conditionType, String conditionValue, String action) {
        this.question = question;
        this.dependentQuestion = dependentQuestion;
        this.conditionType = conditionType;
        this.conditionValue = conditionValue;
        this.action = action;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public Question getQuestion() {
        return question;
    }

    public void setQuestion(Question question) {
        this.question = question;
    }

    public Question getDependentQuestion() {
        return dependentQuestion;
    }

    public void setDependentQuestion(Question dependentQuestion) {
        this.dependentQuestion = dependentQuestion;
    }

    public String getConditionType() {
        return conditionType;
    }

    public void setConditionType(String conditionType) {
        this.conditionType = conditionType;
    }

    public String getConditionValue() {
        return conditionValue;
    }

    public void setConditionValue(String conditionValue) {
        this.conditionValue = conditionValue;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }
}
