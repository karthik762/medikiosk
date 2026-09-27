package com.medikiosk.triage;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.medikiosk.dto.triage.TriggeredRuleDto;
import com.medikiosk.entity.Answer;
import com.medikiosk.entity.MedicalHistory;
import com.medikiosk.entity.Symptom;
import com.medikiosk.entity.TriageRule;
import com.medikiosk.entity.enums.TriagePriority;
import com.medikiosk.repository.TriageRuleRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Deterministic rule-based triage screening engine conforming to docs/TRIAGE_RULES.md.
 * Evaluates configured rules against structured intake data.
 * Does NOT diagnose, does NOT prescribe, and does NOT use LLM generation.
 */
@Component
public class TriageEngineImpl implements TriageEngine {

    private static final Logger log = LoggerFactory.getLogger(TriageEngineImpl.class);

    private final TriageRuleRepository triageRuleRepository;
    private final ObjectMapper objectMapper;

    public TriageEngineImpl(TriageRuleRepository triageRuleRepository, ObjectMapper objectMapper) {
        this.triageRuleRepository = triageRuleRepository;
        this.objectMapper = objectMapper != null ? objectMapper : new ObjectMapper();
    }

    @Override
    public TriageDecision evaluate(TriageInputContext context) {
        List<TriageRule> activeRules = triageRuleRepository.findByEnabledTrueOrderByCreatedAtAsc();
        List<TriggeredRuleDto> triggeredRules = new ArrayList<>();
        List<String> reasons = new ArrayList<>();

        for (TriageRule rule : activeRules) {
            if (rule.getEnabled() == null || !rule.getEnabled()) {
                continue;
            }

            RuleEvaluationResult result = evaluateRule(rule, context);
            if (result.isTriggered()) {
                TriggeredRuleDto triggeredDto = new TriggeredRuleDto(
                        rule.getRuleId(),
                        rule.getVersion(),
                        rule.getName(),
                        rule.getPriority(),
                        result.getReason()
                );
                triggeredRules.add(triggeredDto);
                reasons.add(result.getReason());
            }
        }

        // Priority resolution: URGENT > PRIORITY > STABLE
        TriagePriority finalPriority = TriagePriority.STABLE;
        boolean hasUrgent = triggeredRules.stream().anyMatch(r -> r.getPriority() == TriagePriority.URGENT);
        boolean hasPriority = triggeredRules.stream().anyMatch(r -> r.getPriority() == TriagePriority.PRIORITY);

        if (hasUrgent) {
            finalPriority = TriagePriority.URGENT;
        } else if (hasPriority) {
            finalPriority = TriagePriority.PRIORITY;
        }

        String explanation;
        String patientInstruction;

        if (finalPriority == TriagePriority.URGENT) {
            explanation = "Potential red flag identified: " + String.join("; ", reasons);
            patientInstruction = "Your responses indicate that a healthcare professional should review your information promptly.";
        } else if (finalPriority == TriagePriority.PRIORITY) {
            explanation = "Priority clinical review recommended: " + String.join("; ", reasons);
            patientInstruction = "Your responses have been recorded and marked for priority clinical review.";
        } else {
            explanation = "No configured red flag identified";
            patientInstruction = "Your intake responses have been recorded for doctor review.";
        }

        return new TriageDecision(finalPriority, triggeredRules, reasons, explanation, patientInstruction);
    }

    @Override
    public Map<String, Object> evaluateTriage(UUID intakeSessionId, List<String> symptoms, Map<String, Object> parameters) {
        List<Symptom> symptomEntities = new ArrayList<>();
        if (symptoms != null) {
            for (String s : symptoms) {
                Symptom sym = new Symptom();
                sym.setName(s);
                symptomEntities.add(sym);
            }
        }

        TriageInputContext context = new TriageInputContext(
                null,
                symptomEntities,
                Collections.emptyList(),
                Collections.emptyList(),
                parameters
        );

        TriageDecision decision = evaluate(context);

        Map<String, Object> result = new HashMap<>();
        result.put("sessionId", intakeSessionId);
        result.put("priority", decision.getPriority().name());
        result.put("explanation", decision.getExplanation());
        result.put("triggeredRules", decision.getTriggeredRules());
        result.put("status", "COMPLETED");
        return result;
    }

    private RuleEvaluationResult evaluateRule(TriageRule rule, TriageInputContext context) {
        if (rule.getConditions() == null || rule.getConditions().isBlank()) {
            return new RuleEvaluationResult(false, null);
        }

        try {
            JsonNode conditionsNode = objectMapper.readTree(rule.getConditions());
            while (conditionsNode != null && conditionsNode.isTextual()) {
                conditionsNode = objectMapper.readTree(conditionsNode.asText());
            }
            if (conditionsNode == null || !conditionsNode.isArray() || conditionsNode.isEmpty()) {
                log.warn("Rule {} has invalid conditions structure: {}", rule.getRuleId(), rule.getConditions());
                return new RuleEvaluationResult(false, null);
            }

            boolean allConditionsMatch = true;
            String matchedReason = rule.getDescription();

            for (JsonNode condNode : conditionsNode) {
                String field = condNode.has("field") ? condNode.get("field").asText() : "";
                String operator = condNode.has("operator") ? condNode.get("operator").asText() : "EQUALS";
                JsonNode targetValue = condNode.get("value");
                String conditionReason = condNode.has("reason") ? condNode.get("reason").asText() : null;

                boolean matched = evaluateCondition(field, operator, targetValue, context);
                if (!matched) {
                    allConditionsMatch = false;
                    break;
                } else if (conditionReason != null && !conditionReason.isBlank()) {
                    matchedReason = conditionReason;
                }
            }

            if (allConditionsMatch) {
                return new RuleEvaluationResult(true, matchedReason);
            }
        } catch (Exception e) {
            log.warn("Error evaluating conditions for rule {}: {}", rule.getRuleId(), e.getMessage());
        }

        return new RuleEvaluationResult(false, null);
    }

    private boolean evaluateCondition(String field, String operator, JsonNode targetValue, TriageInputContext context) {
        if (field == null) {
            return false;
        }

        // 1. Symptom severity
        if ("symptom.severity".equalsIgnoreCase(field)) {
            if (context.getSymptoms().isEmpty()) {
                return false;
            }
            int targetInt = targetValue != null && targetValue.isNumber() ? targetValue.asInt() : 0;
            for (Symptom s : context.getSymptoms()) {
                if (s.getSeverity() != null && compareNumeric(s.getSeverity(), operator, targetInt)) {
                    return true;
                }
            }
            return false;
        }

        // 2. Symptom name
        if ("symptom.name".equalsIgnoreCase(field) || "symptom.presence".equalsIgnoreCase(field)) {
            if (context.getSymptoms().isEmpty() || targetValue == null) {
                return false;
            }
            String target = targetValue.asText();
            for (Symptom s : context.getSymptoms()) {
                if (s.getName() != null && compareString(s.getName(), operator, target)) {
                    return true;
                }
            }
            return false;
        }

        // 3. Symptom duration
        if ("symptom.duration".equalsIgnoreCase(field)) {
            if (context.getSymptoms().isEmpty() || targetValue == null) {
                return false;
            }
            String target = targetValue.asText();
            for (Symptom s : context.getSymptoms()) {
                if (s.getDuration() != null && compareString(s.getDuration(), operator, target)) {
                    return true;
                }
            }
            return false;
        }

        // 4. Test flag (conforming to docs/TRIAGE_RULES.md Section 35)
        if ("testFlag".equalsIgnoreCase(field)) {
            Object val = context.getParameters().get("testFlag");
            if (val != null) {
                boolean targetBool = targetValue != null && targetValue.asBoolean();
                boolean actualBool = Boolean.parseBoolean(val.toString());
                return actualBool == targetBool;
            }
            return false;
        }

        // 5. Chief complaint
        if ("chief_complaint".equalsIgnoreCase(field)) {
            if (targetValue == null) {
                return false;
            }
            String target = targetValue.asText();
            for (Answer a : context.getAnswers()) {
                if (a.getQuestion() != null &&
                        ("Q_PRIMARY_COMPLAINT".equalsIgnoreCase(a.getQuestion().getQuestionCode()) ||
                                "Q_CHIEF_COMPLAINT".equalsIgnoreCase(a.getQuestion().getQuestionCode()))) {
                    if (a.getAnswerText() != null && compareString(a.getAnswerText(), operator, target)) {
                        return true;
                    }
                }
            }
            return false;
        }

        // 6. Question answers (field format: "answer.<questionCode>")
        if (field.startsWith("answer.")) {
            String questionCode = field.substring("answer.".length());
            if (targetValue == null) {
                return false;
            }
            String target = targetValue.asText();
            for (Answer a : context.getAnswers()) {
                if (a.getQuestion() != null && questionCode.equalsIgnoreCase(a.getQuestion().getQuestionCode())) {
                    if (a.getAnswerText() != null && compareString(a.getAnswerText(), operator, target)) {
                        return true;
                    }
                }
            }
            return false;
        }

        // 7. Medical history category
        if ("medical_history.category".equalsIgnoreCase(field)) {
            if (targetValue == null) {
                return false;
            }
            String target = targetValue.asText();
            for (MedicalHistory h : context.getMedicalHistories()) {
                if (h.getCategory() != null && compareString(h.getCategory(), operator, target)) {
                    return true;
                }
            }
            return false;
        }

        // 8. General parameters lookup
        if (context.getParameters().containsKey(field)) {
            Object actual = context.getParameters().get(field);
            if (targetValue == null) {
                return "NOT_EXISTS".equalsIgnoreCase(operator);
            }
            return compareString(actual.toString(), operator, targetValue.asText());
        }

        return false;
    }

    private boolean compareNumeric(int actual, String operator, int target) {
        return switch (operator.toUpperCase()) {
            case "EQUALS", "==" -> actual == target;
            case "NOT_EQUALS", "!=" -> actual != target;
            case "GREATER_THAN", ">" -> actual > target;
            case "GREATER_THAN_OR_EQUAL", ">=" -> actual >= target;
            case "LESS_THAN", "<" -> actual < target;
            case "LESS_THAN_OR_EQUAL", "<=" -> actual <= target;
            default -> false;
        };
    }

    private boolean compareString(String actual, String operator, String target) {
        if (actual == null || target == null) {
            return false;
        }
        return switch (operator.toUpperCase()) {
            case "EQUALS" -> actual.equalsIgnoreCase(target);
            case "NOT_EQUALS" -> !actual.equalsIgnoreCase(target);
            case "CONTAINS" -> actual.toLowerCase().contains(target.toLowerCase());
            case "EXISTS" -> true;
            default -> false;
        };
    }

    private static class RuleEvaluationResult {
        private final boolean triggered;
        private final String reason;

        public RuleEvaluationResult(boolean triggered, String reason) {
            this.triggered = triggered;
            this.reason = reason;
        }

        public boolean isTriggered() {
            return triggered;
        }

        public String getReason() {
            return reason;
        }
    }
}
