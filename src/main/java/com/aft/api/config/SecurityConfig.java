package com.aft.api.config;

import com.aft.api.security.ApiKeyAuthenticationFilter;
import com.aft.api.security.JwtAuthenticationFilter;
import com.aft.api.security.PermissionEvaluatorImpl;
import com.aft.api.security.RateLimitFilter;
import java.io.IOException;
import java.net.URI;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.security.access.expression.method.DefaultMethodSecurityExpressionHandler;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import tools.jackson.databind.json.JsonMapper;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {
    private static final String[] PUBLIC_PATHS = {
            "/api/v1/auth/login",
            "/api/v1/auth/register",
            "/api/v1/auth/refresh",
            "/api/v1/invitations/accept"
    };

    private static final String HANDSHAKE_PATH = WebSocketConfig.ENDPOINT + "/**";

    private final JwtAuthenticationFilter jwtFilter;
    private final ApiKeyAuthenticationFilter apiKeyFilter;
    private final RateLimitFilter rateLimitFilter;
    private final PermissionEvaluatorImpl permissionEvaluator;
    private final JsonMapper jsonMapper;

    public SecurityConfig(JwtAuthenticationFilter jwtFilter,
                          ApiKeyAuthenticationFilter apiKeyFilter,
                          RateLimitFilter rateLimitFilter,
                          PermissionEvaluatorImpl permissionEvaluator,
                          JsonMapper jsonMapper) {
        this.jwtFilter = jwtFilter;
        this.apiKeyFilter = apiKeyFilter;
        this.rateLimitFilter = rateLimitFilter;
        this.permissionEvaluator = permissionEvaluator;
        this.jsonMapper = jsonMapper;
    }

    @Bean
    SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable)
                .cors(Customizer.withDefaults())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.GET, "/actuator/health/**", "/actuator/info").permitAll()
                        .requestMatchers(PUBLIC_PATHS).permitAll()
                        .requestMatchers(WebSocketConfig.ENDPOINT, HANDSHAKE_PATH).permitAll()
                        .requestMatchers("/actuator/**").hasRole("ADMIN")
                        .anyRequest().authenticated())
                .exceptionHandling(handling -> handling
                        .authenticationEntryPoint(unauthorizedEntryPoint())
                        .accessDeniedHandler(forbiddenHandler()))
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class)
                .addFilterAfter(apiKeyFilter, JwtAuthenticationFilter.class)
                .addFilterAfter(rateLimitFilter, ApiKeyAuthenticationFilter.class);
        return http.build();
    }

    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12);
    }

    @Bean
    DefaultMethodSecurityExpressionHandler methodSecurityExpressionHandler() {
        DefaultMethodSecurityExpressionHandler handler = new DefaultMethodSecurityExpressionHandler();
        handler.setPermissionEvaluator(permissionEvaluator);
        return handler;
    }

    private AuthenticationEntryPoint unauthorizedEntryPoint() {
        return (request, response, exception) ->
                writeProblem(request, response, 401, "UNAUTHENTICATED", "Jeton yok veya gecersiz");
    }

    private AccessDeniedHandler forbiddenHandler() {
        return (request, response, exception) ->
                writeProblem(request, response, 403, "FORBIDDEN", "Rol veya organizasyon kapsami yetersiz");
    }

    private void writeProblem(HttpServletRequest request, HttpServletResponse response,
                              int status, String code, String title) throws IOException {
        ProblemDetail detail = ProblemDetail.forStatus(status);
        detail.setType(URI.create("https://docs.aft.local/errors/" + code));
        detail.setTitle(title);
        detail.setInstance(URI.create(request.getRequestURI()));
        detail.setProperty("code", code);

        response.setStatus(status);
        response.setContentType(MediaType.APPLICATION_PROBLEM_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(jsonMapper.writeValueAsString(detail));
    }
}
