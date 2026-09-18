package com.aft.api.agent.provider;

import java.util.List;
import java.util.Map;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.model.tool.ToolCallingChatOptions;
import org.springframework.ai.tool.ToolCallback;
import reactor.core.publisher.Flux;

public interface ModelProvider {
    ProviderName name();

    ChatModel chatModel();

    default ToolCallingChatOptions toolOptions(String model,
                                               List<ToolCallback> callbacks,
                                               Map<String, Object> toolContext) {
        ChatOptions defaults = chatModel().getDefaultOptions();
        if (defaults instanceof ToolCallingChatOptions tooling) {
            ToolCallingChatOptions.Builder<?> builder = tooling.mutate();
            builder.model(model);
            builder.toolCallbacks(callbacks);
            builder.toolContext(toolContext);
            return builder.build();
        }
        return ToolCallingChatOptions.builder()
                .model(model)
                .toolCallbacks(callbacks)
                .toolContext(toolContext)
                .build();
    }

    default ChatResponse call(List<Message> messages, String model) {
        return call(messages, options(model));
    }

    default Flux<ChatResponse> stream(List<Message> messages, String model) {
        return stream(messages, options(model));
    }

    default ChatResponse call(List<Message> messages, ChatOptions options) {
        return chatModel().call(new Prompt(messages, options));
    }

    default Flux<ChatResponse> stream(List<Message> messages, ChatOptions options) {
        return chatModel().stream(new Prompt(messages, options));
    }

    private ChatOptions options(String model) {
        return ChatOptions.builder().model(model).build();
    }
}
