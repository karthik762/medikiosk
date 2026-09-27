package com.medikiosk.voice.provider;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.util.Map;

/**
 * Mock development implementation of TextToSpeechProvider.
 * Generates valid standard PCM WAV audio stream for approved UI prompts.
 * Does NOT generate autonomous medical advice or clinical conclusions.
 */
public class MockTextToSpeechProvider implements TextToSpeechProvider {

    private static final Logger log = LoggerFactory.getLogger(MockTextToSpeechProvider.class);

    public static final String PROVIDER_NAME = "MOCK_TTS_PROVIDER";
    public static final String CONTENT_TYPE_WAV = "audio/wav";

    @Override
    public AudioSynthesisResult synthesize(String text, String language, Map<String, Object> metadata) {
        log.info("MockTextToSpeechProvider synthesizing prompt (length: {} chars, language: {})",
                text != null ? text.length() : 0, language);

        if (metadata != null && Boolean.TRUE.equals(metadata.get("simulate_failure"))) {
            log.warn("MockTextToSpeechProvider simulating provider failure as requested by test metadata");
            return AudioSynthesisResult.failure("Speech synthesis service temporarily unavailable.", language, true);
        }

        try {
            byte[] wavBytes = generatePcmWav(text != null ? text : "");
            return AudioSynthesisResult.success(wavBytes, CONTENT_TYPE_WAV, language != null ? language : "en", true);
        } catch (IOException e) {
            log.error("Failed to generate synthetic WAV audio", e);
            return AudioSynthesisResult.failure("Failed to synthesize speech audio", language, true);
        }
    }

    /**
     * Generates a completely silent PCM WAV byte stream (flatline zeros).
     */
    public static byte[] generateSilentPcmWav(int durationMs) throws IOException {
        int sampleRate = 8000;
        short channels = 1;
        short bitsPerSample = 16;
        int numSamples = (sampleRate * Math.max(100, durationMs)) / 1000;
        int dataSize = numSamples * channels * (bitsPerSample / 8);

        ByteArrayOutputStream baos = new ByteArrayOutputStream(44 + dataSize);
        ByteBuffer header = ByteBuffer.allocate(44).order(ByteOrder.LITTLE_ENDIAN);

        // RIFF header
        header.put(new byte[]{'R', 'I', 'F', 'F'});
        header.putInt(36 + dataSize);
        header.put(new byte[]{'W', 'A', 'V', 'E'});

        // "fmt " chunk
        header.put(new byte[]{'f', 'm', 't', ' '});
        header.putInt(16);
        header.putShort((short) 1);
        header.putShort(channels);
        header.putInt(sampleRate);
        header.putInt(sampleRate * channels * (bitsPerSample / 8));
        header.putShort((short) (channels * (bitsPerSample / 8)));
        header.putShort(bitsPerSample);

        // "data" chunk
        header.put(new byte[]{'d', 'a', 't', 'a'});
        header.putInt(dataSize);

        baos.write(header.array());

        // Zero samples (pure silence)
        byte[] zeroSamples = new byte[dataSize];
        baos.write(zeroSamples);

        return baos.toByteArray();
    }

    /**
     * Generates a valid standard RIFF/WAVE PCM audio byte stream with optional embedded text metadata.
     */
    public static byte[] generatePcmWav(String text) throws IOException {
        if (text == null || text.trim().isEmpty()) {
            return generateSilentPcmWav(500);
        }

        int sampleRate = 8000;
        short channels = 1;
        short bitsPerSample = 16;
        int durationMs = Math.min(2000, Math.max(200, text.length() * 20));
        int numSamples = (sampleRate * durationMs) / 1000;
        int dataSize = numSamples * channels * (bitsPerSample / 8);

        // Metadata chunk: LIST -> INFO -> INAM
        byte[] textBytes = text.getBytes(StandardCharsets.UTF_8);
        int textLen = textBytes.length;
        int inamPadding = (textLen % 2 != 0) ? 1 : 0;
        int inamTotal = 8 + textLen + inamPadding; // "INAM" + size + data + pad
        int listSize = 4 + inamTotal; // "INFO" + INAM chunk
        int listTotal = 8 + listSize; // "LIST" + size + contents

        int totalFileSize = 36 + dataSize + listTotal;

        ByteArrayOutputStream baos = new ByteArrayOutputStream(44 + dataSize + listTotal);
        ByteBuffer header = ByteBuffer.allocate(44).order(ByteOrder.LITTLE_ENDIAN);

        // RIFF header
        header.put(new byte[]{'R', 'I', 'F', 'F'});
        header.putInt(totalFileSize);
        header.put(new byte[]{'W', 'A', 'V', 'E'});

        // "fmt " chunk
        header.put(new byte[]{'f', 'm', 't', ' '});
        header.putInt(16); // Subchunk1Size for PCM
        header.putShort((short) 1); // AudioFormat: 1 = PCM
        header.putShort(channels);
        header.putInt(sampleRate);
        header.putInt(sampleRate * channels * (bitsPerSample / 8)); // ByteRate
        header.putShort((short) (channels * (bitsPerSample / 8))); // BlockAlign
        header.putShort(bitsPerSample);

        // "data" chunk
        header.put(new byte[]{'d', 'a', 't', 'a'});
        header.putInt(dataSize);

        baos.write(header.array());

        // Synthetic tone (440 Hz standard A tone, non-silent speech proxy)
        ByteBuffer sampleBuffer = ByteBuffer.allocate(dataSize).order(ByteOrder.LITTLE_ENDIAN);
        double frequency = 440.0;
        for (int i = 0; i < numSamples; i++) {
            double angle = 2.0 * Math.PI * i * frequency / sampleRate;
            short sample = (short) (Math.sin(angle) * 8000);
            sampleBuffer.putShort(sample);
        }
        baos.write(sampleBuffer.array());

        // Append standard RIFF metadata chunk
        ByteBuffer metaBuffer = ByteBuffer.allocate(listTotal).order(ByteOrder.LITTLE_ENDIAN);
        metaBuffer.put(new byte[]{'L', 'I', 'S', 'T'});
        metaBuffer.putInt(listSize);
        metaBuffer.put(new byte[]{'I', 'N', 'F', 'O'});
        metaBuffer.put(new byte[]{'I', 'N', 'A', 'M'});
        metaBuffer.putInt(textLen);
        metaBuffer.put(textBytes);
        if (inamPadding > 0) {
            metaBuffer.put((byte) 0);
        }
        baos.write(metaBuffer.array());

        return baos.toByteArray();
    }

    @Override
    public String getProviderName() {
        return PROVIDER_NAME;
    }

    @Override
    public boolean isAvailable() {
        return true;
    }
}
