package com.aft.api;

import com.aft.api.config.AiProperties;
import com.aft.api.config.CorsProperties;
import com.aft.api.config.JwtProperties;
import com.aft.api.config.SecurityProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import com.aft.api.config.StateProperties;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing(auditorAwareRef = "auditorAware")
@EnableConfigurationProperties({JwtProperties.class, SecurityProperties.class, AiProperties.class,
        CorsProperties.class, StateProperties.class})
public class AftApiApplication {
    public static void main(String[] args) {
        SpringApplication.run(AftApiApplication.class, args);
    }
}
