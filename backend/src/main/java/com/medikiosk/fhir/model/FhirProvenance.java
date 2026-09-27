package com.medikiosk.fhir.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.ArrayList;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class FhirProvenance extends FhirResource {
    private List<FhirReference> target;
    private String recorded;
    private FhirCodeableConcept activity;
    private List<ProvenanceAgent> agent;

    public FhirProvenance() {
        super("Provenance", null);
    }

    public FhirProvenance(String id) {
        super("Provenance", id);
    }

    public List<FhirReference> getTarget() {
        return target;
    }

    public void setTarget(List<FhirReference> target) {
        this.target = target;
    }

    public void addTarget(FhirReference t) {
        if (this.target == null) {
            this.target = new ArrayList<>();
        }
        this.target.add(t);
    }

    public String getRecorded() {
        return recorded;
    }

    public void setRecorded(String recorded) {
        this.recorded = recorded;
    }

    public FhirCodeableConcept getActivity() {
        return activity;
    }

    public void setActivity(FhirCodeableConcept activity) {
        this.activity = activity;
    }

    public List<ProvenanceAgent> getAgent() {
        return agent;
    }

    public void setAgent(List<ProvenanceAgent> agent) {
        this.agent = agent;
    }

    public void addAgent(ProvenanceAgent a) {
        if (this.agent == null) {
            this.agent = new ArrayList<>();
        }
        this.agent.add(a);
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class ProvenanceAgent {
        private FhirCodeableConcept type;
        private List<FhirCodeableConcept> role;
        private FhirReference who;

        public ProvenanceAgent() {
        }

        public ProvenanceAgent(String typeText, FhirReference who) {
            this.type = new FhirCodeableConcept(typeText);
            this.who = who;
        }

        public ProvenanceAgent(String system, String code, String display, FhirReference who) {
            this.type = new FhirCodeableConcept(system, code, display);
            this.who = who;
        }

        public FhirCodeableConcept getType() {
            return type;
        }

        public void setType(FhirCodeableConcept type) {
            this.type = type;
        }

        public List<FhirCodeableConcept> getRole() {
            return role;
        }

        public void setRole(List<FhirCodeableConcept> role) {
            this.role = role;
        }

        public FhirReference getWho() {
            return who;
        }

        public void setWho(FhirReference who) {
            this.who = who;
        }
    }
}
