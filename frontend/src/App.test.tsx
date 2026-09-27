import { render, screen, waitFor, fireEvent } from '@testing-library/react';
import { describe, it, expect, vi, beforeEach } from 'vitest';
import App from './App';

describe('MediKiosk Foundation Application Shell', () => {
  beforeEach(() => {
    // Mock global fetch to return UP health status
    globalThis.fetch = vi.fn().mockImplementation(() =>
      Promise.resolve({
        ok: true,
        json: () =>
          Promise.resolve({
            status: 'UP',
            service: 'MediKiosk Backend',
            timestamp: new Date().toISOString(),
            version: '1.0.0',
          }),
      }) as any
    );
  });

  it('renders patient kiosk landing by default and navigation bar', async () => {
    render(<App />);
    expect(screen.getByRole('button', { name: /Patient Kiosk/i })).toBeInTheDocument();
    expect(screen.getByText(/Start Check-In/i)).toBeInTheDocument();
    expect(screen.getByText(/Clinical Intake Kiosk Active/i)).toBeInTheDocument();
  });

  it('navigates to developer foundation sandbox when clicked', async () => {
    render(<App />);
    const devBtn = screen.getByText(/Dev Sandbox/i);
    fireEvent.click(devBtn);
    expect(screen.getByText(/System Architecture & Module Boundaries/i)).toBeInTheDocument();
    expect(screen.getByText(/Phase 1 Foundation/i)).toBeInTheDocument();

    await waitFor(() => {
      expect(screen.getAllByText('Connected').length).toBeGreaterThan(0);
    });
  });

  it('successfully logs in via 1-Click Demo Doctor Login when demo mode is enabled', async () => {
    globalThis.fetch = vi.fn().mockImplementation((url: string) => {
      if (url.includes('/api/demo/status')) {
        return Promise.resolve({
          ok: true,
          json: () => Promise.resolve({ success: true, data: { demoEnabled: true } }),
        });
      }
      if (url.includes('/api/demo/doctor-login')) {
        return Promise.resolve({
          ok: true,
          json: () =>
            Promise.resolve({
              success: true,
              data: {
                accessToken: 'demo-test-jwt-token',
                email: 'doctor@medikiosk.org',
                firstName: 'Sarah',
                lastName: 'Jenkins',
                role: 'DOCTOR',
              },
            }),
        });
      }
      if (url.includes('/api/doctor/queue')) {
        return Promise.resolve({
          ok: true,
          json: () => Promise.resolve({ success: true, data: [] }),
        });
      }
      return Promise.resolve({
        ok: true,
        json: () =>
          Promise.resolve({
            status: 'UP',
            service: 'MediKiosk Backend',
            timestamp: new Date().toISOString(),
            version: '1.0.0',
          }),
      });
    }) as any;

    render(<App />);

    // Switch to Clinician Portal
    const clinicianTab = screen.getByText(/Clinician Review Console/i);
    fireEvent.click(clinicianTab);

    // Verify Clinician Authentication Gate is displayed
    expect(screen.getByText(/Clinician Authentication Gate/i)).toBeInTheDocument();

    // Wait for demo mode detection and click 1-Click Demo Login
    await waitFor(() => {
      expect(screen.getByText(/1-Click Demo Doctor Login \(Demo Mode Active\)/i)).toBeInTheDocument();
    });

    const demoLoginBtn = screen.getByText(/1-Click Demo Doctor Login \(Demo Mode Active\)/i);
    fireEvent.click(demoLoginBtn);

    // Verify clinician dashboard state is rendered and token stored
    await waitFor(() => {
      expect(screen.getByText(/Dr\. Sarah Jenkins \(DOCTOR\)/i)).toBeInTheDocument();
    });

    expect(sessionStorage.getItem('medikiosk_doctor_token')).toBe('demo-test-jwt-token');
  });
});
