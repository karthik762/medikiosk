import { describe, it, expect, vi, beforeEach } from 'vitest';
import { render, screen, fireEvent, waitFor } from '@testing-library/react';
import { ClinicalReviewWorkstation } from './ClinicalReviewWorkstation';
import { doctorService } from '../../services/doctorService';
import { DoctorReviewDetail } from '../../types/doctor';

vi.mock('../../services/doctorService', () => ({
  doctorService: {
    getQueue: vi.fn(),
    getCaseReview: vi.fn(),
    amendSummary: vi.fn(),
    verifySummary: vi.fn(),
    getInteroperabilityStatus: vi.fn().mockResolvedValue({
      sessionId: 'sess-123',
      patientId: 'pat-123',
      sessionLocked: false,
      clinicalSummaryStatus: 'AI_GENERATED',
      fhirStatus: 'SUCCESS',
      abdmStatus: 'NOT_SYNCED',
      hisStatus: 'NOT_SYNCED',
      healthLakeStatus: 'NOT_SYNCED',
      adapterStatuses: { FHIR: 'SUCCESS', ABDM: 'NOT_SYNCED', HIS: 'NOT_SYNCED', HealthLake: 'NOT_SYNCED' }
    }),
    syncInteroperability: vi.fn().mockResolvedValue({
      sessionId: 'sess-123',
      patientId: 'pat-123',
      sessionLocked: true,
      clinicalSummaryStatus: 'VERIFIED',
      fhirStatus: 'SUCCESS',
      abdmStatus: 'SUCCESS',
      hisStatus: 'SUCCESS',
      healthLakeStatus: 'SUCCESS',
      adapterStatuses: { FHIR: 'SUCCESS', ABDM: 'SUCCESS', HIS: 'SUCCESS', HealthLake: 'SUCCESS' }
    }),
  },
}));

describe('ClinicalReviewWorkstation Component', () => {
  const mockSessionId = 'sess-123';

  const mockDetail: DoctorReviewDetail = {
    sessionId: mockSessionId,
    patientId: 'pat-123',
    patientIdentifier: 'PAT-DOC-123',
    patientName: 'Jane Smith',
    age: 42,
    gender: 'Female',
    dateOfBirth: '1984-05-12',
    phone: '******1234',
    sessionStatus: 'COMPLETED',
    sessionCreatedAt: '2026-09-11T10:00:00Z',
    currentVersion: 1,
    currentStatus: 'AI_GENERATED',
    verified: false,
    aiSummaryAvailable: true,
    triage: {
      priority: 'PRIORITY',
      explanation: 'High fever and persistent cough',
      source: 'RULE_ENGINE',
      triggeredRules: [{ ruleCode: 'RULE_FEVER_HIGH' }],
    },
    clinicalSummary: {
      id: 'sum-123',
      sessionId: mockSessionId,
      patientId: 'pat-123',
      chiefComplaint: 'Persistent dry cough and fever',
      symptoms: [
        {
          name: 'Fever',
          severity: 7,
          duration: '3 days',
          source: 'PATIENT_RESPONSE',
        },
      ],
      medicalHistory: [
        {
          category: 'CHRONIC',
          description: 'Asthma',
          source: 'PATIENT_RESPONSE',
        },
      ],
      medications: [
        {
          medicationName: 'Albuterol',
          dosage: '90 mcg',
          frequency: 'As needed',
          source: 'PATIENT_RESPONSE',
        },
      ],
      allergies: [
        {
          allergen: 'Penicillin',
          reaction: 'Hives',
          source: 'PATIENT_RESPONSE',
        },
      ],
      potentialConflicts: [
        {
          field: 'medication',
          patientStated: 'Albuterol',
          documentStated: 'Salbutamol',
          conflictDescription: 'Document indicates Salbutamol while patient stated Albuterol.',
          resolutionStatus: 'PENDING_REVIEW',
        },
      ],
      documentFindings: [],
      missingInformation: [],
      doctorVerification: {
        status: 'PENDING',
      },
      metadata: {
        generationModel: 'MOCK',
        generatedAt: '2026-09-11T10:05:00Z',
        isMock: true,
        version: 1,
      },
      status: 'AI_GENERATED',
    },
    originalPatientData: {
      chiefComplaint: 'Persistent dry cough and fever',
      intakeAnswers: [
        {
          questionCode: 'CC_01',
          questionText: 'What is your primary reason for visiting today?',
          answerText: 'Persistent dry cough and fever',
          inputMethod: 'VOICE',
          originalTranscription: 'persistent dry cough and fever',
          answeredAt: '2026-09-11T10:01:00Z',
        },
      ],
      symptoms: [
        {
          name: 'Fever',
          severity: 7,
          duration: '3 days',
          source: 'PATIENT',
        },
      ],
      medicalHistory: [
        {
          category: 'CHRONIC',
          description: 'Asthma',
          source: 'PATIENT',
        },
      ],
      medications: [
        {
          medicationName: 'Albuterol',
          dosage: '90 mcg',
          frequency: 'As needed',
          source: 'PATIENT',
        },
      ],
      allergies: [
        {
          allergen: 'Penicillin',
          reaction: 'Hives',
          severity: 'MILD',
          source: 'PATIENT',
        },
      ],
    },
    documents: [
      {
        id: 'doc-001',
        patientId: 'pat-123',
        sessionId: mockSessionId,
        fileName: 'prescription.pdf',
        fileType: 'application/pdf',
        fileSize: 1024,
        documentType: 'PRESCRIPTION',
        processingStatus: 'COMPLETED',
        uploadedAt: '2026-09-11T10:02:00Z',
        extraction: {
          id: 'ext-001',
          documentId: 'doc-001',
          extractedText: 'Rx: Albuterol inhaler',
          confidenceScore: 0.94,
          status: 'SUCCESS',
          createdAt: '2026-09-11T10:03:00Z',
        },
      },
    ],
    reviewHistory: [],
  };

  beforeEach(() => {
    vi.clearAllMocks();
  });

  it('renders two-column workstation with triage, AI summary, and original patient data', async () => {
    vi.mocked(doctorService.getCaseReview).mockResolvedValue({
      success: true,
      data: mockDetail,
      message: 'Details retrieved',
    });

    render(
      <ClinicalReviewWorkstation
        sessionId={mockSessionId}
        authToken="token-123"
        onBackToQueue={vi.fn()}
      />
    );

    await waitFor(() => {
      expect(screen.getByText(/Case Review: Jane Smith/i)).toBeInTheDocument();
    });

    expect(screen.getByText('PAT-DOC-123')).toBeInTheDocument();
    expect(screen.getByText('PRIORITY')).toBeInTheDocument();
    expect(screen.getByText('High fever and persistent cough')).toBeInTheDocument();
    expect(screen.getByText('Pre-Consult Clinical Summary')).toBeInTheDocument();
    expect(screen.getByText('Original Patient Intake Source')).toBeInTheDocument();
    expect(screen.getAllByText('Persistent dry cough and fever').length).toBeGreaterThanOrEqual(2);
  });

  it('switches tabs in the original source data column', async () => {
    vi.mocked(doctorService.getCaseReview).mockResolvedValue({
      success: true,
      data: mockDetail,
      message: 'Details retrieved',
    });

    render(
      <ClinicalReviewWorkstation
        sessionId={mockSessionId}
        authToken="token-123"
        onBackToQueue={vi.fn()}
      />
    );

    await waitFor(() => {
      expect(screen.getByText(/Case Review: Jane Smith/i)).toBeInTheDocument();
    });

    // Switch to Documents tab
    const docsTab = screen.getByRole('button', { name: /Documents \(1\)/i });
    fireEvent.click(docsTab);

    expect(screen.getByText('prescription.pdf')).toBeInTheDocument();
    expect(screen.getByText('Rx: Albuterol inhaler')).toBeInTheDocument();
    expect(screen.getByText(/Confidence: 94%/i)).toBeInTheDocument();
  });

  it('allows physician to edit summary and save amendments', async () => {
    vi.mocked(doctorService.getCaseReview).mockResolvedValue({
      success: true,
      data: mockDetail,
      message: 'Details retrieved',
    });
    vi.mocked(doctorService.amendSummary).mockResolvedValue({
      success: true,
      data: {
        ...mockDetail.clinicalSummary!,
        metadata: { ...mockDetail.clinicalSummary!.metadata, version: 2 },
      },
      message: 'Amended',
    });

    render(
      <ClinicalReviewWorkstation
        sessionId={mockSessionId}
        authToken="token-123"
        onBackToQueue={vi.fn()}
      />
    );

    await waitFor(() => {
      expect(screen.getByRole('button', { name: /Edit Summary/i })).toBeInTheDocument();
    });

    fireEvent.click(screen.getByRole('button', { name: /Edit Summary/i }));

    const complaintInput = screen.getByPlaceholderText(/Enter chief complaint.../i);
    fireEvent.change(complaintInput, { target: { value: 'Persistent dry cough and fever (amended)' } });

    const saveBtn = screen.getByRole('button', { name: /Save Amendments/i });
    fireEvent.click(saveBtn);

    await waitFor(() => {
      expect(doctorService.amendSummary).toHaveBeenCalledWith(
        mockSessionId,
        expect.objectContaining({
          expectedVersion: 1,
          amendedSummary: expect.objectContaining({
            chiefComplaint: 'Persistent dry cough and fever (amended)',
          }),
        }),
        'token-123'
      );
    });
  });

  it('handles 409 concurrency conflict when saving amendments', async () => {
    vi.mocked(doctorService.getCaseReview).mockResolvedValue({
      success: true,
      data: mockDetail,
      message: 'Details retrieved',
    });
    const conflictErr = new Error('Conflict: The clinical summary has been modified');
    (conflictErr as any).status = 409;
    vi.mocked(doctorService.amendSummary).mockRejectedValue(conflictErr);

    render(
      <ClinicalReviewWorkstation
        sessionId={mockSessionId}
        authToken="token-123"
        onBackToQueue={vi.fn()}
      />
    );

    await waitFor(() => {
      expect(screen.getByRole('button', { name: /Edit Summary/i })).toBeInTheDocument();
    });

    fireEvent.click(screen.getByRole('button', { name: /Edit Summary/i }));
    fireEvent.click(screen.getByRole('button', { name: /Save Amendments/i }));

    await waitFor(() => {
      expect(screen.getByText(/Concurrency Conflict:/i)).toBeInTheDocument();
    });
  });

  it('opens verification modal and executes verification with confirmation checkbox', async () => {
    vi.mocked(doctorService.getCaseReview).mockResolvedValue({
      success: true,
      data: mockDetail,
      message: 'Details retrieved',
    });
    vi.mocked(doctorService.verifySummary).mockResolvedValue({
      success: true,
      data: { ...mockDetail.clinicalSummary!, status: 'DOCTOR_VERIFIED' },
      message: 'Verified',
    });

    render(
      <ClinicalReviewWorkstation
        sessionId={mockSessionId}
        authToken="token-123"
        onBackToQueue={vi.fn()}
      />
    );

    await waitFor(() => {
      expect(screen.getByRole('button', { name: /Verify Case/i })).toBeInTheDocument();
    });

    fireEvent.click(screen.getByRole('button', { name: /Verify Case/i }));

    expect(screen.getByText('Confirm Clinical Verification & Sign-Off')).toBeInTheDocument();

    const signButton = screen.getByRole('button', { name: /Sign & Complete Verification/i });
    expect(signButton).toBeDisabled();

    // Check confirmation checkbox
    const checkbox = screen.getByRole('checkbox', {
      name: /I confirm that I have reviewed the intake data and verify this clinical summary/i,
    });
    fireEvent.click(checkbox);
    expect(signButton).not.toBeDisabled();

    fireEvent.click(signButton);

    await waitFor(() => {
      expect(doctorService.verifySummary).toHaveBeenCalledWith(
        mockSessionId,
        expect.objectContaining({
          expectedVersion: 1,
          confirmUnderstood: true,
        }),
        'token-123'
      );
    });
  });

  it('displays resiliency notification when AI summary is unavailable', async () => {
    const resilientDetail = { ...mockDetail, aiSummaryAvailable: false, clinicalSummary: undefined };
    vi.mocked(doctorService.getCaseReview).mockResolvedValue({
      success: true,
      data: resilientDetail,
      message: 'Details retrieved',
    });

    render(
      <ClinicalReviewWorkstation
        sessionId={mockSessionId}
        authToken="token-123"
        onBackToQueue={vi.fn()}
      />
    );

    await waitFor(() => {
      expect(screen.getByText('AI Pre-Consult Summary Unavailable')).toBeInTheDocument();
    });
  });
});
