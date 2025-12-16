package com.rebuild.backend.config.security;



import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;

import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.security.core.session.SessionRegistryImpl;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.RememberMeServices;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.authentication.logout.HeaderWriterLogoutHandler;
import org.springframework.security.web.authentication.rememberme.RememberMeAuthenticationFilter;
import org.springframework.security.web.csrf.CsrfTokenRepository;
import org.springframework.security.web.csrf.HttpSessionCsrfTokenRepository;
import org.springframework.security.web.header.writers.ClearSiteDataHeaderWriter;
import org.springframework.security.web.session.HttpSessionEventPublisher;

import static org.springframework.security.web.header.writers.ClearSiteDataHeaderWriter.Directive.COOKIES;


@Configuration
@EnableWebSecurity
public class SecureAuthConfig {

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
    public CsrfTokenRepository csrfTokenRepository() {
        HttpSessionCsrfTokenRepository repository = new HttpSessionCsrfTokenRepository();

        repository.setHeaderName("X-CSRF-TOKEN");
        repository.setSessionAttributeName("_csrfToken");
        repository.setParameterName("_csrfToken");

        return repository;

    }

    @Bean
    @Order(3)
    public SecurityFilterChain filterChainAuthentication(HttpSecurity security,
                                                         RememberMeServices rememberMeServices,
                                                         RememberMeAuthenticationFilter rememberMeAuthenticationFilter,
                                                         ClientRegistrationRepository registrationRepository,
                                                         CsrfTokenRepository tokenRepository) throws Exception {
        security
                //Form login is disabled, because it expects everything to be done in one endpoint.
                //However, our standard (non-OAuth) login flow has 2 separate endpoints that need to be called.
                .formLogin(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)
                .oauth2Login(config ->
                        config.clientRegistrationRepository(registrationRepository).
                                loginPage("/login"))
                .logout(config -> config.
                    logoutUrl("/logout").
                    logoutSuccessUrl("/login?logout=true").
                    addLogoutHandler(new HeaderWriterLogoutHandler(new ClearSiteDataHeaderWriter(COOKIES))).
                        permitAll())
                .rememberMe(rememberMe ->
                        rememberMe.rememberMeServices(rememberMeServices).useSecureCookie(true).
                                rememberMeCookieName("REMEMBERED_USER_COOKIE"))
                .addFilterAfter(rememberMeAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                .csrf(csrf ->
                        csrf.csrfTokenRepository(tokenRepository)).
                sessionManagement(session ->
                        session.invalidSessionUrl("/login?sessionInvalid=true").
                                sessionFixation().changeSessionId().
                        sessionCreationPolicy(SessionCreationPolicy.ALWAYS)
                                .maximumSessions(1).maxSessionsPreventsLogin(true).
                                sessionRegistry(sessionRegistry()).
                                expiredUrl("/login?sessionExpired=true"));

        return security.build();


    }


}
