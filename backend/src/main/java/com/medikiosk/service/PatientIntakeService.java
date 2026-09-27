package com.medikiosk.service;

import com.medikiosk.dto.intake.AllergyRequest;
import com.medikiosk.dto.intake.AllergyResponse;
import com.medikiosk.dto.intake.AnswerRequest;
import com.medikiosk.dto.intake.AnswerResponse;
import com.medikiosk.dto.intake.ChiefComplaintRequest;
import com.medikiosk.dto.intake.ChiefComplaintResponse;
import com.medikiosk.dto.intake.ConsentRequest;
import com.medikiosk.dto.intake.ConsentResponse;
import com.medikiosk.dto.intake.IntakeSessionRequest;
import com.medikiosk.dto.intake.IntakeSessionResponse;
import com.medikiosk.dto.intake.MedicalHistoryRequest;
import com.medikiosk.dto.intake.MedicalHistoryResponse;
import com.medikiosk.dto.intake.MedicationRequest;
import com.medikiosk.dto.intake.MedicationResponse;
import com.medikiosk.dto.intake.QuestionResponse;
import com.medikiosk.dto.intake.SymptomRequest;
import com.medikiosk.dto.intake.SymptomResponse;
import com.medikiosk.entity.User;

import java.util.List;
import java.util.UUID;

/**
 * Service contract orchestrating the patient intake workflow and lifecycle.
 */
public interface PatientIntakeService {

    IntakeSessionResponse createSession(IntakeSessionRequest request, User authenticatedUser);

    IntakeSessionResponse getSession(UUID sessionId, User authenticatedUser);

    ConsentResponse recordConsent(UUID sessionId, ConsentRequest request, User authenticatedUser);

    ChiefComplaintResponse recordChiefComplaint(UUID sessionId, ChiefComplaintRequest request, User authenticatedUser);

    SymptomResponse recordSymptom(UUID sessionId, SymptomRequest request, User authenticatedUser);

    MedicalHistoryResponse recordMedicalHistory(UUID sessionId, MedicalHistoryRequest request, User authenticatedUser);

    MedicationResponse recordMedication(UUID sessionId, MedicationRequest request, User authenticatedUser);

    AllergyResponse recordAllergy(UUID sessionId, AllergyRequest request, User authenticatedUser);

    List<QuestionResponse> getApplicableQuestions(UUID sessionId, User authenticatedUser);

    AnswerResponse recordAnswer(UUID sessionId, AnswerRequest request, User authenticatedUser);

    IntakeSessionResponse completeSession(UUID sessionId, User authenticatedUser);
}
