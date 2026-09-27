package com.medikiosk.voice.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.medikiosk.voice.provider.MockSpeechToTextProvider;
import com.medikiosk.voice.provider.MockTextToSpeechProvider;
import com.medikiosk.voice.provider.OpenAiCompatibleSpeechToTextProvider;
import com.medikiosk.voice.provider.OpenAiCompatibleTextToSpeechProvider;
import com.medikiosk.voice.provider.SpeechToTextProvider;
import com.medikiosk.voice.provider.TextToSpeechProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class VoiceConfig {

    @Bean
    @ConditionalOnProperty(name = "app.voice.provider", havingValue = "mock", matchIfMissing = true)
    public SpeechToTextProvider speechToTextProvider() {
        return new MockSpeechToTextProvider();
    }

    @Bean
    @ConditionalOnProperty(name = "app.voice.provider", havingValue = "mock", matchIfMissing = true)
    public TextToSpeechProvider textToSpeechProvider() {
        return new MockTextToSpeechProvider();
    }

    @Bean
    @ConditionalOnProperty(name = "app.voice.provider", havingValue = "openai")
    public SpeechToTextProvider openAiSpeechToTextProvider(ObjectMapper objectMapper) {
        return new OpenAiCompatibleSpeechToTextProvider(new MockSpeechToTextProvider(), objectMapper);
    }

    @Bean
    @ConditionalOnProperty(name = "app.voice.provider", havingValue = "openai")
    public TextToSpeechProvider openAiTextToSpeechProvider(ObjectMapper objectMapper) {
        return new OpenAiCompatibleTextToSpeechProvider(new MockTextToSpeechProvider(), objectMapper);
    }
}
