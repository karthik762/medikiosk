/**
 * State machine states for the voice intake interaction.
 */
export type VoiceState =
  | 'idle'
  | 'recording'
  | 'processing'
  | 'transcript'
  | 'confirmed'
  | 'rejected'
  | 'error'
  | 'fallback';

/**
 * Supported clinical intake languages.
 */
export type SupportedLanguage = 'en' | 'hi' | 'te';

export interface LanguageOption {
  code: SupportedLanguage;
  label: string;
  nativeLabel: string;
}

export const SUPPORTED_LANGUAGES: LanguageOption[] = [
  { code: 'en', label: 'English', nativeLabel: 'English' },
  { code: 'hi', label: 'Hindi', nativeLabel: 'हिंदी' },
  { code: 'te', label: 'Telugu', nativeLabel: 'తెలుగు' },
];

/**
 * Speech-to-text response from backend
 */
export interface TranscriptionData {
  sessionId: string;
  transcript: string;
  confidence: number;
  language: string;
  status: string;
  isMock: boolean;
  provider?: string;
  providerType?: 'REAL' | 'MOCK';
  inputMethod?: string;
  timestamp?: string;
}

/**
 * Text-to-speech request payload
 */
export interface SpeechSynthesisPayload {
  text: string;
  language?: string;
  sessionId?: string;
}
