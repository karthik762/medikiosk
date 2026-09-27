package com.medikiosk.dto.intake;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public class AnswerResponse {

    private UUID id;
    private UUID sessionId;
    private UUID questionId;
    private String questionCode;
    private String answerText;
    private String inputMethod;
    private String originalTranscription;
    private BigDecimal confidenceScore;
    private Boolean confirmedByPatient;
    private Instant answeredAt;

    public AnswerResponse() {
    }

    public AnswerResponse(UUID id, UUID sessionId, UUID questionId, String questionCode,
                          String answerText, String inputMethod, Instant answeredAt) {
        this(id, sessionId, questionId, questionCode, answerText, inputMethod, null, null, true, answeredAt);
    }

    public AnswerResponse(UUID id, UUID sessionId, UUID questionId, String questionCode,
                          String answerText, String inputMethod, String originalTranscription,
                          BigDecimal confidenceScore, Boolean confirmedByPatient, Instant answeredAt) {
        this.id = id;
        this.sessionId = sessionId;
        this.questionId = questionId;
        this.questionCode = questionCode;
        this.answerText = answerText;
        this.inputMethod = inputMethod;
        this.originalTranscription = originalTranscription;
        this.confidenceScore = confidenceScore;
        this.confirmedByPatient = confirmedByPatient;
        this.answeredAt = answeredAt;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getSessionId() {
        return sessionId;
    }

    public void setSessionId(UUID sessionId) {
        this.sessionId = sessionId;
    }

    public UUID getQuestionId() {
        return questionId;
    }

    public void setQuestionId(UUID questionId) {
        this.questionId = questionId;
    }

    public String getQuestionCode() {
        return questionCode;
    }

    public void setQuestionCode(String questionCode) {
        this.questionCode = questionCode;
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
