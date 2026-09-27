import React, { useState } from 'react';
import { NextQuestionData } from '../../services/kioskService';
import { UniversalAnswerInput } from './UniversalAnswerInput';
import { getTranslation } from '../../i18n/translations';

interface ConversationTurnItem {
  questionText: string;
  answerText: string;
}

interface AdaptiveQuestionViewProps {
  question: NextQuestionData;
  onSubmitAnswer: (answerText: string, inputMethod: 'TOUCH' | 'TEXT' | 'VOICE') => Promise<void>;
  onSkip?: () => void;
  loading?: boolean;
  history?: ConversationTurnItem[];
  language?: 'en' | 'hi' | 'te';
}

export const AdaptiveQuestionView: React.FC<AdaptiveQuestionViewProps> = ({
  question,
  onSubmitAnswer,
  onSkip,
  loading = false,
  history = [],
  language = 'en',
}) => {
  const [isSpeakingQuestion, setIsSpeakingQuestion] = useState<boolean>(false);
  const [showHistory, setShowHistory] = useState<boolean>(false);
  const t = (key: Parameters<typeof getTranslation>[1]) => getTranslation(language, key);

  // Text-to-Speech (TTS) for low literacy users
  const speakQuestion = () => {
    if (!('speechSynthesis' in window)) return;
    if (isSpeakingQuestion) {
      window.speechSynthesis.cancel();
      setIsSpeakingQuestion(false);
      return;
    }
    window.speechSynthesis.cancel();
    const utterance = new SpeechSynthesisUtterance(question.questionText);
    utterance.rate = 0.95;
    utterance.pitch = 1.0;
    utterance.lang = language === 'hi' ? 'hi-IN' : language === 'te' ? 'te-IN' : 'en-US';
    utterance.onstart = () => setIsSpeakingQuestion(true);
    utterance.onend = () => setIsSpeakingQuestion(false);
    utterance.onerror = () => setIsSpeakingQuestion(false);
    window.speechSynthesis.speak(utterance);
  };

  return (
    <div className="max-w-3xl mx-auto py-6 px-4 animate-fadeIn space-y-6">
      {/* Stage Badge & TTS Reader Header */}
      <div className="flex flex-wrap items-center justify-between gap-3 bg-[#131936] backdrop-blur-md p-3.5 rounded-2xl border border-[#AEA989]/40 shadow-md">
        <div className="flex flex-wrap items-center gap-2">
          <span className="px-3 py-1 rounded-full text-xs font-extrabold bg-[#AEA989] text-[#131936] shadow-xs">
            {question.progressStage || t('clinicalQuestionStage')}
          </span>
          <span className="px-2.5 py-0.5 rounded text-[11px] font-bold bg-[#0b0e1e] text-[#AEA989] border border-[#AEA989]/30">
            {t('universalInputEnabled')}
          </span>
        </div>

        <div className="flex items-center gap-2">
          <button
            type="button"
            onClick={speakQuestion}
            className={`px-3.5 py-1.5 rounded-xl text-xs font-bold flex items-center gap-1.5 transition-all shadow-xs border ${
              isSpeakingQuestion
                ? 'bg-rose-600 text-white border-rose-600 animate-pulse'
                : 'bg-[#0b0e1e] text-[#AEA989] hover:bg-[#AEA989] hover:text-[#131936] border-[#AEA989]/30'
            }`}
          >
            <span>{isSpeakingQuestion ? `🔊 ${t('speakingState')}` : `🔈 ${t('readAloud')}`}</span>
          </button>

          {onSkip && (
            <button
              type="button"
              onClick={onSkip}
              className="text-xs font-semibold text-[#AEA989] hover:text-white px-2 py-1"
            >
              {t('skip')} →
            </button>
          )}
        </div>
      </div>

      {/* Optional Conversation History */}
      {history.length > 0 && (
        <div className="bg-[#131936] rounded-2xl p-3.5 border border-[#AEA989]/30 text-xs">
          <div className="flex items-center justify-between font-bold text-[#AEA989] mb-1">
            <span>💬 {t('intakeHistoryTitle')} ({history.length})</span>
            <button
              type="button"
              onClick={() => setShowHistory(!showHistory)}
              className="text-[#AEA989] underline hover:text-white"
            >
              {showHistory ? t('hide') : t('show')}
            </button>
          </div>
          {showHistory && (
            <div className="space-y-2 mt-2 max-h-36 overflow-y-auto pr-1">
              {history.map((turn, i) => (
                <div key={i} className="bg-[#0b0e1e] p-2.5 rounded-xl border border-[#AEA989]/20">
                  <span className="font-bold text-white block">Q: {turn.questionText}</span>
                  <div className="text-[#AEA989] font-semibold mt-0.5">A: "{turn.answerText}"</div>
                </div>
              ))}
            </div>
          )}
        </div>
      )}

      {/* Main Question Container */}
      <div className="bg-[#131936] rounded-3xl p-6 sm:p-8 border border-[#AEA989]/40 shadow-xl space-y-8">
        {/* Question Text */}
        <div className="text-center sm:text-left">
          <h2 className="text-2xl sm:text-3xl font-extrabold text-white leading-snug tracking-tight">
            {question.questionText}
          </h2>
          <p className="text-xs text-slate-300 mt-2 font-medium">
            {t('questionInstruction')}
          </p>
        </div>

        {/* SINGLE UNIVERSAL ANSWER INPUT COMPONENT FOR ALL QUESTIONS */}
        <UniversalAnswerInput
          question={question}
          onAnswer={onSubmitAnswer}
          loading={loading}
          language={language}
        />
      </div>
    </div>
  );
};
