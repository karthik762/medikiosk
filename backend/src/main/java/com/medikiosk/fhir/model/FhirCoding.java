package com.medikiosk.fhir.model;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class FhirCoding {
    private String system;
    private String version;
    private String code;
    private String display;
    private Boolean userSelected;

    public FhirCoding() {
    }

    public FhirCoding(String system, String code, String display) {
        this.system = system;
        this.code = code;
        this.display = display;
    }

    public String getSystem() {
        return system;
    }

    public void setSystem(String system) {
        this.system = system;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getDisplay() {
        return display;
    }

    public void setDisplay(String display) {
        this.display = display;
    }

    public Boolean getUserSelected() {
        return userSelected;
    }

    public void setUserSelected(Boolean userSelected) {
        this.userSelected = userSelected;
    }
}
