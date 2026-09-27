package com.medikiosk.dto.intake;

import java.util.UUID;

public class QuestionResponse {

    private UUID id;
    private String questionCode;
    private String questionText;
    private String questionType;
    private String category;
    private String language;
    private Boolean required;

    public QuestionResponse() {
    }

    public QuestionResponse(UUID id, String questionCode, String questionText, String questionType,
                            String category, String language, Boolean required) {
        this.id = id;
        this.questionCode = questionCode;
        this.questionText = questionText;
        this.questionType = questionType;
        this.category = category;
        this.language = language;
        this.required = required;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getQuestionCode() {
        return questionCode;
    }

    public void setQuestionCode(String questionCode) {
        this.questionCode = questionCode;
    }

    public String getQuestionText() {
        return questionText;
    }

    public void setQuestionText(String questionText) {
        this.questionText = questionText;
    }

    public String getQuestionType() {
        return questionType;
    }

    public void setQuestionType(String questionType) {
        this.questionType = questionType;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public Boolean getRequired() {
        return required;
    }

    public void setRequired(Boolean required) {
        this.required = required;
    }
}
