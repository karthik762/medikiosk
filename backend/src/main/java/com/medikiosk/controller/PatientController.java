package com.medikiosk.controller;

import com.medikiosk.dto.ApiResponse;
import com.medikiosk.dto.intake.PatientRequest;
import com.medikiosk.dto.intake.PatientResponse;
import com.medikiosk.entity.User;
import com.medikiosk.service.PatientService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/patients")
public class PatientController {

    private final PatientService patientService;

    public PatientController(PatientService patientService) {
        this.patientService = patientService;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<PatientResponse>> registerPatient(
            @Valid @RequestBody PatientRequest request,
            Authentication authentication) {
        User user = getUserFromAuth(authentication);
        PatientResponse response = patientService.registerPatient(request, user);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "Patient registered successfully"));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<PatientResponse>> getPatient(
            @PathVariable UUID id,
            Authentication authentication) {
        User user = getUserFromAuth(authentication);
        PatientResponse response = patientService.getPatient(id, user);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/by-identifier/{identifier}")
    public ResponseEntity<ApiResponse<PatientResponse>> getPatientByIdentifier(
            @PathVariable String identifier,
            Authentication authentication) {
        User user = getUserFromAuth(authentication);
        PatientResponse response = patientService.getPatientByIdentifier(identifier, user);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    private User getUserFromAuth(Authentication authentication) {
        if (authentication != null && authentication.getPrincipal() instanceof User user) {
            return user;
        }
        return null;
    }
}
