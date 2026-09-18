package com.aft.api.agent;

import com.aft.api.agent.provider.ModelProvider;
import com.aft.api.agent.provider.ProviderName;
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

public class StubModelProvider implements ModelProvider {
    private final List<String> chunks;
    private final RuntimeException failure;
    private final ProviderName providerName;
    private Prompt lastPrompt;

    public StubModelProvider(List<String> chunks) {
        this(chunks, null, ProviderName.OLLAMA);
    }

    public StubModelProvider(List<String> chunks, ProviderName providerName) {
        this(chunks, null, providerName);
    }

    public StubModelProvider(List<String> chunks, RuntimeException failure) {
        this(chunks, failure, ProviderName.OLLAMA);
    }

    public StubModelProvider(List<String> chunks, RuntimeException failure, ProviderName providerName) {
        this.chunks = chunks;
        this.failure = failure;
        this.providerName = providerName;
    }

    public static StubModelProvider failing(RuntimeException failure) {
        return new StubModelProvider(List.of(), failure);
    }

    public StubModelProvider withDefaultOptions(ChatOptions options) {
        this.defaultOptions = options;
        return this;
    }

    public Prompt lastPrompt() {
        return lastPrompt;
    }

    @Override
    public ProviderName name() {
        return providerName;
    }

    @Override
    public ChatModel chatModel() {
        return new StubChatModel();
    }

    @Override
    public ChatResponse call(List<Message> messages, ChatOptions options) {
        lastPrompt = new Prompt(messages, options);
        if (failure != null) {
            throw failure;
        }
        return response(String.join("", chunks), 11, 7);
    }

    @Override
    public Flux<ChatResponse> stream(List<Message> messages, ChatOptions options) {
        lastPrompt = new Prompt(messages, options);
        if (failure != null) {
            return Flux.error(failure);
        }
        return Flux.fromIterable(chunks).map(chunk -> response(chunk, 11, 7));
    }

    @Override
    public ChatResponse call(List<Message> messages, String model) {
        return call(messages, ChatOptions.builder().model(model).build());
    }

    @Override
    public Flux<ChatResponse> stream(List<Message> messages, String model) {
        return stream(messages, ChatOptions.builder().model(model).build());
    }

    private ChatResponse response(String text, int promptTokens, int completionTokens) {
        ChatResponseMetadata metadata = ChatResponseMetadata.builder()
                .usage(new DefaultUsage(promptTokens, completionTokens))
                .build();
        return new ChatResponse(List.of(new Generation(new AssistantMessage(text))), metadata);
    }

    private final class StubChatModel implements ChatModel {
        @Override
        public ChatOptions getDefaultOptions() {
            return defaultOptions;
        }

        @Override
        public ChatResponse call(Prompt prompt) {
            return StubModelProvider.this.call(prompt.getInstructions(), prompt.getOptions());
        }

        @Override
        public Flux<ChatResponse> stream(Prompt prompt) {
            return StubModelProvider.this.stream(prompt.getInstructions(), prompt.getOptions());
        }
    }
}
