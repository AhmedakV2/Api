package com.aft.api.config;

import com.aft.api.agent.provider.AnthropicProvider;
import com.aft.api.agent.provider.ModelProvider;
import com.aft.api.agent.provider.OllamaProvider;
import com.aft.api.agent.provider.OpenAiProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.anthropic.AnthropicChatModel;
import org.springframework.ai.ollama.OllamaChatModel;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ModelProviderConfig {
    private static final Logger log = LoggerFactory.getLogger(ModelProviderConfig.class);

    @Bean
    ModelProvider ollamaModelProvider(ObjectProvider<OllamaChatModel> chatModel) {
        OllamaChatModel model = chatModel.getIfAvailable();
        if (model == null) {
            log.info("Ollama sohbet modeli yapilandirilmadi, OLLAMA saglayicisi devre disi");
            return null;
        }
        return new OllamaProvider(model);
    }

    @Bean
    ModelProvider openAiModelProvider(ObjectProvider<OpenAiChatModel> chatModel) {
        OpenAiChatModel model = chatModel.getIfAvailable();
        if (model == null) {
            log.info("OpenAI sohbet modeli yapilandirilmadi, OPENAI saglayicisi devre disi");
            return null;
        }
        return new OpenAiProvider(model);
    }

    @Bean
    ModelProvider anthropicModelProvider(ObjectProvider<AnthropicChatModel> chatModel) {
        AnthropicChatModel model = chatModel.getIfAvailable();
        if (model == null) {
            log.info("Anthropic sohbet modeli yapilandirilmadi, ANTHROPIC saglayicisi devre disi");
            return null;
        }
        return new AnthropicProvider(model);
    }
}
