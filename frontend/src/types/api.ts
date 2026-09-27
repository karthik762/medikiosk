/**
 * Health check response payload from backend /api/health
 */
export interface HealthResponse {
  status: 'UP' | 'DOWN' | 'DEGRADED' | string;
  service: string;
  timestamp: string;
  version: string;
}

/**
 * Standard API response envelope
 */
export interface ApiResponse<T> {
  success: boolean;
  data?: T;
  message?: string;
  timestamp?: string;
}

/**
 * Frontend state tracking backend connectivity
 */
export type ConnectionState = 'checking' | 'connected' | 'unavailable';
