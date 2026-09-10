package com.aft.api;

import com.aft.api.config.JwtProperties;
import com.aft.api.config.SecurityProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing(auditorAwareRef = "auditorAware")
@EnableConfigurationProperties({JwtProperties.class, SecurityProperties.class})
public class AftApiApplication {
    public static void main(String[] args) {
        SpringApplication.run(AftApiApplication.class, args);
    }
}
