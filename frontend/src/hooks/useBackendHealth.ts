import { useState, useEffect, useCallback } from 'react';
import { checkBackendHealth } from '../services/healthService';
import { ConnectionState, HealthResponse } from '../types/api';

export function useBackendHealth() {
  const [connectionState, setConnectionState] = useState<ConnectionState>('checking');
  const [healthData, setHealthData] = useState<HealthResponse | null>(null);
  const [errorMessage, setErrorMessage] = useState<string | null>(null);
  const [lastChecked, setLastChecked] = useState<Date | null>(null);
  const [isRefreshing, setIsRefreshing] = useState<boolean>(false);

  const performHealthCheck = useCallback(async () => {
    setIsRefreshing(true);
    setConnectionState('checking');
    try {
      const data = await checkBackendHealth();
      if (data && (data.status === 'UP' || data.status === 'DEGRADED')) {
        setHealthData(data);
        setConnectionState('connected');
        setErrorMessage(null);
      } else {
        setHealthData(data);
        setConnectionState('unavailable');
        setErrorMessage(`Unexpected status: ${data?.status || 'Unknown'}`);
      }
    } catch (err: any) {
      setHealthData(null);
      setConnectionState('unavailable');
      setErrorMessage(err?.message || 'Failed to connect to backend service');
    } finally {
      setLastChecked(new Date());
      setIsRefreshing(false);
    }
  }, []);

  useEffect(() => {
    performHealthCheck();
  }, [performHealthCheck]);

  return {
    connectionState,
    healthData,
    errorMessage,
    lastChecked,
    isRefreshing,
    refresh: performHealthCheck,
  };
}
