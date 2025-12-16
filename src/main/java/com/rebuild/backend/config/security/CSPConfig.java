package com.rebuild.backend.config.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class CSPConfig {

    @Bean
    @Order()
    public SecurityFilterChain otherPropertiesChain(HttpSecurity security) throws Exception{
        security.
                headers(
                        headers -> {
                            headers.contentSecurityPolicy(
                                    csp -> {
                                        csp.policyDirectives("default-src 'self'; script-src 'self'; " +
                                                "img-src 'self'; frame-ancestors 'none';");
                                    }
                            );
                        }
                );
        return security.build();
    }
}
