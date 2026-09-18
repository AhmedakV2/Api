package com.aft.api.agent.memory;

import com.aft.api.agent.entity.AgentMessage;
import com.aft.api.agent.entity.MessageRole;
import com.aft.api.config.AiProperties;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.stereotype.Component;

@Component
public class ConversationWindow {
    private final AiProperties properties;

    public ConversationWindow(AiProperties properties) {
        this.properties = properties;
    }

    public List<Message> build(String systemPrompt, List<AgentMessage> history) {
        Deque<Message> window = new ArrayDeque<>();
        int tokens = estimate(systemPrompt);
        int count = 0;

        for (int i = history.size() - 1; i >= 0; i--) {
            AgentMessage entry = history.get(i);
            int entryTokens = entry.getTokenCount() > 0 ? entry.getTokenCount() : estimate(entry.getContent());
            if (count >= properties.maxWindowMessages()
                    || tokens + entryTokens > properties.maxWindowTokens()) {
                break;
            }
            Message converted = convert(entry);
            if (converted != null) {
                window.addFirst(converted);
                tokens += entryTokens;
                count++;
            }
        }

        List<Message> result = new ArrayList<>(window.size() + 1);
        result.add(new SystemMessage(systemPrompt));
        result.addAll(window);
        return result;
    }

    public static int estimate(String text) {
        return text == null || text.isBlank() ? 0 : Math.max(1, text.length() / 4);
    }

    private Message convert(AgentMessage entry) {
        if (entry.getContent() == null || entry.getContent().isBlank()) {
            return null;
        }
        if (entry.getRole() == MessageRole.USER) {
            return new UserMessage(entry.getContent());
        }
        if (entry.getRole() == MessageRole.ASSISTANT) {
            return new AssistantMessage(entry.getContent());
        }
        return null;
    }
}
