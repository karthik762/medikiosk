import { API_BASE_URL } from '../constants';
import { ApiResponse } from '../types/api';
import { ApiError } from './api';
import { DocumentType, DocumentUploadResponse } from '../types/document';

/**
 * Service for uploading and retrieving patient medical documents and OCR extractions.
 */
export const documentService = {
  /**
   * Uploads a document binary and triggers OCR extraction for an intake session.
   */
  async uploadDocument(
    sessionId: string,
    file: File,
    documentType: DocumentType = 'OTHER',
    token?: string
  ): Promise<ApiResponse<DocumentUploadResponse>> {
    const formData = new FormData();
    formData.append('file', file);
    formData.append('documentType', documentType);

    const headers: Record<string, string> = {};
    if (token) {
      headers['Authorization'] = `Bearer ${token}`;
    }

    const response = await fetch(`${API_BASE_URL}/api/intake/sessions/${sessionId}/documents`, {
      method: 'POST',
      headers,
      body: formData,
    });

    if (!response.ok) {
      const err = await response.json().catch(() => ({ message: 'Failed to upload document' }));
      throw new ApiError(response.status, err.message || `HTTP error ${response.status}`);
    }

    return response.json();
  },

  /**
   * Retrieves all documents associated with an intake session.
   */
  async getSessionDocuments(sessionId: string, token?: string): Promise<ApiResponse<DocumentUploadResponse[]>> {
    const headers: Record<string, string> = {
      'Content-Type': 'application/json',
    };
    if (token) {
      headers['Authorization'] = `Bearer ${token}`;
    }

    const response = await fetch(`${API_BASE_URL}/api/intake/sessions/${sessionId}/documents`, {
      method: 'GET',
      headers,
    });

    if (!response.ok) {
      const err = await response.json().catch(() => ({ message: 'Failed to fetch session documents' }));
      throw new ApiError(response.status, err.message || `HTTP error ${response.status}`);
    }

    return response.json();
  },

  /**
   * Retrieves document metadata and latest extraction result.
   */
  async getDocumentDetails(
    sessionId: string,
    documentId: string,
    token?: string
  ): Promise<ApiResponse<DocumentUploadResponse>> {
    const headers: Record<string, string> = {
      'Content-Type': 'application/json',
    };
    if (token) {
      headers['Authorization'] = `Bearer ${token}`;
    }

    const response = await fetch(`${API_BASE_URL}/api/intake/sessions/${sessionId}/documents/${documentId}`, {
      method: 'GET',
      headers,
    });

    if (!response.ok) {
      const err = await response.json().catch(() => ({ message: 'Failed to fetch document details' }));
      throw new ApiError(response.status, err.message || `HTTP error ${response.status}`);
    }

    return response.json();
  },
};
