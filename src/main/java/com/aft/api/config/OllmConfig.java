package com.aft.api.config;

import com.aft.api.agent.provider.OllmProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OllmConfig {
    private static final Logger log = LoggerFactory.getLogger(OllmConfig.class);

    @Bean
    OllmProvider ollmProvider(ObjectProvider<OpenAiChatModel> chatModel) {
        OpenAiChatModel model = chatModel.getIfAvailable();
        if (model == null) {
            log.warn("Ollm sohbet modeli yapilandirilmadi, ajan yalnizca hata donecek");
            return null;
        }
        log.info("Ollm sohbet modeli hazir");
        return new OllmProvider(model);
    }
}
