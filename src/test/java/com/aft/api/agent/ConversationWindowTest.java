package com.aft.api.agent;

import static org.assertj.core.api.Assertions.assertThat;

import com.aft.api.agent.entity.AgentMessage;
import com.aft.api.agent.entity.MessageRole;
import com.aft.api.agent.memory.ConversationWindow;
import com.aft.api.config.AiProperties;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.messages.MessageType;

class ConversationWindowTest {

    private static final UUID SESSION = UUID.randomUUID();

    private ConversationWindow window(int maxMessages, int maxTokens) {
        return new ConversationWindow(new AiProperties("ollama", null,
                Duration.ofSeconds(30), maxMessages, maxTokens));
    }

    private List<AgentMessage> history(int count) {
        List<AgentMessage> messages = new ArrayList<>();
        for (int i = 1; i <= count; i++) {
            MessageRole role = (i % 2 == 1) ? MessageRole.USER : MessageRole.ASSISTANT;
            messages.add(new AgentMessage(SESSION, i, role, "mesaj-" + i, 10));
        }
        return messages;
    }

    @Test
    void sistemIstemiHerZamanIlkSiradadir() {
        var result = window(40, 24000).build("sistem istemi", history(4));

        assertThat(result).isNotEmpty();
        assertThat(result.getFirst().getMessageType()).isEqualTo(MessageType.SYSTEM);
        assertThat(result.getFirst().getText()).isEqualTo("sistem istemi");
    }

    @Test
    void pencereKronolojikSiradaDoner() {
        var result = window(40, 24000).build("sistem", history(3));

        assertThat(result).hasSize(4);
        assertThat(result.get(1).getText()).isEqualTo("mesaj-1");
        assertThat(result.get(2).getText()).isEqualTo("mesaj-2");
        assertThat(result.get(3).getText()).isEqualTo("mesaj-3");
    }

    @Test
    void mesajAdediSiniriEnYenileriBirakir() {
        var result = window(2, 24000).build("sistem", history(10));

        assertThat(result).hasSize(3);
        assertThat(result.get(1).getText()).isEqualTo("mesaj-9");
        assertThat(result.get(2).getText()).isEqualTo("mesaj-10");
    }

    @Test
    void tokenButcesiPencereyiDaraltir() {
        var result = window(40, 25).build("sistem", history(10));

        assertThat(result.size()).isLessThan(5);
        assertThat(result.getFirst().getMessageType()).isEqualTo(MessageType.SYSTEM);
    }

    @Test
    void aracVeSistemMesajlariPencereyeAlinmaz() {
        List<AgentMessage> history = List.of(
                new AgentMessage(SESSION, 1, MessageRole.USER, "soru", 5),
                new AgentMessage(SESSION, 2, MessageRole.TOOL, "arac ciktisi", 5),
                new AgentMessage(SESSION, 3, MessageRole.ASSISTANT, "yanit", 5));

        var result = window(40, 24000).build("sistem", history);

        assertThat(result).hasSize(3);
        assertThat(result).noneMatch(message -> "arac ciktisi".equals(message.getText()));
    }

    @Test
    void bosGecmisteYalnizcaSistemIstemiDoner() {
        assertThat(window(40, 24000).build("sistem", List.of())).hasSize(1);
    }

    @Test
    void tokenTahminiBosMetinIcinSifirdir() {
        assertThat(ConversationWindow.estimate(null)).isZero();
        assertThat(ConversationWindow.estimate("   ")).isZero();
        assertThat(ConversationWindow.estimate("dort")).isPositive();
    }
}
