package com.aft.api.config;

import com.aft.api.state.StateStoreProvider;
import com.aft.api.state.ToolResultBus;
import com.aft.api.state.valkey.ValkeyStateStoreProvider;
import com.aft.api.state.valkey.ValkeyToolResultBus;
import java.util.List;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;

@Configuration
@ConditionalOnProperty(name = "aft.state.provider", havingValue = "valkey")
public class ValkeyStateConfig {

    @Bean
    RedisConnectionFactory valkeyConnectionFactory(StateProperties properties) {
        StateProperties.Valkey settings = properties.valkey();
        RedisStandaloneConfiguration standalone =
                new RedisStandaloneConfiguration(settings.host(), settings.port());
        if (settings.password() != null && !settings.password().isBlank()) {
            standalone.setPassword(settings.password());
        }
        standalone.setDatabase(settings.database());
        return new LettuceConnectionFactory(standalone);
    }

    @Bean
    StringRedisTemplate valkeyTemplate(RedisConnectionFactory connectionFactory) {
        return new StringRedisTemplate(connectionFactory);
    }

    @Bean
    RedisMessageListenerContainer valkeyListenerContainer(RedisConnectionFactory connectionFactory) {
        RedisMessageListenerContainer container = new RedisMessageListenerContainer();
        container.setConnectionFactory(connectionFactory);
        return container;
    }

    @Bean
    RedisScript<List> counterScript() {
        DefaultRedisScript<List> script = new DefaultRedisScript<>();
        script.setScriptText("""
                local current = redis.call('INCR', KEYS[1])
                if current == 1 then
                  redis.call('PEXPIRE', KEYS[1], ARGV[1])
                end
                return { current, redis.call('PTTL', KEYS[1]) }
                """);
        script.setResultType(List.class);
        return script;
    }

    @Bean
    StateStoreProvider stateStoreProvider(StringRedisTemplate template, RedisScript<List> counterScript,
                                          StateProperties properties) {
        return new ValkeyStateStoreProvider(template, counterScript, properties.valkey().keyPrefix());
    }

    @Bean
    ToolResultBus toolResultBus(StringRedisTemplate template, RedisMessageListenerContainer container,
                                StateProperties properties) {
        return new ValkeyToolResultBus(template, container, properties.valkey().toolResultChannel());
    }
}
