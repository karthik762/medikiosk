import { describe, it, expect, vi, beforeEach, afterEach } from 'vitest';
import { render, screen, fireEvent, act } from '@testing-library/react';
import { UniversalAnswerInput } from './UniversalAnswerInput';
import { NextQuestionData } from '../../services/kioskService';

const mockQuestion: NextQuestionData = {
  questionId: 'Q-101',
  questionCode: 'EYE_PAIN',
  questionText: 'Are you experiencing any eye pain or pressure?',
  category: 'CHIEF_COMPLAINT_EXPLORATION',
  questionType: 'YES_NO',
  safetyCritical: false,
  priority: 1,
  completed: false,
  options: ['YES', 'NO'],
  progressStage: 'CHIEF_COMPLAINT',
};

class MockSpeechRecognition {
  continuous = false;
  interimResults = false;
  lang = 'en-US';
  onstart: (() => void) | null = null;
  onresult: ((event: any) => void) | null = null;
  onerror: ((event: any) => void) | null = null;
  onend: (() => void) | null = null;

  start = vi.fn(() => {
    if (this.onstart) this.onstart();
  });

  stop = vi.fn(() => {
    if (this.onend) this.onend();
  });

  abort = vi.fn(() => {
    if (this.onend) this.onend();
  });
}

describe('UniversalAnswerInput — Voice Input Requirements Suite', () => {
  let originalSpeechRecognition: any;
  let originalWebkitSpeechRecognition: any;
  let originalMediaDevices: any;

  beforeEach(() => {
    originalSpeechRecognition = (window as any).SpeechRecognition;
    originalWebkitSpeechRecognition = (window as any).webkitSpeechRecognition;
    originalMediaDevices = navigator.mediaDevices;

    delete (window as any).SpeechRecognition;
    delete (window as any).webkitSpeechRecognition;

    // Default mock mediaDevices.getUserMedia
    Object.defineProperty(navigator, 'mediaDevices', {
      writable: true,
      configurable: true,
      value: {
        getUserMedia: vi.fn().mockResolvedValue({
          getTracks: () => [{ stop: vi.fn() }],
        }),
      },
    });
  });

  afterEach(() => {
    (window as any).SpeechRecognition = originalSpeechRecognition;
    (window as any).webkitSpeechRecognition = originalWebkitSpeechRecognition;
    Object.defineProperty(navigator, 'mediaDevices', {
      writable: true,
      configurable: true,
      value: originalMediaDevices,
    });
    vi.restoreAllMocks();
  });

  it('1. Handles browser supporting standard window.SpeechRecognition', async () => {
    (window as any).SpeechRecognition = MockSpeechRecognition;

    render(<UniversalAnswerInput question={mockQuestion} onAnswer={vi.fn()} />);

    const speakBtn = screen.getByLabelText(/speak your response/i);
    expect(speakBtn).toBeInTheDocument();

    await act(async () => {
      fireEvent.click(speakBtn);
    });

    expect(navigator.mediaDevices.getUserMedia).toHaveBeenCalledWith({ audio: true });
    expect(screen.getByText(/listening to your voice/i)).toBeInTheDocument();
  });

  it('2. Handles browser supporting window.webkitSpeechRecognition (Chrome default)', async () => {
    (window as any).webkitSpeechRecognition = MockSpeechRecognition;

    render(<UniversalAnswerInput question={mockQuestion} onAnswer={vi.fn()} />);

    const speakBtn = screen.getByLabelText(/speak your response/i);

    await act(async () => {
      fireEvent.click(speakBtn);
    });

    expect(navigator.mediaDevices.getUserMedia).toHaveBeenCalledWith({ audio: true });
    expect(screen.getByText(/listening to your voice/i)).toBeInTheDocument();
  });

  it('3. Provides fallback behavior when SpeechRecognition is unsupported', async () => {
    delete (window as any).MediaRecorder;
    render(<UniversalAnswerInput question={mockQuestion} onAnswer={vi.fn()} />);

    const speakBtn = screen.getByLabelText(/speak your response/i);

    await act(async () => {
      fireEvent.click(speakBtn);
    });

    expect(screen.getAllByText(/voice input unavailable/i)[0]).toBeInTheDocument();
    // Touch options are available
    expect(screen.getByRole('button', { name: /^YES$/i })).toBeInTheDocument();
  });

  it('4. Handles microphone permission denied (NotAllowedError)', async () => {
    (window as any).SpeechRecognition = MockSpeechRecognition;
    (navigator.mediaDevices.getUserMedia as any).mockRejectedValueOnce({
      name: 'NotAllowedError',
    });

    render(<UniversalAnswerInput question={mockQuestion} onAnswer={vi.fn()} />);

    await act(async () => {
      fireEvent.click(screen.getByLabelText(/speak your response/i));
    });

    expect(screen.getAllByText(/microphone permission is required/i)[0]).toBeInTheDocument();
  });

  it('5. Handles microphone device unavailable (NotFoundError)', async () => {
    (window as any).SpeechRecognition = MockSpeechRecognition;
    (navigator.mediaDevices.getUserMedia as any).mockRejectedValueOnce({
      name: 'NotFoundError',
    });

    render(<UniversalAnswerInput question={mockQuestion} onAnswer={vi.fn()} />);

    await act(async () => {
      fireEvent.click(screen.getByLabelText(/speak your response/i));
    });

    expect(screen.getAllByText(/no microphone detected/i)[0]).toBeInTheDocument();
  });

  it('6 & 7. Triggers recognition start and updates interim transcript', async () => {
    let mockInstance: any;
    class CustomMockSpeechRecognition extends MockSpeechRecognition {
      constructor() {
        super();
        mockInstance = this;
      }
    }
    (window as any).SpeechRecognition = CustomMockSpeechRecognition;

    render(<UniversalAnswerInput question={mockQuestion} onAnswer={vi.fn()} />);

    await act(async () => {
      fireEvent.click(screen.getByLabelText(/speak your response/i));
    });

    // Simulate interim result
    act(() => {
      if (mockInstance.onresult) {
        mockInstance.onresult({
          results: [[{ transcript: 'I feel sharp pain behind my right eye' }]],
        });
      }
    });

    const textInput = screen.getByDisplayValue('I feel sharp pain behind my right eye');
    expect(textInput).toBeInTheDocument();
  });

  it('8 & 10. Handles recognition completion and displays transcript review', async () => {
    let mockInstance: any;
    class CustomMockSpeechRecognition extends MockSpeechRecognition {
      constructor() {
        super();
        mockInstance = this;
      }
    }
    (window as any).SpeechRecognition = CustomMockSpeechRecognition;

    render(<UniversalAnswerInput question={mockQuestion} onAnswer={vi.fn()} />);

    await act(async () => {
      fireEvent.click(screen.getByLabelText(/speak your response/i));
    });

    act(() => {
      if (mockInstance.onresult) {
        mockInstance.onresult({
          results: [[{ transcript: 'Constant throbbing pain for two days' }]],
        });
      }
      if (mockInstance.onend) {
        mockInstance.onend();
      }
    });

    expect(screen.getByText(/answer captured/i)).toBeInTheDocument();
    expect(screen.getByDisplayValue('Constant throbbing pain for two days')).toBeInTheDocument();
  });

  it('9. Handles recognition error gracefully with user message', async () => {
    let mockInstance: any;
    class CustomMockSpeechRecognition extends MockSpeechRecognition {
      constructor() {
        super();
        mockInstance = this;
      }
    }
    (window as any).SpeechRecognition = CustomMockSpeechRecognition;

    render(<UniversalAnswerInput question={mockQuestion} onAnswer={vi.fn()} />);

    await act(async () => {
      fireEvent.click(screen.getByLabelText(/speak your response/i));
    });

    act(() => {
      if (mockInstance.onerror) {
        mockInstance.onerror({ error: 'no-speech' });
      }
    });

    expect(screen.getAllByText(/no speech detected/i)[0]).toBeInTheDocument();
  });

  it('11. Provides explicit Retry Microphone control', async () => {
    let mockInstance: any;
    class CustomMockSpeechRecognition extends MockSpeechRecognition {
      constructor() {
        super();
        mockInstance = this;
      }
    }
    (window as any).SpeechRecognition = CustomMockSpeechRecognition;

    render(<UniversalAnswerInput question={mockQuestion} onAnswer={vi.fn()} />);

    await act(async () => {
      fireEvent.click(screen.getByLabelText(/speak your response/i));
    });

    act(() => {
      if (mockInstance.onerror) {
        mockInstance.onerror({ error: 'no-speech' });
      }
    });

    const retryBtn = screen.getByRole('button', { name: /retry microphone/i });
    expect(retryBtn).toBeInTheDocument();

    await act(async () => {
      fireEvent.click(retryBtn);
    });

    expect(navigator.mediaDevices.getUserMedia).toHaveBeenCalledTimes(2);
  });

  it('12. Sets BCP-47 language code en-IN for English', async () => {
    let mockInstance: any;
    class CustomMockSpeechRecognition extends MockSpeechRecognition {
      constructor() {
        super();
        mockInstance = this;
      }
    }
    (window as any).SpeechRecognition = CustomMockSpeechRecognition;

    render(<UniversalAnswerInput question={mockQuestion} language="en" onAnswer={vi.fn()} />);

    await act(async () => {
      fireEvent.click(screen.getByLabelText(/speak your response/i));
    });

    expect(mockInstance.lang).toBe('en-IN');
  });

  it('13. Sets BCP-47 language code hi-IN for Hindi', async () => {
    let mockInstance: any;
    class CustomMockSpeechRecognition extends MockSpeechRecognition {
      constructor() {
        super();
        mockInstance = this;
      }
    }
    (window as any).SpeechRecognition = CustomMockSpeechRecognition;

    render(<UniversalAnswerInput question={mockQuestion} language="hi" onAnswer={vi.fn()} />);

    await act(async () => {
      fireEvent.click(screen.getByLabelText(/speak your response/i));
    });

    expect(mockInstance.lang).toBe('hi-IN');
  });

  it('14. Sets BCP-47 language code te-IN for Telugu', async () => {
    let mockInstance: any;
    class CustomMockSpeechRecognition extends MockSpeechRecognition {
      constructor() {
        super();
        mockInstance = this;
      }
    }
    (window as any).SpeechRecognition = CustomMockSpeechRecognition;

    render(<UniversalAnswerInput question={mockQuestion} language="te" onAnswer={vi.fn()} />);

    await act(async () => {
      fireEvent.click(screen.getByLabelText(/speak your response/i));
    });

    expect(mockInstance.lang).toBe('te-IN');
  });

  it('15, 16, 17, 18. Transcript is editable, NOT auto-submitted, explicit confirm submits answer with VOICE input method', async () => {
    let mockInstance: any;
    class CustomMockSpeechRecognition extends MockSpeechRecognition {
      constructor() {
        super();
        mockInstance = this;
      }
    }
    (window as any).SpeechRecognition = CustomMockSpeechRecognition;

    const onAnswerMock = vi.fn().mockResolvedValue(undefined);

    render(<UniversalAnswerInput question={mockQuestion} onAnswer={onAnswerMock} />);

    await act(async () => {
      fireEvent.click(screen.getByLabelText(/speak your response/i));
    });

    // Simulate STT result & end
    act(() => {
      if (mockInstance.onresult) {
        mockInstance.onresult({
          results: [[{ transcript: 'Pain behind eyes' }]],
        });
      }
      if (mockInstance.onend) {
        mockInstance.onend();
      }
    });

    // Verify NOT auto-submitted
    expect(onAnswerMock).not.toHaveBeenCalled();

    // 15. Edit transcript text
    const textInput = screen.getByDisplayValue('Pain behind eyes');
    fireEvent.change(textInput, { target: { value: 'Severe pain behind left eye since morning' } });

    // 17 & 18. Explicit patient confirmation submits answer to adaptive pipeline with VOICE inputMethod
    const confirmBtn = screen.getByRole('button', { name: /confirm & continue/i });
    await act(async () => {
      fireEvent.click(confirmBtn);
    });

    expect(onAnswerMock).toHaveBeenCalledTimes(1);
    expect(onAnswerMock).toHaveBeenCalledWith('Severe pain behind left eye since morning', 'VOICE');
  });
});
