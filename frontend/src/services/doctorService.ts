import { API_BASE_URL } from '../constants';
import { ApiResponse } from '../types/api';
import { ApiError } from './api';
import { ClinicalSummary } from '../types/clinicalSummary';
import {
  DoctorAmendmentRequest,
  DoctorInteroperabilityStatus,
  DoctorQueueItem,
  DoctorReviewDetail,
  DoctorVerificationRequest,
  TriagePriority,
  ClinicalSummaryStatus,
} from '../types/doctor';

export const doctorService = {
  /**
   * Retrieves the consultation queue with optional priority and status filters.
   */
  async getQueue(
    priority?: TriagePriority,
    status?: ClinicalSummaryStatus,
    token?: string
  ): Promise<ApiResponse<DoctorQueueItem[]>> {
    const params = new URLSearchParams();
    if (priority) params.append('priority', priority);
    if (status) params.append('status', status);

    const query = params.toString() ? `?${params.toString()}` : '';
    const headers: Record<string, string> = {
      'Content-Type': 'application/json',
    };
    if (token) {
      headers['Authorization'] = `Bearer ${token}`;
    }

    const response = await fetch(`${API_BASE_URL}/api/doctor/queue${query}`, {
      method: 'GET',
      headers,
    });

    if (!response.ok) {
      let msg = 'Failed to retrieve doctor queue';
      try {
        const err = await response.json();
        if (err && err.message) msg = err.message;
      } catch {
        const text = await response.text().catch(() => '');
        if (text && text.trim()) msg = text.trim();
      }
      throw new ApiError(response.status, msg);
    }

    try {
      return await response.json();
    } catch (err: any) {
      throw new ApiError(response.status, `Invalid JSON response: ${err.message}`);
    }
  },

  /**
   * Retrieves the comprehensive clinical review workstation payload for an intake session.
   */
  async getCaseReview(sessionId: string, token?: string): Promise<ApiResponse<DoctorReviewDetail>> {
    const headers: Record<string, string> = {
      'Content-Type': 'application/json',
    };
    if (token) {
      headers['Authorization'] = `Bearer ${token}`;
    }

    const response = await fetch(`${API_BASE_URL}/api/doctor/sessions/${sessionId}/review`, {
      method: 'GET',
      headers,
    });

    if (!response.ok) {
      let msg = 'Failed to retrieve case review details';
      try {
        const err = await response.json();
        if (err && err.message) msg = err.message;
      } catch {
        const text = await response.text().catch(() => '');
        if (text && text.trim()) msg = text.trim();
      }
      throw new ApiError(response.status, msg);
    }

    try {
      return await response.json();
    } catch (err: any) {
      throw new ApiError(response.status, `Invalid JSON response: ${err.message}`);
    }
  },

  /**
   * Submits physician amendments and corrections to an existing pre-consult summary.
   */
  async amendSummary(
    sessionId: string,
    request: DoctorAmendmentRequest,
    token?: string
  ): Promise<ApiResponse<ClinicalSummary>> {
    const headers: Record<string, string> = {
      'Content-Type': 'application/json',
    };
    if (token) {
      headers['Authorization'] = `Bearer ${token}`;
    }

    const response = await fetch(`${API_BASE_URL}/api/doctor/sessions/${sessionId}/summary/amend`, {
      method: 'PUT',
      headers,
      body: JSON.stringify(request),
    });

    if (!response.ok) {
      let msg = 'Failed to amend clinical summary';
      try {
        const err = await response.json();
        if (err && err.message) msg = err.message;
      } catch {
        const text = await response.text().catch(() => '');
        if (text && text.trim()) msg = text.trim();
      }
      throw new ApiError(response.status, msg);
    }

    try {
      return await response.json();
    } catch (err: any) {
      throw new ApiError(response.status, `Invalid JSON response: ${err.message}`);
    }
  },

  /**
   * Submits formal physician verification of the reviewed clinical summary.
   */
  async verifySummary(
    sessionId: string,
    request: DoctorVerificationRequest,
    token?: string
  ): Promise<ApiResponse<ClinicalSummary>> {
    const headers: Record<string, string> = {
      'Content-Type': 'application/json',
    };
    if (token) {
      headers['Authorization'] = `Bearer ${token}`;
    }

    const response = await fetch(`${API_BASE_URL}/api/doctor/sessions/${sessionId}/summary/verify`, {
      method: 'POST',
      headers,
      body: JSON.stringify(request),
    });

    if (!response.ok) {
      let msg = 'Failed to verify clinical summary';
      try {
        const err = await response.json();
        if (err && err.message) msg = err.message;
      } catch {
        const text = await response.text().catch(() => '');
        if (text && text.trim()) msg = text.trim();
      }
      throw new ApiError(response.status, msg);
    }

    try {
      return await response.json();
    } catch (err: any) {
      throw new ApiError(response.status, `Invalid JSON response: ${err.message}`);
    }
  },

  /**
   * Retrieves multi-system interoperability sync status across FHIR, ABDM, HIS, and AWS HealthLake.
   */
  async getInteroperabilityStatus(
    sessionId: string,
    token?: string
  ): Promise<ApiResponse<DoctorInteroperabilityStatus>> {
    const headers: Record<string, string> = {
      'Content-Type': 'application/json',
    };
    if (token) {
      headers['Authorization'] = `Bearer ${token}`;
    }

    const response = await fetch(`${API_BASE_URL}/api/doctor/sessions/${sessionId}/integrations`, {
      method: 'GET',
      headers,
    });

    if (!response.ok) {
      let msg = 'Failed to retrieve interoperability status';
      try {
        const err = await response.json();
        if (err && err.message) msg = err.message;
      } catch {
        const text = await response.text().catch(() => '');
        if (text && text.trim()) msg = text.trim();
      }
      throw new ApiError(response.status, msg);
    }

    try {
      return await response.json();
    } catch (err: any) {
      throw new ApiError(response.status, `Invalid JSON response: ${err.message}`);
    }
  },

  /**
   * Triggers manual re-synchronization of multi-system interoperability adapters.
   */
  async syncInteroperability(
    sessionId: string,
    token?: string
  ): Promise<ApiResponse<DoctorInteroperabilityStatus>> {
    const headers: Record<string, string> = {
      'Content-Type': 'application/json',
    };
    if (token) {
      headers['Authorization'] = `Bearer ${token}`;
    }

    const response = await fetch(`${API_BASE_URL}/api/doctor/sessions/${sessionId}/sync-integrations`, {
      method: 'POST',
      headers,
    });

    if (!response.ok) {
      let msg = 'Failed to execute interoperability sync';
      try {
        const err = await response.json();
        if (err && err.message) msg = err.message;
      } catch {
        const text = await response.text().catch(() => '');
        if (text && text.trim()) msg = text.trim();
      }
      throw new ApiError(response.status, msg);
    }

    try {
      return await response.json();
    } catch (err: any) {
      throw new ApiError(response.status, `Invalid JSON response: ${err.message}`);
    }
  },
};
