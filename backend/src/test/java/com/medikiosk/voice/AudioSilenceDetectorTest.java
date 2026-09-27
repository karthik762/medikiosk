package com.medikiosk.voice;

import com.medikiosk.voice.provider.MockTextToSpeechProvider;
import com.medikiosk.voice.util.AudioSilenceDetector;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AudioSilenceDetectorTest {

    @Test
    @DisplayName("1. Null and empty audio byte arrays are detected as silence")
    void testNullAndEmptyBytesAreSilence() {
        assertTrue(AudioSilenceDetector.isSilence(null, "audio/wav"));
        assertTrue(AudioSilenceDetector.isSilence(new byte[0], "audio/wav"));
        assertTrue(AudioSilenceDetector.isSilence(new byte[20], "audio/wav"));
    }

    @Test
    @DisplayName("2. Pure flatline silent WAV (zero samples) is detected as silence")
    void testSilentFlatlineWavIsDetectedAsSilence() throws Exception {
        byte[] silentWav = MockTextToSpeechProvider.generateSilentPcmWav(500);
        assertNotNull(silentWav);
        assertTrue(silentWav.length > 44);

        boolean isSilent = AudioSilenceDetector.isSilence(silentWav, "audio/wav");
        assertTrue(isSilent, "Pure flatline zero samples WAV must be detected as silence");
    }

    @Test
    @DisplayName("3. Synthetic audio with audible signal is NOT detected as silence")
    void testAudibleWavIsNotSilence() throws Exception {
        byte[] audibleWav = MockTextToSpeechProvider.generatePcmWav("I have severe retro-orbital pain");
        assertNotNull(audibleWav);

        boolean isSilent = AudioSilenceDetector.isSilence(audibleWav, "audio/wav");
        assertFalse(isSilent, "WAV with audible sine tone signal must not be classified as silence");
    }

    @Test
    @DisplayName("4. Sub-threshold micro noise (low RMS) is detected as silence")
    void testSubThresholdNoiseIsSilence() {
        // Create 44-byte WAV with low noise samples (amplitude < 50)
        byte[] header = new byte[44 + 400];
        // RIFF header
        header[0] = 'R'; header[1] = 'I'; header[2] = 'F'; header[3] = 'F';
        header[8] = 'W'; header[9] = 'A'; header[10] = 'V'; header[11] = 'E';
        // fmt
        header[12] = 'f'; header[13] = 'm'; header[14] = 't'; header[15] = ' ';
        header[16] = 16;
        header[20] = 1; // PCM
        header[22] = 1; // 1 channel
        // sample rate 8000
        header[24] = 0x40; header[25] = 0x1F;
        header[32] = 2; // block align
        header[34] = 16; // 16 bits
        // data
        header[36] = 'd'; header[37] = 'a'; header[38] = 't'; header[39] = 'a';
        header[40] = (byte) (400 & 0xFF); header[41] = (byte) ((400 >> 8) & 0xFF);

        // Samples: amplitude 10 (very low micro noise, well below speech threshold)
        for (int i = 44; i < header.length; i += 2) {
            header[i] = 10;
            header[i + 1] = 0;
        }

        assertTrue(AudioSilenceDetector.isSilence(header, "audio/wav"));
    }
}
