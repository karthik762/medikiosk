import React, { useState, useRef, useEffect } from 'react';
import { KioskButton } from './KioskButton';
import { NextQuestionData, kioskService } from '../../services/kioskService';
import { voiceService } from '../../services/voiceService';
import { getTranslation } from '../../i18n/translations';

export interface UniversalAnswerInputProps {
  question: NextQuestionData;
  onAnswer: (answerText: string, inputMethod: 'TOUCH' | 'TEXT' | 'VOICE') => Promise<void>;
  loading?: boolean;
  language?: 'en' | 'hi' | 'te';
}

export const UniversalAnswerInput: React.FC<UniversalAnswerInputProps> = ({
  question,
  onAnswer,
  loading = false,
  language = 'en',
}) => {
  const t = (key: Parameters<typeof getTranslation>[1]) => getTranslation(language, key);
  const [answerText, setAnswerText] = useState<string>('');
  const [voiceState, setVoiceState] = useState<'IDLE' | 'LISTENING' | 'PROCESSING' | 'REVIEW' | 'ERROR'>('IDLE');
  const [voiceError, setVoiceError] = useState<string | null>(null);
  const [audioSeconds, setAudioSeconds] = useState<number>(0);
  const [inputMethod, setInputMethod] = useState<'TOUCH' | 'TEXT' | 'VOICE'>('TOUCH');
  const [showTypeArea, setShowTypeArea] = useState<boolean>(false);

  const recognitionRef = useRef<any>(null);
  const mediaRecorderRef = useRef<any>(null);
  const audioChunksRef = useRef<Blob[]>([]);
  const activeStreamRef = useRef<MediaStream | null>(null);
  const timerRef = useRef<any>(null);

  // Reset internal state on question change
  useEffect(() => {
    setAnswerText('');
    setVoiceState('IDLE');
    setVoiceError(null);
    setAudioSeconds(0);
    setInputMethod('TOUCH');
    setShowTypeArea(false);
  }, [question.questionId, (question as any).id, question.questionText]);

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
      if (timerRef.current) clearInterval(timerRef.current);
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

  // ----------------------------------------------------
  // TIER 1: 🎤 VOICE RECOGNITION (STT)
  // ----------------------------------------------------
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
      if (!answerText.trim() && audioBlob) {
        const session = kioskService.getPersistedSession();
        if (session && session.sessionId) {
          try {
            const res = await voiceService.transcribeAudio(session.sessionId, audioBlob, language, session.token);
            if (res && res.data && res.data.transcript && res.data.status === 'SUCCESS') {
              setAnswerText(res.data.transcript);
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
            console.warn('Backend audio transcription error on stop:', err);
          }
        }
      }
      if (answerText.trim()) {
        setVoiceState('REVIEW');
      } else {
        setVoiceError(t('noSpeechDetected'));
        setVoiceState('ERROR');
      }
      return;
    }

    // Explicitly request microphone permission before speech recognition if mediaDevices is supported
    let stream: MediaStream | null = null;
    if (navigator.mediaDevices && typeof navigator.mediaDevices.getUserMedia === 'function') {
      try {
        stream = await navigator.mediaDevices.getUserMedia({ audio: true });
        activeStreamRef.current = stream;
      } catch (err: any) {
        console.warn('Microphone permission / device error:', err);
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

    const SpeechRecognition = (window as any).SpeechRecognition || (window as any).webkitSpeechRecognition;

    if (!SpeechRecognition) {
      if (typeof MediaRecorder === 'undefined' || !stream) {
        setVoiceError(t('voiceError'));
        setVoiceState('ERROR');
        return;
      }
      // If Web Speech API is missing but MediaRecorder is supported, we record via MediaRecorder and transcribe on stop
      setVoiceState('LISTENING');
      setVoiceError(null);
      setAudioSeconds(0);
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
        setVoiceError(null);
        setAudioSeconds(0);
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
          setAnswerText(fullTranscript);
        }
      };

      recognition.onerror = async (event: any) => {
        console.warn('UniversalAnswerInput STT error:', event.error);
        if (timerRef.current) clearInterval(timerRef.current);
        if (event.error === 'aborted') {
          return;
        }

        // If network error (Chrome cloud STT failed), attempt backend audio transcription fallback!
        if (event.error === 'network') {
          setVoiceState('PROCESSING');
          const audioBlob = await stopStreamAndRecorder();
          if (audioBlob) {
            const session = kioskService.getPersistedSession();
            if (session && session.sessionId) {
              try {
                const res = await voiceService.transcribeAudio(session.sessionId, audioBlob, language, session.token);
                if (res && res.data && res.data.transcript) {
                  setAnswerText(res.data.transcript);
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
          stopStreamAndRecorder();
        } else {
          const audioBlob = await stopStreamAndRecorder();
          if (audioBlob) {
            const session = kioskService.getPersistedSession();
            if (session && session.sessionId) {
              try {
                const res = await voiceService.transcribeAudio(session.sessionId, audioBlob, language, session.token);
                if (res && res.data && res.data.transcript && res.data.status === 'SUCCESS') {
                  setAnswerText(res.data.transcript);
                  setVoiceError(null);
                  setVoiceState('REVIEW');
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

  // ----------------------------------------------------
  // TIER 2: 👆 TOUCH OPTIONS DERIVATION
  // ----------------------------------------------------
  const deriveTouchOptions = (): string[] | null => {
    let rawOptions: string[] | null = null;
    if (question.options && question.options.length > 0) {
      rawOptions = [...question.options];
    } else {
      const text = (question.questionText || '').toLowerCase();
      const category = (question.category || '').toLowerCase();

      if (
        question.questionType === 'YES_NO' ||
        text.includes('do you') ||
        text.includes('have you') ||
        text.includes('is there') ||
        text.includes('are you')
      ) {
        rawOptions = language === 'te' ? ['అవును', 'లేదు'] : language === 'hi' ? ['हाँ', 'नहीं'] : ['YES', 'NO'];
      } else if (
        question.questionType === 'SCALE_0_10' ||
        text.includes('severe') ||
        category.includes('severity')
      ) {
        rawOptions = language === 'te'
          ? ['తేలికపాటి', 'మధ్యస్థమైన', 'తీవ్రమైన']
          : language === 'hi'
          ? ['हल्का', 'मध्यम', 'गंभीर']
          : ['Mild', 'Moderate', 'Severe'];
      }
    }

    if (!rawOptions) return null;

    // Ensure Not Sure option is present
    const notSureLabel = t('notSure');
    if (!rawOptions.includes(notSureLabel)) {
      rawOptions.push(notSureLabel);
    }
    return rawOptions;
  };

  const touchOptions = deriveTouchOptions();

  const handleSelectOption = (opt: string) => {
    setAnswerText(opt);
    setInputMethod('TOUCH');
  };

  const handleSubmit = async () => {
    if (!answerText.trim()) return;
    await onAnswer(answerText.trim(), inputMethod);
  };

  return (
    <div className="space-y-8 animate-fadeIn">
      {/* TIER 1: 🎤 SPEAK */}
      <div className="p-6 rounded-3xl bg-[#0b0e1e] border-2 border-[#AEA989]/40 flex flex-col items-center text-center shadow-lg">
        <div className="mb-3">
          <button
            type="button"
            disabled={loading || voiceState === 'PROCESSING'}
            onClick={handleToggleVoice}
            aria-label="Speak your response"
            className={`w-24 h-24 rounded-full flex flex-col items-center justify-center shadow-xl transition-all transform active:scale-95 ${
              voiceState === 'LISTENING'
                ? 'bg-rose-600 text-white animate-pulse ring-8 ring-rose-900/50 scale-105'
                : voiceState === 'PROCESSING'
                ? 'bg-amber-500 text-white animate-spin'
                : voiceState === 'REVIEW'
                ? 'bg-[#AEA989] text-[#131936] font-bold shadow-md'
                : voiceState === 'ERROR'
                ? 'bg-rose-950 text-rose-200 border-2 border-rose-500 shadow-rose-900/40'
                : 'bg-[#AEA989] hover:bg-[#b8b394] text-[#131936] font-bold shadow-md hover:scale-105'
            }`}
          >
            <span className="text-4xl">
              {voiceState === 'LISTENING'
                ? '⏹'
                : voiceState === 'PROCESSING'
                ? '⚙'
                : voiceState === 'REVIEW'
                ? '✓'
                : voiceState === 'ERROR'
                ? '⚠️'
                : '🎤'}
            </span>
          </button>
        </div>

        <div className="font-extrabold text-white text-lg mb-1">
          {voiceState === 'IDLE' && `🎤 ${t('tapToSpeak')}`}
          {voiceState === 'LISTENING' && `🔴 ${t('voiceListening')} (${audioSeconds}s)`}
          {voiceState === 'PROCESSING' && `⏳ ${t('voiceUnderstanding')}`}
          {voiceState === 'REVIEW' && `✓ ${t('voiceCaptured')}`}
          {voiceState === 'ERROR' && (voiceError || t('voiceError'))}
        </div>

        <p className="text-xs text-slate-300 max-w-md">
          {voiceState === 'IDLE' && t('speakNaturallyInstruction')}
          {voiceState === 'LISTENING' && t('speakNowInstruction')}
          {voiceState === 'REVIEW' && t('reviewVoiceInstruction')}
          {voiceState === 'ERROR' && (voiceError || t('voiceError'))}
        </p>

        {voiceState === 'ERROR' && voiceError && (
          <div className="mt-3 text-xs font-semibold text-rose-200 bg-rose-950/80 px-3.5 py-2 rounded-xl border border-rose-500/50 max-w-md flex flex-col items-center gap-2">
            <span>{voiceError}</span>
            <button
              type="button"
              onClick={handleToggleVoice}
              className="mt-1 px-3 py-1 bg-[#AEA989] hover:bg-[#b8b394] text-[#131936] font-bold rounded-lg text-xs"
            >
              🔄 {t('retryMicrophone')}
            </button>
          </div>
        )}

        {voiceState === 'REVIEW' && (
          <div className="mt-3 flex items-center gap-2">
            <button
              type="button"
              onClick={handleToggleVoice}
              className="px-3.5 py-1.5 bg-[#AEA989]/20 hover:bg-[#AEA989]/30 text-[#AEA989] border border-[#AEA989]/40 font-bold rounded-xl text-xs flex items-center gap-1.5"
            >
              <span>🔄</span>
              <span>{t('retryMicrophone')}</span>
            </button>
          </div>
        )}
      </div>

      {/* TIER 2: 👆 TAP OPTIONS */}
      {touchOptions && touchOptions.length > 0 && (
        <div className="space-y-4">
          <div className="flex items-center justify-between">
            <span className="text-sm font-extrabold text-[#AEA989] uppercase tracking-wider flex items-center gap-1.5">
              <span>👆</span>
              <span>{t('touchOptionsShortcut')}</span>
            </span>
            {answerText && inputMethod === 'TOUCH' && (
              <span className="text-xs font-bold text-[#131936] bg-[#AEA989] px-3 py-1 rounded-full shadow-xs">
                {answerText}
              </span>
            )}
          </div>

          {question.questionType === 'SCALE_0_10' ? (
            <div className="grid grid-cols-6 sm:grid-cols-11 gap-2">
              {['0', '1', '2', '3', '4', '5', '6', '7', '8', '9', '10'].map((num) => {
                const isSelected = answerText === num;
                return (
                  <button
                    key={num}
                    type="button"
                    disabled={loading}
                    onClick={() => handleSelectOption(num)}
                    className={`h-16 rounded-2xl font-black text-xl flex flex-col items-center justify-center transition-all border ${
                      isSelected
                        ? 'bg-[#AEA989] text-[#131936] border-[#AEA989] shadow-md ring-4 ring-[#AEA989]/40 scale-105'
                        : 'bg-[#0b0e1e] border-[#AEA989]/30 text-white hover:border-[#AEA989] hover:bg-[#1a2147] active:scale-95'
                    }`}
                  >
                    <span>{num}</span>
                  </button>
                );
              })}
            </div>
          ) : (
            <div className="grid grid-cols-2 sm:grid-cols-3 gap-3">
              {touchOptions.map((opt) => {
                const isSelected = answerText === opt;
                return (
                  <button
                    key={opt}
                    type="button"
                    disabled={loading}
                    onClick={() => handleSelectOption(opt)}
                    className={`p-5 rounded-2xl border-2 text-center font-bold text-lg sm:text-xl flex items-center justify-center transition-all active:scale-98 min-h-[64px] ${
                      isSelected
                        ? 'bg-[#AEA989] border-[#AEA989] text-[#131936] shadow-lg ring-4 ring-[#AEA989]/40 scale-[1.02]'
                        : 'bg-[#0b0e1e] border-[#AEA989]/30 hover:border-[#AEA989] hover:bg-[#1a2147] text-white'
                    }`}
                  >
                    <span>{opt}</span>
                  </button>
                );
              })}
            </div>
          )}
        </div>
      )}

      {/* TIER 3: ⌨️ TYPE FALLBACK */}
      <div className="space-y-3">
        <input
          type="text"
          value={answerText}
          onChange={(e) => {
            setAnswerText(e.target.value);
            if (voiceState !== 'REVIEW') {
              setInputMethod('TEXT');
            }
          }}
          placeholder={t('typePlaceholder')}
          className="w-full p-4 rounded-2xl bg-[#0b0e1e] border border-[#AEA989]/50 text-white placeholder-slate-400 text-lg font-semibold focus:ring-2 focus:ring-[#AEA989] focus:border-[#AEA989]"
        />

        <div>
          <button
            type="button"
            onClick={() => setShowTypeArea(!showTypeArea)}
            className="text-xs font-bold text-[#AEA989] hover:text-white flex items-center gap-1.5 pt-1"
          >
            <span>⌨️</span>
            <span>{showTypeArea ? t('hideTypingBox') : t('typeCustomDetails')}</span>
          </button>

          {showTypeArea && (
            <div className="mt-3 p-4 bg-[#0b0e1e] rounded-2xl border border-[#AEA989]/30 space-y-3 animate-fadeIn">
              <textarea
                rows={3}
                value={answerText}
                onChange={(e) => {
                  setAnswerText(e.target.value);
                  if (voiceState !== 'REVIEW') {
                    setInputMethod('TEXT');
                  }
                }}
                placeholder={t('typePlaceholder')}
                className="w-full p-3 rounded-xl bg-[#131936] border border-[#AEA989]/50 text-white text-base focus:ring-2 focus:ring-[#AEA989]"
              />
            </div>
          )}
        </div>
      </div>

      {/* CONFIRM & SUBMIT BUTTON */}
      <div className="pt-4 flex items-center justify-between gap-4 border-t border-slate-700/60">
        <button
          type="button"
          onClick={() => handleSelectOption(t('notSure'))}
          className="text-xs font-bold text-[#AEA989] hover:text-white bg-[#0b0e1e] hover:bg-[#1a2147] px-3.5 py-2 rounded-xl border border-[#AEA989]/30 transition-colors"
        >
          ❓ {t('notSure')}
        </button>

        <KioskButton
          type="button"
          variant="primary"
          size="lg"
          loading={loading}
          disabled={!answerText.trim()}
          onClick={handleSubmit}
          className="px-8 font-extrabold text-lg py-4"
        >
          {t('confirmContinue')} →
        </KioskButton>
      </div>
    </div>
  );
};
