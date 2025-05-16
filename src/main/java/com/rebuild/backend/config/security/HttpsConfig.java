package com.rebuild.backend.config.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.server.SecurityWebFilterChain;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@EnableWebFluxSecurity
public class HttpsConfig {

    @Bean
    @Order(Integer.MIN_VALUE)
    SecurityWebFilterChain httpsFilterChain(ServerHttpSecurity http){
        http.redirectToHttps(customizer ->
                customizer.httpsRedirectWhen(when ->
                        when.getRequest().getHeaders().containsKey("X-Forwarded-Proto")));
        return http.build();
    }

}
