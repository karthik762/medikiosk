package com.medikiosk.concept;

import com.medikiosk.dto.clinical.ClinicalCodingDto;
import com.medikiosk.dto.clinical.DualCodedConceptDto;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * In-memory authoritative clinical concept registry with normalized synonym lookup
 * and dual terminology coding (ICD-10, NAMASTE, UNMAPPED).
 */
@Component
public class ConceptRegistry {

    private final Map<String, ClinicalConcept> conceptsByCode = new HashMap<>();
    private final Map<String, ClinicalConcept> conceptsBySynonym = new HashMap<>();

    public ConceptRegistry() {
        registerBuiltInConcepts();
    }

    public void register(ClinicalConcept concept) {
        conceptsByCode.put(concept.getCode(), concept);
        conceptsBySynonym.put(concept.getDisplayName().toLowerCase(), concept);
        for (String syn : concept.getSynonyms()) {
            conceptsBySynonym.put(syn.toLowerCase(), concept);
        }
    }

    public Optional<ClinicalConcept> findByCode(String code) {
        if (code == null) return Optional.empty();
        return Optional.ofNullable(conceptsByCode.get(code.toUpperCase()));
    }

    public Optional<ClinicalConcept> findByTerm(String term) {
        if (term == null || term.trim().isEmpty()) return Optional.empty();
        String normalized = term.trim().toLowerCase();
        ClinicalConcept exact = conceptsBySynonym.get(normalized);
        if (exact != null) {
            return Optional.of(exact);
        }
        for (Map.Entry<String, ClinicalConcept> entry : conceptsBySynonym.entrySet()) {
            if (normalized.contains(entry.getKey())) {
                return Optional.of(entry.getValue());
            }
        }
        return Optional.empty();
    }

    public List<ClinicalCodingDto> getCodingsForTerm(String term) {
        Optional<ClinicalConcept> conceptOpt = findByTerm(term);
        List<ClinicalCodingDto> codings = new ArrayList<>();

        if (conceptOpt.isPresent()) {
            ClinicalConcept concept = conceptOpt.get();
            if (concept.getIcdCode() != null) {
                codings.add(new ClinicalCodingDto("ICD", concept.getIcdCode(), concept.getIcdDisplay(), 0.95, "CONCEPT_REGISTRY"));
            }
            if (concept.getNamasteCode() != null) {
                codings.add(new ClinicalCodingDto("NAMASTE", concept.getNamasteCode(), concept.getNamasteDisplay(), 0.85, "CONCEPT_REGISTRY"));
            }
        }

        if (codings.isEmpty()) {
            codings.add(ClinicalCodingDto.unmapped(term != null ? term : "Unknown Concept"));
        }

        return codings;
    }

    public DualCodedConceptDto getDualCodedConcept(String term, String status, Double confidence, String source, Boolean verified) {
        Optional<ClinicalConcept> conceptOpt = findByTerm(term);
        String conceptCode = conceptOpt.map(ClinicalConcept::getCode).orElse("UNMAPPED");
        List<ClinicalCodingDto> codings = getCodingsForTerm(term);

        return new DualCodedConceptDto(
                term != null ? term : "Unknown Concept",
                conceptCode,
                codings,
                status != null ? status : "REPORTED",
                confidence != null ? confidence : (conceptOpt.isPresent() ? 0.95 : 0.50),
                source != null ? source : "PATIENT_REPORTED",
                verified != null ? verified : false
        );
    }

    public Collection<ClinicalConcept> getAllConcepts() {
        return Collections.unmodifiableCollection(conceptsByCode.values());
    }

    private void registerBuiltInConcepts() {
        register(new ClinicalConcept("CHEST_PAIN", "Chest Pain", ClinicalConceptCategory.CARDIOVASCULAR, "Pain or pressure in chest cavity", true)
                .withIcdCoding("R07.9", "Chest pain, unspecified")
                .withNamasteCoding("NAM-UROS-SHULA", "Urah Shula (Chest pain)")
                .addSynonym("chest pain")
                .addSynonym("pain in chest")
                .addSynonym("chest discomfort")
                .addSynonym("angina")
                .addSynonym("chest tightness")
                .addSynonym("chest pressure"));

        register(new ClinicalConcept("SHORTNESS_OF_BREATH", "Shortness of Breath", ClinicalConceptCategory.RESPIRATORY, "Dyspnea or difficulty breathing", true)
                .withIcdCoding("R06.02", "Shortness of breath")
                .withNamasteCoding("NAM-SHVASA", "Shvasa Roga (Dyspnea)")
                .addSynonym("shortness of breath")
                .addSynonym("difficulty breathing")
                .addSynonym("breathlessness")
                .addSynonym("dyspnea")
                .addSynonym("hard to breathe")
                .addSynonym("cannot breathe"));

        register(new ClinicalConcept("HEADACHE", "Headache", ClinicalConceptCategory.NEUROLOGICAL, "Pain in cranial or facial region", false)
                .withIcdCoding("R51.9", "Headache, unspecified")
                .withNamasteCoding("NAM-SHIRO-RUK", "Shiroruk / Shiroshula (Headache)")
                .addSynonym("headache")
                .addSynonym("head pain")
                .addSynonym("migraine")
                .addSynonym("throbbing head"));

        register(new ClinicalConcept("FEVER", "Fever", ClinicalConceptCategory.CONSTITUTIONAL, "Elevated core body temperature", false)
                .withIcdCoding("R50.9", "Fever, unspecified")
                .withNamasteCoding("NAM-JWARA", "Jwara (Fever)")
                .addSynonym("fever")
                .addSynonym("high temperature")
                .addSynonym("feverish")
                .addSynonym("chills")
                .addSynonym("pyrexia"));

        register(new ClinicalConcept("COUGH", "Cough", ClinicalConceptCategory.RESPIRATORY, "Sudden expulsion of air from lungs", false)
                .withIcdCoding("R05.9", "Cough, unspecified")
                .withNamasteCoding("NAM-KASA", "Kasa Roga (Cough)")
                .addSynonym("cough")
                .addSynonym("coughing")
                .addSynonym("dry cough")
                .addSynonym("productive cough"));

        register(new ClinicalConcept("DIZZINESS", "Dizziness", ClinicalConceptCategory.NEUROLOGICAL, "Impairment in spatial perception and stability", true)
                .withIcdCoding("R42", "Dizziness and giddiness")
                .withNamasteCoding("NAM-BHRAMA", "Bhrama (Dizziness/Vertigo)")
                .addSynonym("dizziness")
                .addSynonym("dizzy")
                .addSynonym("lightheaded")
                .addSynonym("vertigo")
                .addSynonym("faint")
                .addSynonym("feeling faint")
                .addSynonym("near syncope"));

        register(new ClinicalConcept("ABDOMINAL_PAIN", "Abdominal Pain", ClinicalConceptCategory.GASTROINTESTINAL, "Pain in belly or stomach area", false)
                .withIcdCoding("R10.9", "Unspecified abdominal pain")
                .withNamasteCoding("NAM-UDARA-SHULA", "Udara Shula (Abdominal pain)")
                .addSynonym("abdominal pain")
                .addSynonym("stomach ache")
                .addSynonym("stomach pain")
                .addSynonym("belly pain")
                .addSynonym("cramps in stomach"));

        register(new ClinicalConcept("NAUSEA", "Nausea", ClinicalConceptCategory.GASTROINTESTINAL, "Sensation of unease and urge to vomit", false)
                .withIcdCoding("R11.0", "Nausea")
                .withNamasteCoding("NAM-HRTLASA", "Hrtlasa (Nausea)")
                .addSynonym("nausea")
                .addSynonym("nauseous")
                .addSynonym("queasy")
                .addSynonym("sick to stomach"));

        register(new ClinicalConcept("VOMITING", "Vomiting", ClinicalConceptCategory.GASTROINTESTINAL, "Involuntary expulsion of stomach contents", false)
                .withIcdCoding("R11.10", "Vomiting, unspecified")
                .withNamasteCoding("NAM-CHHARDI", "Chhardi (Vomiting)")
                .addSynonym("vomiting")
                .addSynonym("vomit")
                .addSynonym("threw up")
                .addSynonym("throwing up")
                .addSynonym("emesis"));

        register(new ClinicalConcept("FATIGUE", "Fatigue", ClinicalConceptCategory.CONSTITUTIONAL, "Overwhelming tiredness or weakness", false)
                .withIcdCoding("R53.83", "Other fatigue")
                .withNamasteCoding("NAM-SHRAMA", "Klama / Shrama (Fatigue)")
                .addSynonym("fatigue")
                .addSynonym("tiredness")
                .addSynonym("exhaustion")
                .addSynonym("lethargy")
                .addSynonym("weakness"));

        register(new ClinicalConcept("SWEATING", "Diaphoresis / Sweating", ClinicalConceptCategory.CONSTITUTIONAL, "Excessive perspiration", true)
                .withIcdCoding("R61", "Generalized hyperhidrosis")
                .withNamasteCoding("NAM-SVEDA-ADHIKYA", "Sveda Adhikya (Excessive Sweating)")
                .addSynonym("sweating")
                .addSynonym("cold sweats")
                .addSynonym("profuse sweating")
                .addSynonym("diaphoresis"));

        register(new ClinicalConcept("PALPITATIONS", "Palpitations", ClinicalConceptCategory.CARDIOVASCULAR, "Rapid, fluttering or pounding heartbeat", true)
                .withIcdCoding("R00.2", "Palpitations")
                .withNamasteCoding("NAM-HRD-DAVA", "Hrd Dava / Hrddravata (Palpitations)")
                .addSynonym("palpitations")
                .addSynonym("racing heart")
                .addSynonym("rapid heartbeat")
                .addSynonym("heart fluttering"));

        register(new ClinicalConcept("ALLERGY", "Allergy", ClinicalConceptCategory.ALLERGY_IMMUNOLOGY, "Hypersensitivity reaction to substances", true)
                .withIcdCoding("Z88.9", "Personal history of allergy to unspecified drugs")
                .withNamasteCoding("NAM-SATMYA-ASATMYA", "Asatmya (Allergy / Intolerance)")
                .addSynonym("allergy")
                .addSynonym("allergic")
                .addSynonym("hives")
                .addSynonym("anaphylaxis"));
    }
}

