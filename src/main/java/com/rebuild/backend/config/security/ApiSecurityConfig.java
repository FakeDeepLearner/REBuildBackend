package com.rebuild.backend.config.security;

import com.rebuild.backend.config.properties.AppUrlBase;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class ApiSecurityConfig {

    private final AppUrlBase base;

    @Autowired
    public ApiSecurityConfig(AppUrlBase base) {
        this.base = base;
    }

    @Bean
    @Order(4)
    public SecurityFilterChain apiSecurityChain(HttpSecurity security) throws Exception {
        security.authorizeHttpRequests(config -> config.
                requestMatchers(HttpMethod.POST, base.baseUrl() + "/api/refresh_token").permitAll().
                requestMatchers(HttpMethod.GET, base.baseUrl() +"/api/**").authenticated().
                requestMatchers(HttpMethod.POST, base.baseUrl() + "/api/**").authenticated().
                requestMatchers(HttpMethod.PUT, base.baseUrl() + "/api/**").authenticated().
                requestMatchers(HttpMethod.DELETE, base.baseUrl() + "/api/**").authenticated().
                requestMatchers(HttpMethod.GET, base.baseUrl() + "/api/post/**",
                        base.baseUrl() + "/api/put/**",
                        base.baseUrl() + "/api/delete/**").denyAll()
        );
        return security.build();


    }
}
