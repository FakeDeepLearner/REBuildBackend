package com.rebuild.backend.config.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.servlet.util.matcher.PathPatternRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;

@Configuration
@EnableWebSecurity
public class CustomMatchersConfig {

    @Bean
    @Order(2)
    public SecurityFilterChain filterChainLoginSignup(HttpSecurity security) throws Exception {
        RequestMatcher loginFail = PathPatternRequestMatcher.withDefaults().matcher(HttpMethod.GET,
                        "/login?error=true");
        RequestMatcher logoutSuccess = PathPatternRequestMatcher.withDefaults().matcher(HttpMethod.GET,
                 "/login?logout=true");
        security.authorizeHttpRequests(config ->
                config.requestMatchers(loginFail, logoutSuccess).permitAll());

        return security.build();
    }
}
