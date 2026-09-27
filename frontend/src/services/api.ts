import { API_BASE_URL } from '../constants';

export class ApiError extends Error {
  constructor(public status: number, message: string) {
    super(message);
    this.name = 'ApiError';
  }
}

/**
 * Base HTTP client for calling MediKiosk REST APIs
 */
export async function fetchApi<T>(endpoint: string, options: RequestInit = {}): Promise<T> {
  const cleanEndpoint = endpoint.startsWith('/') ? endpoint : `/${endpoint}`;
  const normalizedEndpoint = cleanEndpoint.startsWith('/api') ? cleanEndpoint : `/api${cleanEndpoint}`;
  const url = `${API_BASE_URL}${normalizedEndpoint}`;

  // Destructure headers out of options so we can merge them properly
  const { headers: extraHeaders, ...restOptions } = options;

  const response = await fetch(url, {
    ...restOptions,
    headers: {
      'Content-Type': 'application/json',
      ...(extraHeaders as Record<string, string>),
    },
  });

  if (!response.ok) {
    let errorMsg = `HTTP error ${response.status}: ${response.statusText || 'Error'}`;
    try {
      const errorJson = await response.json();
      if (errorJson && errorJson.message) {
        errorMsg = errorJson.message;
      }
    } catch {
      try {
        const text = await response.text();
        if (text && text.trim()) {
          errorMsg = text.trim();
        }
      } catch {
        // Fallback to default errorMsg
      }
    }
    throw new ApiError(response.status, errorMsg);
  }

  try {
    return (await response.json()) as T;
  } catch (err: any) {
    throw new ApiError(response.status, `Invalid JSON response from server (${err.message})`);
  }
}
