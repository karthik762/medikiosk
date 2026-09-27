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

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

/**
 * Stores patient intake question responses, preserving original speech transcription and input channel.
 */
@Entity
@Table(name = "answers")
public class Answer {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "session_id", nullable = false)
    private IntakeSession session;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "question_id", nullable = false)
    private Question question;

    @Column(name = "answer_text", nullable = false, columnDefinition = "TEXT")
    private String answerText;

    @Column(name = "input_method", length = 32)
    private String inputMethod;

    @Column(name = "original_transcription", columnDefinition = "TEXT")
    private String originalTranscription;

    @Column(name = "confidence_score", precision = 5, scale = 4)
    private BigDecimal confidenceScore;

    @Column(name = "confirmed_by_patient")
    private Boolean confirmedByPatient = true;

    @Column(name = "answered_at", nullable = false)
    private Instant answeredAt;

    @PrePersist
    protected void onCreate() {
        if (this.answeredAt == null) {
            this.answeredAt = Instant.now();
        }
    }

    public Answer() {
    }

    public Answer(IntakeSession session, Question question, String answerText, String inputMethod) {
        this.session = session;
        this.question = question;
        this.answerText = answerText;
        this.inputMethod = inputMethod;
        this.answeredAt = Instant.now();
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public IntakeSession getSession() {
        return session;
    }

    public void setSession(IntakeSession session) {
        this.session = session;
    }

    public Question getQuestion() {
        return question;
    }

    public void setQuestion(Question question) {
        this.question = question;
    }

    public String getAnswerText() {
        return answerText;
    }

    public void setAnswerText(String answerText) {
        this.answerText = answerText;
    }

    public String getInputMethod() {
        return inputMethod;
    }

    public void setInputMethod(String inputMethod) {
        this.inputMethod = inputMethod;
    }

    public String getOriginalTranscription() {
        return originalTranscription;
    }

    public void setOriginalTranscription(String originalTranscription) {
        this.originalTranscription = originalTranscription;
    }

    public BigDecimal getConfidenceScore() {
        return confidenceScore;
    }

    public void setConfidenceScore(BigDecimal confidenceScore) {
        this.confidenceScore = confidenceScore;
    }

    public Boolean getConfirmedByPatient() {
        return confirmedByPatient;
    }

    public void setConfirmedByPatient(Boolean confirmedByPatient) {
        this.confirmedByPatient = confirmedByPatient;
    }

    public Instant getAnsweredAt() {
        return answeredAt;
    }

    public void setAnsweredAt(Instant answeredAt) {
        this.answeredAt = answeredAt;
    }
}
