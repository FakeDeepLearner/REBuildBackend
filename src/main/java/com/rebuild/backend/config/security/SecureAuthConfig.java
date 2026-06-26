package com.rebuild.backend.config.security;



import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.security.core.session.SessionRegistryImpl;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.context.SecurityContextHolderFilter;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.csrf.CsrfTokenRepository;
import org.springframework.security.web.session.HttpSessionEventPublisher;
import org.springframework.web.filter.HiddenHttpMethodFilter;
import org.springframework.web.filter.UrlHandlerFilter;

@Configuration
@EnableWebSecurity
public class SecureAuthConfig {

    @Bean
    public SessionRegistry sessionRegistry(){
        return new SessionRegistryImpl();
    }

    //Needed to make sure that the SessionRegistry listens to session invalidations and timeouts
    @Bean
    public HttpSessionEventPublisher eventPublisher(){
        return new HttpSessionEventPublisher();
    }

    @Bean
    public CsrfTokenRepository csrfTokenRepository() {
        CookieCsrfTokenRepository repository = CookieCsrfTokenRepository.withHttpOnlyFalse();

        repository.setCookieName("CSRF-TOKEN");
        repository.setHeaderName("X-CSRF-TOKEN");

        return repository;
    }

    //This will redirect to the original endpoint if there is a trailing slash at the end of the request's URL.
    @Bean
    public UrlHandlerFilter handlerFilter()
    {
        return UrlHandlerFilter
                .trailingSlashHandler("/api/**", "/home/**")
                .redirect(HttpStatus.PERMANENT_REDIRECT)
                .build();
    }

    @Bean
    @Order(3)
    public SecurityFilterChain filterChainAuthentication(HttpSecurity security,
                                                         ClerkAuthenticationFilter authenticationFilter,
                                                         UrlHandlerFilter urlHandlerFilter,
                                                         CsrfTokenRepository csrfTokenRepository) {
        security
                .formLogin(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)
                .logout(AbstractHttpConfigurer::disable)
                .sessionManagement(management ->
                        management.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .oauth2Login(AbstractHttpConfigurer::disable)
                .addFilterBefore(urlHandlerFilter, SecurityContextHolderFilter.class)
                .addFilterAfter(new HiddenHttpMethodFilter(), UrlHandlerFilter.class)
                .addFilterAfter(authenticationFilter, HiddenHttpMethodFilter.class)
                .csrf(management ->
                        management.csrfTokenRepository(csrfTokenRepository))
                .headers(
                headers -> headers.contentSecurityPolicy(
                        csp -> csp.policyDirectives("default-src 'self'; script-src 'self'; " +
                                "img-src 'self'; frame-ancestors 'none';")
                ));
        return security.build();


    }


}
