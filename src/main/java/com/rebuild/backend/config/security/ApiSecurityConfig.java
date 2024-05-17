package com.rebuild.backend.config.security;

import jakarta.servlet.Filter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class ApiSecurityConfig {

    private final Filter filter;

    @Autowired
    public ApiSecurityConfig(@Qualifier("JWTVerificationFilter") Filter filter) {
        this.filter = filter;
    }

    @Bean
    public SecurityFilterChain apiSecurityChain(HttpSecurity security) throws Exception {
        security.authorizeHttpRequests(config -> config.
                requestMatchers(HttpMethod.GET, "/api/**").authenticated().
                requestMatchers(HttpMethod.POST, "/api/**").authenticated().
                requestMatchers(HttpMethod.PUT, "/api/**").authenticated().
                requestMatchers(HttpMethod.DELETE, "/api/**").authenticated()).
                addFilterBefore(filter, JWTVerificationFilter.class);
        return security.build();


    }
}
