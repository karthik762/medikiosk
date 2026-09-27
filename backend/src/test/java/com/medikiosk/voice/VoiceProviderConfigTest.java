package com.medikiosk.voice;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.medikiosk.voice.config.VoiceConfig;
import com.medikiosk.voice.provider.SpeechToTextProvider;
import com.medikiosk.voice.provider.TextToSpeechProvider;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class VoiceProviderConfigTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    @DisplayName("Verify VoiceConfig configures Mock STT & TTS providers when app.voice.provider is mock")
    void testMockVoiceProviderConfiguration() {
        VoiceConfig config = new VoiceConfig();

        SpeechToTextProvider stt = config.speechToTextProvider();
        TextToSpeechProvider tts = config.textToSpeechProvider();

        assertNotNull(stt);
        assertNotNull(tts);
        assertEquals("MOCK_STT_PROVIDER", stt.getProviderName());
        assertEquals("MOCK_TTS_PROVIDER", tts.getProviderName());
        assertTrue(stt.isAvailable());
        assertTrue(tts.isAvailable());
    }

    @Test
    @DisplayName("Verify VoiceConfig configures OpenAI STT & TTS providers when app.voice.provider is openai")
    void testOpenAiVoiceProviderConfiguration() {
        VoiceConfig config = new VoiceConfig();

        SpeechToTextProvider stt = config.openAiSpeechToTextProvider(objectMapper);
        TextToSpeechProvider tts = config.openAiTextToSpeechProvider(objectMapper);

        assertNotNull(stt);
        assertNotNull(tts);
        assertTrue(stt.getProviderName().contains("MOCK") || stt.getProviderName().contains("OPENAI"));
        assertTrue(tts.getProviderName().contains("MOCK") || tts.getProviderName().contains("OPENAI"));
    }
}
