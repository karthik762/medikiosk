import { API_BASE_URL } from '../constants';
import { ApiResponse } from '../types/api';
import { ApiError } from './api';
import { IntegrationRecordItem } from '../types/abdm';
import {
  HisExportRequest,
  HisExportResponse,
  HisLinkPatientRequest,
  HisLinkPatientResponse,
  HisPatientSearchResponse,
  HisStatus,
} from '../types/his';

export const hisService = {
  /**
   * Retrieves HIS adapter operating mode, configuration, and connectivity.
   */
  async getStatus(token?: string): Promise<ApiResponse<HisStatus>> {
    const headers: Record<string, string> = {
      'Content-Type': 'application/json',
    };
    if (token) {
      headers['Authorization'] = `Bearer ${token}`;
    }

    const response = await fetch(`${API_BASE_URL}/api/his/status`, {
      method: 'GET',
      headers,
    });

    if (!response.ok) {
      const errorText = await response.text();
      throw new ApiError(response.status, errorText || `Failed to fetch HIS status: ${response.statusText}`);
    }

    return response.json();
  },

  /**
   * Transmits doctor-verified clinical FHIR bundle to the hospital HIS intake queue.
   */
  async exportClinicalData(
    sessionId: string,
    request?: HisExportRequest,
    token?: string
  ): Promise<ApiResponse<HisExportResponse>> {
    const headers: Record<string, string> = {
      'Content-Type': 'application/json',
    };
    if (token) {
      headers['Authorization'] = `Bearer ${token}`;
    }

    const response = await fetch(`${API_BASE_URL}/api/his/sessions/${sessionId}/export`, {
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
   * Retrieves HIS integration transaction history for a session.
   */
  async getSessionStatus(sessionId: string, token?: string): Promise<ApiResponse<IntegrationRecordItem[]>> {
    const headers: Record<string, string> = {
      'Content-Type': 'application/json',
    };
    if (token) {
      headers['Authorization'] = `Bearer ${token}`;
    }

    const response = await fetch(`${API_BASE_URL}/api/his/sessions/${sessionId}/status`, {
      method: 'GET',
      headers,
    });

    if (!response.ok) {
      const errorText = await response.text();
      throw new ApiError(response.status, errorText || `Failed to get session HIS status: ${response.statusText}`);
    }

    return response.json();
  },

  /**
   * Searches hospital Master Patient Index (MPI) for matching patient candidates.
   */
  async searchPatients(
    query: string,
    searchType = 'ALL',
    token?: string
  ): Promise<ApiResponse<HisPatientSearchResponse>> {
    const headers: Record<string, string> = {
      'Content-Type': 'application/json',
    };
    if (token) {
      headers['Authorization'] = `Bearer ${token}`;
    }

    const encodedQuery = encodeURIComponent(query);
    const encodedType = encodeURIComponent(searchType);
    const response = await fetch(`${API_BASE_URL}/api/his/patients/search?query=${encodedQuery}&searchType=${encodedType}`, {
      method: 'GET',
      headers,
    });

    if (!response.ok) {
      const errorText = await response.text();
      throw new ApiError(response.status, errorText || `Failed to search hospital patients: ${response.statusText}`);
    }

    return response.json();
  },

  /**
   * Links a hospital HIS MRN / ID to a patient record.
   */
  async linkPatient(
    patientId: string,
    request: HisLinkPatientRequest,
    token?: string
  ): Promise<ApiResponse<HisLinkPatientResponse>> {
    const headers: Record<string, string> = {
      'Content-Type': 'application/json',
    };
    if (token) {
      headers['Authorization'] = `Bearer ${token}`;
    }

    const response = await fetch(`${API_BASE_URL}/api/his/patients/${patientId}/link`, {
      method: 'POST',
      headers,
      body: JSON.stringify(request),
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
};
