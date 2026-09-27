import React, { useState, useRef, useEffect } from 'react';
import { KioskButton } from './KioskButton';
import { getTranslation } from '../../i18n/translations';
import { voiceService } from '../../services/voiceService';
import { kioskService } from '../../services/kioskService';

declare global {
  interface Window {
    SpeechRecognition: any;
    webkitSpeechRecognition: any;
  }
}

interface ChiefComplaintVoiceViewProps {
  onSubmit: (complaintText: string, inputMethod: 'TEXT' | 'VOICE') => Promise<void>;
  onBack: () => void;
  loading?: boolean;
  language?: 'en' | 'hi' | 'te';
}

export const ChiefComplaintVoiceView: React.FC<ChiefComplaintVoiceViewProps> = ({
  onSubmit,
  onBack,
  loading = false,
  language = 'en',
}) => {
  const [complaintText, setComplaintText] = useState('');
  const [voiceState, setVoiceState] = useState<'IDLE' | 'LISTENING' | 'PROCESSING' | 'REVIEW' | 'ERROR'>('IDLE');
  const [confirmedByPatient, setConfirmedByPatient] = useState(false);
  const [inputMethod, setInputMethod] = useState<'TEXT' | 'VOICE'>('TEXT');
  const [audioSeconds, setAudioSeconds] = useState(0);
  const [voiceError, setVoiceError] = useState<string | null>(null);

  const recognitionRef = useRef<any>(null);
  const mediaRecorderRef = useRef<any>(null);
  const audioChunksRef = useRef<Blob[]>([]);
  const activeStreamRef = useRef<MediaStream | null>(null);
  const timerRef = useRef<any>(null);

  const t = (key: Parameters<typeof getTranslation>[1]) => getTranslation(language, key);

  useEffect(() => {
    return () => {
      stopStreamAndRecorder();
      if (recognitionRef.current) {
        try {
          recognitionRef.current.abort();
        } catch {
          // Ignore cleanup error
        }
      }
      if (timerRef.current) {
        clearInterval(timerRef.current);
      }
    };
  }, []);

  const stopStreamAndRecorder = (): Promise<Blob | null> => {
    return new Promise((resolve) => {
      if (activeStreamRef.current) {
        try {
          activeStreamRef.current.getTracks().forEach((track) => track.stop());
        } catch {}
        activeStreamRef.current = null;
      }
      if (mediaRecorderRef.current && mediaRecorderRef.current.state !== 'inactive') {
        mediaRecorderRef.current.onstop = () => {
          const blob = new Blob(audioChunksRef.current, {
            type: mediaRecorderRef.current?.mimeType || 'audio/webm',
          });
          resolve(blob);
        };
        try {
          mediaRecorderRef.current.stop();
        } catch {
          resolve(null);
        }
      } else {
        const blob = audioChunksRef.current.length > 0
          ? new Blob(audioChunksRef.current, { type: 'audio/webm' })
          : null;
        resolve(blob);
      }
    });
  };

  const getLangCode = () => {
    switch (language) {
      case 'hi':
        return 'hi-IN';
      case 'te':
        return 'te-IN';
      default:
        return 'en-IN';
    }
  };

  const handleToggleVoice = async () => {
    if (voiceState === 'LISTENING') {
      setVoiceState('PROCESSING');
      if (timerRef.current) clearInterval(timerRef.current);
      if (recognitionRef.current) {
        try {
          recognitionRef.current.stop();
        } catch {}
      }
      const audioBlob = await stopStreamAndRecorder();
      if (!complaintText.trim() && audioBlob) {
        const session = kioskService.getPersistedSession();
        if (session && session.sessionId) {
          try {
            const res = await voiceService.transcribeAudio(session.sessionId, audioBlob, language, session.token);
            if (res && res.data && res.data.transcript && res.data.status === 'SUCCESS') {
              setComplaintText(res.data.transcript);
              setVoiceError(null);
              setVoiceState('REVIEW');
              setInputMethod('VOICE');
              return;
            } else if (res && res.data && res.data.status === 'NO_SPEECH_DETECTED') {
              setVoiceError(t('noSpeechDetected'));
              setVoiceState('ERROR');
              return;
            }
          } catch (err) {
            console.warn('Backend chief complaint transcription error:', err);
          }
        }
      }
      if (complaintText.trim()) {
        setVoiceState('REVIEW');
      } else {
        setVoiceError(t('noSpeechDetected'));
        setVoiceState('ERROR');
      }
      return;
    }

    let stream: MediaStream | null = null;
    if (navigator.mediaDevices && typeof navigator.mediaDevices.getUserMedia === 'function') {
      try {
        stream = await navigator.mediaDevices.getUserMedia({ audio: true });
        activeStreamRef.current = stream;
      } catch (err: any) {
        console.warn('Microphone permission / device error in ChiefComplaint:', err);
        if (err.name === 'NotAllowedError' || err.name === 'PermissionDeniedError') {
          setVoiceError(t('micPermissionDenied'));
        } else if (err.name === 'NotFoundError' || err.name === 'DevicesNotFoundError') {
          setVoiceError(t('micNotFound'));
        } else if (err.name === 'NotReadableError' || err.name === 'TrackStartError') {
          setVoiceError(t('micInUse'));
        } else {
          setVoiceError(t('voiceError'));
        }
        setVoiceState('ERROR');
        return;
      }
    }

    // Initialize MediaRecorder for backend audio fallback
    audioChunksRef.current = [];
    if (stream && typeof MediaRecorder !== 'undefined') {
      try {
        const recorder = new MediaRecorder(stream);
        recorder.ondataavailable = (e) => {
          if (e.data && e.data.size > 0) {
            audioChunksRef.current.push(e.data);
          }
        };
        recorder.start(100);
        mediaRecorderRef.current = recorder;
      } catch (e) {
        console.warn('MediaRecorder init warning:', e);
      }
    }

    const SpeechRecognition = window.SpeechRecognition || window.webkitSpeechRecognition;

    if (!SpeechRecognition) {
      setVoiceState('LISTENING');
      setAudioSeconds(0);
      setConfirmedByPatient(false);
      setInputMethod('VOICE');

      if (timerRef.current) clearInterval(timerRef.current);
      timerRef.current = setInterval(() => {
        setAudioSeconds((prev) => prev + 1);
      }, 1000);
      return;
    }

    try {
      setVoiceError(null);
      const recognition = new SpeechRecognition();
      recognition.continuous = false;
      recognition.interimResults = true;
      recognition.lang = getLangCode();

      let fullTranscript = '';

      recognition.onstart = () => {
        setVoiceState('LISTENING');
        setAudioSeconds(0);
        setConfirmedByPatient(false);
        setInputMethod('VOICE');

        if (timerRef.current) clearInterval(timerRef.current);
        timerRef.current = setInterval(() => {
          setAudioSeconds((prev) => prev + 1);
        }, 1000);
      };

      recognition.onresult = (event: any) => {
        let current = '';
        for (let i = 0; i < event.results.length; i++) {
          current += event.results[i][0].transcript;
        }
        if (current.trim()) {
          fullTranscript = current.trim();
          setComplaintText(fullTranscript);
        }
      };

      recognition.onerror = async (event: any) => {
        console.warn('Speech recognition error:', event.error);
        if (timerRef.current) clearInterval(timerRef.current);
        if (event.error === 'aborted') {
          return;
        }

        if (event.error === 'network') {
          setVoiceState('PROCESSING');
          const audioBlob = await stopStreamAndRecorder();
          if (audioBlob) {
            const session = kioskService.getPersistedSession();
            if (session && session.sessionId) {
              try {
                const res = await voiceService.transcribeAudio(session.sessionId, audioBlob, language, session.token);
                if (res && res.data && res.data.transcript) {
                  setComplaintText(res.data.transcript);
                  setVoiceError(null);
                  setVoiceState('REVIEW');
                  setInputMethod('VOICE');
                  return;
                }
              } catch (backendErr) {
                console.warn('Backend audio transcription failed on network error:', backendErr);
              }
            }
          }
          setVoiceError(t('voiceNetworkError'));
          setVoiceState('ERROR');
          return;
        }

        if (event.error === 'not-allowed') {
          setVoiceError(t('micPermissionDenied'));
        } else if (event.error === 'no-speech') {
          setVoiceError(t('noSpeechDetected'));
        } else if (event.error === 'audio-capture') {
          setVoiceError(t('micNotFound'));
        } else {
          setVoiceError(t('voiceError'));
        }
        setVoiceState('ERROR');
      };

      recognition.onend = async () => {
        if (timerRef.current) clearInterval(timerRef.current);
        if (fullTranscript.trim()) {
          setVoiceState('REVIEW');
          setConfirmedByPatient(false);
          stopStreamAndRecorder();
        } else {
          const audioBlob = await stopStreamAndRecorder();
          if (audioBlob) {
            const session = kioskService.getPersistedSession();
            if (session && session.sessionId) {
              try {
                const res = await voiceService.transcribeAudio(session.sessionId, audioBlob, language, session.token);
                if (res && res.data && res.data.transcript && res.data.status === 'SUCCESS') {
                  setComplaintText(res.data.transcript);
                  setVoiceError(null);
                  setVoiceState('REVIEW');
                  setConfirmedByPatient(false);
                  setInputMethod('VOICE');
                  return;
                } else if (res && res.data && res.data.status === 'NO_SPEECH_DETECTED') {
                  setVoiceError(t('noSpeechDetected'));
                  setVoiceState('ERROR');
                  return;
                }
              } catch {}
            }
          }
          setVoiceError(t('noSpeechDetected'));
          setVoiceState('ERROR');
        }
      };

      recognitionRef.current = recognition;
      recognition.start();
    } catch (err: any) {
      console.error('Failed to start speech recognition:', err);
      setVoiceError(t('voiceError'));
      setVoiceState('ERROR');
    }
  };

  const handleResetVoice = () => {
    if (recognitionRef.current) {
      try {
        recognitionRef.current.abort();
      } catch {
        // Ignore abort error
      }
      recognitionRef.current = null;
    }
    if (timerRef.current) clearInterval(timerRef.current);
    setVoiceState('IDLE');
    setAudioSeconds(0);
    setConfirmedByPatient(false);
    setComplaintText('');
    setVoiceError(null);
  };

  const handleSubmit = async () => {
    if (!complaintText.trim()) return;
    await onSubmit(complaintText.trim(), inputMethod);
  };

  const commonChips = [
    { key: 'chipChestPain', text: t('chipChestPain') },
    { key: 'chipHighFever', text: t('chipHighFever') },
    { key: 'chipHeadache', text: t('chipHeadache') },
    { key: 'chipBreathing', text: t('chipBreathing') },
    { key: 'chipStomach', text: t('chipStomach') },
    { key: 'chipDizziness', text: t('chipDizziness') },
  ];

  return (
    <div className="max-w-2xl mx-auto py-8 px-6 animate-fadeIn">
      <div className="mb-6 text-center">
        <h2 className="text-3xl font-extrabold text-white mb-2">{t('complaintHeader')}</h2>
        <p className="text-slate-300 text-sm">
          {t('complaintDescription')}
        </p>
      </div>

      <div className="bg-[#131936] rounded-3xl p-8 border border-[#AEA989]/40 shadow-xl space-y-6">
        {/* Voice Input Section */}
        <div className="p-6 rounded-2xl bg-[#0b0e1e] border border-[#AEA989]/30 flex flex-col items-center text-center">
          <div className="mb-3">
            <button
              type="button"
              onClick={handleToggleVoice}
              disabled={voiceState === 'PROCESSING'}
              className={`w-20 h-20 rounded-full flex items-center justify-center text-3xl shadow-lg transition-all active:scale-95 ${
                voiceState === 'LISTENING'
                  ? 'bg-rose-600 text-white animate-pulse ring-8 ring-rose-900/50'
                  : voiceState === 'PROCESSING'
                  ? 'bg-amber-500 text-white animate-spin'
                  : voiceState === 'ERROR'
                  ? 'bg-rose-950 hover:bg-rose-900 text-rose-200 border-2 border-rose-500 shadow-rose-900/40'
                  : voiceState === 'REVIEW'
                  ? 'bg-[#AEA989] text-[#131936] font-bold shadow-md'
                  : 'bg-[#AEA989] hover:bg-[#b8b394] text-[#131936] font-bold shadow-md'
              }`}
              title={
                voiceState === 'IDLE'
                  ? t('tapMicToSpeak')
                  : voiceState === 'LISTENING'
                  ? t('stopListening')
                  : voiceState === 'ERROR'
                  ? t('tryAgain')
                  : t('tapMicToSpeak')
              }
            >
              {voiceState === 'LISTENING'
                ? '⏹'
                : voiceState === 'PROCESSING'
                ? '⚙'
                : voiceState === 'ERROR'
                ? '⚠️'
                : voiceState === 'REVIEW'
                ? '✓'
                : '🎤'}
            </button>
          </div>

          <div className="font-bold text-white text-base mb-1">
            {voiceState === 'IDLE' && t('tapMicToSpeak')}
            {voiceState === 'LISTENING' && `${t('listeningSeconds')} (${audioSeconds}s)`}
            {voiceState === 'PROCESSING' && t('processingSpeech')}
            {voiceState === 'REVIEW' && t('transcriptionReady')}
            {voiceState === 'ERROR' && t('voiceOffline')}
          </div>

          <p className="text-xs text-slate-300 max-w-sm">
            {t('voiceInstructions')}
          </p>

          {voiceState === 'ERROR' && voiceError && (
            <div className="mt-3 text-xs font-medium text-rose-200 bg-rose-950/80 px-3.5 py-2 rounded-xl border border-rose-500/50 max-w-md">
              {voiceError}
            </div>
          )}
        </div>

        {/* Text Area & Confirmation */}
        <div>
          <div className="flex items-center justify-between mb-2">
            <label className="text-sm font-bold text-[#AEA989]">
              {t('primaryHealthConcern')} *
            </label>
            {voiceState === 'REVIEW' && (
              <span className="text-xs font-semibold px-2.5 py-0.5 rounded-full bg-[#AEA989]/20 text-[#AEA989] border border-[#AEA989]/40">
                {t('voiceTranscribedNotice')}
              </span>
            )}
          </div>

          <textarea
            rows={4}
            value={complaintText}
            onChange={(e) => {
              setComplaintText(e.target.value);
              setInputMethod('TEXT');
            }}
            placeholder={t('complaintPlaceholder')}
            className="w-full p-4 rounded-2xl bg-[#0b0e1e] border border-[#AEA989]/50 text-white placeholder-slate-400 text-lg focus:ring-2 focus:ring-[#AEA989] focus:border-[#AEA989] leading-relaxed"
          />
        </div>

        {/* Voice Confirmation Box if transcribed */}
        {voiceState === 'REVIEW' && (
          <div className="p-4 rounded-2xl bg-[#0b0e1e] border border-[#AEA989]/40 flex items-center justify-between gap-4">
            <div className="flex items-center gap-3">
              <input
                type="checkbox"
                id="voice-confirm"
                checked={confirmedByPatient}
                onChange={(e) => setConfirmedByPatient(e.target.checked)}
                className="w-5 h-5 accent-[#AEA989] rounded focus:ring-[#AEA989]"
              />
              <label htmlFor="voice-confirm" className="text-sm font-semibold text-[#AEA989] cursor-pointer">
                {t('confirmTextCheckbox')}
              </label>
            </div>
            <button
              type="button"
              onClick={handleResetVoice}
              className="text-xs text-[#AEA989] underline hover:text-white shrink-0 font-medium"
            >
              {t('tryAgain')}
            </button>
          </div>
        )}

        {/* Common Quick Complaints Chips */}
        <div>
          <span className="text-xs font-semibold text-[#AEA989] uppercase tracking-wider block mb-2">
            {t('commonConcernsHeader')}
          </span>
          <div className="flex flex-wrap gap-2">
            {commonChips.map((chip) => (
              <button
                type="button"
                key={chip.key}
                onClick={() => {
                  setComplaintText(chip.text);
                  setInputMethod('TEXT');
                  setConfirmedByPatient(true);
                }}
                className="text-xs font-semibold px-3 py-2 rounded-xl bg-[#0b0e1e] hover:bg-[#AEA989] hover:text-[#131936] text-slate-200 border border-[#AEA989]/30 transition-colors"
              >
                + {chip.text}
              </button>
            ))}
          </div>
        </div>

        {/* Voice Review Helper Message */}
        {voiceState === 'REVIEW' && !confirmedByPatient && (
          <div className="text-xs font-semibold text-[#AEA989] bg-[#0b0e1e] px-3 py-2 rounded-xl border border-[#AEA989]/40 flex items-center justify-center gap-1.5 text-center">
            <span>⚠️</span>
            <span>{t('confirmCheckNotice')}</span>
          </div>
        )}

        {/* Action Buttons */}
        <div className="pt-4 flex items-center justify-between gap-4 border-t border-slate-700/60">
          <KioskButton type="button" variant="outline" size="lg" onClick={onBack}>
            {t('back')}
          </KioskButton>

          <KioskButton
            type="button"
            variant="primary"
            size="lg"
            loading={loading}
            disabled={!complaintText.trim() || (voiceState === 'REVIEW' && !confirmedByPatient)}
            onClick={handleSubmit}
            className="px-8 font-bold"
          >
            {voiceState === 'REVIEW' ? `${t('confirmContinue')} →` : `${t('continueToQuestions')} →`}
          </KioskButton>
        </div>
      </div>
    </div>
  );
};
