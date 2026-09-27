import { describe, it, expect, vi, beforeEach } from 'vitest';
import { render, screen, fireEvent, waitFor } from '@testing-library/react';
import { AdminWorkstation } from './AdminWorkstation';
import { adminService } from '../../services/adminService';

vi.mock('../../services/adminService', () => ({
  adminService: {
    getAnalytics: vi.fn(),
    getUsers: vi.fn(),
    createUser: vi.fn(),
    updateUserStatus: vi.fn(),
    getAuditLogs: vi.fn(),
    getConfigSummary: vi.fn(),
  },
}));

describe('AdminWorkstation Component', () => {
  const mockAnalytics = {
    totalIntakes: 12,
    completedIntakes: 10,
    inProgressIntakes: 2,
    totalPatients: 8,
    totalDoctors: 4,
    avgIntakeDurationSeconds: 145.5,
    triageDistribution: { EMERGENCY: 1, URGENT: 3, PRIORITY: 4, ROUTINE: 4 },
    totalSummariesGenerated: 12,
    totalSummariesVerified: 9,
    totalDocumentsProcessed: 5,
    fhirBundlesGenerated: 9,
    abdmExchangesCount: 7,
    hisExportsCount: 6,
    healthLakeSyncsCount: 6,
  };

  const mockUsers = [
    {
      id: 'usr-1',
      email: 'doctor1@hospital.org',
      firstName: 'Arun',
      lastName: 'Sharma',
      role: 'DOCTOR',
      status: 'ACTIVE',
      createdAt: '2026-09-01T00:00:00Z',
      updatedAt: '2026-09-01T00:00:00Z',
    },
    {
      id: 'usr-2',
      email: 'admin1@hospital.org',
      firstName: 'Super',
      lastName: 'Admin',
      role: 'ADMIN',
      status: 'ACTIVE',
      createdAt: '2026-09-01T00:00:00Z',
      updatedAt: '2026-09-01T00:00:00Z',
    },
  ];

  const mockAuditLogs = [
    {
      id: 'log-1',
      userId: 'usr-1',
      userEmail: 'doctor1@hospital.org',
      action: 'DOCTOR_SUMMARY_VERIFIED',
      entityType: 'ClinicalSummary',
      entityId: 'sum-1',
      metadata: '{"sessionId":"sess-1","action":"VERIFIED"}',
      createdAt: '2026-09-15T10:00:00Z',
    },
  ];

  const mockConfig = {
    environment: 'PRODUCTION_READY',
    aiProviderMode: 'MOCK_DETERMINISTIC_FALLBACK',
    externalAiEnabled: false,
    abdmOperatingMode: 'MOCK',
    abdmMock: true,
    hisOperatingMode: 'MOCK',
    hisMock: true,
    healthLakeOperatingMode: 'MOCK',
    healthLakeMock: true,
    databaseStatus: 'CONNECTED_HEALTHY',
    featureToggles: { fhirR4Mapper: true, abdmAdapter: true },
  };

  beforeEach(() => {
    vi.clearAllMocks();
    (adminService.getAnalytics as any).mockResolvedValue({ success: true, data: mockAnalytics });
    (adminService.getUsers as any).mockResolvedValue({ success: true, data: mockUsers });
    (adminService.getAuditLogs as any).mockResolvedValue({ success: true, data: mockAuditLogs });
    (adminService.getConfigSummary as any).mockResolvedValue({ success: true, data: mockConfig });
  });

  it('renders Admin Workstation header and default System Analytics tab', async () => {
    render(<AdminWorkstation authToken="test-token" />);

    expect(screen.getByText(/Hospital Administration Workstation/i)).toBeInTheDocument();

    await waitFor(() => {
      expect(screen.getByText(/Total Intakes/i)).toBeInTheDocument();
      expect(screen.getAllByText('12').length).toBeGreaterThan(0);
      expect(screen.getByText('145.5s')).toBeInTheDocument();
      expect(screen.getByText(/Triage Priority Distribution/i)).toBeInTheDocument();
    });
  });

  it('switches to Clinicians & Staff tab and displays user accounts', async () => {
    render(<AdminWorkstation authToken="test-token" />);

    fireEvent.click(screen.getByText(/Clinicians & Staff/i));

    await waitFor(() => {
      expect(screen.getByText('doctor1@hospital.org')).toBeInTheDocument();
      expect(screen.getByText('admin1@hospital.org')).toBeInTheDocument();
    });
  });

  it('opens Add New Clinician modal and creates new doctor user', async () => {
    (adminService.createUser as any).mockResolvedValue({
      success: true,
      data: {
        id: 'usr-3',
        email: 'newdoc@hospital.org',
        firstName: 'Anita',
        lastName: 'Roy',
        role: 'DOCTOR',
        status: 'ACTIVE',
        createdAt: '2026-09-22T00:00:00Z',
        updatedAt: '2026-09-22T00:00:00Z',
      },
    });

    render(<AdminWorkstation authToken="test-token" />);

    fireEvent.click(screen.getByText(/Clinicians & Staff/i));

    await waitFor(() => {
      expect(screen.getByText(/Add New Clinician \/ Staff/i)).toBeInTheDocument();
    });

    fireEvent.click(screen.getByText(/Add New Clinician \/ Staff/i));
    expect(screen.getByText(/Register New Clinician or Administrator/i)).toBeInTheDocument();

    fireEvent.change(screen.getByPlaceholderText('doctor@hospital.org'), { target: { value: 'newdoc@hospital.org' } });
    fireEvent.change(screen.getByPlaceholderText('Minimum 8 characters'), { target: { value: 'Pass12345!' } });
    fireEvent.change(screen.getByPlaceholderText('Arun'), { target: { value: 'Anita' } });
    fireEvent.change(screen.getByPlaceholderText('Sharma'), { target: { value: 'Roy' } });

    fireEvent.click(screen.getByText('Create Account'));

    await waitFor(() => {
      expect(adminService.createUser).toHaveBeenCalledWith(
        {
          email: 'newdoc@hospital.org',
          password: 'Pass12345!',
          firstName: 'Anita',
          lastName: 'Roy',
          role: 'DOCTOR',
        },
        'test-token'
      );
    });
  });

  it('switches to Security Audit Log tab and inspects metadata', async () => {
    render(<AdminWorkstation authToken="test-token" />);

    fireEvent.click(screen.getByText('Security Audit Log'));

    const actionCell = await screen.findByText('DOCTOR_SUMMARY_VERIFIED');
    expect(actionCell).toBeInTheDocument();
    expect(screen.getByText(/Inspect JSON/i)).toBeInTheDocument();

    fireEvent.click(screen.getByText(/Inspect JSON/i));
    expect(screen.getByText(/Security Audit Log Metadata/i)).toBeInTheDocument();
  });

  it('switches to System Config & Status tab and renders environment info', async () => {
    render(<AdminWorkstation authToken="test-token" />);

    fireEvent.click(screen.getByText('System Config & Status'));

    const envBadge = await screen.findByText('PRODUCTION_READY');
    expect(envBadge).toBeInTheDocument();
    expect(screen.getByText(/MOCK_DETERMINISTIC_FALLBACK/i)).toBeInTheDocument();
    expect(screen.getByText('fhirR4Mapper')).toBeInTheDocument();
  });
});
