package com.medikiosk.fhir.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.ArrayList;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class FhirConsent extends FhirResource {
    private String status; // draft | proposed | active | rejected | inactive | entered-in-error
    private FhirCodeableConcept scope;
    private List<FhirCodeableConcept> category;
    private FhirReference patient;
    private String dateTime;
    private List<FhirReference> performer;
    private ConsentPolicy policy;
    private ConsentProvision provision;

    public FhirConsent() {
        super("Consent", null);
    }

    public FhirConsent(String id) {
        super("Consent", id);
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public FhirCodeableConcept getScope() {
        return scope;
    }

    public void setScope(FhirCodeableConcept scope) {
        this.scope = scope;
    }

    public List<FhirCodeableConcept> getCategory() {
        return category;
    }

    public void setCategory(List<FhirCodeableConcept> category) {
        this.category = category;
    }

    public void addCategory(FhirCodeableConcept c) {
        if (this.category == null) {
            this.category = new ArrayList<>();
        }
        this.category.add(c);
    }

    public FhirReference getPatient() {
        return patient;
    }

    public void setPatient(FhirReference patient) {
        this.patient = patient;
    }

    public String getDateTime() {
        return dateTime;
    }

    public void setDateTime(String dateTime) {
        this.dateTime = dateTime;
    }

    public List<FhirReference> getPerformer() {
        return performer;
    }

    public void setPerformer(List<FhirReference> performer) {
        this.performer = performer;
    }

    public ConsentPolicy getPolicy() {
        return policy;
    }

    public void setPolicy(ConsentPolicy policy) {
        this.policy = policy;
    }

    public ConsentProvision getProvision() {
        return provision;
    }

    public void setProvision(ConsentProvision provision) {
        this.provision = provision;
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class ConsentPolicy {
        private String authority;
        private String uri;

        public ConsentPolicy() {
        }

        public ConsentPolicy(String uri) {
            this.uri = uri;
        }

        public String getAuthority() {
            return authority;
        }

        public void setAuthority(String authority) {
            this.authority = authority;
        }

        public String getUri() {
            return uri;
        }

        public void setUri(String uri) {
            this.uri = uri;
        }
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class ConsentProvision {
        private String type; // opt-in / permit | deny
        private FhirPeriod period;

        public ConsentProvision() {
        }

        public ConsentProvision(String type) {
            this.type = type;
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public FhirPeriod getPeriod() {
            return period;
        }

        public void setPeriod(FhirPeriod period) {
            this.period = period;
        }
    }
}
