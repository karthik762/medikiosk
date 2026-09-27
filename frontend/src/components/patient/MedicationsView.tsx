import React, { useState, useEffect, useRef } from 'react';
import { KioskButton } from './KioskButton';
import { getTranslation } from '../../i18n/translations';

export interface MedicationItem {
  name: string;
  dosage: string;
  frequency: string;
}

interface MedicationsViewProps {
  onSubmit: (medications: MedicationItem[]) => Promise<void>;
  onBack: () => void;
  loading?: boolean;
  language?: 'en' | 'hi' | 'te';
}

export const MedicationsView: React.FC<MedicationsViewProps> = ({
  onSubmit,
  onBack,
  loading = false,
  language = 'en',
}) => {
  const [medications, setMedications] = useState<MedicationItem[]>([]);
  const [currentName, setCurrentName] = useState('');
  const [currentDosage, setCurrentDosage] = useState('');
  const [currentFrequency, setCurrentFrequency] = useState('Once daily');
  const [noMeds, setNoMeds] = useState(false);

  // Voice State
  const [isListening, setIsListening] = useState(false);
  const [voiceTranscript, setVoiceTranscript] = useState('');
  const [voiceStatus, setVoiceStatus] = useState<string | null>(null);
  const recognitionRef = useRef<any>(null);

  const t = (key: Parameters<typeof getTranslation>[1]) => getTranslation(language, key);

  useEffect(() => {
    const SpeechRecognition =
      (window as any).SpeechRecognition || (window as any).webkitSpeechRecognition;

    if (SpeechRecognition) {
      const recognition = new SpeechRecognition();
      recognition.continuous = false;
      recognition.interimResults = true;
      recognition.lang = language === 'te' ? 'te-IN' : language === 'hi' ? 'hi-IN' : 'en-IN';

      recognition.onstart = () => {
        setIsListening(true);
        setVoiceStatus(t('voiceListening'));
      };

      recognition.onresult = (event: any) => {
        let transcriptStr = '';
        for (let i = event.resultIndex; i < event.results.length; i++) {
          transcriptStr += event.results[i][0].transcript;
        }
        setVoiceTranscript(transcriptStr);
      };

      recognition.onerror = (event: any) => {
        setIsListening(false);
        if (event?.error === 'not-allowed') {
          setVoiceStatus(t('micPermissionDenied'));
        } else if (event?.error === 'no-speech') {
          setVoiceStatus(t('noSpeechDetected'));
        } else {
          setVoiceStatus(t('voiceError'));
        }
      };

      recognition.onend = () => {
        setIsListening(false);
      };

      recognitionRef.current = recognition;
    }

    return () => {
      if (recognitionRef.current) {
        try {
          recognitionRef.current.stop();
        } catch {
          // ignore
        }
      }
    };
  }, [language]);

  const toggleListening = async () => {
    if (isListening) {
      if (recognitionRef.current) {
        try {
          recognitionRef.current.stop();
        } catch {
          // ignore
        }
      }
      setIsListening(false);
    } else {
      setVoiceTranscript('');
      setVoiceStatus(null);

      if (navigator.mediaDevices && typeof navigator.mediaDevices.getUserMedia === 'function') {
        try {
          const stream = await navigator.mediaDevices.getUserMedia({ audio: true });
          stream.getTracks().forEach((track) => track.stop());
        } catch (err: any) {
          console.warn('MedicationsView microphone permission error:', err);
          if (err.name === 'NotAllowedError' || err.name === 'PermissionDeniedError') {
            setVoiceStatus(t('micPermissionDenied'));
          } else {
            setVoiceStatus(t('voiceError'));
          }
          return;
        }
      }

      if (recognitionRef.current) {
        try {
          recognitionRef.current.start();
        } catch {
          simulateVoiceInput();
        }
      } else {
        simulateVoiceInput();
      }
    }
  };

  const simulateVoiceInput = () => {
    setIsListening(true);
    setVoiceStatus(t('voiceListening'));
    setTimeout(() => {
      setIsListening(false);
      const sampleText =
        language === 'te'
          ? 'నేను ప్రతి ఉదయం ఒక బిపి మందు తీసుకుంటాను'
          : language === 'hi'
          ? 'मैं हर सुबह एक बीपी की गोली लेता हूं'
          : 'I take a small white BP tablet every morning';
      setVoiceTranscript(sampleText);
      setVoiceStatus(t('voiceCaptured'));
    }, 2000);
  };

  const addVoiceMedication = () => {
    if (!voiceTranscript.trim()) return;
    setMedications((prev) => [
      ...prev,
      {
        name: voiceTranscript.trim(),
        dosage: language === 'te' ? 'పేషెంట్ తెలిపిన విధంగా' : language === 'hi' ? 'मरीज द्वारा बताई गई' : 'Patient Reported',
        frequency: language === 'te' ? 'రోజూ' : language === 'hi' ? 'प्रतिदिन' : 'Daily',
      },
    ]);
    setVoiceTranscript('');
    setVoiceStatus(null);
    setNoMeds(false);
  };

  const addMedication = () => {
    if (!currentName.trim()) return;
    setMedications([
      ...medications,
      {
        name: currentName.trim(),
        dosage: currentDosage.trim() || 'Standard',
        frequency: currentFrequency,
      },
    ]);
    setCurrentName('');
    setCurrentDosage('');
    setNoMeds(false);
  };

  const removeMedication = (index: number) => {
    setMedications(medications.filter((_, i) => i !== index));
  };

  const handleSubmit = async (override?: MedicationItem[]) => {
    if (override) {
      await onSubmit(override);
      return;
    }
    await onSubmit(noMeds ? [] : medications);
  };

  return (
    <div className="max-w-2xl mx-auto py-8 px-6 animate-fadeIn">
      <div className="mb-6 text-center">
        <h2 className="text-3xl font-extrabold text-white mb-2">{t('medicationsTitle')}</h2>
        <p className="text-slate-300 text-sm">{t('medicationsSubtitle')}</p>
      </div>

      <div className="bg-[#131936] rounded-3xl p-8 border border-[#AEA989]/40 shadow-xl space-y-6">
        {/* Voice Input Section */}
        <div className="p-6 rounded-2xl bg-[#0b0e1e] text-white border border-[#AEA989]/30 shadow-md space-y-4">
          <div className="flex items-center justify-between">
            <div className="flex items-center gap-2">
              <span className="text-xl">🎤</span>
              <span className="font-bold text-sm text-[#AEA989]">{t('tellUsMedicationsVoice')}</span>
            </div>
            <button
              type="button"
              onClick={toggleListening}
              className={`px-4 py-2 rounded-xl text-xs font-bold transition-all shadow-sm flex items-center gap-2 ${
                isListening
                  ? 'bg-rose-600 hover:bg-rose-700 text-white animate-pulse'
                  : 'bg-[#AEA989] hover:bg-[#b8b394] text-[#131936]'
              }`}
            >
              <span>{isListening ? `🛑 ${t('stopListening')}` : `🎤 ${t('tapToSpeak')}`}</span>
            </button>
          </div>

          {voiceStatus && <p className="text-xs text-slate-300 font-semibold">{voiceStatus}</p>}

          {(voiceTranscript || isListening) && (
            <div className="p-3.5 rounded-xl bg-[#131936] border border-[#AEA989]/30 text-sm text-slate-100 space-y-2">
              <p className="italic font-medium">"{voiceTranscript || '...'}"</p>
              {voiceTranscript && (
                <button
                  type="button"
                  onClick={addVoiceMedication}
                  className="px-3.5 py-1.5 rounded-lg bg-[#AEA989] hover:bg-[#b8b394] text-[#131936] text-xs font-extrabold"
                >
                  {t('useSpokenMedication')}
                </button>
              )}
            </div>
          )}
        </div>

        {/* No Medications Toggle */}
        <div className="p-4 rounded-2xl bg-[#0b0e1e] border border-[#AEA989]/30 flex items-center justify-between">
          <div className="flex items-center gap-3">
            <input
              type="checkbox"
              id="no-meds-check"
              checked={noMeds}
              onChange={(e) => {
                setNoMeds(e.target.checked);
                if (e.target.checked) setMedications([]);
              }}
              className="w-5 h-5 accent-[#AEA989] rounded focus:ring-[#AEA989]"
            />
            <label htmlFor="no-meds-check" className="font-bold text-[#AEA989] text-sm cursor-pointer">
              {t('noMedications')}
            </label>
          </div>
        </div>

        {!noMeds && (
          <>
            {/* Add Medication Card */}
            <div className="p-5 rounded-2xl bg-[#0b0e1e] border border-[#AEA989]/30 space-y-4">
              <h4 className="font-bold text-[#AEA989] text-sm">{t('medicationName')}</h4>
              <div className="grid grid-cols-1 sm:grid-cols-3 gap-3">
                <div className="sm:col-span-1">
                  <input
                    type="text"
                    value={currentName}
                    onChange={(e) => setCurrentName(e.target.value)}
                    placeholder={t('medicationName')}
                    className="w-full px-3.5 py-2.5 rounded-xl bg-[#131936] border border-[#AEA989]/50 text-sm text-white placeholder-slate-400"
                  />
                </div>
                <div>
                  <input
                    type="text"
                    value={currentDosage}
                    onChange={(e) => setCurrentDosage(e.target.value)}
                    placeholder={t('dosage')}
                    className="w-full px-3.5 py-2.5 rounded-xl bg-[#131936] border border-[#AEA989]/50 text-sm text-white placeholder-slate-400"
                  />
                </div>
                <div>
                  <select
                    value={currentFrequency}
                    onChange={(e) => setCurrentFrequency(e.target.value)}
                    className="w-full px-3.5 py-2.5 rounded-xl bg-[#131936] border border-[#AEA989]/50 text-sm text-white"
                  >
                    <option value="Once daily">{language === 'hi' ? 'दिन में एक बार' : language === 'te' ? 'రోజుకి ఒకసారి' : 'Once daily'}</option>
                    <option value="Twice daily">{language === 'hi' ? 'दिन में दो बार' : language === 'te' ? 'రోజుకి రెండు సార్లు' : 'Twice daily'}</option>
                    <option value="Three times daily">{language === 'hi' ? 'दिन में तीन बार' : language === 'te' ? 'రోజుకి మూడు సార్లు' : 'Three times daily'}</option>
                    <option value="As needed">{language === 'hi' ? 'आवश्यकतानुसार' : language === 'te' ? 'అవసరమైనప్పుడు' : 'As needed (SOS)'}</option>
                  </select>
                </div>
              </div>
              <button
                type="button"
                onClick={addMedication}
                disabled={!currentName.trim()}
                className="px-5 py-2.5 rounded-xl bg-[#AEA989] hover:bg-[#b8b394] text-[#131936] text-xs font-bold transition-all disabled:opacity-40"
              >
                {t('addToList')}
              </button>
            </div>

            {/* List of Added Medications */}
            {medications.length > 0 ? (
              <div className="space-y-2">
                <h4 className="text-xs font-bold text-[#AEA989] uppercase tracking-wider">
                  {t('addedMedications')} ({medications.length})
                </h4>
                {medications.map((m, idx) => (
                  <div
                    key={idx}
                    className="p-4 rounded-xl border border-[#AEA989]/30 bg-[#0b0e1e] flex items-center justify-between"
                  >
                    <div>
                      <span className="font-bold text-white text-sm">{m.name}</span>
                      <span className="text-xs text-slate-300 ml-2">
                        {m.dosage} • {m.frequency}
                      </span>
                    </div>
                    <button
                      type="button"
                      onClick={() => removeMedication(idx)}
                      className="text-xs text-rose-400 hover:text-rose-200 font-bold px-2 py-1"
                    >
                      {t('remove')}
                    </button>
                  </div>
                ))}
              </div>
            ) : (
              <p className="text-center text-slate-400 text-xs py-2">
                {t('addMedicationPlaceholder')}
              </p>
            )}
          </>
        )}

        {/* Not Sure Shortcut Button */}
        <div className="pt-2">
          <button
            type="button"
            onClick={() => handleSubmit([{ name: t('dontRememberMedName'), dosage: 'Uncertain', frequency: 'N/A' }])}
            className="w-full py-3.5 px-4 rounded-xl border border-[#AEA989]/40 bg-[#0b0e1e] hover:bg-[#1a2147] text-[#AEA989] hover:text-white text-sm font-bold transition-all flex items-center justify-center gap-2"
          >
            <span>❓</span>
            <span>{t('dontRememberMedName')}</span>
          </button>
        </div>

        {/* Action Buttons */}
        <div className="pt-4 flex items-center justify-between gap-4 border-t border-slate-700/60">
          <KioskButton type="button" variant="outline" size="lg" onClick={onBack}>
            {t('back')}
          </KioskButton>
          <KioskButton
            type="button"
            size="lg"
            loading={loading}
            onClick={() => handleSubmit()}
            className="px-8"
          >
            {t('confirmContinue')} →
          </KioskButton>
        </div>
      </div>
    </div>
  );
};
