package com.medikiosk.fhir.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
public abstract class FhirResource {
    private String resourceType;
    private String id;
    private Map<String, Object> meta;
    private List<FhirExtension> extension;

    public FhirResource() {
    }

    public FhirResource(String resourceType, String id) {
        this.resourceType = resourceType;
        this.id = id;
    }

    public String getResourceType() {
        return resourceType;
    }

    public void setResourceType(String resourceType) {
        this.resourceType = resourceType;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Map<String, Object> getMeta() {
        return meta;
    }

    public void setMeta(Map<String, Object> meta) {
        this.meta = meta;
    }

    public void setProfile(String profileUrl) {
        if (this.meta == null) {
            this.meta = new HashMap<>();
        }
        List<String> profiles = new ArrayList<>();
        profiles.add(profileUrl);
        this.meta.put("profile", profiles);
    }

    public List<FhirExtension> getExtension() {
        return extension;
    }

    public void setExtension(List<FhirExtension> extension) {
        this.extension = extension;
    }

    public void addExtension(String url, String valueString) {
        if (this.extension == null) {
            this.extension = new ArrayList<>();
        }
        this.extension.add(new FhirExtension(url, valueString));
    }

    public void addExtension(String url, Boolean valueBoolean) {
        if (this.extension == null) {
            this.extension = new ArrayList<>();
        }
        this.extension.add(new FhirExtension(url, valueBoolean));
    }
}
