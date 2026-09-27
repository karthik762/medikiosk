import { describe, it, expect, vi, beforeEach } from 'vitest';
import { render, screen, fireEvent, waitFor } from '@testing-library/react';
import { ClinicalSummarySection } from './ClinicalSummarySection';
import { clinicalSummaryService } from '../../services/clinicalSummaryService';
import { ClinicalSummary } from '../../types/clinicalSummary';

vi.mock('../../services/clinicalSummaryService', () => ({
  clinicalSummaryService: {
    getSummaryBySession: vi.fn(),
    generateClinicalSummary: vi.fn(),
    getSummaryById: vi.fn(),
  },
}));

describe('ClinicalSummarySection Component', () => {
  const mockSessionId = '11111111-1111-1111-1111-111111111100';

  const mockSummary: ClinicalSummary = {
    id: 'sum-001',
    sessionId: mockSessionId,
    patientId: 'pat-001',
    chiefComplaint: 'High fever and dry cough for 3 days',
    symptoms: [
      {
        name: 'Fever',
        severity: 7,
        duration: '3 days',
        onset: 'Acute',
        location: 'Whole body',
        frequency: 'Continuous',
        source: 'PATIENT_RESPONSE',
      },
      {
        name: 'Cough',
        severity: 4,
        duration: '3 days',
        onset: 'Gradual',
        location: 'Chest',
        frequency: 'Intermittent',
        source: 'PATIENT_RESPONSE',
      },
    ],
    medicalHistory: [
      {
        category: 'CHRONIC_CONDITION',
        description: 'Type 2 Diabetes Mellitus',
        source: 'PATIENT_RESPONSE',
      },
    ],
    medications: [
      {
        medicationName: 'Metformin',
        dosage: '500 mg',
        frequency: 'Twice daily',
        duration: '2 years',
        source: 'PATIENT_RESPONSE',
      },
    ],
    allergies: [
      {
        allergen: 'Penicillin',
        reaction: 'Urticaria and facial rash',
        severity: 'MODERATE',
        source: 'PATIENT_RESPONSE',
      },
    ],
    ayurvedaSummary: {
      prakritiAssessment: 'Vata-Pitta',
      agniType: 'Tikshnagni',
      kosthaType: 'Krura',
      nidanaFactors: 'Irregular eating and night shifts',
      source: 'AYURVEDA_HISTORY',
    },
    documentFindings: [
      {
        documentId: 'doc-001',
        documentType: 'PRESCRIPTION',
        fileName: 'rx_metformin.png',
        findings: 'Metformin 1000 mg twice daily prescribed',
        confidence: 0.65,
        uncertaintyWarning: 'OCR confidence is 65%. Please verify against the original scanned image.',
        source: 'OCR_EXTRACTION',
      },
    ],
    triage: {
      priority: 'PRIORITY',
      summaryReason: 'Fever with underlying diabetes mellitus',
      triggeredRules: ['TRIAGE-008', 'TRIAGE-014'],
      source: 'RULE_ENGINE',
    },
    missingInformation: [
      {
        category: 'Symptom',
        description: 'Cough duration is unspecified in patient interview',
        clinicalRelevance: 'Helps distinguish acute bronchitis from chronic respiratory condition',
      },
    ],
    potentialConflicts: [
      {
        field: 'Medication.dosage',
        patientStated: '500 mg',
        documentStated: '1000 mg',
        conflictDescription: 'Patient reported 500 mg, but prescription document indicates 1000 mg',
        resolutionStatus: 'PENDING_REVIEW',
      },
    ],
    doctorVerification: {
      status: 'PENDING',
    },
    metadata: {
      generationModel: 'medikiosk-clinical-structuring-mock-v1',
      generatedAt: '2026-09-11T12:00:00Z',
      isMock: true,
      version: 1,
    },
    status: 'AI_GENERATED',
  };

  beforeEach(() => {
    vi.clearAllMocks();
  });

  it('renders non-diagnostic disclaimer and empty state when no summary exists', async () => {
    vi.mocked(clinicalSummaryService.getSummaryBySession).mockRejectedValueOnce(new Error('Not found'));

    render(<ClinicalSummarySection sessionId={mockSessionId} />);

    expect(
      screen.getByText(/PRE-CONSULTATION ASSISTIVE SUMMARY — FOR DOCTOR REVIEW ONLY/i)
    ).toBeInTheDocument();
    expect(
      screen.getByText(/MediKiosk is NOT an autonomous diagnostic system/i)
    ).toBeInTheDocument();

    await waitFor(() => {
      expect(screen.getByText(/No Summary Generated Yet/i)).toBeInTheDocument();
      expect(screen.getByRole('button', { name: /Generate AI Summary/i })).toBeInTheDocument();
    });
  });

  it('renders complete structured clinical summary with source attribution and triage', async () => {
    vi.mocked(clinicalSummaryService.getSummaryBySession).mockResolvedValueOnce({
      success: true,
      data: mockSummary,
    });

    render(<ClinicalSummarySection sessionId={mockSessionId} />);

    await waitFor(() => {
      // Chief Complaint
      expect(screen.getByText('High fever and dry cough for 3 days')).toBeInTheDocument();
      // Symptoms
      expect(screen.getByText('Fever')).toBeInTheDocument();
      expect(screen.getByText('Severity: 7/10')).toBeInTheDocument();
      expect(screen.getByText('Cough')).toBeInTheDocument();
      // Past Medical History
      expect(screen.getByText('Type 2 Diabetes Mellitus')).toBeInTheDocument();
      // Medications
      expect(screen.getByText('Metformin')).toBeInTheDocument();
      // Allergies
      expect(screen.getByText('Penicillin')).toBeInTheDocument();
      // Ayurveda
      expect(screen.getByText('Tikshnagni')).toBeInTheDocument();
      expect(screen.getByText('Krura')).toBeInTheDocument();
      // Deterministic Triage Banner
      expect(screen.getByText(/Rapid Triage Result \(Rule Engine\)/i)).toBeInTheDocument();
      expect(screen.getByText('Fever with underlying diabetes mellitus')).toBeInTheDocument();
    });
  });

  it('renders low-confidence OCR document findings with uncertainty warning', async () => {
    vi.mocked(clinicalSummaryService.getSummaryBySession).mockResolvedValueOnce({
      success: true,
      data: mockSummary,
    });

    render(<ClinicalSummarySection sessionId={mockSessionId} />);

    await waitFor(() => {
      expect(screen.getByText(/OCR Confidence: 65%/i)).toBeInTheDocument();
      expect(
        screen.getByText(/OCR confidence is 65%. Please verify against the original scanned image./i)
      ).toBeInTheDocument();
    });
  });

  it('renders detected clinical conflicts and missing information alerts', async () => {
    vi.mocked(clinicalSummaryService.getSummaryBySession).mockResolvedValueOnce({
      success: true,
      data: mockSummary,
    });

    render(<ClinicalSummarySection sessionId={mockSessionId} />);

    await waitFor(() => {
      expect(screen.getByText(/Detected Clinical Conflicts \(1\)/i)).toBeInTheDocument();
      expect(
        screen.getByText(/Patient reported 500 mg, but prescription document indicates 1000 mg/i)
      ).toBeInTheDocument();
      expect(screen.getByText(/Missing Clinical Details \(1\)/i)).toBeInTheDocument();
      expect(
        screen.getByText(/Cough duration is unspecified in patient interview/i)
      ).toBeInTheDocument();
    });
  });

  it('triggers generation and displays regenerated summary on button click', async () => {
    vi.mocked(clinicalSummaryService.getSummaryBySession).mockRejectedValueOnce(new Error('Not found'));
    vi.mocked(clinicalSummaryService.generateClinicalSummary).mockResolvedValueOnce({
      success: true,
      data: mockSummary,
    });

    render(<ClinicalSummarySection sessionId={mockSessionId} />);

    await waitFor(() => {
      expect(screen.getByText(/No Summary Generated Yet/i)).toBeInTheDocument();
    });

    const generateBtn = screen.getByRole('button', { name: /Generate AI Summary/i });
    fireEvent.click(generateBtn);

    await waitFor(() => {
      expect(clinicalSummaryService.generateClinicalSummary).toHaveBeenCalledWith(mockSessionId, undefined);
      expect(screen.getByText('AI Clinical Summary generated successfully.')).toBeInTheDocument();
      expect(screen.getByText('High fever and dry cough for 3 days')).toBeInTheDocument();
    });
  });

  it('displays error alert when generation fails', async () => {
    vi.mocked(clinicalSummaryService.getSummaryBySession).mockRejectedValueOnce(new Error('Not found'));
    vi.mocked(clinicalSummaryService.generateClinicalSummary).mockRejectedValueOnce(
      new Error('AI structuring failed: clinical safety rule violated')
    );

    render(<ClinicalSummarySection sessionId={mockSessionId} />);

    await waitFor(() => {
      expect(screen.getByText(/No Summary Generated Yet/i)).toBeInTheDocument();
    });

    const generateBtn = screen.getByRole('button', { name: /Generate AI Summary/i });
    fireEvent.click(generateBtn);

    await waitFor(() => {
      expect(
        screen.getByText('AI structuring failed: clinical safety rule violated')
      ).toBeInTheDocument();
    });
  });
});
