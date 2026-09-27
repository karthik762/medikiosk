package com.medikiosk.triage;

import com.medikiosk.entity.Answer;
import com.medikiosk.entity.IntakeSession;
import com.medikiosk.entity.MedicalHistory;
import com.medikiosk.entity.Symptom;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Encapsulates structured clinical intake data provided to the triage engine for screening evaluation.
 * Conforms to docs/TRIAGE_RULES.md (Sections 8, 10).
 */
public class TriageInputContext {

    private final IntakeSession session;
    private final List<Symptom> symptoms;
    private final List<Answer> answers;
    private final List<MedicalHistory> medicalHistories;
    private final Map<String, Object> parameters;

    public TriageInputContext(IntakeSession session,
                              List<Symptom> symptoms,
                              List<Answer> answers,
                              List<MedicalHistory> medicalHistories,
                              Map<String, Object> parameters) {
        this.session = session;
        this.symptoms = symptoms != null ? symptoms : new ArrayList<>();
        this.answers = answers != null ? answers : new ArrayList<>();
        this.medicalHistories = medicalHistories != null ? medicalHistories : new ArrayList<>();
        this.parameters = parameters != null ? parameters : new HashMap<>();
    }

    public IntakeSession getSession() {
        return session;
    }

    public List<Symptom> getSymptoms() {
        return symptoms;
    }

    public List<Answer> getAnswers() {
        return answers;
    }

    public List<MedicalHistory> getMedicalHistories() {
        return medicalHistories;
    }

    public Map<String, Object> getParameters() {
        return parameters;
    }
}
