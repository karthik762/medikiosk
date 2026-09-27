import { API_BASE_URL } from '../constants';
import { ApiResponse } from '../types/api';
import { ApiError } from './api';
import { IntegrationRecordItem } from '../types/abdm';
import {
  HealthLakeExportRequest,
  HealthLakeExportResponse,
  HealthLakeStatus,
} from '../types/healthlake';

export const healthlakeService = {
  /**
   * Retrieves AWS HealthLake adapter operating mode, configuration, and connectivity.
   */
  async getStatus(token?: string): Promise<ApiResponse<HealthLakeStatus>> {
    const headers: Record<string, string> = {
      'Content-Type': 'application/json',
    };
    if (token) {
      headers['Authorization'] = `Bearer ${token}`;
    }

    const response = await fetch(`${API_BASE_URL}/api/healthlake/status`, {
      method: 'GET',
      headers,
    });

    if (!response.ok) {
      const errorText = await response.text();
      throw new ApiError(response.status, errorText || `Failed to fetch AWS HealthLake status: ${response.statusText}`);
    }

    return response.json();
  },

  /**
   * Transmits doctor-verified clinical FHIR bundle to AWS HealthLake.
   */
  async exportClinicalData(
    sessionId: string,
    request?: HealthLakeExportRequest,
    token?: string
  ): Promise<ApiResponse<HealthLakeExportResponse>> {
    const headers: Record<string, string> = {
      'Content-Type': 'application/json',
    };
    if (token) {
      headers['Authorization'] = `Bearer ${token}`;
    }

    const response = await fetch(`${API_BASE_URL}/api/healthlake/sessions/${sessionId}/export`, {
      method: 'POST',
      headers,
      body: JSON.stringify(request || {}),
    });

    if (!response.ok) {
      let errMsg = response.statusText;
      try {
        const errorJson = await response.json();
        errMsg = errorJson.message || errMsg;
      } catch {
        const text = await response.text();
        if (text) errMsg = text;
      }
      throw new ApiError(response.status, errMsg);
    }

    return response.json();
  },

  /**
   * Retrieves AWS HealthLake integration transaction history for a session.
   */
  async getSessionStatus(sessionId: string, token?: string): Promise<ApiResponse<IntegrationRecordItem[]>> {
    const headers: Record<string, string> = {
      'Content-Type': 'application/json',
    };
    if (token) {
      headers['Authorization'] = `Bearer ${token}`;
    }

    const response = await fetch(`${API_BASE_URL}/api/healthlake/sessions/${sessionId}/status`, {
      method: 'GET',
      headers,
    });

    if (!response.ok) {
      const errorText = await response.text();
      throw new ApiError(response.status, errorText || `Failed to get session HealthLake status: ${response.statusText}`);
    }

    return response.json();
  },
};
