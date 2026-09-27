import { render, screen, fireEvent, waitFor } from '@testing-library/react';
import { describe, it, expect, vi, beforeEach } from 'vitest';
import { PatientKiosk } from './PatientKiosk';
import { PatientLandingView } from './PatientLandingView';
import { PatientRegistrationView } from './PatientRegistrationView';
import { PatientConsentView } from './PatientConsentView';
import { ChiefComplaintVoiceView } from './ChiefComplaintVoiceView';
import { PatientCompletionView } from './PatientCompletionView';
import { kioskService } from '../../services/kioskService';

describe('Complete Patient Kiosk Internationalization & Language Switching', () => {
  const mockOnSwitchToClinician = vi.fn();

  beforeEach(() => {
    vi.clearAllMocks();
    localStorage.clear();

    vi.spyOn(kioskService, 'checkDemoStatus').mockResolvedValue(true);
    vi.spyOn(kioskService, 'registerKioskPatient').mockResolvedValue({
      sessionId: 'sess-i18n-123',
      patientId: 'pat-123',
      patientIdentifier: 'PAT-HI-001',
      sessionStatus: 'IN_PROGRESS',
      sessionStartTime: '2026-09-22T10:00:00Z',
      token: 'jwt-token-i18n',
    });
  });

  it('renders English by default and switches immediately to Hindi without page refresh', async () => {
    render(<PatientKiosk onSwitchToClinician={mockOnSwitchToClinician} />);

    // Default English strings
    expect(screen.getByText('AI-Powered Clinical Intake')).toBeInTheDocument();
    expect(screen.getByText('Start Check-In')).toBeInTheDocument();
    expect(screen.getByText('Voice or Touch')).toBeInTheDocument();

    // Click Hindi selector
    const hindiBtn = screen.getByText('हिंदी');
    fireEvent.click(hindiBtn);

    // Immediate reactive update to Hindi
    await waitFor(() => {
      expect(screen.getByText('AI-संचालित क्लिनिकल जानकारी संग्रह')).toBeInTheDocument();
      expect(screen.getByText('चेक-इन शुरू करें')).toBeInTheDocument();
      expect(screen.getByText('आवाज़ या टच')).toBeInTheDocument();
      expect(screen.getByText('दस्तावेज़ स्कैन करें')).toBeInTheDocument();
      expect(screen.getByText('डॉक्टर द्वारा सत्यापित')).toBeInTheDocument();
    });

    // Check localStorage persistence
    expect(localStorage.getItem('medikiosk_language')).toBe('hi');
  });

  it('restores Hindi selected language from localStorage on initial mount', () => {
    localStorage.setItem('medikiosk_language', 'hi');

    render(<PatientKiosk onSwitchToClinician={mockOnSwitchToClinician} />);

    expect(screen.getByText('AI-संचालित क्लिनिकल जानकारी संग्रह')).toBeInTheDocument();
    expect(screen.getByText('चेक-इन शुरू करें')).toBeInTheDocument();
  });

  it('switches seamlessly between English, Hindi, and Telugu', async () => {
    render(<PatientKiosk onSwitchToClinician={mockOnSwitchToClinician} />);

    // Switch to Telugu
    const teluguBtn = screen.getByText('తెలుగు');
    fireEvent.click(teluguBtn);

    await waitFor(() => {
      expect(screen.getByText('AI-ఆధారిత క్లినికల్ సమాచార సేకరణ')).toBeInTheDocument();
      expect(screen.getByText('పేషెంట్ రిజిస్ట్రేషన్ ప్రారంభించండి')).toBeInTheDocument();
    });
    expect(localStorage.getItem('medikiosk_language')).toBe('te');

    // Switch back to English
    const englishBtn = screen.getByText('English');
    fireEvent.click(englishBtn);

    await waitFor(() => {
      expect(screen.getByText('AI-Powered Clinical Intake')).toBeInTheDocument();
      expect(screen.getByText('Start Check-In')).toBeInTheDocument();
    });
    expect(localStorage.getItem('medikiosk_language')).toBe('en');
  });

  it('renders Patient Landing screen completely in Hindi without hardcoded English UI strings', () => {
    render(
      <PatientLandingView
        onStartCheckIn={vi.fn()}
        onContinueSession={vi.fn()}
        onDemoPatientLogin={vi.fn()}
        isDemoEnabled={true}
        language="hi"
      />
    );

    expect(screen.getByText('क्लिनिकल इंटेक कियोस्क सक्रिय')).toBeInTheDocument();
    expect(screen.getByText('AI-संचालित क्लिनिकल जानकारी संग्रह')).toBeInTheDocument();
    expect(screen.getByText('चेक-इन शुरू करें')).toBeInTheDocument();
    expect(screen.getByText('मौजूदा सत्र जारी रखें')).toBeInTheDocument();
    expect(screen.getByText('1-क्लिक डेमो मरीज (आरव शर्मा, उम्र 28)')).toBeInTheDocument();
    expect(screen.getByText('आवाज़ या टच')).toBeInTheDocument();
    expect(screen.getByText('दस्तावेज़ स्कैन करें')).toBeInTheDocument();
    expect(screen.getByText('डॉक्टर द्वारा सत्यापित')).toBeInTheDocument();

    // Regression check: Hardcoded English text should NOT be present
    expect(screen.queryByText('AI-Powered Clinical Intake')).not.toBeInTheDocument();
    expect(screen.queryByText('Start Check-In')).not.toBeInTheDocument();
    expect(screen.queryByText('Voice or Touch')).not.toBeInTheDocument();
  });

  it('renders Patient Registration screen in Hindi', () => {
    render(
      <PatientRegistrationView
        onSubmit={vi.fn()}
        onCancel={vi.fn()}
        language="hi"
      />
    );

    expect(screen.getByText('मरीज पंजीकरण')).toBeInTheDocument();
    expect(screen.getByText('पहला नाम *')).toBeInTheDocument();
    expect(screen.getByText('अंतिम नाम *')).toBeInTheDocument();
    expect(screen.getByText('पुरुष')).toBeInTheDocument();
    expect(screen.getByText('महिला')).toBeInTheDocument();
    expect(screen.getByText('अन्य')).toBeInTheDocument();
    expect(screen.getByText('पुष्टि करें और आगे बढ़ें →')).toBeInTheDocument();
  });

  it('renders Patient Consent screen in Hindi', () => {
    render(
      <PatientConsentView
        onAccept={vi.fn()}
        onDecline={vi.fn()}
        language="hi"
      />
    );

    expect(screen.getByText('मरीज सहमति')).toBeInTheDocument();
    expect(screen.getByText('क्लिनिकल इंटेक का उद्देश्य')).toBeInTheDocument();
    expect(screen.getByText('डॉक्टर सत्यापन और अंतिम निर्णय')).toBeInTheDocument();
    expect(screen.getByText('समीक्षा और संशोधन')).toBeInTheDocument();
    expect(screen.getByText('स्वीकार करें और आगे बढ़ें')).toBeInTheDocument();
  });

  it('renders Chief Complaint screen in Hindi', () => {
    render(
      <ChiefComplaintVoiceView
        onSubmit={vi.fn()}
        onBack={vi.fn()}
        language="hi"
      />
    );

    expect(screen.getByText('आज आप किस समस्या के लिए आए हैं?')).toBeInTheDocument();
    expect(screen.getByText('बोलने के लिए माइक पर टैप करें')).toBeInTheDocument();
    expect(screen.getByText('मुख्य स्वास्थ्य समस्या / शिकायत *')).toBeInTheDocument();
    expect(screen.getByText('या सामान्य समस्याओं पर टैप करें:')).toBeInTheDocument();
    expect(screen.getByText('+ छाती में दर्द या भारीपन')).toBeInTheDocument();
  });

  it('renders Patient Completion screen in Hindi', () => {
    render(
      <PatientCompletionView
        patientIdentifier="TK-9999"
        patientName="आरव शर्मा"
        onReset={vi.fn()}
        onSwitchToClinician={vi.fn()}
        language="hi"
      />
    );

    expect(screen.getByText('इंटेक सफलतापूर्वक पूरा हुआ')).toBeInTheDocument();
    expect(screen.getByText('आपका परामर्श टोकन नंबर')).toBeInTheDocument();
    expect(screen.getByText('TK-9999')).toBeInTheDocument();
    expect(screen.getByText('समाप्त करें और होम पर लौटें')).toBeInTheDocument();
    expect(screen.getByText('डॉक्टर कतार खोलें')).toBeInTheDocument();
  });
});
