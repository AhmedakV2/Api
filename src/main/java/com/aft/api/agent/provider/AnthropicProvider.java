package com.aft.api.agent.provider;

import org.springframework.ai.anthropic.AnthropicChatModel;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnBean(AnthropicChatModel.class)
public class AnthropicProvider implements ModelProvider {

    private final AnthropicChatModel chatModel;

    public AnthropicProvider(AnthropicChatModel chatModel) {
        this.chatModel = chatModel;
    }

    @Override
    public ProviderName name() {
        return ProviderName.ANTHROPIC;
    }
    @Override
    public ChatModel chatModel() {
        return chatModel;
    }
}
