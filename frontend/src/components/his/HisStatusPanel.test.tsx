import { describe, it, expect, vi, beforeEach } from 'vitest';
import { render, screen, fireEvent, waitFor } from '@testing-library/react';
import { HisStatusPanel } from './HisStatusPanel';
import { hisService } from '../../services/hisService';
import { HisStatus, HisExportResponse, HisPatientSearchResponse, HisLinkPatientResponse } from '../../types/his';

vi.mock('../../services/hisService', () => ({
  hisService: {
    getStatus: vi.fn(),
    getSessionStatus: vi.fn(),
    exportClinicalData: vi.fn(),
    searchPatients: vi.fn(),
    linkPatient: vi.fn(),
  },
}));

describe('HisStatusPanel Component', () => {
  const mockSessionId = 'session-1234';
  const mockPatientId = 'patient-5678';

  const mockHisStatus: HisStatus = {
    mode: 'MOCK',
    isMock: true,
    enabled: true,
    baseUrl: 'mock://localhost/his-api',
    connected: false,
    message: 'HIS Mock Mode active. External live connection: NOT CONNECTED.',
    timestamp: '2026-09-12T00:00:00Z',
  };

  const mockExportSuccess: HisExportResponse = {
    integrationRecordId: 'his-rec-001',
    sessionId: mockSessionId,
    patientId: mockPatientId,
    status: 'SUCCESS',
    encounterReference: 'HIS-ENC-998877',
    bundleId: 'Bundle-session-1234',
    hisId: 'HIS-MRN-90210',
    intakeQueue: 'OPD_INTAKE',
    idempotencyKey: 'HIS-KEY-1',
    isMock: true,
    attemptedAt: '2026-09-12T00:00:00Z',
    completedAt: '2026-09-12T00:00:01Z',
    errorMessage: null,
    message: 'Verified clinical summary transmitted to HIS intake queue successfully',
  };

  const mockSearchResponse: HisPatientSearchResponse = {
    candidates: [
      {
        hisId: 'HIS-MRN-90210',
        displayName: 'Aarav Sharma',
        phone: '9876543210',
        gender: 'MALE',
        dateOfBirth: '1985-04-12',
        department: 'Cardiology',
        roomOrWard: 'Room 302',
      },
    ],
    totalFound: 1,
    query: 'Aarav',
    searchType: 'ALL',
    isMock: true,
    message: 'Matched 1 hospital record(s)',
  };

  const mockLinkResponse: HisLinkPatientResponse = {
    patientId: mockPatientId,
    patientIdentifier: 'PAT-001',
    hisId: 'HIS-MRN-90210',
    linkedAt: '2026-09-12T00:00:00Z',
    message: 'Patient successfully associated with hospital HIS ID: HIS-MRN-90210',
  };

  beforeEach(() => {
    vi.clearAllMocks();
    (hisService.getStatus as any).mockResolvedValue({
      success: true,
      data: mockHisStatus,
    });
    (hisService.getSessionStatus as any).mockResolvedValue({
      success: true,
      data: [],
    });
  });

  it('renders HIS panel with MOCK Mode badge and header', async () => {
    render(
      <HisStatusPanel
        sessionId={mockSessionId}
        patientId={mockPatientId}
        isDoctorVerified={false}
      />
    );

    expect(screen.getByText(/Hospital Information System \(HIS\)/i)).toBeInTheDocument();
    await waitFor(() => {
      expect(screen.getByTestId('his-mock-badge')).toHaveTextContent(/MOCK MODE \(NOT CONNECTED\)/i);
    });
  });

  it('enforces doctor verification gate: disables export when unverified', async () => {
    render(
      <HisStatusPanel
        sessionId={mockSessionId}
        patientId={mockPatientId}
        isDoctorVerified={false}
      />
    );

    await waitFor(() => {
      const gate = screen.getByTestId('his-gate-status');
      expect(gate).toHaveTextContent(/UNVERIFIED \/ PENDING/i);
      const exportBtn = screen.getByTestId('his-export-button');
      expect(exportBtn).toBeDisabled();
    });
  });

  it('enforces doctor verification gate: enables export and executes transmission when verified', async () => {
    (hisService.exportClinicalData as any).mockResolvedValue({
      success: true,
      data: mockExportSuccess,
    });

    render(
      <HisStatusPanel
        sessionId={mockSessionId}
        patientId={mockPatientId}
        isDoctorVerified={true}
      />
    );

    await waitFor(() => {
      const gate = screen.getByTestId('his-gate-status');
      expect(gate).toHaveTextContent(/VERIFIED/i);
      const exportBtn = screen.getByTestId('his-export-button');
      expect(exportBtn).not.toBeDisabled();
    });

    const exportBtn = screen.getByTestId('his-export-button');
    fireEvent.click(exportBtn);

    await waitFor(() => {
      expect(hisService.exportClinicalData).toHaveBeenCalledWith(
        mockSessionId,
        expect.objectContaining({
          intakeQueue: 'OPD_INTAKE',
        }),
        undefined
      );
      expect(screen.getByText(/Successfully exported to HIS intake queue/i)).toBeInTheDocument();
      expect(screen.getByText(/HIS-ENC-998877/i)).toBeInTheDocument();
    });
  });

  it('opens hospital MPI search modal, searches patients, and links hospital ID', async () => {
    (hisService.searchPatients as any).mockResolvedValue({
      success: true,
      data: mockSearchResponse,
    });
    (hisService.linkPatient as any).mockResolvedValue({
      success: true,
      data: mockLinkResponse,
    });

    render(
      <HisStatusPanel
        sessionId={mockSessionId}
        patientId={mockPatientId}
        isDoctorVerified={true}
      />
    );

    const searchBtn = screen.getByRole('button', { name: /Search Hospital MPI/i });
    fireEvent.click(searchBtn);

    expect(screen.getByTestId('mpi-search-modal')).toBeInTheDocument();

    const searchInput = screen.getByPlaceholderText(/Search by Name, Phone, or MRN/i);
    fireEvent.change(searchInput, { target: { value: 'Aarav' } });

    const modalSearchBtn = screen.getByTestId('mpi-search-submit-btn');
    fireEvent.click(modalSearchBtn);

    await waitFor(() => {
      expect(hisService.searchPatients).toHaveBeenCalledWith('Aarav', 'ALL', undefined);
      expect(screen.getByText('Aarav Sharma')).toBeInTheDocument();
      expect(screen.getByText('HIS-MRN-90210')).toBeInTheDocument();
    });

    const linkBtn = screen.getByTestId('link-candidate-HIS-MRN-90210');
    fireEvent.click(linkBtn);

    await waitFor(() => {
      expect(hisService.linkPatient).toHaveBeenCalledWith(
        mockPatientId,
        expect.objectContaining({ hisId: 'HIS-MRN-90210' }),
        undefined
      );
      expect(screen.getByText(/Patient associated with hospital MRN: HIS-MRN-90210/i)).toBeInTheDocument();
    });
  });

  it('displays transaction history when session records are returned', async () => {
    (hisService.getSessionStatus as any).mockResolvedValue({
      success: true,
      data: [
        {
          id: 'his-rec-1',
          patientId: mockPatientId,
          sessionId: mockSessionId,
          integrationType: 'HIS',
          operation: 'HIS_CLINICAL_EXPORT',
          externalReference: 'HIS-ENC-1002',
          status: 'SUCCESS',
          errorMessage: null,
          attemptCount: 1,
          attemptedAt: '2026-09-12T10:00:00Z',
          completedAt: '2026-09-12T10:00:01Z',
        },
      ],
    });

    render(
      <HisStatusPanel
        sessionId={mockSessionId}
        patientId={mockPatientId}
        isDoctorVerified={true}
      />
    );

    await waitFor(() => {
      expect(screen.getByText('HIS_CLINICAL_EXPORT')).toBeInTheDocument();
      expect(screen.getByText('HIS-ENC-1002')).toBeInTheDocument();
    });
  });
});
