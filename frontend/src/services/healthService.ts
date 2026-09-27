import { fetchApi } from './api';
import { HealthResponse } from '../types/api';

/**
 * Service to retrieve backend health status from /api/health
 */
export async function checkBackendHealth(): Promise<HealthResponse> {
  return fetchApi<HealthResponse>('/health');
}
