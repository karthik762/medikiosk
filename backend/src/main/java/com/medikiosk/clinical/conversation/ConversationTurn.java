package com.medikiosk.clinical.conversation;

import java.time.Instant;

/**
 * Represents a single turn in an AI-assisted clinical intake dialogue.
 */
public class ConversationTurn {

    public enum Speaker {
        PATIENT,
        SYSTEM,
        AI_GENERATED
    }

    private int turnIndex;
    private Speaker speaker;
    private String text;
    private String intent;
    private String inputMethod;
    private Instant timestamp;
    private Double confidenceScore;

    public ConversationTurn() {
        this.timestamp = Instant.now();
    }

    public ConversationTurn(int turnIndex, Speaker speaker, String text, String intent, String inputMethod) {
        this.turnIndex = turnIndex;
        this.speaker = speaker;
        this.text = text;
        this.intent = intent;
        this.inputMethod = inputMethod != null ? inputMethod : "TEXT";
        this.timestamp = Instant.now();
        this.confidenceScore = 1.0;
    }

    public static ConversationTurn patient(int index, String text, String inputMethod) {
        return new ConversationTurn(index, Speaker.PATIENT, text, null, inputMethod);
    }

    public static ConversationTurn patient(String text) {
        return new ConversationTurn(0, Speaker.PATIENT, text, null, "TEXT");
    }

    public static ConversationTurn ai(int index, String text, String intent) {
        return new ConversationTurn(index, Speaker.AI_GENERATED, text, intent, "AI");
    }

    public static ConversationTurn system(int index, String text) {
        return new ConversationTurn(index, Speaker.SYSTEM, text, null, "SYSTEM");
    }

    public static ConversationTurn system(String text) {
        return new ConversationTurn(0, Speaker.SYSTEM, text, null, "SYSTEM");
    }

    // Getters and Setters
    public int getTurnIndex() {
        return turnIndex;
    }

    public void setTurnIndex(int turnIndex) {
        this.turnIndex = turnIndex;
    }

    public Speaker getSpeaker() {
        return speaker;
    }

    public void setSpeaker(Speaker speaker) {
        this.speaker = speaker;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getIntent() {
        return intent;
    }

    public void setIntent(String intent) {
        this.intent = intent;
    }

    public String getInputMethod() {
        return inputMethod;
    }

    public void setInputMethod(String inputMethod) {
        this.inputMethod = inputMethod;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Instant timestamp) {
        this.timestamp = timestamp;
    }

    public Double getConfidenceScore() {
        return confidenceScore;
    }

    public void setConfidenceScore(Double confidenceScore) {
        this.confidenceScore = confidenceScore;
    }
}
