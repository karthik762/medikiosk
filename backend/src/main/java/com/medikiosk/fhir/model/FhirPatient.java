package com.medikiosk.fhir.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.ArrayList;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class FhirPatient extends FhirResource {
    private List<FhirIdentifier> identifier;
    private Boolean active;
    private List<FhirHumanName> name;
    private List<FhirContactPoint> telecom;
    private String gender;
    private String birthDate;
    private List<FhirAddress> address;
    private List<PatientCommunication> communication;

    public FhirPatient() {
        super("Patient", null);
    }

    public FhirPatient(String id) {
        super("Patient", id);
    }

    public List<FhirIdentifier> getIdentifier() {
        return identifier;
    }

    public void setIdentifier(List<FhirIdentifier> identifier) {
        this.identifier = identifier;
    }

    public void addIdentifier(FhirIdentifier id) {
        if (this.identifier == null) {
            this.identifier = new ArrayList<>();
        }
        this.identifier.add(id);
    }

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }

    public List<FhirHumanName> getName() {
        return name;
    }

    public void setName(List<FhirHumanName> name) {
        this.name = name;
    }

    public void addName(FhirHumanName n) {
        if (this.name == null) {
            this.name = new ArrayList<>();
        }
        this.name.add(n);
    }

    public List<FhirContactPoint> getTelecom() {
        return telecom;
    }

    public void setTelecom(List<FhirContactPoint> telecom) {
        this.telecom = telecom;
    }

    public void addTelecom(FhirContactPoint t) {
        if (this.telecom == null) {
            this.telecom = new ArrayList<>();
        }
        this.telecom.add(t);
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getBirthDate() {
        return birthDate;
    }

    public void setBirthDate(String birthDate) {
        this.birthDate = birthDate;
    }

    public List<FhirAddress> getAddress() {
        return address;
    }

    public void setAddress(List<FhirAddress> address) {
        this.address = address;
    }

    public void addAddress(FhirAddress a) {
        if (this.address == null) {
            this.address = new ArrayList<>();
        }
        this.address.add(a);
    }

    public List<PatientCommunication> getCommunication() {
        return communication;
    }

    public void setCommunication(List<PatientCommunication> communication) {
        this.communication = communication;
    }

    public void addCommunication(FhirCodeableConcept language, Boolean preferred) {
        if (this.communication == null) {
            this.communication = new ArrayList<>();
        }
        PatientCommunication pc = new PatientCommunication();
        pc.setLanguage(language);
        pc.setPreferred(preferred);
        this.communication.add(pc);
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class PatientCommunication {
        private FhirCodeableConcept language;
        private Boolean preferred;

        public FhirCodeableConcept getLanguage() {
            return language;
        }

        public void setLanguage(FhirCodeableConcept language) {
            this.language = language;
        }

        public Boolean getPreferred() {
            return preferred;
        }

        public void setPreferred(Boolean preferred) {
            this.preferred = preferred;
        }
    }
}
