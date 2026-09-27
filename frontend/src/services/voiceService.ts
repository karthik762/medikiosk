import { API_BASE_URL } from '../constants';
import { ApiResponse } from '../types/api';
import { ApiError } from './api';
import { SpeechSynthesisPayload, TranscriptionData } from '../types/voice';

/**
 * Service for MediKiosk voice input (STT) and speech synthesis (TTS).
 */
export const voiceService = {
  /**
   * Submits recorded audio blob for transcription.
   */
  async transcribeAudio(
    sessionId: string,
    audioBlob: Blob,
    language?: string,
    token?: string
  ): Promise<ApiResponse<TranscriptionData>> {
    const formData = new FormData();
    formData.append('sessionId', sessionId);
    formData.append('file', audioBlob, 'recording.wav');
    if (language) {
      formData.append('language', language);
    }

    const headers: Record<string, string> = {};
    if (token) {
      headers['Authorization'] = `Bearer ${token}`;
    }

    const response = await fetch(`${API_BASE_URL}/api/voice/transcribe`, {
      method: 'POST',
      headers,
      body: formData,
    });

    if (!response.ok) {
      const errorBody = await response.json().catch(() => ({ message: 'Failed to transcribe audio' }));
      throw new ApiError(response.status, errorBody.message || `HTTP error ${response.status}`);
    }

    return response.json();
  },

  /**
   * Synthesizes approved UI or question text into spoken audio.
   */
  async synthesizeSpeech(payload: SpeechSynthesisPayload, token?: string): Promise<Blob> {
    const headers: Record<string, string> = {
      'Content-Type': 'application/json',
    };
    if (token) {
      headers['Authorization'] = `Bearer ${token}`;
    }

    const response = await fetch(`${API_BASE_URL}/api/voice/synthesize`, {
      method: 'POST',
      headers,
      body: JSON.stringify(payload),
    });

    if (!response.ok) {
      throw new ApiError(response.status, `Failed to synthesize speech: HTTP ${response.status}`);
    }

    return response.blob();
  },
};
