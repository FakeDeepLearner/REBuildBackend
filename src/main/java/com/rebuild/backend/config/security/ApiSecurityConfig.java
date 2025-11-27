package com.rebuild.backend.config.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class ApiSecurityConfig {


    @Bean
    @Order(4)
    public SecurityFilterChain apiSecurityChain(HttpSecurity security) throws Exception {
        security.authorizeHttpRequests(config -> config.
                requestMatchers(HttpMethod.GET, "/api/**", "/home/**").authenticated().
                requestMatchers(HttpMethod.POST, "/api/**", "/home/**").authenticated().
                requestMatchers(HttpMethod.PUT,  "/api/**", "/home/**").authenticated().
                requestMatchers(HttpMethod.DELETE,  "/api/**", "/home/**").authenticated().
                requestMatchers(HttpMethod.PATCH,  "/api/**", "/home/**").authenticated().
                        requestMatchers("/auth/**").permitAll())
                .redirectToHttps(Customizer.withDefaults());
        return security.build();


    }
}
