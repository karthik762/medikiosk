import { render, screen, fireEvent, waitFor } from '@testing-library/react';
import { describe, it, expect, vi, beforeEach } from 'vitest';
import { PatientKiosk } from './PatientKiosk';
import { kioskService } from '../../services/kioskService';

describe('PatientKiosk Component Workflow', () => {
  const mockOnSwitchToClinician = vi.fn();

  beforeEach(() => {
    vi.clearAllMocks();
    localStorage.clear();
    sessionStorage.clear();

    vi.spyOn(kioskService, 'checkDemoStatus').mockResolvedValue(true);
    vi.spyOn(kioskService, 'registerKioskPatient').mockResolvedValue({
      sessionId: 'sess-12345',
      patientId: 'pat-12345',
      patientIdentifier: 'PAT-REG-001',
      sessionStatus: 'IN_PROGRESS',
      sessionStartTime: '2026-09-12T08:00:00Z',
      token: 'patient-jwt-token',
    });
    vi.spyOn(kioskService, 'recordConsent').mockResolvedValue({
      id: 'consent-1',
      consentType: 'INFORMATIVE_AND_CARE',
      granted: true,
    } as any);
    vi.spyOn(kioskService, 'submitChiefComplaint').mockResolvedValue({
      sessionId: 'sess-12345',
      chiefComplaint: 'Chest pain and tightness',
    } as any);
    vi.spyOn(kioskService, 'getNextAdaptiveQuestion').mockResolvedValue({
      hasMoreQuestions: true,
      pendingCriticalCount: 1,
      totalAsked: 0,
      question: {
        questionId: 'q-adaptive-1',
        questionCode: 'Q_CHEST_PAIN_SEVERITY',
        questionText: 'On a scale from 0 to 10, how severe is your chest pain?',
        questionType: 'SCALE_0_10',
        category: 'CHEST_PAIN',
        completed: false,
        priority: 100,
        safetyCritical: true,
        options: [],
      },
    });
    vi.spyOn(kioskService, 'submitAnswer').mockResolvedValue({
      sessionId: 'sess-12345',
      questionId: 'q-adaptive-1',
      answerText: '7',
    } as any);
    vi.spyOn(kioskService, 'completeIntake').mockResolvedValue({
      sessionId: 'sess-12345',
      sessionStatus: 'COMPLETED',
      triagePriority: 'URGENT',
      tokenNumber: 'TK-4821',
      queueNumber: 'A-14',
    });
  });

  it('renders landing screen with greeting and Start Check-In button', () => {
    render(<PatientKiosk onSwitchToClinician={mockOnSwitchToClinician} />);

    expect(screen.getByText(/Clinical Intake Kiosk Active/i)).toBeInTheDocument();
    expect(screen.getByText(/AI-Powered Clinical Intake/i)).toBeInTheDocument();
    expect(screen.getByText(/Start Check-In/i)).toBeInTheDocument();
    expect(screen.getByText(/1-Click Demo Patient/i)).toBeInTheDocument();
  });

  it('allows changing language and font size', () => {
    render(<PatientKiosk onSwitchToClinician={mockOnSwitchToClinician} />);

    const hindiBtn = screen.getByText('हिंदी');
    fireEvent.click(hindiBtn);
    expect(hindiBtn).toHaveClass('font-bold');

    const fontLargeBtn = screen.getByTitle('Extra large text size');
    fireEvent.click(fontLargeBtn);
    expect(fontLargeBtn).toHaveClass('font-bold');
  });

  it('triggers onSwitchToClinician when Staff Login button is clicked', () => {
    render(<PatientKiosk onSwitchToClinician={mockOnSwitchToClinician} />);

    const staffLoginButtons = screen.getAllByText(/Staff \/ Doctor Portal/i);
    fireEvent.click(staffLoginButtons[0]);
    expect(mockOnSwitchToClinician).toHaveBeenCalled();
  });

  it('navigates through Registration to Consent', async () => {
    render(<PatientKiosk onSwitchToClinician={mockOnSwitchToClinician} />);

    fireEvent.click(screen.getByText(/Start Check-In/i));

    expect(screen.getByText(/Patient Registration|Patient Check-In/i)).toBeInTheDocument();

    fireEvent.change(screen.getByPlaceholderText(/Ramesh/i), { target: { value: 'Anita' } });
    fireEvent.change(screen.getByPlaceholderText(/Rao/i), { target: { value: 'Deshmukh' } });

    const continueBtn = screen.getByRole('button', { name: /Continue to Consent|Confirm & Continue/i });
    fireEvent.click(continueBtn);

    await waitFor(() => {
      expect(kioskService.registerKioskPatient).toHaveBeenCalledWith(
        expect.objectContaining({
          firstName: 'Anita',
          lastName: 'Deshmukh',
        })
      );
      expect(screen.getByText(/Patient Consent|Consent for Digital Intake/i)).toBeInTheDocument();
    });
  });

  it('navigates through Consent to Chief Complaint and presents Adaptive Question', async () => {
    render(<PatientKiosk onSwitchToClinician={mockOnSwitchToClinician} />);

    // Start Check-in
    fireEvent.click(screen.getByText(/Start Check-In/i));
    fireEvent.change(screen.getByPlaceholderText(/Ramesh/i), { target: { value: 'Vikram' } });
    fireEvent.change(screen.getByPlaceholderText(/Rao/i), { target: { value: 'Patel' } });
    fireEvent.click(screen.getByRole('button', { name: /Continue to Consent|Confirm & Continue/i }));

    // Accept Consent
    await waitFor(() => {
      expect(screen.getByText(/Patient Consent|Consent for Digital Intake/i)).toBeInTheDocument();
    });
    fireEvent.click(screen.getByRole('button', { name: /Accept & Proceed|I Agree & Continue/i }));

    // Chief Complaint screen
    await waitFor(() => {
      expect(screen.getByText(/What brings you in today\?/i)).toBeInTheDocument();
    });

    // Enter complaint
    const complaintInput = screen.getByPlaceholderText(/severe headache/i);
    fireEvent.change(complaintInput, { target: { value: 'Severe chest tightness since 2 hours' } });
    fireEvent.click(screen.getByRole('button', { name: /Continue to Health Questions|Confirm & Continue/i }));

    // Adaptive Question should appear
    await waitFor(() => {
      expect(kioskService.getNextAdaptiveQuestion).toHaveBeenCalledWith('sess-12345');
      expect(screen.getByText(/On a scale from 0 to 10, how severe is your chest pain\?/i)).toBeInTheDocument();
    });

    // Select pain scale 7 and submit
    fireEvent.click(screen.getByRole('button', { name: '7' }));
    fireEvent.click(screen.getByRole('button', { name: /Confirm & Continue/i }));

    await waitFor(() => {
      expect(kioskService.submitAnswer).toHaveBeenCalledWith('sess-12345', 'q-adaptive-1', '7', 'TOUCH');
    });
  });
});
