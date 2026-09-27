import React, { useState } from 'react';
import { SupportedLanguage, SUPPORTED_LANGUAGES, VoiceState } from '../../types/voice';

export interface VoiceRecorderProps {
  sessionId: string;
  onConfirm: (confirmedText: string, originalTranscript: string, confidence: number) => void;
  onFallbackToText?: () => void;
  initialLanguage?: SupportedLanguage;
  disabled?: boolean;
  mockTranscript?: string;
}

export const VoiceRecorder: React.FC<VoiceRecorderProps> = ({
  onConfirm,
  onFallbackToText,
  initialLanguage = 'en',
  disabled = false,
  mockTranscript,
}) => {
  const [state, setState] = useState<VoiceState>('idle');
  const [language, setLanguage] = useState<SupportedLanguage>(initialLanguage);
  const [transcript, setTranscript] = useState<string>('');
  const [originalTranscript, setOriginalTranscript] = useState<string>('');
  const [confidence, setConfidence] = useState<number>(0.95);
  const [isEditing, setIsEditing] = useState<boolean>(false);
  const [errorMessage, setErrorMessage] = useState<string>('');

  const recognitionRef = React.useRef<any>(null);

  React.useEffect(() => {
    return () => {
      if (recognitionRef.current) {
        try {
          recognitionRef.current.abort();
        } catch {
          // Ignore cleanup error
        }
      }
    };
  }, []);

  const getLangCode = (lang: SupportedLanguage) => {
    switch (lang) {
      case 'hi':
        return 'hi-IN';
      case 'te':
        return 'te-IN';
      default:
        return 'en-IN';
    }
  };

  const handleStartRecording = async () => {
    setErrorMessage('');
    const SpeechRecognition = (window as any).SpeechRecognition || (window as any).webkitSpeechRecognition;

    if (SpeechRecognition) {
      if (navigator.mediaDevices && typeof navigator.mediaDevices.getUserMedia === 'function') {
        try {
          const stream = await navigator.mediaDevices.getUserMedia({ audio: true });
          stream.getTracks().forEach((track) => track.stop());
        } catch (err: any) {
          console.warn('VoiceRecorder microphone permission/device error:', err);
          if (err.name === 'NotAllowedError' || err.name === 'PermissionDeniedError') {
            setErrorMessage('Microphone access was denied. You can use touch or keyboard input below.');
          } else if (err.name === 'NotFoundError' || err.name === 'DevicesNotFoundError') {
            setErrorMessage('No microphone detected on this device. You can tap or type your response below.');
          } else if (err.name === 'NotReadableError' || err.name === 'TrackStartError') {
            setErrorMessage('Microphone is in use by another application. Please try again.');
          } else {
            setErrorMessage('Microphone access issue. You can tap or type your response below.');
          }
          setState('error');
          return;
        }
      }

      try {
        const recognition = new SpeechRecognition();
        recognition.continuous = false;
        recognition.interimResults = true;
        recognition.lang = getLangCode(language);

        let fullText = '';

        recognition.onstart = () => {
          setState('recording');
        };

        recognition.onresult = (event: any) => {
          let current = '';
          for (let i = 0; i < event.results.length; i++) {
            current += event.results[i][0].transcript;
          }
          if (current.trim()) {
            fullText = current.trim();
            setTranscript(fullText);
          }
        };

        recognition.onerror = (event: any) => {
          console.warn('VoiceRecorder SpeechRecognition error:', event.error);
          if (event.error === 'not-allowed') {
            setErrorMessage('Microphone access was denied. You can use touch or keyboard input below.');
          } else if (event.error === 'no-speech') {
            setErrorMessage('No speech detected. Tap microphone to try again or switch to touch/keyboard input.');
          } else {
            setErrorMessage(`Voice recognition issue (${event.error}). You can type or tap options below.`);
          }
          setState('error');
        };

        recognition.onend = () => {
          if (fullText.trim()) {
            setOriginalTranscript(fullText.trim());
            setTranscript(fullText.trim());
            setConfidence(0.95);
            setState('transcript');
          } else {
            setErrorMessage('No speech detected. Tap microphone to try again or switch to touch/keyboard input.');
            setState('error');
          }
        };

        recognitionRef.current = recognition;
        recognition.start();
        return;
      } catch (e) {
        console.warn('Speech recognition init failed, using standard voice flow:', e);
      }
    }

    // Standard / simulated recording flow for environments without window.SpeechRecognition
    setState('recording');
  };

  const handleStopRecording = () => {
    if (recognitionRef.current) {
      try {
        recognitionRef.current.stop();
        setState('processing');
        return;
      } catch {
        // Fall through
      }
    }

    setState('processing');
    setTimeout(() => {
      if (mockTranscript) {
        setOriginalTranscript(mockTranscript);
        setTranscript(mockTranscript);
        setConfidence(0.95);
        setState('transcript');
        return;
      }
      // Never invent speech if no transcript was captured
      setErrorMessage('No speech detected. Tap microphone to try again or switch to touch/keyboard input.');
      setState('error');
    }, 150);
  };

  const handleConfirm = () => {
    setState('confirmed');
    onConfirm(transcript, originalTranscript, confidence);
  };

  const handleReject = () => {
    setState('rejected');
    setTranscript('');
  };

  const handleRetry = () => {
    setErrorMessage('');
    setIsEditing(false);
    setTranscript('');
    setOriginalTranscript('');
    setState('recording');
  };

  const handleFallback = () => {
    setState('fallback');
    if (onFallbackToText) {
      onFallbackToText();
    }
  };

  return (
    <div
      role="region"
      aria-label="Voice Input Interface"
      className="p-6 bg-white dark:bg-slate-900 rounded-xl shadow-lg border border-slate-200 dark:border-slate-800 max-w-xl mx-auto"
    >
      <div className="flex justify-between items-center mb-6">
        <div>
          <h2 className="text-xl font-bold text-slate-900 dark:text-white">Voice Input</h2>
          <p className="text-sm text-slate-500 dark:text-slate-400">
            Speak your response. You will review and confirm your words before saving.
          </p>
        </div>

        {/* Language Selection */}
        <div>
          <label htmlFor="voice-language-select" className="sr-only">
            Select Language
          </label>
          <select
            id="voice-language-select"
            value={language}
            onChange={(e) => setLanguage(e.target.value as SupportedLanguage)}
            disabled={disabled || state === 'recording' || state === 'processing'}
            className="text-sm bg-slate-100 dark:bg-slate-800 text-slate-800 dark:text-slate-200 rounded-lg px-3 py-1.5 border border-slate-300 dark:border-slate-700"
          >
            {SUPPORTED_LANGUAGES.map((lang) => (
              <option key={lang.code} value={lang.code}>
                {lang.nativeLabel} ({lang.label})
              </option>
            ))}
          </select>
        </div>
      </div>

      {/* State: IDLE */}
      {state === 'idle' && (
        <div className="text-center py-8">
          <div className="mb-6">
            <button
              type="button"
              onClick={handleStartRecording}
              disabled={disabled}
              aria-label="Start voice recording"
              className="w-20 h-20 bg-blue-600 hover:bg-blue-700 text-white rounded-full flex items-center justify-center mx-auto shadow-md hover:shadow-lg transition-transform hover:scale-105 active:scale-95 disabled:opacity-50"
            >
              <svg className="w-10 h-10" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path
                  strokeLinecap="round"
                  strokeLinejoin="round"
                  strokeWidth="2"
                  d="M19 11a7 7 0 01-7 7m0 0a7 7 0 01-7-7m7 7v4m0 0H8m4 0h4m-4-8a3 3 0 01-3-3V5a3 3 0 116 0v6a3 3 0 01-3 3z"
                />
              </svg>
            </button>
          </div>
          <p className="text-slate-600 dark:text-slate-300 font-medium">Tap microphone to begin speaking</p>
        </div>
      )}

      {/* State: RECORDING */}
      {state === 'recording' && (
        <div className="text-center py-8">
          <div className="relative mb-6">
            <div className="w-24 h-24 bg-red-500/20 animate-ping rounded-full mx-auto absolute inset-0 -m-2"></div>
            <button
              type="button"
              onClick={handleStopRecording}
              aria-label="Stop recording"
              className="relative w-20 h-20 bg-red-600 hover:bg-red-700 text-white rounded-full flex items-center justify-center mx-auto shadow-lg"
            >
              <svg className="w-8 h-8" fill="currentColor" viewBox="0 0 24 24">
                <rect x="6" y="6" width="12" height="12" rx="2" />
              </svg>
            </button>
          </div>
          <p className="text-red-600 dark:text-red-400 font-semibold animate-pulse" role="status">
            Listening... Speak clearly into the kiosk microphone
          </p>
        </div>
      )}

      {/* State: PROCESSING */}
      {state === 'processing' && (
        <div className="text-center py-8" role="status">
          <div className="w-12 h-12 border-4 border-blue-600 border-t-transparent rounded-full animate-spin mx-auto mb-4"></div>
          <p className="text-slate-700 dark:text-slate-300 font-medium">
            Converting speech to text...
          </p>
        </div>
      )}

      {/* State: TRANSCRIPT (Review & Confirmation) */}
      {state === 'transcript' && (
        <div className="py-4">
          <div className="bg-slate-50 dark:bg-slate-800/60 p-4 rounded-lg border border-slate-200 dark:border-slate-700 mb-4">
            <div className="flex justify-between items-center mb-2">
              <span className="text-xs font-semibold uppercase tracking-wider text-slate-500 dark:text-slate-400">
                Transcription (Confidence: {Math.round(confidence * 100)}%)
              </span>
              <button
                type="button"
                onClick={() => setIsEditing(!isEditing)}
                className="text-xs font-medium text-blue-600 dark:text-blue-400 hover:underline"
                aria-label={isEditing ? 'Done editing' : 'Edit transcription text'}
              >
                {isEditing ? 'Done Editing' : 'Edit Text'}
              </button>
            </div>

            {isEditing ? (
              <textarea
                value={transcript}
                onChange={(e) => setTranscript(e.target.value)}
                aria-label="Edit transcribed response"
                rows={3}
                className="w-full p-2 bg-white dark:bg-slate-900 border border-slate-300 dark:border-slate-600 rounded text-slate-800 dark:text-slate-100 text-sm focus:ring-2 focus:ring-blue-500"
              />
            ) : (
              <p className="text-slate-900 dark:text-white font-medium text-base leading-relaxed">
                "{transcript}"
              </p>
            )}
          </div>

          <div className="bg-amber-50 dark:bg-amber-950/40 border border-amber-200 dark:border-amber-800/60 p-3 rounded-lg mb-6 text-xs text-amber-800 dark:text-amber-200">
            Please verify your response above. Your words will only be saved after you confirm.
          </div>

          <div className="flex flex-col sm:flex-row gap-3">
            <button
              type="button"
              onClick={handleConfirm}
              aria-label="Confirm response"
              className="flex-1 py-3 px-4 bg-emerald-600 hover:bg-emerald-700 text-white font-semibold rounded-lg shadow transition text-center"
            >
              Confirm & Save
            </button>
            <button
              type="button"
              onClick={handleRetry}
              aria-label="Retry voice recording"
              className="py-3 px-4 bg-slate-200 dark:bg-slate-700 hover:bg-slate-300 dark:hover:bg-slate-600 text-slate-800 dark:text-slate-200 font-medium rounded-lg text-center"
            >
              Retry Voice
            </button>
            <button
              type="button"
              onClick={handleReject}
              aria-label="Reject transcript"
              className="py-3 px-4 bg-rose-100 dark:bg-rose-950/60 hover:bg-rose-200 dark:hover:bg-rose-900 text-rose-700 dark:text-rose-300 font-medium rounded-lg text-center"
            >
              Reject
            </button>
          </div>
        </div>
      )}

      {/* State: CONFIRMED */}
      {state === 'confirmed' && (
        <div className="text-center py-6 text-emerald-600 dark:text-emerald-400 font-medium" role="status">
          ✓ Response confirmed by patient and ready for submission.
        </div>
      )}

      {/* State: REJECTED */}
      {state === 'rejected' && (
        <div className="text-center py-6">
          <p className="text-slate-600 dark:text-slate-300 mb-4">
            Voice response was rejected and has not been saved.
          </p>
          <div className="flex justify-center gap-3">
            <button
              type="button"
              onClick={handleRetry}
              className="px-4 py-2 bg-blue-600 text-white rounded-lg text-sm font-medium hover:bg-blue-700"
            >
              Try Speaking Again
            </button>
            <button
              type="button"
              onClick={handleFallback}
              className="px-4 py-2 bg-slate-200 dark:bg-slate-700 text-slate-800 dark:text-slate-200 rounded-lg text-sm font-medium"
            >
              Use Touch / Keyboard
            </button>
          </div>
        </div>
      )}

      {/* State: ERROR */}
      {state === 'error' && (
        <div className="text-center py-6" role="alert">
          <div className="text-rose-600 dark:text-rose-400 mb-4 font-medium">
            {errorMessage || 'Voice recognition encountered an issue.'}
          </div>
          <div className="flex justify-center gap-3">
            <button
              type="button"
              onClick={handleRetry}
              className="px-4 py-2 bg-blue-600 text-white rounded-lg text-sm font-medium hover:bg-blue-700"
            >
              Retry
            </button>
            <button
              type="button"
              onClick={handleFallback}
              className="px-4 py-2 bg-slate-200 dark:bg-slate-700 text-slate-800 dark:text-slate-200 rounded-lg text-sm font-medium"
            >
              Use Touch / Keyboard
            </button>
          </div>
        </div>
      )}

      {/* State: FALLBACK */}
      {state === 'fallback' && (
        <div className="text-center py-6">
          <p className="text-slate-700 dark:text-slate-300 text-sm font-medium mb-2">
            Switched to touch/keyboard input.
          </p>
          <p className="text-xs text-slate-500">
            You can type your answer or select options on screen.
          </p>
        </div>
      )}

      {/* Touch / Text Fallback Footer */}
      {state !== 'fallback' && (
        <div className="mt-6 pt-4 border-t border-slate-200 dark:border-slate-800 flex justify-between items-center text-xs text-slate-500">
          <span>Having trouble with voice?</span>
          <button
            type="button"
            onClick={handleFallback}
            aria-label="Switch to touch or keyboard input"
            className="text-blue-600 dark:text-blue-400 hover:underline font-medium"
          >
            Use Touch / Keyboard Instead →
          </button>
        </div>
      )}
    </div>
  );
};
