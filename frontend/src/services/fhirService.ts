import { API_BASE_URL } from '../constants';
import { ApiResponse } from '../types/api';
import { ApiError } from './api';
import { FhirExportSummary, FhirValidationResponse } from '../types/fhir';

export const fhirService = {
  /**
   * Retrieves the complete FHIR R4 Bundle JSON for a clinical intake session.
   */
  async getClinicalBundleJson(sessionId: string, token?: string): Promise<string> {
    const headers: Record<string, string> = {
      Accept: 'application/fhir+json, application/json',
    };
    if (token) {
      headers['Authorization'] = `Bearer ${token}`;
    }

    const response = await fetch(`${API_BASE_URL}/api/fhir/sessions/${sessionId}/bundle`, {
      method: 'GET',
      headers,
    });

    if (!response.ok) {
      const errorText = await response.text();
      throw new ApiError(response.status, errorText || `Failed to fetch FHIR bundle: ${response.statusText}`);
    }

    return response.text();
  },

  /**
   * Retrieves the stored or preview FHIR export summary for an intake session.
   */
  async getExportSummary(sessionId: string, token?: string): Promise<ApiResponse<FhirExportSummary>> {
    const headers: Record<string, string> = {
      'Content-Type': 'application/json',
    };
    if (token) {
      headers['Authorization'] = `Bearer ${token}`;
    }

    const response = await fetch(`${API_BASE_URL}/api/fhir/sessions/${sessionId}/export`, {
      method: 'GET',
      headers,
    });

    if (!response.ok) {
      const errorText = await response.text();
      throw new ApiError(response.status, errorText || `Failed to get export summary: ${response.statusText}`);
    }

    return response.json();
  },

  /**
   * Generates, validates, and stores a verified FHIR R4 export bundle.
   */
  async exportClinicalBundle(sessionId: string, token?: string): Promise<ApiResponse<FhirExportSummary>> {
    const headers: Record<string, string> = {
      'Content-Type': 'application/json',
    };
    if (token) {
      headers['Authorization'] = `Bearer ${token}`;
    }

    const response = await fetch(`${API_BASE_URL}/api/fhir/sessions/${sessionId}/export`, {
      method: 'POST',
      headers,
    });

    if (!response.ok) {
      const errorText = await response.text();
      throw new ApiError(response.status, errorText || `Failed to export FHIR bundle: ${response.statusText}`);
    }

    return response.json();
  },

  /**
   * Validates session data against FHIR R4 invariants and MediKiosk profile requirements.
   */
  async validateSession(sessionId: string, token?: string): Promise<ApiResponse<FhirValidationResponse>> {
    const headers: Record<string, string> = {
      'Content-Type': 'application/json',
    };
    if (token) {
      headers['Authorization'] = `Bearer ${token}`;
    }

    const response = await fetch(`${API_BASE_URL}/api/fhir/sessions/${sessionId}/validate`, {
      method: 'GET',
      headers,
    });

    if (!response.ok) {
      const errorText = await response.text();
      throw new ApiError(response.status, errorText || `Failed to validate FHIR bundle: ${response.statusText}`);
    }

    return response.json();
  },

  /**
   * Helper utility to trigger browser download of a JSON string.
   */
  downloadJson(filename: string, content: string): void {
    const blob = new Blob([content], { type: 'application/fhir+json;charset=utf-8' });
    const url = URL.createObjectURL(blob);
    const link = document.createElement('a');
    link.href = url;
    link.download = filename;
    document.body.appendChild(link);
    link.click();
    document.body.removeChild(link);
    URL.revokeObjectURL(url);
  },
};
