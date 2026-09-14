package com.aft.api.agent.provider;

import java.util.List;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.ai.chat.prompt.Prompt;
import reactor.core.publisher.Flux;

public interface ModelProvider {
    ProviderName name();

    ChatModel chatModel();

    default ChatResponse call(List<Message> messages, String model) {
        return chatModel().call(new Prompt(messages, options(model)));
    }

    default Flux<ChatResponse> stream(List<Message> messages, String model) {
        return chatModel().stream(new Prompt(messages, options(model)));
    }

    private ChatOptions options(String model) {
        return ChatOptions.builder().model(model).build();
    }
}
