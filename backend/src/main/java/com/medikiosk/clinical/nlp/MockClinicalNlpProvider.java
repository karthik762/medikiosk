package com.medikiosk.clinical.nlp;

import com.medikiosk.concept.ClinicalConcept;
import com.medikiosk.concept.ConceptRegistry;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Deterministic offline clinical NLP provider using rule-based parsing,
 * regular expressions, and the controlled concept terminology registry.
 */
@Component
public class MockClinicalNlpProvider implements ClinicalNlpProvider {

    private static final List<String> NEGATION_CUES = Arrays.asList(
            "no ", "not ", "don't have ", "dont have ", "do not have ", "denies ",
            "without ", "never had ", "not having ", "not allergic to ", "no known ", "free of "
    );

    private static final Pattern DURATION_PATTERN = Pattern.compile(
            "\\b(since yesterday|since morning|since last night|for (?:\\d+|one|two|three|four|five) (?:days?|weeks?|months?|hours?)|past (?:\\d+|\\w+) (?:hours?|days?)|\\d+\\s*(?:days?|weeks?|hours?))\\b",
            Pattern.CASE_INSENSITIVE
    );

    private static final Pattern SEVERITY_PATTERN = Pattern.compile(
            "\\b(severe|mild|moderate|excruciating|sharp|throbbing|dull|aching|intense|unbearable|(\\d{1,2})\\s*(?:/|out of)\\s*10)\\b",
            Pattern.CASE_INSENSITIVE
    );

    private final ConceptRegistry conceptRegistry;

    public MockClinicalNlpProvider(ConceptRegistry conceptRegistry) {
        this.conceptRegistry = conceptRegistry;
    }

    @Override
    public ClinicalExtractionResult extractEntities(ClinicalExtractionRequest request) {
        ClinicalExtractionResult result = new ClinicalExtractionResult();
        if (request == null || request.getText() == null || request.getText().trim().isEmpty()) {
            return result;
        }

        String rawText = request.getText().trim();
        String lowerText = rawText.toLowerCase();

        // 1. Sentence/clause level analysis
        String[] clauses = lowerText.split("[,;\\.]|\\band\\b|\\bbut\\b");

        for (String clause : clauses) {
            String trimmedClause = clause.trim();
            if (trimmedClause.isEmpty()) continue;

            boolean isClauseNegated = isNegatedClause(trimmedClause);

            for (ClinicalConcept concept : conceptRegistry.getAllConcepts()) {
                boolean matches = false;
                String matchedTerm = null;

                if (trimmedClause.contains(concept.getDisplayName().toLowerCase())) {
                    matches = true;
                    matchedTerm = concept.getDisplayName();
                } else {
                    for (String syn : concept.getSynonyms()) {
                        if (trimmedClause.contains(syn)) {
                            matches = true;
                            matchedTerm = syn;
                            break;
                        }
                    }
                }

                if (matches) {
                    String assertion = isClauseNegated ? "NEGATED" : "AFFIRMED";
                    ClinicalEntity entity = new ClinicalEntity(
                            matchedTerm,
                            ClinicalEntityType.SYMPTOM,
                            concept.getCode(),
                            assertion,
                            0.95
                    );
                    result.addEntity(entity);
                }
            }
        }

        // 2. Duration Extraction
        Matcher durationMatcher = DURATION_PATTERN.matcher(rawText);
        if (durationMatcher.find()) {
            ClinicalEntity durationEntity = new ClinicalEntity(
                    durationMatcher.group(1),
                    ClinicalEntityType.DURATION,
                    "DURATION",
                    "AFFIRMED",
                    0.90
            );
            result.addEntity(durationEntity);
        }

        // 3. Severity Extraction
        Matcher severityMatcher = SEVERITY_PATTERN.matcher(rawText);
        if (severityMatcher.find()) {
            ClinicalEntity severityEntity = new ClinicalEntity(
                    severityMatcher.group(1),
                    ClinicalEntityType.SEVERITY,
                    "SEVERITY",
                    "AFFIRMED",
                    0.88
            );
            result.addEntity(severityEntity);
        }

        // 4. Allergy Mentions
        if (lowerText.contains("allergic to") || lowerText.contains("allergy")) {
            boolean isAllergyNegated = lowerText.contains("no allerg") || lowerText.contains("not allerg") || lowerText.contains("no known allerg");
            String assertion = isAllergyNegated ? "NEGATED" : "AFFIRMED";
            ClinicalEntity allergyEntity = new ClinicalEntity(
                    "Allergy",
                    ClinicalEntityType.ALLERGY,
                    "ALLERGY",
                    assertion,
                    0.92
            );
            result.addEntity(allergyEntity);
        }

        return result;
    }

    private boolean isNegatedClause(String clause) {
        for (String cue : NEGATION_CUES) {
            if (clause.startsWith(cue) || clause.contains(" " + cue.trim() + " ")) {
                return true;
            }
        }
        return false;
    }
}
