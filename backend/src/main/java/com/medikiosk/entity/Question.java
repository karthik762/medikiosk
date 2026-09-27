package com.medikiosk.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

/**
 * Approved clinical question bank entity for adaptive questioning.
 */
@Entity
@Table(name = "questions")
public class Question extends BaseEntity {

    @Column(name = "question_code", nullable = false, unique = true, length = 64)
    private String questionCode;

    @Column(name = "question_text", nullable = false, columnDefinition = "TEXT")
    private String questionText;

    @Column(name = "question_type", nullable = false, length = 32)
    private String questionType;

    @Column(name = "category", length = 64)
    private String category;

    @Column(name = "language", length = 32)
    private String language = "en";

    @Column(name = "required")
    private Boolean required = true;

    @Column(name = "active")
    private Boolean active = true;

    @Column(name = "clinical_concept_code", length = 64)
    private String clinicalConceptCode;

    @Column(name = "safety_critical")
    private Boolean safetyCritical = false;

    @Column(name = "priority")
    private Integer priority = 50;

    @Column(name = "purpose", length = 128)
    private String purpose;

    @Column(name = "version")
    private Integer version = 1;

    public Question() {
    }

    public Question(String questionCode, String questionText, String questionType, String category) {
        this.questionCode = questionCode;
        this.questionText = questionText;
        this.questionType = questionType;
        this.category = category;
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

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }

    public String getClinicalConceptCode() {
        return clinicalConceptCode;
    }

    public void setClinicalConceptCode(String clinicalConceptCode) {
        this.clinicalConceptCode = clinicalConceptCode;
    }

    public Boolean getSafetyCritical() {
        return safetyCritical != null ? safetyCritical : false;
    }

    public void setSafetyCritical(Boolean safetyCritical) {
        this.safetyCritical = safetyCritical;
    }

    public Integer getPriority() {
        return priority != null ? priority : 50;
    }

    public void setPriority(Integer priority) {
        this.priority = priority;
    }

    public String getPurpose() {
        return purpose;
    }

    public void setPurpose(String purpose) {
        this.purpose = purpose;
    }

    public Integer getVersion() {
        return version != null ? version : 1;
    }

    public void setVersion(Integer version) {
        this.version = version;
    }
}
