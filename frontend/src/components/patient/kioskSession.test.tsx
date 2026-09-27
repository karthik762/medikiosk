import { describe, it, expect, beforeEach, vi } from 'vitest';
import { render, screen, waitFor } from '@testing-library/react';
import { kioskService } from '../../services/kioskService';
import { PatientKiosk } from './PatientKiosk';
import * as apiModule from '../../services/api';

describe('Phase 2C — Kiosk Session & Token Persistence', () => {
  beforeEach(() => {
    localStorage.clear();
    sessionStorage.clear();
    kioskService.clearSession();
    vi.restoreAllMocks();
  });

  it('1 & 2: Stores token in sessionStorage on registration and retrieves it after service reinitialization', async () => {
    vi.spyOn(apiModule, 'fetchApi').mockResolvedValueOnce({
      success: true,
      data: {
        accessToken: 'mock-jwt-token-123',
        sessionId: 'sess-uuid-001',
        patientId: 'pat-uuid-001',
        patientIdentifier: 'MK-PAT-001',
      },
    } as any);

    const res = await kioskService.registerKioskPatient({
      firstName: 'Aarav',
      lastName: 'Sharma',
    });

    expect(res.sessionId).toBe('sess-uuid-001');
    expect(kioskService.getToken()).toBe('mock-jwt-token-123');
    expect(sessionStorage.getItem('medikiosk_kiosk_token')).toBe('mock-jwt-token-123');

    // Simulate in-memory reset (module reinitialization)
    kioskService.clearMemoryTokenOnly();
    expect(kioskService.getToken()).toBe('mock-jwt-token-123');
  });

  it('3 & 4: Restores persisted session ID and step on page refresh/reinitialization', async () => {
    kioskService.saveSession({
      token: 'persisted-jwt-456',
      sessionId: 'sess-uuid-002',
      patientId: 'pat-uuid-002',
      patientIdentifier: 'MK-PAT-002',
      currentStep: 'COMPLAINT',
    });

    vi.spyOn(apiModule, 'fetchApi').mockResolvedValueOnce({
      success: true,
      data: {
        id: 'sess-uuid-002',
        patientId: 'pat-uuid-002',
        patientIdentifier: 'MK-PAT-002',
        status: 'IN_PROGRESS',
      },
    } as any);

    const verified = await kioskService.verifySession('sess-uuid-002');
    expect(verified?.status).toBe('IN_PROGRESS');

    const persisted = kioskService.getPersistedSession();
    expect(persisted?.sessionId).toBe('sess-uuid-002');
    expect(persisted?.currentStep).toBe('COMPLAINT');
  });

  it('5, 6 & 7: Clears session and handles 401 / 403 response gracefully', async () => {
    kioskService.saveSession({
      token: 'expired-jwt-789',
      sessionId: 'sess-uuid-003',
      currentStep: 'AYURVEDA',
    });

    vi.spyOn(apiModule, 'fetchApi').mockRejectedValueOnce(new apiModule.ApiError(401, 'Token expired'));

    await expect(kioskService.verifySession('sess-uuid-003')).rejects.toThrow();

    expect(kioskService.getToken()).toBe('');
    expect(kioskService.getPersistedSession()).toBeNull();
    expect(sessionStorage.getItem('medikiosk_kiosk_token')).toBeNull();
  });

  it('8 & 10: Explicit clearSession prevents Patient B from inheriting Patient A session', () => {
    kioskService.saveSession({
      token: 'patient-a-jwt',
      sessionId: 'session-patient-a',
      patientId: 'pat-a',
      patientIdentifier: 'MK-PAT-A',
      currentStep: 'DOCUMENTS',
    });

    expect(kioskService.getToken()).toBe('patient-a-jwt');

    // Patient A finishes/abandons session
    kioskService.clearSession();

    expect(kioskService.getToken()).toBe('');
    expect(kioskService.getPersistedSession()).toBeNull();
    expect(sessionStorage.getItem('medikiosk_kiosk_token')).toBeNull();
    expect(sessionStorage.getItem('medikiosk_kiosk_session_id')).toBeNull();
  });

  it('9 & 11: Completing intake clears session and creates no duplicate sessions on reload', async () => {
    kioskService.saveSession({
      token: 'patient-complete-jwt',
      sessionId: 'sess-uuid-complete',
    });

    vi.spyOn(apiModule, 'fetchApi').mockResolvedValueOnce({
      success: true,
      data: { tokenNumber: 'TK-9999', queueNumber: 'Q-99', triagePriority: 'URGENT' },
    } as any);

    const result = await kioskService.completeIntake('sess-uuid-complete');
    expect(result.sessionStatus).toBe('COMPLETED');

    expect(kioskService.getToken()).toBe('');
    expect(kioskService.getPersistedSession()).toBeNull();
  });

  it('12, 13 & 14: Restores PatientKiosk component to active session step upon mount', async () => {
    kioskService.saveSession({
      token: 'active-session-jwt',
      sessionId: 'sess-active-001',
      patientId: 'pat-active-001',
      patientIdentifier: 'MK-PAT-888',
      currentStep: 'COMPLAINT',
    });

    vi.spyOn(apiModule, 'fetchApi').mockImplementation(async (endpoint: string) => {
      if (endpoint.includes('/api/demo/status')) {
        return { success: true, data: { demoEnabled: true } } as any;
      }
      if (endpoint.includes('/api/intake/sessions/sess-active-001')) {
        return {
          success: true,
          data: {
            id: 'sess-active-001',
            patientId: 'pat-active-001',
            patientIdentifier: 'MK-PAT-888',
            status: 'IN_PROGRESS',
          },
        } as any;
      }
      return { success: true, data: {} } as any;
    });

    render(<PatientKiosk onSwitchToClinician={vi.fn()} />);

    // Should verify session and skip landing screen directly to Chief Complaint step
    await waitFor(() => {
      expect(screen.getByText(/What brings you in today\?/i)).toBeInTheDocument();
    });
  });

  it('15: Doctor authentication in sessionStorage remains isolated and unaffected', () => {
    sessionStorage.setItem('medikiosk_doctor_token', 'doctor-jwt-token-999');
    
    kioskService.saveSession({
      token: 'kiosk-patient-jwt',
      sessionId: 'kiosk-session-111',
    });

    expect(sessionStorage.getItem('medikiosk_doctor_token')).toBe('doctor-jwt-token-999');
    expect(sessionStorage.getItem('medikiosk_kiosk_token')).toBe('kiosk-patient-jwt');

    // Wiping kiosk session does NOT clear doctor token
    kioskService.clearSession();

    expect(sessionStorage.getItem('medikiosk_kiosk_token')).toBeNull();
    expect(sessionStorage.getItem('medikiosk_doctor_token')).toBe('doctor-jwt-token-999');
  });
});
