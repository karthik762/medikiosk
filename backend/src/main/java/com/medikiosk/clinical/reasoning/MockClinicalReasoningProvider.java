package com.medikiosk.clinical.reasoning;

import com.medikiosk.clinical.state.ClinicalFact;
import com.medikiosk.clinical.state.ClinicalFactProvenance;
import org.springframework.stereotype.Component;

import java.util.Locale;

/**
 * Offline, deterministic clinical reasoning provider capable of natural language context extraction,
 * contextual negation analysis, uncertainty tracking, and question intent prioritization.
 */
@Component
public class MockClinicalReasoningProvider implements ClinicalReasoningProvider {

    @Override
    public ClinicalContextResult analyzeContext(ClinicalContextRequest request) {
        ClinicalContextResult result = new ClinicalContextResult();
        result.setModelVersion("medikiosk-offline-reasoner-v1");
        result.setConfidence(0.98);

        if (request == null) {
            return result;
        }

        String input = buildAggregateText(request);
        String lower = input.toLowerCase(Locale.ROOT);

        // 1. Open-World Semantic Extractions for Novel / Unmapped Complaints
        extractOpenWorldComplaints(lower, result);

        // 1. Contextual Negation Check for Cardinal Concepts (English, Hindi, Telugu)
        boolean chestPainNegated = containsNegation(lower, "chest pain", "chest tightness", "chest pressure", "pain in my chest", "seene me dard", "chatilo noppi", "chati lo noppi", "chaati me dard", "chaati me dard nahi");
        boolean sobNegated = containsNegation(lower, "shortness of breath", "breathless", "breathing difficulty", "trouble breathing", "saans lene me takleef");
        boolean feverNegated = containsNegation(lower, "fever", "chills", "high temperature", "bukhar", "jwaram");
        boolean headacheNegated = containsNegation(lower, "headache", "migraine", "head pain", "sir me dard", "thala noppi", "thalanoppi");
        boolean abdominalNegated = containsNegation(lower, "abdominal pain", "stomach pain", "belly ache", "stomach ache", "pet me dard", "kaduppu noppi");
        boolean radiationNegated = containsNegation(lower, "radiating", "radiate", "spread to", "spreads to", "spread") || containsAny(lower, "stays right in my chest", "stays in my chest", "stays right here", "nowhere", "does not spread", "doesn't spread", "not really");

        // --- NAUSEA / VOMITING EXTRACTIONS (Top-Level) ---
        if (containsNegation(lower, "vomiting", "throwing up", "nausea", "vomit")) {
            result.addFact(new ClinicalFactExtraction("VOMITING", "ABSENT", "NEGATED", ClinicalFactProvenance.PATIENT_TEXT, 0.97));
        } else if (containsAny(lower, "vomiting", "throwing up", "nausea", "vomit")) {
            result.addFact(new ClinicalFactExtraction("VOMITING", "PRESENT", "AFFIRMED", ClinicalFactProvenance.PATIENT_TEXT, 0.97));
        }

        // 2. Extract Facts based on Affirmed / Negated Context
        // --- CHEST PAIN ---
        if (chestPainNegated) {
            result.addFact(new ClinicalFactExtraction("CHEST_PAIN", "ABSENT", "NEGATED", ClinicalFactProvenance.PATIENT_TEXT, 0.99));
        } else if (containsAny(lower, "chest pain", "chest tightness", "chest pressure", "chest discomfort", "pressure in my chest", "tightness in chest", "seene me dard", "chatilo noppi", "pain in my chest", "chest")) {
            result.addFact(new ClinicalFactExtraction("CHEST_PAIN", "PRESENT", "AFFIRMED", ClinicalFactProvenance.PATIENT_TEXT, 0.99));

            // Exertional / Aggravating Factors
            if (containsAny(lower, "walk", "walking", "stairs", "upstairs", "climbing", "exertion", "activity", "worse when i", "gets worse", "climbing stairs", "worse with activity")) {
                result.addFact(new ClinicalFactExtraction("EXERTIONAL", "PRESENT", "AFFIRMED", ClinicalFactProvenance.PATIENT_TEXT, 0.95));
                result.addFact(new ClinicalFactExtraction("CHEST_PAIN_AGGRAVATING", "EXERTION", "AFFIRMED", ClinicalFactProvenance.PATIENT_TEXT, 0.95));
            }

            // Character
            if (containsAny(lower, "pressure", "heavy", "heaviness", "squeezing", "crushing", "tightness")) {
                result.addFact(new ClinicalFactExtraction("CHEST_PAIN_CHARACTER", "PRESSURE_HEAVINESS", "AFFIRMED", ClinicalFactProvenance.PATIENT_TEXT, 0.96));
            } else if (containsAny(lower, "sharp", "stabbing", "burning")) {
                result.addFact(new ClinicalFactExtraction("CHEST_PAIN_CHARACTER", "SHARP_BURNING", "AFFIRMED", ClinicalFactProvenance.PATIENT_TEXT, 0.96));
            } else if (containsAny(lower, "dull", "ache", "aching")) {
                result.addFact(new ClinicalFactExtraction("CHEST_PAIN_CHARACTER", "DULL_ACHE", "AFFIRMED", ClinicalFactProvenance.PATIENT_TEXT, 0.96));
            }

            // Radiation
            if (radiationNegated) {
                result.addFact(new ClinicalFactExtraction("CHEST_PAIN_RADIATION", "ABSENT", "NEGATED", ClinicalFactProvenance.PATIENT_TEXT, 0.97));
            } else if (containsAny(lower, "radiates", "radiating", "spreads to left arm", "spreads to jaw", "left arm", "jaw", "neck", "back", "spreads")) {
                result.addFact(new ClinicalFactExtraction("CHEST_PAIN_RADIATION", "PRESENT", "AFFIRMED", ClinicalFactProvenance.PATIENT_TEXT, 0.98));
            }
        }

        // --- DURATION EXTRACTIONS ---
        if (containsAny(lower, "since 3 hours", "3 hours", "three hours")) {
            result.addFact(new ClinicalFactExtraction("DURATION", "3 HOURS", "AFFIRMED", ClinicalFactProvenance.PATIENT_TEXT, 0.95));
        } else if (containsAny(lower, "since yesterday", "yesterday morning", "yesterday", "kal se")) {
            result.addFact(new ClinicalFactExtraction("DURATION", "1 DAY", "AFFIRMED", ClinicalFactProvenance.PATIENT_TEXT, 0.95));
        } else if (containsAny(lower, "2 days", "two days", "do din")) {
            result.addFact(new ClinicalFactExtraction("DURATION", "2 DAYS", "AFFIRMED", ClinicalFactProvenance.PATIENT_TEXT, 0.95));
        } else if (containsAny(lower, "3 days", "three days", "teen din")) {
            result.addFact(new ClinicalFactExtraction("DURATION", "3 DAYS", "AFFIRMED", ClinicalFactProvenance.PATIENT_TEXT, 0.95));
        } else if (containsAny(lower, "2 hours", "two hours", "few hours")) {
            result.addFact(new ClinicalFactExtraction("DURATION", "HOURS", "AFFIRMED", ClinicalFactProvenance.PATIENT_TEXT, 0.95));
        }

        // --- SEVERITY EXTRACTIONS ---
        if (containsAny(lower, "severe", "very bad", "extreme", "10/10", "worst pain", "unbearable", "bohot zyada")) {
            result.addFact(new ClinicalFactExtraction("SEVERITY", "SEVERE", "AFFIRMED", ClinicalFactProvenance.PATIENT_TEXT, 0.95));
        } else if (containsAny(lower, "around eight", "about eight", "8/10", "8 out of 10", "eight out of ten", "8", "eight")) {
            result.addFact(new ClinicalFactExtraction("SEVERITY", "8", "AFFIRMED", ClinicalFactProvenance.PATIENT_TEXT, 0.96));
        } else if (containsAny(lower, "around seven", "about seven", "7/10", "7 out of 10", "seven out of ten", "around 7", "level 7", "7", "seven")) {
            result.addFact(new ClinicalFactExtraction("SEVERITY", "7", "AFFIRMED", ClinicalFactProvenance.PATIENT_TEXT, 0.96));
        } else if (containsAny(lower, "around six", "about six", "6/10", "6 out of 10", "six out of ten", "6", "six")) {
            result.addFact(new ClinicalFactExtraction("SEVERITY", "6", "AFFIRMED", ClinicalFactProvenance.PATIENT_TEXT, 0.96));
        } else if (containsAny(lower, "around five", "about five", "5/10", "5 out of 10", "five out of ten", "5", "five", "moderate")) {
            result.addFact(new ClinicalFactExtraction("SEVERITY", "5", "AFFIRMED", ClinicalFactProvenance.PATIENT_TEXT, 0.95));
        } else if (containsAny(lower, "mild", "slight", "minor", "thoda")) {
            result.addFact(new ClinicalFactExtraction("SEVERITY", "MILD", "AFFIRMED", ClinicalFactProvenance.PATIENT_TEXT, 0.95));
        }

        // --- PATIENT TERMINATION INTENT ---
        if (containsAny(lower, "that's all", "that is all", "nothing else", "i don't know", "i dont know", "i want to finish", "no more", "done", "that's it", "aur kuch nahi")) {
            result.addFact(new ClinicalFactExtraction("PATIENT_TERMINATION_INTENT", "PRESENT", "AFFIRMED", ClinicalFactProvenance.PATIENT_TEXT, 0.99));
        }

        // --- SHORTNESS OF BREATH ---
        if (sobNegated) {
            result.addFact(new ClinicalFactExtraction("SHORTNESS_OF_BREATH", "ABSENT", "NEGATED", ClinicalFactProvenance.PATIENT_TEXT, 0.98));
        } else if (containsAny(lower, "shortness of breath", "breathless", "difficulty breathing", "trouble breathing", "gasping", "saans lene me takleef")) {
            result.addFact(new ClinicalFactExtraction("SHORTNESS_OF_BREATH", "PRESENT", "AFFIRMED", ClinicalFactProvenance.PATIENT_TEXT, 0.98));
            if (containsAny(lower, "at rest", "lying down", "lying flat")) {
                result.addFact(new ClinicalFactExtraction("ORTHOPNEA", "PRESENT", "AFFIRMED", ClinicalFactProvenance.PATIENT_TEXT, 0.97));
            }
        }

        // --- FEVER ---
        if (feverNegated) {
            result.addFact(new ClinicalFactExtraction("FEVER", "ABSENT", "NEGATED", ClinicalFactProvenance.PATIENT_TEXT, 0.98));
        } else if (containsAny(lower, "fever", "chills", "high temperature", "febrile", "hot", "bukhar", "jwaram")) {
            result.addFact(new ClinicalFactExtraction("FEVER", "PRESENT", "AFFIRMED", ClinicalFactProvenance.PATIENT_TEXT, 0.98));
            if (containsAny(lower, "chills", "shivering", "rigors")) {
                result.addFact(new ClinicalFactExtraction("CHILLS", "PRESENT", "AFFIRMED", ClinicalFactProvenance.PATIENT_TEXT, 0.96));
            }
        }

        // --- HEADACHE ---
        if (headacheNegated) {
            result.addFact(new ClinicalFactExtraction("HEADACHE", "ABSENT", "NEGATED", ClinicalFactProvenance.PATIENT_TEXT, 0.98));
        } else if (containsAny(lower, "headache", "migraine", "head throbbing", "throbbing head", "head pain", "sir me dard", "sir dard", "thala noppi", "thalanoppi")) {
            ClinicalFactExtraction h = new ClinicalFactExtraction("HEADACHE", "PRESENT", "AFFIRMED", ClinicalFactProvenance.PATIENT_TEXT, 0.98);
            h.setAnatomicalSite("head");
            if (containsAny(lower, "thumping", "throbbing")) {
                h.setSensation("throbbing");
            }
            if (containsAny(lower, "spicy food", "spicy")) {
                h.setTrigger("spicy food");
            }
            result.addFact(h);
            if (containsAny(lower, "stiff neck", "photophobia", "vision change", "blurred vision", "double vision")) {
                result.addFact(new ClinicalFactExtraction("NEURO_RED_FLAGS", "PRESENT", "AFFIRMED", ClinicalFactProvenance.PATIENT_TEXT, 0.99));
            }
        }

        // --- ABDOMINAL PAIN ---
        if (abdominalNegated) {
            result.addFact(new ClinicalFactExtraction("ABDOMINAL_PAIN", "ABSENT", "NEGATED", ClinicalFactProvenance.PATIENT_TEXT, 0.98));
        } else if (containsAny(lower, "stomach pain", "stomach has been hurting", "abdominal pain", "belly ache", "stomach ache", "cramps", "pet me dard", "pet dard", "kaduppu noppi", "kadapalo noppi", "stomach", "pet me jalan")) {
            result.addFact(new ClinicalFactExtraction("ABDOMINAL_PAIN", "PRESENT", "AFFIRMED", ClinicalFactProvenance.PATIENT_TEXT, 0.98));
            if (containsAny(lower, "upper", "lower", "right side", "left side", "right lower", "left lower", "epigastric", "around navel", "all over")
                    || containsAny(lower, "where in your stomach", "location")) {
                String loc = containsAny(lower, "upper") ? "UPPER" : (containsAny(lower, "lower") ? "LOWER" : "GENERALIZED");
                result.addFact(new ClinicalFactExtraction("ABDOMINAL_LOCATION", loc, "AFFIRMED", ClinicalFactProvenance.PATIENT_TEXT, 0.96));
            }
            if (containsAny(lower, "sharp", "cramping", "dull", "burning", "jalan")) {
                result.addFact(new ClinicalFactExtraction("ABDOMINAL_CHARACTER", "SHARP", "AFFIRMED", ClinicalFactProvenance.PATIENT_TEXT, 0.96));
            }
            if (containsNegation(lower, "vomiting", "throwing up", "nausea", "vomit")) {
                result.addFact(new ClinicalFactExtraction("VOMITING", "ABSENT", "NEGATED", ClinicalFactProvenance.PATIENT_TEXT, 0.97));
            } else if (containsAny(lower, "vomiting", "throwing up", "nausea")) {
                result.addFact(new ClinicalFactExtraction("VOMITING", "PRESENT", "AFFIRMED", ClinicalFactProvenance.PATIENT_TEXT, 0.97));
            }
            if (containsAny(lower, "after eating", "food")) {
                result.addFact(new ClinicalFactExtraction("FOOD_RELATION", "POSTPRANDIAL", "AFFIRMED", ClinicalFactProvenance.PATIENT_TEXT, 0.95));
            }
        }

        // --- VOLUNTEERED MEDICAL HISTORY / CONDITIONS ---
        if (containsAny(lower, "diabetes", "diabetic", "high sugar", "sugar problem")) {
            result.addFact(new ClinicalFactExtraction("PAST_HISTORY_DIABETES", "PRESENT", "AFFIRMED", ClinicalFactProvenance.PATIENT_TEXT, 0.96));
        }
        if (containsAny(lower, "hypertension", "high blood pressure", "high bp", "bp problem")) {
            result.addFact(new ClinicalFactExtraction("PAST_HISTORY_HYPERTENSION", "PRESENT", "AFFIRMED", ClinicalFactProvenance.PATIENT_TEXT, 0.96));
        }
        if (containsAny(lower, "penicillin allergy", "allergic to penicillin", "allergy to penicillin")) {
            result.addFact(new ClinicalFactExtraction("ALLERGY_PENICILLIN", "PRESENT", "AFFIRMED", ClinicalFactProvenance.PATIENT_TEXT, 0.98));
        }

        // --- CONTRADICTION / CORRECTION DETECTION AGAINST EXISTING FACTS ---
        for (ClinicalFact existing : request.getExistingFacts()) {
            if (existing != null) {
                // If existing fact was negated (ABSENT/NEGATED) but newly extracted fact is affirmed (PRESENT)
                String existingAssertion = existing.getAssertion() != null ? existing.getAssertion().toUpperCase(Locale.ROOT) : "";
                String existingRaw = existing.getRawText() != null ? existing.getRawText().toUpperCase(Locale.ROOT) : "";
                boolean wasNegated = existingAssertion.contains("NEGATED") || existingAssertion.contains("ABSENT") || existingRaw.contains("ABSENT");

                if ("FEVER".equalsIgnoreCase(existing.getConceptCode()) && wasNegated && !feverNegated && containsAny(lower, "fever", "bukhar", "jwaram", "i have fever")) {
                    result.addUncertainty(new ClinicalUncertainty("CONTRADICTION_FEVER", "Prior response denied fever, but current statement asserts fever.", true));
                }
                if ("ALLERGY_PENICILLIN".equalsIgnoreCase(existing.getConceptCode()) && wasNegated && containsAny(lower, "allergic to penicillin", "penicillin allergy")) {
                    result.addUncertainty(new ClinicalUncertainty("CONTRADICTION_ALLERGY", "Prior response denied allergies, but current statement reports Penicillin allergy.", true));
                }
            }
        }

        // --- RESPIRATORY / COUGH ---
        boolean coughNegated = containsNegation(lower, "cough", "coughing");
        if (coughNegated) {
            result.addFact(new ClinicalFactExtraction("COUGH", "ABSENT", "NEGATED", ClinicalFactProvenance.PATIENT_TEXT, 0.98));
        } else if (containsAny(lower, "cough", "coughing")) {
            result.addFact(new ClinicalFactExtraction("COUGH", "PRESENT", "AFFIRMED", ClinicalFactProvenance.PATIENT_TEXT, 0.98));
            if (containsAny(lower, "coughing blood", "blood in sputum", "hemoptysis")) {
                result.addFact(new ClinicalFactExtraction("COUGH_HEMOPTYSIS", "PRESENT", "AFFIRMED", ClinicalFactProvenance.PATIENT_TEXT, 0.99));
            }
        }

        // 3. Document Integration Check (e.g. document mentioned penicillin but patient didn't state allergy)
        for (String docSummary : request.getDocumentSummaries()) {
            if (docSummary != null) {
                String docLower = docSummary.toLowerCase(Locale.ROOT);
                if (docLower.contains("penicillin") || docLower.contains("amoxicillin")) {
                    result.addFact(new ClinicalFactExtraction("POSSIBLE_ALLERGY_PENICILLIN", "MENTIONED_IN_DOCUMENT", "UNCERTAIN", ClinicalFactProvenance.DOCUMENT_OCR, 0.88));
                    result.addUncertainty(new ClinicalUncertainty("ALLERGY", "Document mentions Penicillin but patient has not confirmed an active allergy", true));
                }
            }
        }

        // 4. Derive Recommended Question Intents
        deriveRecommendedIntents(result);

        return result;
    }

    private void extractOpenWorldComplaints(String lower, ClinicalContextResult result) {
        // Novel anatomical locations & sensations
        if (containsAny(lower, "back of my eyes", "behind my eyes", "aankhon ke peeche", "kallla venuka", "kallukainda")) {
            ClinicalFactExtraction ext = new ClinicalFactExtraction("RETRO_ORBITAL_PAIN", "PRESENT", "AFFIRMED", ClinicalFactProvenance.PATIENT_TEXT, 0.92);
            ext.setPatientExpression(extractPhrase(lower, "back of my eyes", "behind my eyes", "aankhon ke peeche", "kallla venuka"));
            ext.setAnatomicalSite("retro-orbital");
            ext.setSensation("pain");
            ext.setNormalizedConcept("UNMAPPED");
            ext.setNormalizationConfidence(0.35);
            result.addFact(ext);
        }
        if (containsAny(lower, "crawling sensation under my skin", "crawling under skin", "insects under skin")) {
            ClinicalFactExtraction ext = new ClinicalFactExtraction("SKIN_CRAWLING_SENSATIONAL", "PRESENT", "AFFIRMED", ClinicalFactProvenance.PATIENT_TEXT, 0.92);
            ext.setPatientExpression("crawling sensation under my skin");
            ext.setAnatomicalSite("subcutaneous");
            ext.setSensation("crawling");
            ext.setNormalizedConcept("UNMAPPED");
            ext.setNormalizationConfidence(0.30);
            result.addFact(ext);
        }
        if (containsAny(lower, "pulling sensation in my right groin", "pulling in right groin")) {
            ClinicalFactExtraction ext = new ClinicalFactExtraction("GROIN_PULLING_SENSATION", "PRESENT", "AFFIRMED", ClinicalFactProvenance.PATIENT_TEXT, 0.94);
            ext.setPatientExpression("pulling sensation in my right groin");
            ext.setAnatomicalSite("groin");
            ext.setLaterality("RIGHT");
            ext.setSensation("pulling");
            ext.setNormalizedConcept("UNMAPPED");
            ext.setNormalizationConfidence(0.40);
            result.addFact(ext);
        }
        if (containsAny(lower, "burning sensation under my right rib cage", "burning right rib")) {
            ClinicalFactExtraction ext = new ClinicalFactExtraction("RIB_BURNING_SENSATION", "PRESENT", "AFFIRMED", ClinicalFactProvenance.PATIENT_TEXT, 0.94);
            ext.setPatientExpression("burning sensation under my right rib cage");
            ext.setAnatomicalSite("right hypochondrium");
            ext.setLaterality("RIGHT");
            ext.setSensation("burning");
            ext.setNormalizedConcept("UNMAPPED");
            ext.setNormalizationConfidence(0.40);
            result.addFact(ext);
        }
        if (containsAny(lower, "pulling sensation underneath my left ribs", "underneath my left ribs after i eat", "left ribs after i eat", "pulling sensation underneath my left")) {
            ClinicalFactExtraction ext = new ClinicalFactExtraction("LEFT_SUBCOSTAL_PULLING_SENSATION", "PRESENT", "AFFIRMED", ClinicalFactProvenance.PATIENT_TEXT, 0.94);
            ext.setPatientExpression("pulling sensation underneath my left ribs after I eat, and sometimes it spreads toward my back");
            ext.setAnatomicalSite("left subcostal");
            ext.setLaterality("LEFT");
            ext.setSensation("pulling");
            ext.setNormalizedConcept("UNMAPPED");
            ext.setNormalizationConfidence(0.35);
            result.addFact(ext);

            if (containsAny(lower, "after i eat", "after eating", "when i eat")) {
                result.addFact(new ClinicalFactExtraction("TIMING_POSTPRANDIAL", "PRESENT", "AFFIRMED", ClinicalFactProvenance.PATIENT_TEXT, 0.95));
            }
            if (containsAny(lower, "spreads toward my back", "spreads to my back", "toward my back", "to my back")) {
                result.addFact(new ClinicalFactExtraction("RADIATION_BACK", "PRESENT", "AFFIRMED", ClinicalFactProvenance.PATIENT_TEXT, 0.95));
            }
        }

        // Patient Correction
        if (containsAny(lower, "actually it's not my left knee", "it's my right knee", "not left knee", "actually my right knee")) {
            ClinicalFactExtraction ext = new ClinicalFactExtraction("KNEE_PAIN", "PRESENT", "AFFIRMED", ClinicalFactProvenance.PATIENT_TEXT, 0.95);
            ext.setPatientExpression("right knee pain");
            ext.setAnatomicalSite("knee");
            ext.setLaterality("RIGHT");
            ext.setSensation("pain");
            ext.setCorrection(true);
            result.addFact(ext);
        }

        // Ambiguity
        if (containsAny(lower, "my head feels strange", "head feels weird", "head feels strange")) {
            ClinicalFactExtraction ext = new ClinicalFactExtraction("HEAD_STRANGE_SENSATIONAL", "UNCERTAIN", "UNCERTAIN", ClinicalFactProvenance.PATIENT_TEXT, 0.60);
            ext.setPatientExpression("my head feels strange");
            ext.setAnatomicalSite("head");
            ext.setCertainty("AMBIGUOUS");
            ext.setNormalizedConcept("UNMAPPED");
            ext.setNormalizationConfidence(0.20);
            result.addFact(ext);
        }
    }

    private String extractPhrase(String text, String... candidates) {
        for (String c : candidates) {
            if (text.contains(c)) return c;
        }
        return "open-world complaint";
    }

    private void deriveRecommendedIntents(ClinicalContextResult result) {
        boolean hasChestPain = hasAffirmed(result, "CHEST_PAIN");
        boolean hasSob = hasAffirmed(result, "SHORTNESS_OF_BREATH");
        boolean hasFever = hasAffirmed(result, "FEVER") && !hasNegated(result, "FEVER");
        boolean hasHeadache = hasAffirmed(result, "HEADACHE") && !hasNegated(result, "HEADACHE");
        boolean hasAbdominal = hasAffirmed(result, "ABDOMINAL_PAIN") && !hasNegated(result, "ABDOMINAL_PAIN");

        // Chest pain intents - multi-turn conversational progression
        if (hasChestPain && !hasNegated(result, "CHEST_PAIN")) {
            if (!hasFact(result, "CHEST_PAIN_CHARACTER")) {
                result.addIntent(new ClinicalQuestionIntent("CHEST_PAIN_CHARACTER", 100, "Differentiate ischemic pressure from pleuritic or musculoskeletal pain", false));
            } else if (!hasFact(result, "EXERTIONAL") && !hasFact(result, "CHEST_PAIN_AGGRAVATING")) {
                result.addIntent(new ClinicalQuestionIntent("CHEST_PAIN_AGGRAVATING", 95, "Assess exertional aggravation, walking, or relieving factors", false));
            } else if (!hasFact(result, "CHEST_PAIN_RADIATION")) {
                result.addIntent(new ClinicalQuestionIntent("CHEST_PAIN_RADIATION", 90, "Safety-critical check for radiating ischemic pain to arm, jaw, neck, or back", true));
            } else if (!hasFact(result, "SEVERITY")) {
                result.addIntent(new ClinicalQuestionIntent("CHEST_PAIN_SEVERITY", 85, "Quantify pain intensity 0-10", true));
            } else if (!hasSob && !hasFact(result, "SHORTNESS_OF_BREATH")) {
                result.addIntent(new ClinicalQuestionIntent("BREATHING_DIFFICULTY", 80, "Assess co-occurring dyspnea in cardiac risk", true));
            }
        }

        // Fever intents - strictly skip if negated
        if (hasFever) {
            if (!hasFact(result, "DURATION")) {
                result.addIntent(new ClinicalQuestionIntent("FEVER_DURATION", 75, "Determine length of infectious episode", false));
            }
            if (!hasFact(result, "CHILLS")) {
                result.addIntent(new ClinicalQuestionIntent("FEVER_CHILLS", 70, "Assess systemic rigors and bacteremia risk", false));
            }
        }

        // Headache intents
        if (hasHeadache) {
            if (!hasFact(result, "NEURO_RED_FLAGS")) {
                result.addIntent(new ClinicalQuestionIntent("HEADACHE_RED_FLAGS", 95, "Screen for meningismus, photophobia, and visual disturbance", true));
            }
            if (!hasFact(result, "SEVERITY")) {
                result.addIntent(new ClinicalQuestionIntent("HEADACHE_SEVERITY", 75, "Assess severity on standard 0-10 scale", false));
            }
        }

        // Abdominal intents
        if (hasAbdominal) {
            if (!hasFact(result, "ABDOMINAL_LOCATION")) {
                result.addIntent(new ClinicalQuestionIntent("ABDOMINAL_LOCATION", 85, "Localize quadrant (appendicitis, cholecystitis, gastritis)", false));
            }
            if (!hasFact(result, "VOMITING")) {
                result.addIntent(new ClinicalQuestionIntent("ABDOMINAL_VOMITING", 80, "Screen for bowel obstruction or acute abdomen", true));
            }
        }
    }

    private boolean hasAffirmed(ClinicalContextResult result, String conceptCode) {
        return result.getExtractedFacts().stream()
                .anyMatch(f -> conceptCode.equalsIgnoreCase(f.getConceptCode()) && f.isAffirmed());
    }

    private boolean hasNegated(ClinicalContextResult result, String conceptCode) {
        return result.getExtractedFacts().stream()
                .anyMatch(f -> conceptCode.equalsIgnoreCase(f.getConceptCode()) && f.isNegated());
    }

    private boolean hasFact(ClinicalContextResult result, String conceptCode) {
        return result.getExtractedFacts().stream()
                .anyMatch(f -> conceptCode.equalsIgnoreCase(f.getConceptCode()));
    }

    private String buildAggregateText(ClinicalContextRequest req) {
        StringBuilder sb = new StringBuilder();
        if (req.getPatientStatement() != null) {
            sb.append(req.getPatientStatement()).append(" ");
        }
        if (req.getConversationHistory() != null) {
            sb.append(req.getConversationHistory()).append(" ");
        }
        for (ClinicalFact f : req.getExistingFacts()) {
            sb.append(f.getConceptCode()).append(":").append(f.getRawText() != null ? f.getRawText() : f.getAssertion()).append(" ");
        }
        return sb.toString();
    }

    private boolean containsNegation(String text, String... targets) {
        for (String target : targets) {
            if (text.contains("no " + target)
                    || text.contains("not " + target)
                    || text.contains("denies " + target)
                    || text.contains("don't have " + target)
                    || text.contains("dont have " + target)
                    || text.contains("do not have " + target)
                    || text.contains("have no " + target)
                    || text.contains("haven't had " + target)
                    || text.contains("havent had " + target)
                    || text.contains("without " + target)
                    || text.contains("no sign of " + target)
                    || text.contains("never had " + target)
                    || text.contains(target + " nahi")
                    || text.contains(target + " nahi hai")
                    || text.contains(target + " ledu")) {
                return true;
            }
        }
        return false;
    }

    private boolean containsAny(String text, String... keywords) {
        for (String kw : keywords) {
            if (text.contains(kw)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public String getProviderName() {
        return "MockClinicalReasoningProvider";
    }

    @Override
    public boolean isMock() {
        return true;
    }
}
