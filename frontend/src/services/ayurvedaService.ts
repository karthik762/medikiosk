import { API_BASE_URL } from '../constants';
import { ApiResponse } from '../types/api';
import { ApiError } from './api';
import { AyurvedaHistoryRequest, AyurvedaHistoryResponse } from '../types/ayurveda';

/**
 * Service for managing structured Ayurveda clinical intake histories.
 */
export const ayurvedaService = {
  /**
   * Retrieves the Ayurveda intake history for an intake session.
   */
  async getAyurvedaHistory(sessionId: string, token?: string): Promise<ApiResponse<AyurvedaHistoryResponse>> {
    const headers: Record<string, string> = {
      'Content-Type': 'application/json',
    };
    if (token) {
      headers['Authorization'] = `Bearer ${token}`;
    }

    const response = await fetch(`${API_BASE_URL}/api/intake/sessions/${sessionId}/ayurveda`, {
      method: 'GET',
      headers,
    });

    if (!response.ok) {
      const err = await response.json().catch(() => ({ message: 'Failed to fetch Ayurveda history' }));
      throw new ApiError(response.status, err.message || `HTTP error ${response.status}`);
    }

    return response.json();
  },

  /**
   * Saves or updates the structured Ayurveda history draft.
   */
  async saveAyurvedaHistory(
    sessionId: string,
    request: AyurvedaHistoryRequest,
    token?: string
  ): Promise<ApiResponse<AyurvedaHistoryResponse>> {
    const headers: Record<string, string> = {
      'Content-Type': 'application/json',
    };
    if (token) {
      headers['Authorization'] = `Bearer ${token}`;
    }

    const response = await fetch(`${API_BASE_URL}/api/intake/sessions/${sessionId}/ayurveda`, {
      method: 'POST',
      headers,
      body: JSON.stringify(request),
    });

    if (!response.ok) {
      const err = await response.json().catch(() => ({ message: 'Failed to save Ayurveda history' }));
      throw new ApiError(response.status, err.message || `HTTP error ${response.status}`);
    }

    return response.json();
  },

  /**
   * Confirms the Ayurveda intake history by the patient before doctor consultation.
   */
  async confirmAyurvedaHistory(sessionId: string, token?: string): Promise<ApiResponse<AyurvedaHistoryResponse>> {
    const headers: Record<string, string> = {
      'Content-Type': 'application/json',
    };
    if (token) {
      headers['Authorization'] = `Bearer ${token}`;
    }

    const response = await fetch(`${API_BASE_URL}/api/intake/sessions/${sessionId}/ayurveda/confirm`, {
      method: 'POST',
      headers,
    });

    if (!response.ok) {
      const err = await response.json().catch(() => ({ message: 'Failed to confirm Ayurveda history' }));
      throw new ApiError(response.status, err.message || `HTTP error ${response.status}`);
    }

    return response.json();
  },
};
