import { API_BASE_URL } from '../constants';
import { ApiResponse } from '../types/api';
import { ApiError } from './api';
import {
  AbdmConsentRequest,
  AbdmConsentResponse,
  AbdmExchangeRequest,
  AbdmExchangeResponse,
  AbdmStatus,
  AbhaVerificationRequest,
  AbhaVerificationResponse,
  IntegrationRecordItem,
} from '../types/abdm';

export const abdmService = {
  /**
   * Retrieves ABDM adapter status, operating mode, and connectivity.
   */
  async getStatus(token?: string): Promise<ApiResponse<AbdmStatus>> {
    const headers: Record<string, string> = {
      'Content-Type': 'application/json',
    };
    if (token) {
      headers['Authorization'] = `Bearer ${token}`;
    }

    const response = await fetch(`${API_BASE_URL}/api/abdm/status`, {
      method: 'GET',
      headers,
    });

    if (!response.ok) {
      const errorText = await response.text();
      throw new ApiError(response.status, errorText || `Failed to fetch ABDM status: ${response.statusText}`);
    }

    return response.json();
  },

  /**
   * Verifies an ABHA identifier or address for a patient.
   */
  async verifyAbha(
    patientId: string,
    request: AbhaVerificationRequest,
    token?: string
  ): Promise<ApiResponse<AbhaVerificationResponse>> {
    const headers: Record<string, string> = {
      'Content-Type': 'application/json',
    };
    if (token) {
      headers['Authorization'] = `Bearer ${token}`;
    }

    const response = await fetch(`${API_BASE_URL}/api/abdm/patients/${patientId}/abha/verify`, {
      method: 'POST',
      headers,
      body: JSON.stringify(request),
    });

    if (!response.ok) {
      const errorText = await response.text();
      throw new ApiError(response.status, errorText || `Failed to verify ABHA: ${response.statusText}`);
    }

    return response.json();
  },

  /**
   * Grants patient consent for ABDM data sharing.
   */
  async grantConsent(
    sessionId: string,
    request?: AbdmConsentRequest,
    token?: string
  ): Promise<ApiResponse<AbdmConsentResponse>> {
    const headers: Record<string, string> = {
      'Content-Type': 'application/json',
    };
    if (token) {
      headers['Authorization'] = `Bearer ${token}`;
    }

    const response = await fetch(`${API_BASE_URL}/api/abdm/sessions/${sessionId}/consent`, {
      method: 'POST',
      headers,
      body: JSON.stringify(request || {}),
    });

    if (!response.ok) {
      const errorText = await response.text();
      throw new ApiError(response.status, errorText || `Failed to grant consent: ${response.statusText}`);
    }

    return response.json();
  },

  /**
   * Revokes patient consent for ABDM data sharing.
   */
  async revokeConsent(sessionId: string, token?: string): Promise<ApiResponse<AbdmConsentResponse>> {
    const headers: Record<string, string> = {
      'Content-Type': 'application/json',
    };
    if (token) {
      headers['Authorization'] = `Bearer ${token}`;
    }

    const response = await fetch(`${API_BASE_URL}/api/abdm/sessions/${sessionId}/consent/revoke`, {
      method: 'POST',
      headers,
    });

    if (!response.ok) {
      const errorText = await response.text();
      throw new ApiError(response.status, errorText || `Failed to revoke consent: ${response.statusText}`);
    }

    return response.json();
  },

  /**
   * Transmits doctor-verified clinical FHIR bundle to the ABDM gateway.
   */
  async exchangeClinicalData(
    sessionId: string,
    request?: AbdmExchangeRequest,
    token?: string
  ): Promise<ApiResponse<AbdmExchangeResponse>> {
    const headers: Record<string, string> = {
      'Content-Type': 'application/json',
    };
    if (token) {
      headers['Authorization'] = `Bearer ${token}`;
    }

    const response = await fetch(`${API_BASE_URL}/api/abdm/sessions/${sessionId}/exchange`, {
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
   * Retrieves ABDM integration transaction history for a session.
   */
  async getSessionStatus(sessionId: string, token?: string): Promise<ApiResponse<IntegrationRecordItem[]>> {
    const headers: Record<string, string> = {
      'Content-Type': 'application/json',
    };
    if (token) {
      headers['Authorization'] = `Bearer ${token}`;
    }

    const response = await fetch(`${API_BASE_URL}/api/abdm/sessions/${sessionId}/status`, {
      method: 'GET',
      headers,
    });

    if (!response.ok) {
      const errorText = await response.text();
      throw new ApiError(response.status, errorText || `Failed to get session ABDM status: ${response.statusText}`);
    }

    return response.json();
  },
};
