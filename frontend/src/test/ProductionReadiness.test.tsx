import { describe, it, expect, vi, beforeEach } from 'vitest';
import { render, screen, fireEvent, waitFor } from '@testing-library/react';
import { App } from '../App';

// Mock global fetch for health & demo status
const mockFetch = vi.fn();
globalThis.fetch = mockFetch as any;

describe('Stage 7: Final Production Readiness & Full System Verification', () => {

  beforeEach(() => {
    vi.clearAllMocks();
    sessionStorage.clear();

    mockFetch.mockImplementation((url: string) => {
      if (url.includes('/api/demo/status')) {
        return Promise.resolve({
          ok: true,
          json: () => Promise.resolve({ success: true, data: { demoEnabled: true } }),
        });
      }
      if (url.includes('/api/health')) {
        return Promise.resolve({
          ok: true,
          json: () => Promise.resolve({ status: 'UP', service: 'MediKiosk Backend', version: '1.0.0' }),
        });
      }
      return Promise.resolve({
        ok: true,
        json: () => Promise.resolve({ success: true, data: {} }),
      });
    });
  });

  it('renders application shell with default Patient Kiosk view and active navigation bar', async () => {
    render(<App />);

    expect(screen.getByText(/MediKiosk:/i)).toBeInTheDocument();
    expect(screen.getByText(/Patient Kiosk/i)).toBeInTheDocument();
    expect(screen.getByText(/Clinician Review Console/i)).toBeInTheDocument();
    expect(screen.getByText(/Admin Workstation/i)).toBeInTheDocument();
    expect(screen.getByText(/RESET DEMO/i)).toBeInTheDocument();
  });

  it('allows switching between all 4 system portals (Patient, Clinician, Admin, Dev Sandbox)', async () => {
    render(<App />);

    // Switch to Dev Sandbox
    fireEvent.click(screen.getByText(/Dev Sandbox/i));
    await waitFor(() => {
      expect(screen.getByText(/Integration Architecture Sandbox/i)).toBeInTheDocument();
    });

    // Switch to Clinician Console
    fireEvent.click(screen.getByText(/Clinician Review Console/i));
    await waitFor(() => {
      expect(screen.getByText(/Clinician Authentication Gate/i)).toBeInTheDocument();
    });

    // Switch to Admin Workstation
    fireEvent.click(screen.getByText(/Admin Workstation/i));
    await waitFor(() => {
      expect(screen.getByText(/Clinician Authentication Gate|Hospital Administration Workstation/i)).toBeInTheDocument();
    });
  });
});
