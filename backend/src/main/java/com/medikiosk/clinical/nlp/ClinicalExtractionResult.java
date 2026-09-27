package com.medikiosk.clinical.nlp;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Result of clinical entity extraction with partitioned affirmed and negated concepts.
 */
public class ClinicalExtractionResult {

    private final List<ClinicalEntity> entities = new ArrayList<>();
    private final List<String> affirmedConcepts = new ArrayList<>();
    private final List<String> negatedConcepts = new ArrayList<>();
    private double overallConfidence = 1.0;

    public ClinicalExtractionResult() {
    }

    public void addEntity(ClinicalEntity entity) {
        if (entity != null) {
            entities.add(entity);
            if (entity.getConceptCode() != null) {
                if (entity.isNegated()) {
                    if (!negatedConcepts.contains(entity.getConceptCode())) {
                        negatedConcepts.add(entity.getConceptCode());
                    }
                } else {
                    if (!affirmedConcepts.contains(entity.getConceptCode())) {
                        affirmedConcepts.add(entity.getConceptCode());
                    }
                }
            }
        }
    }

    public List<ClinicalEntity> getEntities() {
        return Collections.unmodifiableList(entities);
    }

    public List<String> getAffirmedConcepts() {
        return Collections.unmodifiableList(affirmedConcepts);
    }

    public List<String> getNegatedConcepts() {
        return Collections.unmodifiableList(negatedConcepts);
    }

    public double getOverallConfidence() {
        return overallConfidence;
    }

    public void setOverallConfidence(double overallConfidence) {
        this.overallConfidence = overallConfidence;
    }
}
