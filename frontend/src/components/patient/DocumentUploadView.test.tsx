import { describe, it, expect, vi, beforeEach } from 'vitest';
import { render, screen, fireEvent, waitFor } from '@testing-library/react';
import { DocumentUploadView } from './DocumentUploadView';
import { documentService } from '../../services/documentService';

vi.mock('../../services/documentService', () => ({
  documentService: {
    getSessionDocuments: vi.fn(),
    uploadDocument: vi.fn(),
    getDocumentDetails: vi.fn(),
  },
}));

describe('DocumentUploadView Component', () => {
  const mockSessionId = '22222222-2222-2222-2222-222222222222';
  const mockToken = 'mock-jwt-token-123';
  const mockOnSubmit = vi.fn();
  const mockOnBack = vi.fn();

  beforeEach(() => {
    vi.clearAllMocks();
    vi.mocked(documentService.getSessionDocuments).mockResolvedValue({
      success: true,
      data: [],
    });
  });

  it('renders document title, type options, and action buttons', () => {
    render(
      <DocumentUploadView
        onSubmit={mockOnSubmit}
        onBack={mockOnBack}
        sessionId={mockSessionId}
        authToken={mockToken}
      />
    );

    expect(screen.getByText(/Medical Documents & Records/i)).toBeInTheDocument();
    expect(screen.getByText(/Select Document Type:/i)).toBeInTheDocument();
    expect(screen.getByRole('button', { name: /Prescription/i })).toBeInTheDocument();
    expect(screen.getByRole('button', { name: /Lab Report/i })).toBeInTheDocument();
    expect(screen.getByRole('button', { name: /Discharge Summary/i })).toBeInTheDocument();
  });

  it('loads existing session documents on mount', async () => {
    vi.mocked(documentService.getSessionDocuments).mockResolvedValue({
      success: true,
      data: [
        {
          id: 'doc-existing-1',
          patientId: 'pat-1',
          sessionId: mockSessionId,
          documentType: 'PRESCRIPTION',
          fileName: 'existing_rx.pdf',
          fileSize: 102400,
          processingStatus: 'COMPLETED',
          uploadedAt: '2026-09-15T10:00:00Z',
          extraction: {
            id: 'ext-1',
            documentId: 'doc-existing-1',
            extractedText: 'Existing prescription text',
            confidenceScore: 0.96,
            processingEngine: 'MOCK_OCR_PROVIDER',
            status: 'SUCCESS',
            createdAt: '2026-09-15T10:00:01Z',
          },
        },
      ],
    });

    render(
      <DocumentUploadView
        onSubmit={mockOnSubmit}
        onBack={mockOnBack}
        sessionId={mockSessionId}
        authToken={mockToken}
      />
    );

    await waitFor(() => {
      expect(documentService.getSessionDocuments).toHaveBeenCalledWith(mockSessionId, mockToken);
      expect(screen.getByText('existing_rx.pdf')).toBeInTheDocument();
      expect(screen.getByText('Existing prescription text')).toBeInTheDocument();
    });
  });

  it('triggers actual multipart upload when patient selects a valid file', async () => {
    vi.mocked(documentService.uploadDocument).mockResolvedValue({
      success: true,
      message: 'Uploaded',
      data: {
        id: 'doc-new-1',
        patientId: 'pat-1',
        sessionId: mockSessionId,
        documentType: 'LAB_REPORT',
        fileName: 'blood_report.pdf',
        fileSize: 204800,
        processingStatus: 'COMPLETED',
        uploadedAt: '2026-09-15T10:05:00Z',
        extraction: {
          id: 'ext-new-1',
          documentId: 'doc-new-1',
          extractedText: 'Blood Test Results: HbA1c 6.5%',
          confidenceScore: 0.94,
          processingEngine: 'MOCK_OCR_PROVIDER',
          status: 'SUCCESS',
          createdAt: '2026-09-15T10:05:01Z',
        },
      },
    });

    render(
      <DocumentUploadView
        onSubmit={mockOnSubmit}
        onBack={mockOnBack}
        sessionId={mockSessionId}
        authToken={mockToken}
      />
    );

    // Select LAB_REPORT category
    fireEvent.click(screen.getByRole('button', { name: /Lab Report/i }));

    // Select file
    const file = new File(['%PDF-1.4 mock content'], 'blood_report.pdf', { type: 'application/pdf' });
    const fileInput = document.querySelector('input[type="file"]') as HTMLInputElement;
    fireEvent.change(fileInput, { target: { files: [file] } });

    await waitFor(() => {
      expect(documentService.uploadDocument).toHaveBeenCalledWith(
        mockSessionId,
        expect.any(File),
        'LAB_REPORT',
        mockToken
      );
      expect(screen.getByText('blood_report.pdf')).toBeInTheDocument();
      expect(screen.getByText('Blood Test Results: HbA1c 6.5%')).toBeInTheDocument();
      expect(screen.queryByText(/Metformin 500mg/i)).not.toBeInTheDocument();
    });
  });

  it('rejects oversized file (>10MB) with patient-friendly error message', async () => {
    render(
      <DocumentUploadView
        onSubmit={mockOnSubmit}
        onBack={mockOnBack}
        sessionId={mockSessionId}
        authToken={mockToken}
      />
    );

    const oversizedBytes = new Uint8Array(10 * 1024 * 1024 + 100);
    const oversizedFile = new File([oversizedBytes], 'huge_file.pdf', { type: 'application/pdf' });

    const fileInput = document.querySelector('input[type="file"]') as HTMLInputElement;
    fireEvent.change(fileInput, { target: { files: [oversizedFile] } });

    await waitFor(() => {
      expect(screen.getByText(/File exceeds maximum allowed upload size \(10MB\)/i)).toBeInTheDocument();
      expect(documentService.uploadDocument).not.toHaveBeenCalled();
    });
  });

  it('rejects unsupported file extension/MIME type', async () => {
    render(
      <DocumentUploadView
        onSubmit={mockOnSubmit}
        onBack={mockOnBack}
        sessionId={mockSessionId}
        authToken={mockToken}
      />
    );

    const invalidFile = new File(['hello world'], 'script.exe', { type: 'application/x-msdownload' });

    const fileInput = document.querySelector('input[type="file"]') as HTMLInputElement;
    fireEvent.change(fileInput, { target: { files: [invalidFile] } });

    await waitFor(() => {
      expect(screen.getByText(/Unsupported document format/i)).toBeInTheDocument();
      expect(documentService.uploadDocument).not.toHaveBeenCalled();
    });
  });

  it('handles backend upload failure gracefully without breaking UI', async () => {
    vi.mocked(documentService.uploadDocument).mockRejectedValue(new Error('Network error: Service unavailable'));

    render(
      <DocumentUploadView
        onSubmit={mockOnSubmit}
        onBack={mockOnBack}
        sessionId={mockSessionId}
        authToken={mockToken}
      />
    );

    const file = new File(['%PDF-1.4 test'], 'rx.pdf', { type: 'application/pdf' });
    const fileInput = document.querySelector('input[type="file"]') as HTMLInputElement;
    fireEvent.change(fileInput, { target: { files: [file] } });

    await waitFor(() => {
      expect(screen.getByText(/Network error: Service unavailable/i)).toBeInTheDocument();
    });
  });

  it('displays low confidence OCR status when backend returns REVIEW_REQUIRED', async () => {
    vi.mocked(documentService.uploadDocument).mockResolvedValue({
      success: true,
      message: 'Processed',
      data: {
        id: 'doc-low-1',
        patientId: 'pat-1',
        sessionId: mockSessionId,
        documentType: 'PRESCRIPTION',
        fileName: 'blurry_rx.png',
        fileSize: 51200,
        processingStatus: 'REVIEW_REQUIRED',
        uploadedAt: '2026-09-15T10:10:00Z',
        extraction: {
          id: 'ext-low-1',
          documentId: 'doc-low-1',
          extractedText: 'Uncertain reading...',
          confidenceScore: 0.52,
          processingEngine: 'MOCK_OCR_PROVIDER',
          status: 'LOW_CONFIDENCE',
          createdAt: '2026-09-15T10:10:01Z',
        },
      },
    });

    render(
      <DocumentUploadView
        onSubmit={mockOnSubmit}
        onBack={mockOnBack}
        sessionId={mockSessionId}
        authToken={mockToken}
      />
    );

    const file = new File(['fake png'], 'blurry_rx.png', { type: 'image/png' });
    const fileInput = document.querySelector('input[type="file"]') as HTMLInputElement;
    fireEvent.change(fileInput, { target: { files: [file] } });

    await waitFor(() => {
      expect(screen.getByText('blurry_rx.png')).toBeInTheDocument();
      expect(screen.getByText(/Low confidence reading/i)).toBeInTheDocument();
    });
  });

  it('allows uploading multiple documents sequentially without losing previous uploads', async () => {
    vi.mocked(documentService.uploadDocument)
      .mockResolvedValueOnce({
        success: true,
        data: {
          id: 'doc-1',
          patientId: 'pat-1',
          sessionId: mockSessionId,
          documentType: 'PRESCRIPTION',
          fileName: 'rx_first.pdf',
          fileSize: 100000,
          processingStatus: 'COMPLETED',
          uploadedAt: '2026-09-15T10:00:00Z',
          extraction: {
            id: 'ext-1',
            documentId: 'doc-1',
            extractedText: 'First document text',
            confidenceScore: 0.95,
            processingEngine: 'MOCK_OCR_PROVIDER',
            status: 'SUCCESS',
            createdAt: '2026-09-15T10:00:01Z',
          },
        },
      })
      .mockResolvedValueOnce({
        success: true,
        data: {
          id: 'doc-2',
          patientId: 'pat-1',
          sessionId: mockSessionId,
          documentType: 'LAB_REPORT',
          fileName: 'lab_second.pdf',
          fileSize: 150000,
          processingStatus: 'COMPLETED',
          uploadedAt: '2026-09-15T10:02:00Z',
          extraction: {
            id: 'ext-2',
            documentId: 'doc-2',
            extractedText: 'Second document text',
            confidenceScore: 0.98,
            processingEngine: 'MOCK_OCR_PROVIDER',
            status: 'SUCCESS',
            createdAt: '2026-09-15T10:02:01Z',
          },
        },
      });

    render(
      <DocumentUploadView
        onSubmit={mockOnSubmit}
        onBack={mockOnBack}
        sessionId={mockSessionId}
        authToken={mockToken}
      />
    );

    const fileInput = document.querySelector('input[type="file"]') as HTMLInputElement;

    // Upload first file
    const file1 = new File(['pdf 1'], 'rx_first.pdf', { type: 'application/pdf' });
    fireEvent.change(fileInput, { target: { files: [file1] } });

    await waitFor(() => {
      expect(screen.getByText('rx_first.pdf')).toBeInTheDocument();
    });

    // Upload second file
    const file2 = new File(['pdf 2'], 'lab_second.pdf', { type: 'application/pdf' });
    fireEvent.change(fileInput, { target: { files: [file2] } });

    await waitFor(() => {
      expect(screen.getByText('rx_first.pdf')).toBeInTheDocument();
      expect(screen.getByText('lab_second.pdf')).toBeInTheDocument();
      expect(screen.getByText('Scanned Records (2)')).toBeInTheDocument();
    });
  });
});
