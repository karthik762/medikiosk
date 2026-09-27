package com.medikiosk.dto.intake;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.UUID;

public class AnswerRequest {

    @NotNull(message = "Question ID is required")
    private UUID questionId;

    @NotBlank(message = "Answer text is required")
    private String answerText;

    private String inputMethod = "TEXT";
    private String originalTranscription;
    private BigDecimal confidenceScore;
    private Boolean confirmedByPatient;

    public AnswerRequest() {
    }

    public AnswerRequest(UUID questionId, String answerText, String inputMethod) {
        this.questionId = questionId;
        this.answerText = answerText;
        this.inputMethod = inputMethod != null ? inputMethod : "TEXT";
    }

    public AnswerRequest(UUID questionId, String answerText, String inputMethod,
                         String originalTranscription, BigDecimal confidenceScore, Boolean confirmedByPatient) {
        this.questionId = questionId;
        this.answerText = answerText;
        this.inputMethod = inputMethod != null ? inputMethod : "TEXT";
        this.originalTranscription = originalTranscription;
        this.confidenceScore = confidenceScore;
        this.confirmedByPatient = confirmedByPatient;
    }

    public UUID getQuestionId() {
        return questionId;
    }

    public void setQuestionId(UUID questionId) {
        this.questionId = questionId;
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
}
