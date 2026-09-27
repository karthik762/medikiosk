package com.medikiosk.clinical.adaptive;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Controlled taxonomy of clinical information intents with standard safety ratings
 * and pre-approved clinical fallback templates.
 */
public enum QuestionIntent {

    // --- CHEST PAIN ---
    CHEST_PAIN_CHARACTER(
            "CHEST_PAIN_CHARACTER",
            "CHEST_PAIN",
            "Can you describe what the chest pain feels like — such as pressure, tightness, burning, or something else?",
            "CHOICE",
            90,
            false
    ),
    CHEST_PAIN_RADIATION(
            "CHEST_PAIN_RADIATION",
            "CHEST_PAIN",
            "Does the chest discomfort spread anywhere, such as to your left arm, jaw, neck, or back?",
            "YES_NO",
            95,
            true
    ),
    CHEST_PAIN_SEVERITY(
            "CHEST_PAIN_SEVERITY",
            "CHEST_PAIN",
            "On a scale from 0 to 10, where 0 is no pain and 10 is the worst pain imaginable, how severe is your chest discomfort right now?",
            "SCALE_0_10",
            100,
            true
    ),
    CHEST_PAIN_DURATION(
            "CHEST_PAIN_DURATION",
            "CHEST_PAIN",
            "Can you tell me approximately when the chest discomfort started or how long it has lasted?",
            "TEXT",
            85,
            false
    ),
    CHEST_PAIN_AGGRAVATING(
            "CHEST_PAIN_AGGRAVATING",
            "CHEST_PAIN",
            "Does anything make the chest discomfort worse, such as physical activity, deep breathing, or walking?",
            "TEXT",
            80,
            false
    ),

    // --- RESPIRATORY ---
    BREATHING_DIFFICULTY(
            "BREATHING_DIFFICULTY",
            "SHORTNESS_OF_BREATH",
            "Are you experiencing any shortness of breath, breathless feeling, or difficulty breathing?",
            "YES_NO",
            95,
            true
    ),
    BREATHING_DIFFICULTY_REST(
            "BREATHING_DIFFICULTY_REST",
            "SHORTNESS_OF_BREATH",
            "Are you feeling short of breath even while resting or lying completely flat?",
            "YES_NO",
            90,
            true
    ),
    COUGH_DURATION(
            "COUGH_DURATION",
            "COUGH",
            "How many days have you had this cough?",
            "TEXT",
            60,
            false
    ),
    COUGH_HEMOPTYSIS(
            "COUGH_HEMOPTYSIS",
            "COUGH",
            "Have you noticed any blood or rust-colored phlegm when coughing?",
            "YES_NO",
            95,
            true
    ),

    // --- FEVER ---
    FEVER_DURATION(
            "FEVER_DURATION",
            "FEVER",
            "For how many days have you been running a fever?",
            "TEXT",
            75,
            false
    ),
    FEVER_CHILLS(
            "FEVER_CHILLS",
            "FEVER",
            "Are you experiencing any shaking chills, shivering, or feeling unusually cold?",
            "YES_NO",
            70,
            false
    ),
    FEVER_TEMPERATURE(
            "FEVER_TEMPERATURE",
            "FEVER",
            "Have you measured your temperature at home, and if so, what was the highest reading?",
            "TEXT",
            65,
            false
    ),

    // --- NEUROLOGICAL ---
    HEADACHE_SEVERITY(
            "HEADACHE_SEVERITY",
            "HEADACHE",
            "On a scale from 0 to 10, how severe is your headache?",
            "SCALE_0_10",
            75,
            false
    ),
    HEADACHE_RED_FLAGS(
            "HEADACHE_RED_FLAGS",
            "HEADACHE",
            "Are you experiencing any stiff neck, sudden vision changes, or extreme sensitivity to light?",
            "YES_NO",
            95,
            true
    ),
    DIZZINESS_SYNCOPE(
            "DIZZINESS_SYNCOPE",
            "DIZZINESS",
            "Have you experienced any fainting, blackouts, or momentary loss of consciousness?",
            "YES_NO",
            95,
            true
    ),

    // --- ABDOMINAL ---
    ABDOMINAL_LOCATION(
            "ABDOMINAL_LOCATION",
            "ABDOMINAL_PAIN",
            "Where in your stomach or abdomen is the pain located (e.g. upper right, lower right, or generalized)?",
            "TEXT",
            85,
            false
    ),
    ABDOMINAL_VOMITING(
            "ABDOMINAL_VOMITING",
            "ABDOMINAL_PAIN",
            "Are you experiencing persistent vomiting or inability to keep fluids down?",
            "YES_NO",
            90,
            true
    ),

    // --- SYSTEMIC SAFETY BASELINES ---
    ALLERGY_CONFIRMATION(
            "ALLERGY_CONFIRMATION",
            "ALLERGY",
            "Do you have any known allergies to medications, such as Penicillin or Sulfa drugs?",
            "YES_NO",
            75,
            true
    ),
    MEDICATION_CONFIRMATION(
            "MEDICATION_CONFIRMATION",
            "MEDICATION",
            "Are you currently taking any regular prescription medications or supplements?",
            "YES_NO",
            70,
            false
    );

    private final String code;
    private final String clinicalConcept;
    private final String defaultApprovedTemplate;
    private final String answerType;
    private final int priority;
    private final boolean safetyCritical;

    private static final Map<String, QuestionIntent> BY_CODE;

    static {
        Map<String, QuestionIntent> map = new HashMap<>();
        for (QuestionIntent qi : values()) {
            map.put(qi.code, qi);
        }
        BY_CODE = Collections.unmodifiableMap(map);
    }

    QuestionIntent(String code, String clinicalConcept, String defaultApprovedTemplate, String answerType, int priority, boolean safetyCritical) {
        this.code = code;
        this.clinicalConcept = clinicalConcept;
        this.defaultApprovedTemplate = defaultApprovedTemplate;
        this.answerType = answerType;
        this.priority = priority;
        this.safetyCritical = safetyCritical;
    }

    public static QuestionIntent fromCode(String code) {
        if (code == null) return null;
        return BY_CODE.get(code.toUpperCase());
    }

    public String getCode() {
        return code;
    }

    public String getClinicalConcept() {
        return clinicalConcept;
    }

    public String getDefaultApprovedTemplate() {
        return defaultApprovedTemplate;
    }

    public String getAnswerType() {
        return answerType;
    }

    public int getPriority() {
        return priority;
    }

    public boolean isSafetyCritical() {
        return safetyCritical;
    }
}
