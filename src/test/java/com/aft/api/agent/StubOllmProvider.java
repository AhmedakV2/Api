package com.aft.api.agent;

import com.aft.api.agent.provider.OllmProvider;
import java.util.List;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.metadata.ChatResponseMetadata;
import org.springframework.ai.chat.metadata.DefaultUsage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.model.Generation;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.ai.chat.prompt.Prompt;
import reactor.core.publisher.Flux;

public class StubOllmProvider extends OllmProvider {
    private final List<String> chunks;
    private final RuntimeException failure;
    private Prompt lastPrompt;

    public StubOllmProvider(List<String> chunks) {
        this(chunks, null, null);
    }

    public StubOllmProvider(List<String> chunks, ChatOptions defaults) {
        this(chunks, null, defaults);
    }

    public StubOllmProvider(List<String> chunks, RuntimeException failure, ChatOptions defaults) {
        super(new StubChatModel(defaults));
        this.chunks = chunks;
        this.failure = failure;
    }

    public static StubOllmProvider failing(RuntimeException failure) {
        return new StubOllmProvider(List.of(), failure, null);
    }

    public Prompt lastPrompt() {
        return lastPrompt;
    }

    @Override
    public ChatResponse call(List<Message> messages, ChatOptions options) {
        lastPrompt = new Prompt(messages, options);
        if (failure != null) {
            throw failure;
        }
        return response(String.join("", chunks));
    }

    @Override
    public Flux<ChatResponse> stream(List<Message> messages, ChatOptions options) {
        lastPrompt = new Prompt(messages, options);
        if (failure != null) {
            return Flux.error(failure);
        }
        return Flux.fromIterable(chunks).map(this::response);
    }

    private ChatResponse response(String text) {
        ChatResponseMetadata metadata = ChatResponseMetadata.builder()
                .usage(new DefaultUsage(11, 7))
                .build();
        return new ChatResponse(List.of(new Generation(new AssistantMessage(text))), metadata);
    }

    private static final class StubChatModel implements ChatModel {
        private final ChatOptions defaults;

        private StubChatModel(ChatOptions defaults) {
            this.defaults = defaults;
        }

        @Override
        public ChatOptions getDefaultOptions() {
            return defaults;
        }

        @Override
        public ChatResponse call(Prompt prompt) {
            return new ChatResponse(List.of(new Generation(new AssistantMessage(""))));
        }

        @Override
        public Flux<ChatResponse> stream(Prompt prompt) {
            return Flux.empty();
        }
    }
}
