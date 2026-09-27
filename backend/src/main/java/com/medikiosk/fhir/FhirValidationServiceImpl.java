package com.medikiosk.fhir;

import com.medikiosk.dto.fhir.FhirValidationResponseDto;
import com.medikiosk.fhir.model.FhirAllergyIntolerance;
import com.medikiosk.fhir.model.FhirBundle;
import com.medikiosk.fhir.model.FhirBundleEntry;
import com.medikiosk.fhir.model.FhirComposition;
import com.medikiosk.fhir.model.FhirCondition;
import com.medikiosk.fhir.model.FhirDocumentReference;
import com.medikiosk.fhir.model.FhirEncounter;
import com.medikiosk.fhir.model.FhirMedicationStatement;
import com.medikiosk.fhir.model.FhirObservation;
import com.medikiosk.fhir.model.FhirPatient;
import com.medikiosk.fhir.model.FhirResource;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class FhirValidationServiceImpl implements FhirValidationService {

    private static final Set<String> SUPPORTED_TYPES = Set.of(
            "Bundle", "Composition", "Patient", "Encounter", "Consent",
            "Observation", "Condition", "MedicationStatement",
            "AllergyIntolerance", "DocumentReference", "Provenance"
    );

    @Override
    public FhirValidationResponseDto validateBundle(FhirBundle bundle) {
        List<String> errors = new ArrayList<>();
        List<String> warnings = new ArrayList<>();
        int totalChecked = 0;

        if (bundle == null) {
            errors.add("Bundle cannot be null");
            return new FhirValidationResponseDto(false, errors, warnings, 0);
        }

        totalChecked++;

        if (!"Bundle".equals(bundle.getResourceType())) {
            errors.add("Invalid bundle resourceType: " + bundle.getResourceType());
        }

        if (bundle.getId() == null || bundle.getId().isBlank()) {
            errors.add("Bundle must have a non-empty id");
        }

        if (!"document".equalsIgnoreCase(bundle.getType()) && !"collection".equalsIgnoreCase(bundle.getType())) {
            warnings.add("Bundle type is typically 'document' or 'collection', found: " + bundle.getType());
        }

        List<FhirBundleEntry> entries = bundle.getEntry();
        if (entries == null || entries.isEmpty()) {
            errors.add("Bundle entries must not be empty");
            return new FhirValidationResponseDto(errors.isEmpty(), errors, warnings, totalChecked);
        }

        // For document bundles, the first entry MUST be a Composition
        if ("document".equalsIgnoreCase(bundle.getType())) {
            FhirResource firstResource = entries.get(0).getResource();
            if (firstResource == null || !"Composition".equals(firstResource.getResourceType())) {
                errors.add("First entry in a document bundle must be a Composition resource");
            }
        }

        // Collect all fullUrls and resource references in bundle
        Set<String> resourceKeys = new HashSet<>();
        Set<String> fullUrls = new HashSet<>();

        for (FhirBundleEntry entry : entries) {
            totalChecked++;
            if (entry.getFullUrl() != null) {
                fullUrls.add(entry.getFullUrl());
            }
            FhirResource res = entry.getResource();
            if (res != null) {
                if (res.getResourceType() != null && res.getId() != null) {
                    resourceKeys.add(res.getResourceType() + "/" + res.getId());
                    resourceKeys.add("urn:uuid:" + res.getId());
                }
                validateIndividualResource(res, errors, warnings);
            } else {
                errors.add("Bundle entry has null resource");
            }
        }

        return new FhirValidationResponseDto(errors.isEmpty(), errors, warnings, totalChecked);
    }

    @Override
    public FhirValidationResponseDto validateResource(FhirResource resource) {
        List<String> errors = new ArrayList<>();
        List<String> warnings = new ArrayList<>();

        if (resource == null) {
            errors.add("Resource cannot be null");
            return new FhirValidationResponseDto(false, errors, warnings, 0);
        }

        validateIndividualResource(resource, errors, warnings);
        return new FhirValidationResponseDto(errors.isEmpty(), errors, warnings, 1);
    }

    private void validateIndividualResource(FhirResource res, List<String> errors, List<String> warnings) {
        String type = res.getResourceType();
        if (type == null || !SUPPORTED_TYPES.contains(type)) {
            errors.add("Unsupported or missing resourceType: " + type);
            return;
        }

        if (res.getId() == null || res.getId().isBlank()) {
            errors.add(type + " resource is missing required id");
        }

        switch (type) {
            case "Composition" -> {
                if (res instanceof FhirComposition comp) {
                    if (comp.getStatus() == null || comp.getStatus().isBlank()) {
                        errors.add("Composition missing required status");
                    }
                    if (comp.getSubject() == null || comp.getSubject().getReference() == null) {
                        errors.add("Composition missing required subject reference");
                    }
                    if (comp.getTitle() == null || comp.getTitle().isBlank()) {
                        errors.add("Composition missing required title");
                    }
                    if (comp.getType() == null) {
                        warnings.add("Composition missing type CodeableConcept");
                    }
                }
            }
            case "Patient" -> {
                if (res instanceof FhirPatient patient) {
                    if (patient.getName() == null || patient.getName().isEmpty()) {
                        warnings.add("Patient resource has no name element");
                    }
                }
            }
            case "Encounter" -> {
                if (res instanceof FhirEncounter enc) {
                    if (enc.getStatus() == null || enc.getStatus().isBlank()) {
                        errors.add("Encounter missing required status");
                    }
                    if (enc.getClass_() == null) {
                        errors.add("Encounter missing required class coding");
                    }
                }
            }
            case "Observation" -> {
                if (res instanceof FhirObservation obs) {
                    if (obs.getStatus() == null || obs.getStatus().isBlank()) {
                        errors.add("Observation missing required status");
                    }
                    if (obs.getCode() == null) {
                        errors.add("Observation missing required code");
                    }
                }
            }
            case "Condition" -> {
                if (res instanceof FhirCondition cond) {
                    if (cond.getCode() == null) {
                        errors.add("Condition missing required code");
                    }
                }
            }
            case "MedicationStatement" -> {
                if (res instanceof FhirMedicationStatement med) {
                    if (med.getStatus() == null || med.getStatus().isBlank()) {
                        errors.add("MedicationStatement missing required status");
                    }
                    if (med.getMedicationCodeableConcept() == null) {
                        errors.add("MedicationStatement missing medicationCodeableConcept");
                    }
                }
            }
            case "AllergyIntolerance" -> {
                if (res instanceof FhirAllergyIntolerance allergy) {
                    if (allergy.getCode() == null) {
                        errors.add("AllergyIntolerance missing required code");
                    }
                }
            }
            case "DocumentReference" -> {
                if (res instanceof FhirDocumentReference doc) {
                    if (doc.getStatus() == null || doc.getStatus().isBlank()) {
                        errors.add("DocumentReference missing required status");
                    }
                    if (doc.getContent() != null) {
                        for (FhirDocumentReference.DocumentContent dc : doc.getContent()) {
                            if (dc.getAttachment() != null && dc.getAttachment().getUrl() != null) {
                                String url = dc.getAttachment().getUrl();
                                if (url.startsWith("file:") || url.contains(":\\") || url.startsWith("/tmp") || url.startsWith("/var")) {
                                    errors.add("DocumentReference attachment URL must be an authenticated REST endpoint, not a filesystem path: " + url);
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
