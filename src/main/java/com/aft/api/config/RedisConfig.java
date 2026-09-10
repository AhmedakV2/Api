package com.aft.api.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.data.redis.core.script.RedisScript;
import java.util.List;

@Configuration
public class RedisConfig {
    @Bean
    StringRedisTemplate stringRedisTemplate(RedisConnectionFactory connectionFactory) {
        return new StringRedisTemplate(connectionFactory);
    }

    @Bean
    RedisScript<List> rateLimitScript() {
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
}
