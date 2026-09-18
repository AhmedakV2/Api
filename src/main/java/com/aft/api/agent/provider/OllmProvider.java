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

public class OllmProvider {
    private final ChatModel chatModel;

    public OllmProvider(ChatModel chatModel) {
        this.chatModel = chatModel;
    }

    public ChatModel chatModel() {
        return chatModel;
    }

    public ToolCallingChatOptions toolOptions(String model,
                                              List<ToolCallback> callbacks,
                                              Map<String, Object> toolContext) {
        ChatOptions defaults = chatModel.getDefaultOptions();
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

    public ChatResponse call(List<Message> messages, ChatOptions options) {
        return chatModel.call(new Prompt(messages, options));
    }

    public Flux<ChatResponse> stream(List<Message> messages, ChatOptions options) {
        return chatModel.stream(new Prompt(messages, options));
    }
}
