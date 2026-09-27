import { describe, it, expect, vi } from 'vitest';
import { render, screen, fireEvent, act } from '@testing-library/react';
import { VoiceRecorder } from './VoiceRecorder';

describe('VoiceRecorder Component', () => {
  it('renders initial idle state with accessible microphone button and language select', () => {
    render(<VoiceRecorder sessionId="test-session" onConfirm={vi.fn()} />);

    expect(screen.getByRole('region', { name: /voice input interface/i })).toBeInTheDocument();
    expect(screen.getByLabelText(/start voice recording/i)).toBeInTheDocument();
    expect(screen.getByRole('combobox')).toBeInTheDocument();
  });

  it('allows language switching between English, Hindi, and Telugu', () => {
    render(<VoiceRecorder sessionId="test-session" onConfirm={vi.fn()} />);

    const select = screen.getByRole('combobox');
    fireEvent.change(select, { target: { value: 'hi' } });
    expect(select).toHaveValue('hi');

    fireEvent.change(select, { target: { value: 'te' } });
    expect(select).toHaveValue('te');
  });

  it('transitions from idle -> recording -> processing -> transcript when speech is provided', async () => {
    vi.useFakeTimers();
    render(<VoiceRecorder sessionId="test-session" onConfirm={vi.fn()} mockTranscript="I have fever and headache" />);

    // Click start recording
    const recordBtn = screen.getByLabelText(/start voice recording/i);
    fireEvent.click(recordBtn);

    // Now in recording state
    const stopBtn = screen.getByLabelText(/stop recording/i);
    expect(stopBtn).toBeInTheDocument();

    // Click stop recording
    fireEvent.click(stopBtn);

    // Fast-forward fake timer for STT processing
    act(() => {
      vi.advanceTimersByTime(200);
    });

    // Transcript screen should be visible
    expect(screen.getByText(/please verify your response above/i)).toBeInTheDocument();
    expect(screen.getByLabelText(/confirm response/i)).toBeInTheDocument();
    expect(screen.getByLabelText(/retry voice recording/i)).toBeInTheDocument();
    expect(screen.getByLabelText(/reject transcript/i)).toBeInTheDocument();

    vi.useRealTimers();
  });

  it('displays no speech detected error when silence occurs', async () => {
    vi.useFakeTimers();
    render(<VoiceRecorder sessionId="test-session" onConfirm={vi.fn()} />);

    // Click start recording
    const recordBtn = screen.getByLabelText(/start voice recording/i);
    fireEvent.click(recordBtn);

    // Click stop recording with no speech provided
    const stopBtn = screen.getByLabelText(/stop recording/i);
    fireEvent.click(stopBtn);

    act(() => {
      vi.advanceTimersByTime(200);
    });

    // Should NOT show transcript screen; should show error
    expect(screen.getByText(/no speech detected/i)).toBeInTheDocument();
    expect(screen.queryByLabelText(/confirm response/i)).not.toBeInTheDocument();

    vi.useRealTimers();
  });

  it('calls onConfirm with confirmed transcript, original transcript, and confidence', async () => {
    vi.useFakeTimers();
    const onConfirmMock = vi.fn();
    render(<VoiceRecorder sessionId="test-session" onConfirm={onConfirmMock} mockTranscript="I have fever for two days" />);

    fireEvent.click(screen.getByLabelText(/start voice recording/i));
    fireEvent.click(screen.getByLabelText(/stop recording/i));

    act(() => {
      vi.advanceTimersByTime(200);
    });

    const confirmBtn = screen.getByLabelText(/confirm response/i);
    fireEvent.click(confirmBtn);

    expect(onConfirmMock).toHaveBeenCalledTimes(1);
    expect(onConfirmMock).toHaveBeenCalledWith(
      expect.stringContaining('fever'),
      expect.stringContaining('fever'),
      expect.any(Number)
    );

    vi.useRealTimers();
  });

  it('allows patient to edit the transcript before confirming', async () => {
    vi.useFakeTimers();
    const onConfirmMock = vi.fn();
    render(<VoiceRecorder sessionId="test-session" onConfirm={onConfirmMock} mockTranscript="I have fever for two days" />);

    fireEvent.click(screen.getByLabelText(/start voice recording/i));
    fireEvent.click(screen.getByLabelText(/stop recording/i));

    act(() => {
      vi.advanceTimersByTime(200);
    });

    // Toggle edit mode
    const editBtn = screen.getByLabelText(/edit transcription text/i);
    fireEvent.click(editBtn);

    const textarea = screen.getByLabelText(/edit transcribed response/i);
    fireEvent.change(textarea, { target: { value: 'Corrected: Severe headache since yesterday' } });

    // Confirm edited response
    const confirmBtn = screen.getByLabelText(/confirm response/i);
    fireEvent.click(confirmBtn);

    expect(onConfirmMock).toHaveBeenCalledWith(
      'Corrected: Severe headache since yesterday',
      expect.stringContaining('fever'), // Original preserved
      expect.any(Number)
    );

    vi.useRealTimers();
  });

  it('handles patient rejection without saving and offers retry or fallback', async () => {
    vi.useFakeTimers();
    const onConfirmMock = vi.fn();
    render(<VoiceRecorder sessionId="test-session" onConfirm={onConfirmMock} mockTranscript="I have fever for two days" />);

    fireEvent.click(screen.getByLabelText(/start voice recording/i));
    fireEvent.click(screen.getByLabelText(/stop recording/i));

    act(() => {
      vi.advanceTimersByTime(200);
    });

    // Patient clicks Reject
    const rejectBtn = screen.getByLabelText(/reject transcript/i);
    fireEvent.click(rejectBtn);

    expect(onConfirmMock).not.toHaveBeenCalled();
    expect(screen.getByText(/voice response was rejected and has not been saved/i)).toBeInTheDocument();

    vi.useRealTimers();
  });

  it('supports direct fallback to touch/keyboard input', () => {
    const onFallbackMock = vi.fn();
    render(<VoiceRecorder sessionId="test-session" onConfirm={vi.fn()} onFallbackToText={onFallbackMock} />);

    const fallbackLink = screen.getByLabelText(/switch to touch or keyboard input/i);
    fireEvent.click(fallbackLink);

    expect(onFallbackMock).toHaveBeenCalledTimes(1);
    expect(screen.getByText(/switched to touch\/keyboard input/i)).toBeInTheDocument();
  });
});
