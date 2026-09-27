package com.medikiosk.fhir.model;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class FhirContactPoint {
    private String system;
    private String value;
    private String use;
    private Integer rank;

    public FhirContactPoint() {
    }

    public FhirContactPoint(String system, String value, String use) {
        this.system = system;
        this.value = value;
        this.use = use;
    }

    public String getSystem() {
        return system;
    }

    public void setSystem(String system) {
        this.system = system;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getUse() {
        return use;
    }

    public void setUse(String use) {
        this.use = use;
    }

    public Integer getRank() {
        return rank;
    }

    public void setRank(Integer rank) {
        this.rank = rank;
    }
}
