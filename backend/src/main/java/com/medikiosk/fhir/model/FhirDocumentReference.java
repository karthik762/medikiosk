package com.medikiosk.fhir.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.ArrayList;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class FhirDocumentReference extends FhirResource {
    private List<FhirIdentifier> identifier;
    private String status; // current | superseded | entered-in-error
    private String docStatus; // preliminary | final | amended | entered-in-error
    private FhirCodeableConcept type;
    private List<FhirCodeableConcept> category;
    private FhirReference subject; // Patient
    private String date;
    private String description;
    private List<DocumentContent> content;
    private DocumentContext context;

    public FhirDocumentReference() {
        super("DocumentReference", null);
    }

    public FhirDocumentReference(String id) {
        super("DocumentReference", id);
    }

    public List<FhirIdentifier> getIdentifier() {
        return identifier;
    }

    public void setIdentifier(List<FhirIdentifier> identifier) {
        this.identifier = identifier;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getDocStatus() {
        return docStatus;
    }

    public void setDocStatus(String docStatus) {
        this.docStatus = docStatus;
    }

    public FhirCodeableConcept getType() {
        return type;
    }

    public void setType(FhirCodeableConcept type) {
        this.type = type;
    }

    public List<FhirCodeableConcept> getCategory() {
        return category;
    }

    public void setCategory(List<FhirCodeableConcept> category) {
        this.category = category;
    }

    public FhirReference getSubject() {
        return subject;
    }

    public void setSubject(FhirReference subject) {
        this.subject = subject;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<DocumentContent> getContent() {
        return content;
    }

    public void setContent(List<DocumentContent> content) {
        this.content = content;
    }

    public void addAttachment(FhirAttachment attachment) {
        if (this.content == null) {
            this.content = new ArrayList<>();
        }
        this.content.add(new DocumentContent(attachment));
    }

    public DocumentContext getContext() {
        return context;
    }

    public void setContext(DocumentContext context) {
        this.context = context;
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class DocumentContent {
        private FhirAttachment attachment;

        public DocumentContent() {
        }

        public DocumentContent(FhirAttachment attachment) {
            this.attachment = attachment;
        }

        public FhirAttachment getAttachment() {
            return attachment;
        }

        public void setAttachment(FhirAttachment attachment) {
            this.attachment = attachment;
        }
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class DocumentContext {
        private List<FhirReference> encounter;

        public DocumentContext() {
        }

        public DocumentContext(FhirReference encounterRef) {
            this.encounter = new ArrayList<>();
            this.encounter.add(encounterRef);
        }

        public List<FhirReference> getEncounter() {
            return encounter;
        }

        public void setEncounter(List<FhirReference> encounter) {
            this.encounter = encounter;
        }
    }
}
