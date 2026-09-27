package com.medikiosk.clinical.adaptive;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Adaptive question recommendation returned to the patient kiosk interface.
 */
public class NextQuestionResponse {

    private UUID questionId;
    private String questionCode;
    private String questionText;
    private String questionType; // SCALE_0_10, YES_NO, CHOICE, TEXT
    private String category;
    private String clinicalConceptCode;
    private boolean safetyCritical;
    private int priority;
    private boolean completed;
    private String progressStage;

    private List<String> options = new ArrayList<>();
    private String reason;
    private List<String> triggeringConcepts = new ArrayList<>();
    private List<String> missingConcepts = new ArrayList<>();

    // Conversational AI fields
    private String intent;
    private String answerType;
    private boolean generated;
    private String provider = "MOCK";
    private String safetyLevel = "SAFE";
    private boolean requiresConfirmation;

    public NextQuestionResponse() {
    }

    public static NextQuestionResponse completed(String progressStage) {
        NextQuestionResponse res = new NextQuestionResponse();
        res.setCompleted(true);
        res.setProgressStage(progressStage);
        res.setQuestionText("Clinical intake questionnaire complete. Ready for review.");
        return res;
    }

    // Getters and Setters
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

    public String getClinicalConceptCode() {
        return clinicalConceptCode;
    }

    public void setClinicalConceptCode(String clinicalConceptCode) {
        this.clinicalConceptCode = clinicalConceptCode;
    }

    public boolean isSafetyCritical() {
        return safetyCritical;
    }

    public void setSafetyCritical(boolean safetyCritical) {
        this.safetyCritical = safetyCritical;
    }

    public int getPriority() {
        return priority;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }

    public boolean isCompleted() {
        return completed;
    }

    public void setCompleted(boolean completed) {
        this.completed = completed;
    }

    public String getProgressStage() {
        return progressStage;
    }

    public void setProgressStage(String progressStage) {
        this.progressStage = progressStage;
    }

    public List<String> getOptions() {
        return options;
    }

    public void setOptions(List<String> options) {
        this.options = options;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public List<String> getTriggeringConcepts() {
        return triggeringConcepts;
    }

    public void setTriggeringConcepts(List<String> triggeringConcepts) {
        this.triggeringConcepts = triggeringConcepts;
    }

    public List<String> getMissingConcepts() {
        return missingConcepts;
    }

    public void setMissingConcepts(List<String> missingConcepts) {
        this.missingConcepts = missingConcepts;
    }

    public String getIntent() {
        return intent;
    }

    public void setIntent(String intent) {
        this.intent = intent;
    }

    public String getAnswerType() {
        return answerType != null ? answerType : questionType;
    }

    public void setAnswerType(String answerType) {
        this.answerType = answerType;
    }

    public boolean isGenerated() {
        return generated;
    }

    public void setGenerated(boolean generated) {
        this.generated = generated;
    }

    public String getProvider() {
        return provider;
    }

    public void setProvider(String provider) {
        this.provider = provider;
    }

    public String getSafetyLevel() {
        return safetyLevel;
    }

    public void setSafetyLevel(String safetyLevel) {
        this.safetyLevel = safetyLevel;
    }

    public boolean isRequiresConfirmation() {
        return requiresConfirmation;
    }

    public void setRequiresConfirmation(boolean requiresConfirmation) {
        this.requiresConfirmation = requiresConfirmation;
    }
}
