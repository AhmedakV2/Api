package com.aft.api.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import java.util.List;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@Configuration
public class OpenApiConfig {
    @Bean
    OpenAPI aftOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("AFT API")
                        .version("1.0.0")
                        .description("Kullanici yonetimi ve AI agent servisi"))
                .servers(List.of(new Server().url("/").description("Varsayilan")))
                .components(new Components()
                        .addSecuritySchemes("bearer", new SecurityScheme()
                                .type(SecurityScheme.Type.HTTP).scheme("bearer").bearerFormat("JWT"))
                        .addSecuritySchemes("deviceKey", new SecurityScheme()
                                .type(SecurityScheme.Type.APIKEY)
                                .in(SecurityScheme.In.HEADER).name("X-Aft-Key")))
                .addSecurityItem(new SecurityRequirement().addList("bearer"));
    }
}
