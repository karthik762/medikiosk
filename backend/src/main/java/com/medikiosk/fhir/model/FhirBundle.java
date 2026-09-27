package com.medikiosk.fhir.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.ArrayList;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class FhirBundle extends FhirResource {
    private FhirIdentifier identifier;
    private String type; // document | collection | transaction
    private String timestamp;
    private Integer total;
    private List<FhirBundleEntry> entry;

    public FhirBundle() {
        super("Bundle", null);
    }

    public FhirBundle(String id) {
        super("Bundle", id);
    }

    public FhirIdentifier getIdentifier() {
        return identifier;
    }

    public void setIdentifier(FhirIdentifier identifier) {
        this.identifier = identifier;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public Integer getTotal() {
        return total;
    }

    public void setTotal(Integer total) {
        this.total = total;
    }

    public List<FhirBundleEntry> getEntry() {
        return entry;
    }

    public void setEntry(List<FhirBundleEntry> entry) {
        this.entry = entry;
        if (entry != null) {
            this.total = entry.size();
        }
    }

    public void addEntry(String fullUrl, FhirResource res) {
        if (this.entry == null) {
            this.entry = new ArrayList<>();
        }
        this.entry.add(new FhirBundleEntry(fullUrl, res));
        this.total = this.entry.size();
    }
}
