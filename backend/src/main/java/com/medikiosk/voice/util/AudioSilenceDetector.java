package com.medikiosk.voice.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * Utility to inspect audio bytes and detect silence, background room noise below speech threshold,
 * or empty audio payloads.
 *
 * Prevents treating silent or non-speech audio as valid patient statements.
 */
public final class AudioSilenceDetector {

    private static final Logger log = LoggerFactory.getLogger(AudioSilenceDetector.class);

    /**
     * RMS amplitude threshold for 16-bit PCM audio (0 to 32767).
     * Typical speech has RMS > 1000. Silent microphone or low room noise is < 300.
     */
    public static final double DEFAULT_WAV_RMS_THRESHOLD = 300.0;

    /**
     * Peak amplitude threshold for 16-bit PCM audio (0 to 32767).
     * Silence or microphone hum rarely exceeds 600.
     */
    public static final int DEFAULT_WAV_PEAK_THRESHOLD = 600;

    private AudioSilenceDetector() {
        // Prevent instantiation
    }

    /**
     * Checks if the given audio payload represents silence, empty data, or absence of speech.
     *
     * @param audioData   Raw audio bytes.
     * @param contentType Content-Type header (e.g. "audio/wav", "audio/webm").
     * @return true if the audio is detected as silent or containing no speech energy.
     */
    public static boolean isSilence(byte[] audioData, String contentType) {
        if (audioData == null || audioData.length == 0) {
            log.debug("Audio is null or empty; detected as silence.");
            return true;
        }

        // Check if all bytes are identical / all zero (flatline)
        if (isAllZeroOrFlat(audioData)) {
            log.debug("Audio payload is entirely zero or flatline; detected as silence.");
            return true;
        }

        // Check if it is a WAV file
        if (isWav(audioData)) {
            return isSilentWav(audioData, DEFAULT_WAV_RMS_THRESHOLD, DEFAULT_WAV_PEAK_THRESHOLD);
        }

        return false;
    }

    /**
     * Determines whether WAV audio data is below the speech energy threshold.
     */
    public static boolean isSilentWav(byte[] wavData, double rmsThreshold, int peakThreshold) {
        if (wavData == null || wavData.length < 44) {
            return true;
        }

        try {
            int dataOffset = findWavDataOffset(wavData);
            if (dataOffset < 0 || dataOffset >= wavData.length) {
                return true;
            }

            int pcmBytes = wavData.length - dataOffset;
            if (pcmBytes < 2) {
                return true;
            }

            int numSamples = pcmBytes / 2;
            if (numSamples == 0) {
                return true;
            }

            ByteBuffer buffer = ByteBuffer.wrap(wavData, dataOffset, numSamples * 2).order(ByteOrder.LITTLE_ENDIAN);

            double sumSquared = 0.0;
            int peak = 0;

            for (int i = 0; i < numSamples; i++) {
                short sample = buffer.getShort();
                int absSample = Math.abs(sample);
                if (absSample > peak) {
                    peak = absSample;
                }
                sumSquared += (double) sample * (double) sample;
            }

            double rms = Math.sqrt(sumSquared / numSamples);

            log.debug("WAV analysis: samples={}, peak={}, rms={}, peakThreshold={}, rmsThreshold={}",
                    numSamples, peak, String.format("%.2f", rms), peakThreshold, rmsThreshold);

            return rms < rmsThreshold && peak < peakThreshold;
        } catch (Exception e) {
            log.warn("Failed to parse WAV samples for silence detection ({}); falling back to false.", e.getMessage());
            return false;
        }
    }

    private static boolean isWav(byte[] data) {
        return data.length >= 12 &&
                data[0] == 'R' && data[1] == 'I' && data[2] == 'F' && data[3] == 'F' &&
                data[8] == 'W' && data[9] == 'A' && data[10] == 'V' && data[11] == 'E';
    }

    private static int findWavDataOffset(byte[] data) {
        int offset = 12;
        while (offset + 8 <= data.length) {
            byte c0 = data[offset];
            byte c1 = data[offset + 1];
            byte c2 = data[offset + 2];
            byte c3 = data[offset + 3];

            int chunkSize = ByteBuffer.wrap(data, offset + 4, 4).order(ByteOrder.LITTLE_ENDIAN).getInt();

            if (c0 == 'd' && c1 == 'a' && c2 == 't' && c3 == 'a') {
                return offset + 8;
            }

            offset += 8 + Math.max(0, chunkSize);
            if (offset % 2 != 0) offset++;
        }
        return 44;
    }

    private static boolean isAllZeroOrFlat(byte[] data) {
        if (data.length == 0) return true;
        byte first = data[0];
        for (int i = 1; i < data.length; i++) {
            if (data[i] != first) {
                return false;
            }
        }
        return true;
    }
}
