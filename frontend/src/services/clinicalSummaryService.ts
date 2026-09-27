import { API_BASE_URL } from '../constants';
import { ApiResponse } from '../types/api';
import { ApiError } from './api';
import { ClinicalSummary } from '../types/clinicalSummary';

/**
 * Service for generating, fetching, and managing AI clinical summaries.
 */
export const clinicalSummaryService = {
  /**
   * Generates or regenerates an AI clinical summary for the given intake session.
   */
  async generateClinicalSummary(
    sessionId: string,
    token?: string
  ): Promise<ApiResponse<ClinicalSummary>> {
    const headers: Record<string, string> = {
      'Content-Type': 'application/json',
    };
    if (token) {
      headers['Authorization'] = `Bearer ${token}`;
    }

    const response = await fetch(
      `${API_BASE_URL}/api/intake/sessions/${sessionId}/clinical-summary/generate`,
      {
        method: 'POST',
        headers,
      }
    );

    if (!response.ok) {
      const err = await response.json().catch(() => ({ message: 'Failed to generate clinical summary' }));
      throw new ApiError(response.status, err.message || `HTTP error ${response.status}`);
    }

    return response.json();
  },

  /**
   * Retrieves the generated clinical summary for an intake session.
   */
  async getSummaryBySession(
    sessionId: string,
    token?: string
  ): Promise<ApiResponse<ClinicalSummary>> {
    const headers: Record<string, string> = {
      'Content-Type': 'application/json',
    };
    if (token) {
      headers['Authorization'] = `Bearer ${token}`;
    }

    const response = await fetch(
      `${API_BASE_URL}/api/intake/sessions/${sessionId}/clinical-summary`,
      {
        method: 'GET',
        headers,
      }
    );

    if (!response.ok) {
      const err = await response.json().catch(() => ({ message: 'Failed to retrieve clinical summary' }));
      throw new ApiError(response.status, err.message || `HTTP error ${response.status}`);
    }

    return response.json();
  },

  /**
   * Retrieves a clinical summary directly by its UUID.
   */
  async getSummaryById(
    summaryId: string,
    token?: string
  ): Promise<ApiResponse<ClinicalSummary>> {
    const headers: Record<string, string> = {
      'Content-Type': 'application/json',
    };
    if (token) {
      headers['Authorization'] = `Bearer ${token}`;
    }

    const response = await fetch(
      `${API_BASE_URL}/api/intake/clinical-summary/${summaryId}`,
      {
        method: 'GET',
        headers,
      }
    );

    if (!response.ok) {
      const err = await response.json().catch(() => ({ message: 'Failed to retrieve clinical summary' }));
      throw new ApiError(response.status, err.message || `HTTP error ${response.status}`);
    }

    return response.json();
  },
};
