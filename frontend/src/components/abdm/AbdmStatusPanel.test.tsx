import { describe, it, expect, vi, beforeEach } from 'vitest';
import { render, screen, fireEvent, waitFor } from '@testing-library/react';
import { AbdmStatusPanel } from './AbdmStatusPanel';
import { abdmService } from '../../services/abdmService';
import { AbdmStatus, AbhaVerificationResponse, AbdmConsentResponse, AbdmExchangeResponse } from '../../types/abdm';

vi.mock('../../services/abdmService', () => ({
  abdmService: {
    getStatus: vi.fn(),
    verifyAbha: vi.fn(),
    grantConsent: vi.fn(),
    revokeConsent: vi.fn(),
    exchangeClinicalData: vi.fn(),
    getSessionStatus: vi.fn(),
  },
}));

describe('AbdmStatusPanel Component', () => {
  const mockSessionId = 'session-1234';
  const mockPatientId = 'patient-5678';

  const mockAbdmStatus: AbdmStatus = {
    mode: 'MOCK',
    isMock: true,
    enabled: true,
    gatewayBaseUrl: 'mock://localhost',
    connected: false,
    message: 'ABDM Mock Mode active. External live connection: NOT CONNECTED.',
    timestamp: '2026-09-12T00:00:00Z',
  };

  const mockVerificationSuccess: AbhaVerificationResponse = {
    verified: true,
    status: 'VERIFIED',
    maskedAbha: 'XX-XXXX-XXXX-1234',
    verificationMethod: 'MOCK_DETERMINISTIC_OTP',
    isMock: true,
    message: 'ABHA verified successfully (MOCK mode)',
    verifiedAt: '2026-09-12T00:00:00Z',
  };

  const mockConsentSuccess: AbdmConsentResponse = {
    consentId: 'consent-abc',
    patientId: mockPatientId,
    sessionId: mockSessionId,
    consentStatus: 'ACCEPTED',
    consentType: 'ABDM_DATA_SHARE',
    externalArtefactId: 'MOCK-ARTEFACT-777',
    consentedAt: '2026-09-12T00:00:00Z',
    revokedAt: null,
    isMock: true,
    message: 'Consent granted',
  };

  const mockExchangeSuccess: AbdmExchangeResponse = {
    integrationRecordId: 'rec-001',
    sessionId: mockSessionId,
    patientId: mockPatientId,
    status: 'SUCCESS',
    transactionId: 'MOCK-TXN-999',
    bundleId: 'Bundle-session-1234',
    idempotencyKey: 'KEY-123',
    isMock: true,
    attemptedAt: '2026-09-12T00:00:00Z',
    completedAt: '2026-09-12T00:00:01Z',
    errorMessage: null,
    message: 'Exchange successful',
  };

  beforeEach(() => {
    vi.clearAllMocks();
    (abdmService.getStatus as any).mockResolvedValue({
      success: true,
      data: mockAbdmStatus,
    });
    (abdmService.getSessionStatus as any).mockResolvedValue({
      success: true,
      data: [],
    });
  });

  it('renders ABDM panel with MOCK Mode badge and isolated mock warning banner', async () => {
    render(
      <AbdmStatusPanel
        sessionId={mockSessionId}
        patientId={mockPatientId}
        isDoctorVerified={false}
      />
    );

    expect(screen.getByText(/ABDM \/ ABHA Interoperability/i)).toBeInTheDocument();
    await waitFor(() => {
      expect(screen.getByText(/MOCK MODE/i)).toBeInTheDocument();
      expect(screen.getByText(/Deterministic Mock Adapter Active/i)).toBeInTheDocument();
      expect(screen.getByText(/No live government ABDM sandbox\/production connection is established/i)).toBeInTheDocument();
    });
  });

  it('shows unverified status initially and verifies ABHA with masked view on verify click', async () => {
    (abdmService.verifyAbha as any).mockResolvedValue({
      success: true,
      data: mockVerificationSuccess,
    });

    render(
      <AbdmStatusPanel
        sessionId={mockSessionId}
        patientId={mockPatientId}
        isDoctorVerified={false}
      />
    );

    const input = screen.getByPlaceholderText(/e\.g\. 12-3456-7890-1234/i);
    fireEvent.change(input, { target: { value: '12-3456-7890-1234' } });

    const verifyButton = screen.getByRole('button', { name: /^Verify$/i });
    fireEvent.click(verifyButton);

    await waitFor(() => {
      expect(abdmService.verifyAbha).toHaveBeenCalledWith(
        mockPatientId,
        { abhaId: '12-3456-7890-1234' },
        undefined
      );
      expect(screen.getByText(/XX-XXXX-XXXX-1234/i)).toBeInTheDocument();
    });
  });

  it('grants patient consent when Grant Consent is clicked', async () => {
    (abdmService.grantConsent as any).mockResolvedValue({
      success: true,
      data: mockConsentSuccess,
    });

    render(
      <AbdmStatusPanel
        sessionId={mockSessionId}
        patientId={mockPatientId}
        isDoctorVerified={false}
      />
    );

    const grantButton = screen.getByRole('button', { name: /Grant Consent/i });
    fireEvent.click(grantButton);

    await waitFor(() => {
      expect(abdmService.grantConsent).toHaveBeenCalledWith(
        mockSessionId,
        { patientId: mockPatientId },
        undefined
      );
      expect(screen.getByText(/^GRANTED$/)).toBeInTheDocument();
    });
  });

  it('disables Exchange button when clinical summary is not doctor verified', async () => {
    render(
      <AbdmStatusPanel
        sessionId={mockSessionId}
        patientId={mockPatientId}
        isDoctorVerified={false}
      />
    );

    const exchangeButton = screen.getByRole('button', { name: /Exchange with ABDM/i });
    expect(exchangeButton).toBeDisabled();
    expect(screen.getByText(/Doctor Unverified/i)).toBeInTheDocument();
  });

  it('enables Exchange button and performs exchange when doctor verified and consent is active', async () => {
    (abdmService.grantConsent as any).mockResolvedValue({
      success: true,
      data: mockConsentSuccess,
    });
    (abdmService.exchangeClinicalData as any).mockResolvedValue({
      success: true,
      data: mockExchangeSuccess,
    });

    render(
      <AbdmStatusPanel
        sessionId={mockSessionId}
        patientId={mockPatientId}
        isDoctorVerified={true}
      />
    );

    expect(screen.getByText(/Doctor Verified/i)).toBeInTheDocument();

    // Grant consent first
    const grantButton = screen.getByRole('button', { name: /Grant Consent/i });
    fireEvent.click(grantButton);

    await waitFor(() => {
      expect(screen.getByText(/^GRANTED$/)).toBeInTheDocument();
    });

    // Exchange button should now be enabled
    const exchangeButton = screen.getByRole('button', { name: /Exchange with ABDM/i });
    expect(exchangeButton).not.toBeDisabled();

    fireEvent.click(exchangeButton);

    await waitFor(() => {
      expect(abdmService.exchangeClinicalData).toHaveBeenCalledWith(
        mockSessionId,
        { notes: 'Clinical care exchange' },
        undefined
      );
      expect(screen.getByText(/Transaction ID: MOCK-TXN-999/i)).toBeInTheDocument();
    });
  });

  it('displays past transaction records in history list', async () => {
    (abdmService.getSessionStatus as any).mockResolvedValue({
      success: true,
      data: [
        {
          id: 'rec-1',
          patientId: mockPatientId,
          sessionId: mockSessionId,
          integrationType: 'ABDM',
          operation: 'ABDM_CLINICAL_EXCHANGE',
          externalReference: 'MOCK-TXN-PREV',
          idempotencyKey: 'KEY-001',
          status: 'SUCCESS',
          errorMessage: null,
          attemptCount: 1,
          attemptedAt: '2026-09-12T00:10:00Z',
          completedAt: '2026-09-12T00:10:01Z',
        },
      ],
    });

    render(
      <AbdmStatusPanel
        sessionId={mockSessionId}
        patientId={mockPatientId}
        isDoctorVerified={true}
      />
    );

    await waitFor(() => {
      expect(screen.getByText(/Transaction History/i)).toBeInTheDocument();
      expect(screen.getByText(/1 Record\(s\)/i)).toBeInTheDocument();
      expect(screen.getByText(/Ref: MOCK-TXN-PREV/i)).toBeInTheDocument();
    });
  });
});
