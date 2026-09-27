package com.medikiosk.fhir.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.ArrayList;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class FhirComposition extends FhirResource {
    private List<FhirIdentifier> identifier;
    private String status; // preliminary | final | amended | entered-in-error
    private FhirCodeableConcept type;
    private List<FhirCodeableConcept> category;
    private FhirReference subject; // Patient
    private FhirReference encounter;
    private String date;
    private List<FhirReference> author;
    private String title;
    private List<CompositionSection> section;

    public FhirComposition() {
        super("Composition", null);
    }

    public FhirComposition(String id) {
        super("Composition", id);
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

    public FhirReference getEncounter() {
        return encounter;
    }

    public void setEncounter(FhirReference encounter) {
        this.encounter = encounter;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public List<FhirReference> getAuthor() {
        return author;
    }

    public void setAuthor(List<FhirReference> author) {
        this.author = author;
    }

    public void addAuthor(FhirReference a) {
        if (this.author == null) {
            this.author = new ArrayList<>();
        }
        this.author.add(a);
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public List<CompositionSection> getSection() {
        return section;
    }

    public void setSection(List<CompositionSection> section) {
        this.section = section;
    }

    public void addSection(CompositionSection s) {
        if (this.section == null) {
            this.section = new ArrayList<>();
        }
        this.section.add(s);
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class CompositionSection {
        private String title;
        private FhirCodeableConcept code;
        private SectionText text;
        private List<FhirReference> entry;

        public CompositionSection() {
        }

        public CompositionSection(String title, String codeString, String textNarrative) {
            this.title = title;
            if (codeString != null) {
                this.code = new FhirCodeableConcept(codeString);
            }
            if (textNarrative != null) {
                this.text = new SectionText("generated", textNarrative);
            }
        }

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public FhirCodeableConcept getCode() {
            return code;
        }

        public void setCode(FhirCodeableConcept code) {
            this.code = code;
        }

        public SectionText getText() {
            return text;
        }

        public void setText(SectionText text) {
            this.text = text;
        }

        public List<FhirReference> getEntry() {
            return entry;
        }

        public void setEntry(List<FhirReference> entry) {
            this.entry = entry;
        }

        public void addEntry(FhirReference ref) {
            if (this.entry == null) {
                this.entry = new ArrayList<>();
            }
            this.entry.add(ref);
        }
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class SectionText {
        private String status;
        private String div;

        public SectionText() {
        }

        public SectionText(String status, String div) {
            this.status = status;
            this.div = div;
        }

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }

        public String getDiv() {
            return div;
        }

        public void setDiv(String div) {
            this.div = div;
        }
    }
}
