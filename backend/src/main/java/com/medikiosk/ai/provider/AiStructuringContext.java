package com.medikiosk.ai.provider;

import com.medikiosk.entity.Allergy;
import com.medikiosk.entity.Answer;
import com.medikiosk.entity.AyurvedaHistory;
import com.medikiosk.entity.DocumentExtraction;
import com.medikiosk.entity.IntakeSession;
import com.medikiosk.entity.MedicalDocument;
import com.medikiosk.entity.MedicalHistory;
import com.medikiosk.entity.Medication;
import com.medikiosk.entity.Patient;
import com.medikiosk.entity.Symptom;
import com.medikiosk.entity.TriageResult;

import java.util.List;
import java.util.Map;

/**
 * Encapsulates all collected patient intake and document OCR context supplied to the AI provider.
 */
public class AiStructuringContext {

    private Patient patient;
    private IntakeSession session;
    private List<Answer> answers;
    private List<Symptom> symptoms;
    private List<MedicalHistory> medicalHistories;
    private List<Medication> medications;
    private List<Allergy> allergies;
    private AyurvedaHistory ayurvedaHistory;
    private TriageResult triageResult;
    private List<MedicalDocument> documents;
    private Map<MedicalDocument, DocumentExtraction> extractions;

    public AiStructuringContext() {
    }

    public Patient getPatient() {
        return patient;
    }

    public void setPatient(Patient patient) {
        this.patient = patient;
    }

    public IntakeSession getSession() {
        return session;
    }

    public void setSession(IntakeSession session) {
        this.session = session;
    }

    public List<Answer> getAnswers() {
        return answers;
    }

    public void setAnswers(List<Answer> answers) {
        this.answers = answers;
    }

    public List<Symptom> getSymptoms() {
        return symptoms;
    }

    public void setSymptoms(List<Symptom> symptoms) {
        this.symptoms = symptoms;
    }

    public List<MedicalHistory> getMedicalHistories() {
        return medicalHistories;
    }

    public void setMedicalHistories(List<MedicalHistory> medicalHistories) {
        this.medicalHistories = medicalHistories;
    }

    public List<Medication> getMedications() {
        return medications;
    }

    public void setMedications(List<Medication> medications) {
        this.medications = medications;
    }

    public List<Allergy> getAllergies() {
        return allergies;
    }

    public void setAllergies(List<Allergy> allergies) {
        this.allergies = allergies;
    }

    public AyurvedaHistory getAyurvedaHistory() {
        return ayurvedaHistory;
    }

    public void setAyurvedaHistory(AyurvedaHistory ayurvedaHistory) {
        this.ayurvedaHistory = ayurvedaHistory;
    }

    public TriageResult getTriageResult() {
        return triageResult;
    }

    public void setTriageResult(TriageResult triageResult) {
        this.triageResult = triageResult;
    }

    public List<MedicalDocument> getDocuments() {
        return documents;
    }

    public void setDocuments(List<MedicalDocument> documents) {
        this.documents = documents;
    }

    public Map<MedicalDocument, DocumentExtraction> getExtractions() {
        return extractions;
    }

    public void setExtractions(Map<MedicalDocument, DocumentExtraction> extractions) {
        this.extractions = extractions;
    }
}
