package com.rebuild.backend.config.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class ApiSecurityConfig {

    @Bean
    public SecurityFilterChain apiSecurityChain(HttpSecurity security) throws Exception {
        security.authorizeHttpRequests(config -> config.
                requestMatchers(HttpMethod.POST, "/api/refresh_token").permitAll().
                requestMatchers(HttpMethod.GET, "/api/**").authenticated().
                requestMatchers(HttpMethod.POST, "/api/**").authenticated().
                requestMatchers(HttpMethod.PUT, "/api/**").authenticated().
                requestMatchers(HttpMethod.DELETE, "/api/**").authenticated());
        return security.build();


    }
}
