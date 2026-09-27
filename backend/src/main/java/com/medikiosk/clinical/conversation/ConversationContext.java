package com.medikiosk.clinical.conversation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Manages the multi-turn conversational context of an intake session,
 * enforcing bounded memory to avoid unbounded LLM context growth while preserving clinical continuity.
 */
public class ConversationContext {

    private static final int DEFAULT_MAX_TURNS = 20;

    private UUID sessionId;
    private String language = "en";
    private String chiefComplaint;
    private final List<ConversationTurn> turns = new ArrayList<>();

    public ConversationContext() {
    }

    public ConversationContext(UUID sessionId, String language) {
        this.sessionId = sessionId;
        this.language = language != null ? language : "en";
    }

    public synchronized void addTurn(ConversationTurn turn) {
        if (turn != null) {
            turn.setTurnIndex(turns.size() + 1);
            turns.add(turn);
        }
    }

    public synchronized void addPatientTurn(String text, String inputMethod) {
        if (text != null && !text.trim().isEmpty()) {
            addTurn(ConversationTurn.patient(turns.size() + 1, text.trim(), inputMethod));
        }
    }

    public synchronized void addAiTurn(String text, String intent) {
        if (text != null && !text.trim().isEmpty()) {
            addTurn(ConversationTurn.ai(turns.size() + 1, text.trim(), intent));
        }
    }

    public List<ConversationTurn> getTurns() {
        return Collections.unmodifiableList(turns);
    }

    /**
     * Retrieves the most recent N turns for prompt inclusion, bounding context size.
     */
    public List<ConversationTurn> getRecentTurns(int limit) {
        int start = Math.max(0, turns.size() - limit);
        return turns.subList(start, turns.size());
    }

    /**
     * Formats bounded conversational history for prompt consumption.
     */
    public String toFormattedHistory(int maxTurns) {
        List<ConversationTurn> recent = getRecentTurns(maxTurns);
        if (recent.isEmpty()) {
            return "(No prior conversation)";
        }
        return recent.stream()
                .map(t -> String.format("[%s]: %s", t.getSpeaker().name(), t.getText()))
                .collect(Collectors.joining("\n"));
    }

    // Getters and Setters
    public UUID getSessionId() {
        return sessionId;
    }

    public void setSessionId(UUID sessionId) {
        this.sessionId = sessionId;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public String getChiefComplaint() {
        return chiefComplaint;
    }

    public void setChiefComplaint(String chiefComplaint) {
        this.chiefComplaint = chiefComplaint;
    }

    public int size() {
        return turns.size();
    }
}
