package com.rebuild.backend.config.security;


import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;

import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.server.resource.web.BearerTokenAuthenticationEntryPoint;
import org.springframework.security.oauth2.server.resource.web.access.BearerTokenAccessDeniedHandler;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;

import java.io.IOException;

import static org.springframework.security.config.Customizer.*;

@Configuration
@EnableWebSecurity
public class SecureAuthConfig {

    @Bean
    public SecurityFilterChain filterChainAuthentication(HttpSecurity security) throws Exception {
        security.
                authorizeHttpRequests(config -> config.
                        requestMatchers(HttpMethod.GET, "/home/**").authenticated().
                        requestMatchers(HttpMethod.POST, "/home/**").authenticated().
                        requestMatchers(HttpMethod.PUT, "/home/**").authenticated().
                        requestMatchers(HttpMethod.DELETE, "/home/**").authenticated().
                        requestMatchers(HttpMethod.PATCH, "/home/**").authenticated()).
                formLogin(login -> login.loginPage("/login")).
                //oauth2Login(login -> login.loginPage("/login")).
                oauth2ResourceServer(server -> server.jwt(withDefaults())).
                exceptionHandling(handler -> handler.accessDeniedHandler(new BearerTokenAccessDeniedHandler()).
                        authenticationEntryPoint(new BearerTokenAuthenticationEntryPoint()));
        return security.build();


    }

    @Bean
    public AuthenticationEntryPoint unauthorizedAuthEntry(){
        return (request, response, authException) -> {
            //401 = Unauthorized
            response.setStatus(401);
            response.setHeader("WWW-Authenticate",
                    "Bearer error=\"invalid_token\", " +
                            "error_description=\"Your token has either expired, its credentials do not match," +
                            "or its claims do not match\"");
            response.getWriter().write(authException.getMessage());

        };
    }
}
