package com.medikiosk.fhir;

import com.medikiosk.dto.fhir.FhirValidationResponseDto;
import com.medikiosk.fhir.model.FhirBundle;
import com.medikiosk.fhir.model.FhirResource;

public interface FhirValidationService {

    /**
     * Validates a complete FHIR R4 Bundle against MediKiosk FHIR profiles and invariants.
     *
     * @param bundle FHIR Bundle to validate
     * @return Validation result including errors and warnings
     */
    FhirValidationResponseDto validateBundle(FhirBundle bundle);

    /**
     * Validates a single FHIR R4 resource against MediKiosk FHIR invariants.
     *
     * @param resource FHIR resource to validate
     * @return Validation result
     */
    FhirValidationResponseDto validateResource(FhirResource resource);
}
