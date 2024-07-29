package com.rebuild.backend.config.security.sessions;

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

    //Needed in order to make sure that the SessionRegistry listens to session invalidations and timeouts
    @Bean
    public HttpSessionEventPublisher eventPublisher(){
        return new HttpSessionEventPublisher();
    }

    @Bean
    public SecurityFilterChain sessionsFilterChain(HttpSecurity security) throws Exception {
        return security.sessionManagement(management ->
                management.
                        invalidSessionStrategy(new SessionInvalidationCustomStrategy()).
                        sessionConcurrency(concurrency -> concurrency.maximumSessions(-1)).
                        sessionAuthenticationStrategy(new RegisterSessionAuthenticationStrategy(sessionRegistry())).
                        maximumSessions(-1).
                        sessionRegistry(sessionRegistry()).
                        expiredUrl("/expired")).
                exceptionHandling(handling -> handling.authenticationEntryPoint(new InvalidSessionAuthenticationEntry())).
                build();
    }
}
