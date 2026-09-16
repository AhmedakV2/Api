package com.aft.api.state.valkey;

import com.aft.api.state.ToolResultBus;
import java.nio.charset.StandardCharsets;
import java.util.function.Consumer;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;

public class ValkeyToolResultBus implements ToolResultBus, MessageListener {
    private final StringRedisTemplate template;
    private final RedisMessageListenerContainer container;
    private final String channel;
    private volatile Consumer<String> handler;

    public ValkeyToolResultBus(StringRedisTemplate template, RedisMessageListenerContainer container,
                               String channel) {
        this.template = template;
        this.container = container;
        this.channel = channel;
    }

    @Override
    public void publish(String payload) {
        template.convertAndSend(channel, payload);
    }

    @Override
    public void subscribe(Consumer<String> consumer) {
        this.handler = consumer;
        container.addMessageListener(this, new ChannelTopic(channel));
    }

    @Override
    public void onMessage(Message message, byte[] pattern) {
        Consumer<String> current = handler;
        if (current != null) {
            current.accept(new String(message.getBody(), StandardCharsets.UTF_8));
        }
    }
}
