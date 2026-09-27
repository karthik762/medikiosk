import { describe, it, expect, vi, beforeEach } from 'vitest';
import { render, screen, fireEvent, waitFor } from '@testing-library/react';
import { AyurvedaSection } from './AyurvedaSection';
import { ayurvedaService } from '../../services/ayurvedaService';

vi.mock('../../services/ayurvedaService', () => ({
  ayurvedaService: {
    getAyurvedaHistory: vi.fn(),
    saveAyurvedaHistory: vi.fn(),
    confirmAyurvedaHistory: vi.fn(),
  },
}));

describe('AyurvedaSection Component', () => {
  const mockSessionId = '11111111-1111-1111-1111-111111111100';

  beforeEach(() => {
    vi.clearAllMocks();
    vi.mocked(ayurvedaService.getAyurvedaHistory).mockResolvedValue({
      success: true,
      data: {
        id: '22222222-2222-2222-2222-222222222200',
        patientId: '33333333-3333-3333-3333-333333333300',
        sessionId: mockSessionId,
        status: 'DRAFT',
        patientConfirmed: false,
        confirmedAt: null,
        prakriti: { bodyFrame: 'MEDIUM', weatherPreference: 'DISLIKES_COLD' },
        nidana: { dietaryHabits: 'BALANCED', activityPattern: 'MODERATE' },
        pariksha: { koshtha: 'MADHYA', agni: 'SAMA' },
        notes: 'Initial notes',
        createdAt: '2026-09-11T10:00:00Z',
        updatedAt: '2026-09-11T10:00:00Z',
      },
    });

    vi.mocked(ayurvedaService.saveAyurvedaHistory).mockResolvedValue({
      success: true,
      message: 'Saved',
      data: {
        id: '22222222-2222-2222-2222-222222222200',
        patientId: '33333333-3333-3333-3333-333333333300',
        sessionId: mockSessionId,
        status: 'DRAFT',
        patientConfirmed: false,
        createdAt: '2026-09-11T10:00:00Z',
        updatedAt: '2026-09-11T10:05:00Z',
      },
    });

    vi.mocked(ayurvedaService.confirmAyurvedaHistory).mockResolvedValue({
      success: true,
      message: 'Confirmed',
      data: {
        id: '22222222-2222-2222-2222-222222222200',
        patientId: '33333333-3333-3333-3333-333333333300',
        sessionId: mockSessionId,
        status: 'PATIENT_CONFIRMED',
        patientConfirmed: true,
        confirmedAt: '2026-09-11T10:10:00Z',
        createdAt: '2026-09-11T10:00:00Z',
        updatedAt: '2026-09-11T10:10:00Z',
      },
    });
  });

  it('renders header, disclaimer banner, and initial Prakriti step', async () => {
    render(<AyurvedaSection sessionId={mockSessionId} />);

    expect(screen.getByText(/Ayurvedic Clinical Intake Assessment/i)).toBeInTheDocument();
    expect(screen.getByText(/MediKiosk is NOT an autonomous diagnostic system/i)).toBeInTheDocument();
    expect(screen.getByText(/The doctor remains the final clinical decision-maker/i)).toBeInTheDocument();

    await waitFor(() => {
      expect(screen.getByText('DRAFT')).toBeInTheDocument();
      expect(screen.getByLabelText(/Body Frame \/ Physical Build/i)).toBeInTheDocument();
    });

    expect(screen.getByText('1. Prakriti (Constitution)')).toBeInTheDocument();
  });

  it('navigates across tabs: Prakriti -> Nidana -> Pariksha -> Review', async () => {
    render(<AyurvedaSection sessionId={mockSessionId} />);

    // Click Next: Nidana
    const nextNidanaBtn = await screen.findByText(/Next: Nidana \(Lifestyle\) →/i);
    fireEvent.click(nextNidanaBtn);

    expect(screen.getByText('Etiological & Lifestyle Factors (Nidana)')).toBeInTheDocument();
    expect(screen.getByLabelText(/Dietary Habits & Preferences/i)).toBeInTheDocument();

    // Click Next: Pariksha
    const nextParikshaBtn = screen.getByText(/Next: Pariksha \(Clinical\) →/i);
    fireEvent.click(nextParikshaBtn);

    expect(screen.getByText('Clinical Examination Indicators (Pariksha)')).toBeInTheDocument();
    expect(screen.getByLabelText(/Bowel Motility Pattern \(Koshtha\)/i)).toBeInTheDocument();

    // Click Next: Review & Confirm
    const nextReviewBtn = screen.getByText(/Next: Review & Confirm →/i);
    fireEvent.click(nextReviewBtn);

    expect(screen.getByText('Review & Confirm Ayurvedic Assessment')).toBeInTheDocument();
    expect(screen.getByRole('button', { name: /Save Draft/i })).toBeInTheDocument();
    expect(screen.getByRole('button', { name: /Confirm Assessment for Doctor/i })).toBeInTheDocument();
  });

  it('allows changing inputs and saving draft', async () => {
    render(<AyurvedaSection sessionId={mockSessionId} />);

    // Update Prakriti selection
    const bodyFrameSelect = await screen.findByLabelText(/Body Frame \/ Physical Build/i);
    fireEvent.change(bodyFrameSelect, { target: { value: 'SLENDER' } });
    expect(bodyFrameSelect).toHaveValue('SLENDER');

    // Go to Review tab
    fireEvent.click(screen.getByText('4. Review & Confirm'));

    // Click Save Draft
    const saveDraftBtn = screen.getByRole('button', { name: /Save Draft/i });
    fireEvent.click(saveDraftBtn);

    await waitFor(() => {
      expect(ayurvedaService.saveAyurvedaHistory).toHaveBeenCalledWith(
        mockSessionId,
        expect.objectContaining({
          prakriti: expect.objectContaining({ bodyFrame: 'SLENDER' }),
        }),
        undefined
      );
    });

    expect(await screen.findByText(/Ayurveda assessment draft saved successfully/i)).toBeInTheDocument();
  });

  it('allows patient to confirm assessment and updates status to PATIENT_CONFIRMED', async () => {
    const onConfirmedMock = vi.fn();
    render(<AyurvedaSection sessionId={mockSessionId} onConfirmed={onConfirmedMock} />);

    // Go to Review tab
    fireEvent.click(await screen.findByText('4. Review & Confirm'));

    // Click Confirm
    const confirmBtn = screen.getByRole('button', { name: /Confirm Assessment for Doctor/i });
    fireEvent.click(confirmBtn);

    await waitFor(() => {
      expect(ayurvedaService.confirmAyurvedaHistory).toHaveBeenCalledWith(mockSessionId, undefined);
      expect(onConfirmedMock).toHaveBeenCalledTimes(1);
    });

    expect(
      await screen.findByText(/Your Ayurvedic assessment has been confirmed and is ready for doctor review/i)
    ).toBeInTheDocument();
    expect(screen.getByText('PATIENT_CONFIRMED')).toBeInTheDocument();
    expect(screen.getByText(/✓ Confirmed by Patient/i)).toBeInTheDocument();
  });

  it('triggers onBack and onSkip callbacks when navigation buttons are clicked', async () => {
    const onBackMock = vi.fn();
    const onSkipMock = vi.fn();

    render(
      <AyurvedaSection
        sessionId={mockSessionId}
        onBack={onBackMock}
        onSkip={onSkipMock}
      />
    );

    const backBtn = screen.getAllByRole('button', { name: /Back/i })[0];
    fireEvent.click(backBtn);
    expect(onBackMock).toHaveBeenCalledTimes(1);

    const skipBtn = screen.getAllByRole('button', { name: /Skip/i })[0];
    fireEvent.click(skipBtn);
    expect(onSkipMock).toHaveBeenCalledTimes(1);
  });

  it('handles backend confirmation failure gracefully and keeps user on step', async () => {
    vi.mocked(ayurvedaService.confirmAyurvedaHistory).mockRejectedValue(new Error('Database error saving confirmation'));

    const onConfirmedMock = vi.fn();
    render(<AyurvedaSection sessionId={mockSessionId} onConfirmed={onConfirmedMock} />);

    fireEvent.click(await screen.findByText('4. Review & Confirm'));

    const confirmBtn = screen.getByRole('button', { name: /Confirm Assessment for Doctor/i });
    fireEvent.click(confirmBtn);

    await waitFor(() => {
      expect(screen.getByText(/Database error saving confirmation/i)).toBeInTheDocument();
      expect(onConfirmedMock).not.toHaveBeenCalled();
    });
  });
});
