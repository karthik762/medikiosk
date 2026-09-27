import { describe, it, expect, vi, beforeEach } from 'vitest';
import { render, screen, fireEvent, waitFor } from '@testing-library/react';
import { FhirExportViewer } from './FhirExportViewer';
import { fhirService } from '../../services/fhirService';
import { FhirExportSummary, FhirValidationResponse } from '../../types/fhir';

vi.mock('../../services/fhirService', () => ({
  fhirService: {
    getExportSummary: vi.fn(),
    exportClinicalBundle: vi.fn(),
    validateSession: vi.fn(),
    downloadJson: vi.fn(),
  },
}));

describe('FhirExportViewer Component', () => {
  const mockSessionId = '11111111-2222-3333-4444-555555555555';

  const mockExportSummaryUnverified: FhirExportSummary = {
    sessionId: mockSessionId,
    patientId: 'patient-abc',
    bundleId: 'bundle-xyz',
    bundleType: 'document',
    totalResources: 6,
    resourceTypeCounts: {
      Composition: 1,
      Patient: 1,
      Encounter: 1,
      Observation: 2,
      Condition: 1,
    },
    verificationStatus: 'UNVERIFIED',
    compositionStatus: 'preliminary',
    validationStatus: 'VALID',
    exportedAt: '2026-09-12T00:00:00Z',
    fhirBundleJson: '{\n  "resourceType": "Bundle",\n  "type": "document"\n}',
  };

  const mockExportSummaryVerified: FhirExportSummary = {
    ...mockExportSummaryUnverified,
    verificationStatus: 'VERIFIED',
    compositionStatus: 'final',
  };

  const mockValidationResponse: FhirValidationResponse = {
    valid: true,
    errors: [],
    warnings: [],
    totalChecked: 6,
  };

  beforeEach(() => {
    vi.clearAllMocks();
    (fhirService.getExportSummary as any).mockResolvedValue({
      success: true,
      data: mockExportSummaryUnverified,
    });
    (fhirService.validateSession as any).mockResolvedValue({
      success: true,
      data: mockValidationResponse,
    });
  });

  it('renders modal with unverified warning when status is UNVERIFIED', async () => {
    render(
      <FhirExportViewer
        sessionId={mockSessionId}
        patientName="Ananya Sharma"
        isOpen={true}
        onClose={vi.fn()}
      />
    );

    expect(screen.getByText(/FHIR R4 Clinical Document Export/i)).toBeInTheDocument();
    expect(screen.getByText(/Patient: Ananya Sharma/i)).toBeInTheDocument();

    await waitFor(() => {
      expect(screen.getByText(/UNVERIFIED PRE-CONSULT/i)).toBeInTheDocument();
      expect(screen.getByText(/Composition.status = "preliminary"/i)).toBeInTheDocument();
      expect(screen.getByText(/Valid FHIR R4 Document/i)).toBeInTheDocument();
    });

    expect(screen.getByText(/Interoperability Export Boundary/i)).toBeInTheDocument();
  });

  it('renders verified final badge when status is VERIFIED', async () => {
    (fhirService.getExportSummary as any).mockResolvedValue({
      success: true,
      data: mockExportSummaryVerified,
    });

    render(
      <FhirExportViewer
        sessionId={mockSessionId}
        patientName="Ananya Sharma"
        isOpen={true}
        onClose={vi.fn()}
      />
    );

    await waitFor(() => {
      expect(screen.getByText(/VERIFIED CLINICAL DOCUMENT/i)).toBeInTheDocument();
      expect(screen.getByText(/Composition.status = "final"/i)).toBeInTheDocument();
    });
  });

  it('switches tabs and displays resource breakdown and validation details', async () => {
    render(
      <FhirExportViewer
        sessionId={mockSessionId}
        patientName="Ananya Sharma"
        isOpen={true}
        onClose={vi.fn()}
      />
    );

    await waitFor(() => {
      expect(screen.getByText(/"resourceType": "Bundle"/i)).toBeInTheDocument();
    });

    // Switch to Resource Details tab
    fireEvent.click(screen.getByText(/Resource Details/i));
    expect(screen.getByText(/Bundle Specification/i)).toBeInTheDocument();
    expect(screen.getByText(/Clinical Resource Entries/i)).toBeInTheDocument();

    // Switch to Validation tab
    fireEvent.click(screen.getByText(/Validation & Invariants/i));
    expect(screen.getByText(/Profile Invariant Validation/i)).toBeInTheDocument();
    expect(screen.getByText(/All 6 resources conform to MediKiosk FHIR R4 profile specifications/i)).toBeInTheDocument();
  });

  it('handles export action and triggers download', async () => {
    (fhirService.exportClinicalBundle as any).mockResolvedValue({
      success: true,
      data: mockExportSummaryVerified,
    });

    render(
      <FhirExportViewer
        sessionId={mockSessionId}
        patientName="Ananya Sharma"
        isOpen={true}
        onClose={vi.fn()}
      />
    );

    await waitFor(() => {
      expect(screen.getByText(/Export & Persist Mappings/i)).toBeInTheDocument();
    });

    // Click Export & Persist
    fireEvent.click(screen.getByText(/Export & Persist Mappings/i));
    await waitFor(() => {
      expect(fhirService.exportClinicalBundle).toHaveBeenCalledWith(mockSessionId, undefined);
    });

    // Click Download
    fireEvent.click(screen.getByText(/Download .json/i));
    expect(fhirService.downloadJson).toHaveBeenCalled();
  });

  it('does not render when isOpen is false', () => {
    const { container } = render(
      <FhirExportViewer
        sessionId={mockSessionId}
        patientName="Ananya Sharma"
        isOpen={false}
        onClose={vi.fn()}
      />
    );

    expect(container).toBeEmptyDOMElement();
  });
});
