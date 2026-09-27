import { describe, it, expect, vi, beforeEach } from 'vitest';
import { render, screen, fireEvent, waitFor } from '@testing-library/react';
import { HealthLakeStatusPanel } from './HealthLakeStatusPanel';
import { healthlakeService } from '../../services/healthlakeService';
import { HealthLakeStatus, HealthLakeExportResponse } from '../../types/healthlake';

vi.mock('../../services/healthlakeService', () => ({
  healthlakeService: {
    getStatus: vi.fn(),
    getSessionStatus: vi.fn(),
    exportClinicalData: vi.fn(),
  },
}));

describe('HealthLakeStatusPanel Component', () => {
  const mockSessionId = 'session-1234';
  const mockPatientId = 'patient-5678';

  const mockHealthLakeStatus: HealthLakeStatus = {
    mode: 'MOCK',
    isMock: true,
    enabled: true,
    endpoint: 'mock://healthlake.ap-south-1.amazonaws.com/datastore/mock-ds/r4/',
    datastoreId: 'mock-healthlake-datastore',
    region: 'ap-south-1',
    connected: false,
    message: 'AWS HealthLake Mock Mode active. External live connection: NOT CONNECTED.',
    timestamp: '2026-09-12T00:00:00Z',
  };

  const mockExportSuccess: HealthLakeExportResponse = {
    integrationRecordId: 'hl-rec-001',
    sessionId: mockSessionId,
    patientId: mockPatientId,
    status: 'SUCCESS',
    healthlakeRef: 'HL-PAT-001',
    bundleId: 'Bundle-session-1234',
    datastoreId: 'mock-healthlake-datastore',
    idempotencyKey: 'HL-KEY-1',
    isMock: true,
    attemptedAt: '2026-09-12T00:00:00Z',
    completedAt: '2026-09-12T00:00:01Z',
    errorMessage: null,
    message: 'Verified clinical summary and FHIR bundle exported to AWS HealthLake successfully',
  };

  beforeEach(() => {
    vi.clearAllMocks();
    (healthlakeService.getStatus as any).mockResolvedValue({
      success: true,
      data: mockHealthLakeStatus,
    });
    (healthlakeService.getSessionStatus as any).mockResolvedValue({
      success: true,
      data: [],
    });
  });

  it('renders HealthLake panel with MOCK Mode badge and header', async () => {
    render(
      <HealthLakeStatusPanel
        sessionId={mockSessionId}
        patientId={mockPatientId}
        isDoctorVerified={false}
      />
    );

    expect(screen.getByText(/AWS HealthLake Integration/i)).toBeInTheDocument();
    await waitFor(() => {
      expect(screen.getByTestId('healthlake-mock-badge')).toHaveTextContent(/MOCK MODE \(NOT CONNECTED\)/i);
      expect(screen.getByText(/AWS HealthLake connection: NOT CONNECTED \(MOCK ONLY\)/i)).toBeInTheDocument();
      expect(screen.getByTestId('healthlake-adapter-state')).toHaveTextContent(/Mock Enabled \(Not Connected\)/i);
      expect(screen.getByTestId('healthlake-endpoint-state')).toHaveTextContent(/Not Connected \(Mock\)/i);
      expect(screen.queryByText(/Active \(Ready\)/i)).not.toBeInTheDocument();
      expect(screen.queryByText(/FHIR R4 Endpoint Active/i)).not.toBeInTheDocument();
    });
  });

  it('enforces doctor verification gate: disables export when unverified', async () => {
    render(
      <HealthLakeStatusPanel
        sessionId={mockSessionId}
        patientId={mockPatientId}
        isDoctorVerified={false}
      />
    );

    await waitFor(() => {
      const gate = screen.getByTestId('healthlake-gate-status');
      expect(gate).toHaveTextContent(/UNVERIFIED \/ PENDING/i);
      const exportBtn = screen.getByTestId('healthlake-export-button');
      expect(exportBtn).toBeDisabled();
    });
  });

  it('enforces doctor verification gate: enables export and executes transmission when verified', async () => {
    (healthlakeService.exportClinicalData as any).mockResolvedValue({
      success: true,
      data: mockExportSuccess,
    });

    render(
      <HealthLakeStatusPanel
        sessionId={mockSessionId}
        patientId={mockPatientId}
        isDoctorVerified={true}
      />
    );

    await waitFor(() => {
      const gate = screen.getByTestId('healthlake-gate-status');
      expect(gate).toHaveTextContent(/PASSED/i);
      const exportBtn = screen.getByTestId('healthlake-export-button');
      expect(exportBtn).not.toBeDisabled();
    });

    const exportBtn = screen.getByTestId('healthlake-export-button');
    fireEvent.click(exportBtn);

    await waitFor(() => {
      expect(healthlakeService.exportClinicalData).toHaveBeenCalledWith(
        mockSessionId,
        expect.objectContaining({
          datastoreId: undefined,
          notes: undefined,
        }),
        undefined
      );
      expect(screen.getByText(/Successfully exported to AWS HealthLake/i)).toBeInTheDocument();
    });
  });

  it('displays error feedback when export fails', async () => {
    (healthlakeService.exportClinicalData as any).mockRejectedValue(
      new Error('Simulated AWS HealthLake timeout after 5000ms')
    );

    render(
      <HealthLakeStatusPanel
        sessionId={mockSessionId}
        patientId={mockPatientId}
        isDoctorVerified={true}
      />
    );

    await waitFor(() => {
      const exportBtn = screen.getByTestId('healthlake-export-button');
      expect(exportBtn).not.toBeDisabled();
      fireEvent.click(exportBtn);
    });

    await waitFor(() => {
      expect(screen.getByText(/Simulated AWS HealthLake timeout after 5000ms/i)).toBeInTheDocument();
    });
  });

  it('displays transaction history from getSessionStatus', async () => {
    (healthlakeService.getSessionStatus as any).mockResolvedValue({
      success: true,
      data: [
        {
          id: 'rec-1',
          integrationType: 'HEALTHLAKE',
          operation: 'HEALTHLAKE_CLINICAL_EXPORT',
          status: 'SUCCESS',
          externalReference: 'HL-BUNDLE-12345',
          attemptCount: 1,
          attemptedAt: '2026-09-12T01:00:00Z',
          completedAt: '2026-09-12T01:00:01Z',
          errorMessage: null,
        },
      ],
    });

    render(
      <HealthLakeStatusPanel
        sessionId={mockSessionId}
        patientId={mockPatientId}
        isDoctorVerified={true}
      />
    );

    await waitFor(() => {
      expect(screen.getByText('HL-BUNDLE-12345')).toBeInTheDocument();
      expect(screen.getByText('HEALTHLAKE_CLINICAL_EXPORT')).toBeInTheDocument();
    });
  });
});
