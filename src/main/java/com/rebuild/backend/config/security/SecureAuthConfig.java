package com.rebuild.backend.config.security;


import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;

import org.springframework.security.oauth2.server.resource.web.BearerTokenAuthenticationEntryPoint;
import org.springframework.security.oauth2.server.resource.web.access.BearerTokenAccessDeniedHandler;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecureAuthConfig {

    @Bean
    public SecurityFilterChain filterChainAuthentication(HttpSecurity security) throws Exception {
        security.
                authorizeHttpRequests(config -> config.
                        requestMatchers(HttpMethod.GET, "home/**").authenticated().
                        requestMatchers(HttpMethod.POST, "home/**").authenticated().
                        requestMatchers(HttpMethod.PUT, "home/**").authenticated().
                        requestMatchers(HttpMethod.DELETE, "home/**").authenticated().
                        requestMatchers(HttpMethod.PATCH, "home/**").authenticated()).
                formLogin(login -> login.loginPage("/login")).
                oauth2ResourceServer(server -> server.jwt(Customizer.withDefaults())).
                exceptionHandling(handler -> handler.accessDeniedHandler(new BearerTokenAccessDeniedHandler()).
                        authenticationEntryPoint(new BearerTokenAuthenticationEntryPoint()));
        return security.build();


    }
}
