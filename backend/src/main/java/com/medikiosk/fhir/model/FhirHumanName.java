package com.medikiosk.fhir.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class FhirHumanName {
    private String use;
    private String text;
    private String family;
    private List<String> given;
    private List<String> prefix;
    private List<String> suffix;

    public FhirHumanName() {
    }

    public FhirHumanName(String use, String text) {
        this.use = use;
        this.text = text;
    }

    public String getUse() {
        return use;
    }

    public void setUse(String use) {
        this.use = use;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getFamily() {
        return family;
    }

    public void setFamily(String family) {
        this.family = family;
    }

    public List<String> getGiven() {
        return given;
    }

    public void setGiven(List<String> given) {
        this.given = given;
    }

    public List<String> getPrefix() {
        return prefix;
    }

    public void setPrefix(List<String> prefix) {
        this.prefix = prefix;
    }

    public List<String> getSuffix() {
        return suffix;
    }

    public void setSuffix(List<String> suffix) {
        this.suffix = suffix;
    }
}
