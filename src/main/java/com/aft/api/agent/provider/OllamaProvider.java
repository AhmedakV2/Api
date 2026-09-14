package com.aft.api.agent.provider;

import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.ollama.OllamaChatModel;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnBean(OllamaChatModel.class)
public class OllamaProvider implements ModelProvider {

    private final OllamaChatModel chatModel;

    public OllamaProvider(OllamaChatModel chatModel) {
        this.chatModel = chatModel;
    }

    @Override
    public ProviderName name() {
        return ProviderName.OLLAMA;
    }
    @Override
    public ChatModel chatModel() {
        return chatModel;
    }
}
