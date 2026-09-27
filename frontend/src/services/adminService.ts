import { API_BASE_URL } from '../constants';
import { ApiResponse } from '../types/api';
import {
  SystemAnalytics,
  AdminUser,
  CreateUserRequest,
  AuditLogDetail,
  SystemConfigSummary,
} from '../types/admin';

export const adminService = {
  getAnalytics: async (token?: string | null): Promise<ApiResponse<SystemAnalytics>> => {
    const headers: Record<string, string> = {
      'Content-Type': 'application/json',
    };
    if (token) {
      headers['Authorization'] = `Bearer ${token}`;
    }
    const res = await fetch(`${API_BASE_URL}/api/admin/analytics`, { headers });
    if (!res.ok) {
      const err = await res.json().catch(() => ({ message: 'Failed to fetch system analytics' }));
      throw new Error(err.message || `HTTP ${res.status}`);
    }
    return res.json();
  },

  getUsers: async (
    token?: string | null,
    role?: string,
    query?: string
  ): Promise<ApiResponse<AdminUser[]>> => {
    const headers: Record<string, string> = {
      'Content-Type': 'application/json',
    };
    if (token) {
      headers['Authorization'] = `Bearer ${token}`;
    }
    const params = new URLSearchParams();
    if (role) params.append('role', role);
    if (query) params.append('query', query);

    const url = `${API_BASE_URL}/api/admin/users?${params.toString()}`;
    const res = await fetch(url, { headers });
    if (!res.ok) {
      const err = await res.json().catch(() => ({ message: 'Failed to fetch users' }));
      throw new Error(err.message || `HTTP ${res.status}`);
    }
    return res.json();
  },

  createUser: async (
    req: CreateUserRequest,
    token?: string | null
  ): Promise<ApiResponse<AdminUser>> => {
    const headers: Record<string, string> = {
      'Content-Type': 'application/json',
    };
    if (token) {
      headers['Authorization'] = `Bearer ${token}`;
    }
    const res = await fetch(`${API_BASE_URL}/api/admin/users`, {
      method: 'POST',
      headers,
      body: JSON.stringify(req),
    });
    if (!res.ok) {
      const err = await res.json().catch(() => ({ message: 'Failed to create user' }));
      throw new Error(err.message || `HTTP ${res.status}`);
    }
    return res.json();
  },

  updateUserStatus: async (
    userId: string,
    status: 'ACTIVE' | 'INACTIVE' | 'SUSPENDED',
    token?: string | null
  ): Promise<ApiResponse<AdminUser>> => {
    const headers: Record<string, string> = {
      'Content-Type': 'application/json',
    };
    if (token) {
      headers['Authorization'] = `Bearer ${token}`;
    }
    const res = await fetch(`${API_BASE_URL}/api/admin/users/${userId}/status?status=${status}`, {
      method: 'PUT',
      headers,
    });
    if (!res.ok) {
      const err = await res.json().catch(() => ({ message: 'Failed to update user status' }));
      throw new Error(err.message || `HTTP ${res.status}`);
    }
    return res.json();
  },

  getAuditLogs: async (
    token?: string | null,
    action?: string,
    userId?: string,
    limit: number = 100
  ): Promise<ApiResponse<AuditLogDetail[]>> => {
    const headers: Record<string, string> = {
      'Content-Type': 'application/json',
    };
    if (token) {
      headers['Authorization'] = `Bearer ${token}`;
    }
    const params = new URLSearchParams();
    if (action) params.append('action', action);
    if (userId) params.append('userId', userId);
    params.append('limit', limit.toString());

    const res = await fetch(`${API_BASE_URL}/api/admin/audit-logs?${params.toString()}`, { headers });
    if (!res.ok) {
      const err = await res.json().catch(() => ({ message: 'Failed to fetch audit logs' }));
      throw new Error(err.message || `HTTP ${res.status}`);
    }
    return res.json();
  },

  getConfigSummary: async (token?: string | null): Promise<ApiResponse<SystemConfigSummary>> => {
    const headers: Record<string, string> = {
      'Content-Type': 'application/json',
    };
    if (token) {
      headers['Authorization'] = `Bearer ${token}`;
    }
    const res = await fetch(`${API_BASE_URL}/api/admin/config`, { headers });
    if (!res.ok) {
      const err = await res.json().catch(() => ({ message: 'Failed to fetch system config' }));
      throw new Error(err.message || `HTTP ${res.status}`);
    }
    return res.json();
  },
};
