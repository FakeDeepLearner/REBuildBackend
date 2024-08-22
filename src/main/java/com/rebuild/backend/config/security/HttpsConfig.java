package com.rebuild.backend.config.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class HttpsConfig {

    @Bean
    @Order(Integer.MIN_VALUE)
    public SecurityFilterChain filterChain(HttpSecurity security) throws Exception {
        security.requiresChannel(customizer -> customizer.anyRequest().requiresSecure());
        return security.build();
    }

}
