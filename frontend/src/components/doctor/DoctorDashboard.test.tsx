import { describe, it, expect, vi, beforeEach } from 'vitest';
import { render, screen, fireEvent, waitFor } from '@testing-library/react';
import { DoctorDashboard } from './DoctorDashboard';
import { doctorService } from '../../services/doctorService';
import { DoctorQueueItem } from '../../types/doctor';

vi.mock('../../services/doctorService', () => ({
  doctorService: {
    getQueue: vi.fn(),
    getCaseReview: vi.fn(),
    amendSummary: vi.fn(),
    verifySummary: vi.fn(),
  },
}));

describe('DoctorDashboard Component', () => {
  const mockQueue: DoctorQueueItem[] = [
    {
      sessionId: 'sess-001',
      patientId: 'pat-001',
      patientIdentifier: 'PAT-001',
      patientName: 'Jane Doe',
      patientAge: 45,
      patientGender: 'Female',
      chiefComplaint: 'Severe chest tightness',
      sessionStatus: 'COMPLETED',
      triagePriority: 'URGENT',
      triageReason: 'Red-flag chest pain',
      summaryStatus: 'AI_GENERATED',
      reviewStatus: 'READY_FOR_REVIEW',
      sessionCreatedAt: '2026-09-11T10:00:00Z',
      urgent: true,
    },
    {
      sessionId: 'sess-002',
      patientId: 'pat-002',
      patientIdentifier: 'PAT-002',
      patientName: 'Bob Smith',
      patientAge: 32,
      patientGender: 'Male',
      chiefComplaint: 'Mild headache and sore throat',
      sessionStatus: 'COMPLETED',
      triagePriority: 'STABLE',
      triageReason: 'Routine viral illness',
      summaryStatus: 'VERIFIED',
      reviewStatus: 'VERIFIED',
      sessionCreatedAt: '2026-09-11T11:00:00Z',
      urgent: false,
    },
  ];

  beforeEach(() => {
    vi.clearAllMocks();
  });

  it('renders consultation queue workstation with metrics and patient list', async () => {
    vi.mocked(doctorService.getQueue).mockResolvedValue({
      success: true,
      data: mockQueue,
      message: 'Queue retrieved',
    });

    const mockSelect = vi.fn();
    render(<DoctorDashboard authToken="test-token" onSelectCase={mockSelect} />);

    expect(screen.getByText('Clinician Intake Workstation')).toBeInTheDocument();

    await waitFor(() => {
      expect(screen.getByText('Jane Doe')).toBeInTheDocument();
      expect(screen.getByText('Bob Smith')).toBeInTheDocument();
    });

    expect(screen.getByText('PAT-001')).toBeInTheDocument();
    expect(screen.getByText('Severe chest tightness')).toBeInTheDocument();
  });

  it('filters queue by search query', async () => {
    vi.mocked(doctorService.getQueue).mockResolvedValue({
      success: true,
      data: mockQueue,
      message: 'Queue retrieved',
    });

    render(<DoctorDashboard authToken="test-token" onSelectCase={vi.fn()} />);

    await waitFor(() => {
      expect(screen.getByText('Jane Doe')).toBeInTheDocument();
    });

    const searchInput = screen.getByPlaceholderText(/Search by patient name/i);
    fireEvent.change(searchInput, { target: { value: 'Jane' } });

    expect(screen.getByText('Jane Doe')).toBeInTheDocument();
    expect(screen.queryByText('Bob Smith')).not.toBeInTheDocument();
  });

  it('calls onSelectCase when clicking "Review Case" button', async () => {
    vi.mocked(doctorService.getQueue).mockResolvedValue({
      success: true,
      data: mockQueue,
      message: 'Queue retrieved',
    });

    const mockSelect = vi.fn();
    render(<DoctorDashboard authToken="test-token" onSelectCase={mockSelect} />);

    await waitFor(() => {
      expect(screen.getByText('Jane Doe')).toBeInTheDocument();
    });

    const reviewButtons = screen.getAllByRole('button', { name: /Review Case/i });
    fireEvent.click(reviewButtons[0]);

    expect(mockSelect).toHaveBeenCalledWith('sess-001');
  });

  it('displays error banner when queue retrieval fails', async () => {
    vi.mocked(doctorService.getQueue).mockRejectedValue(new Error('Network error'));

    render(<DoctorDashboard authToken="test-token" onSelectCase={vi.fn()} />);

    await waitFor(() => {
      expect(screen.getByText('Network error')).toBeInTheDocument();
    });
  });
});
