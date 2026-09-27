package com.medikiosk.fhir.model;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class FhirBundleEntry {
    private String fullUrl;
    private FhirResource resource;

    public FhirBundleEntry() {
    }

    public FhirBundleEntry(String fullUrl, FhirResource resource) {
        this.fullUrl = fullUrl;
        this.resource = resource;
    }

    public String getFullUrl() {
        return fullUrl;
    }

    public void setFullUrl(String fullUrl) {
        this.fullUrl = fullUrl;
    }

    public FhirResource getResource() {
        return resource;
    }

    public void setResource(FhirResource resource) {
        this.resource = resource;
    }
}
