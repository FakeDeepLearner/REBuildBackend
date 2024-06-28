package com.rebuild.backend.config.security;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.security.core.session.SessionRegistryImpl;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.session.RegisterSessionAuthenticationStrategy;
import org.springframework.security.web.session.HttpSessionEventPublisher;

@Configuration
@EnableWebSecurity
public class SessionsConfig {

    @Bean
    public SessionRegistry sessionRegistry(){
        return new SessionRegistryImpl();
    }

    @Bean
    public HttpSessionEventPublisher eventPublisher(){
        return new HttpSessionEventPublisher();
    }



    @Bean
    public SecurityFilterChain sessionsFilterChain(HttpSecurity security) throws Exception {
        return security.sessionManagement(management ->
                management.
                        sessionConcurrency(concurrency -> concurrency.maximumSessions(-1)).
                        sessionAuthenticationStrategy(new RegisterSessionAuthenticationStrategy(sessionRegistry())).
                        maximumSessions(-1).
                        sessionRegistry(sessionRegistry()).
                        expiredUrl("/expired")).
                build();
    }
}
