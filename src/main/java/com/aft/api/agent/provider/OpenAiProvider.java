package com.aft.api.agent.provider;

import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.openai.OpenAiChatModel;

public class OpenAiProvider implements ModelProvider {
    private final OpenAiChatModel chatModel;

    public OpenAiProvider(OpenAiChatModel chatModel) {
        this.chatModel = chatModel;
    }

    @Override
    public ProviderName name() {
        return ProviderName.OPENAI;
    }

    @Override
    public ChatModel chatModel() {
        return chatModel;
    }
}
