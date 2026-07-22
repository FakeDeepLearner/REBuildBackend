package com.rebuild.backend.config.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.*;
import org.springframework.security.config.annotation.web.configurers.oauth2.client.OAuth2LoginConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.security.core.session.SessionRegistryImpl;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.context.SecurityContextHolderFilter;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.csrf.CsrfTokenRepository;
import org.springframework.security.web.session.HttpSessionEventPublisher;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.HiddenHttpMethodFilter;
import org.springframework.web.filter.UrlHandlerFilter;

import java.util.List;

@Configuration
@EnableWebSecurity
public class SecureAuthConfig {

    @Bean
    public UrlBasedCorsConfigurationSource corsConfigurationSource()
    {
        CorsConfiguration corsConfiguration = new CorsConfiguration();
        corsConfiguration.setAllowedOrigins(List.of("localhost", "rerebuild.ca"));
        corsConfiguration.setAllowedHeaders(List.of("Idempotency-Key"));
        corsConfiguration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS",  "PATCH", "HEAD"));
        corsConfiguration.setAllowCredentials(false);
        corsConfiguration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource urlBasedCorsConfigurationSource = new UrlBasedCorsConfigurationSource();
        urlBasedCorsConfigurationSource.registerCorsConfiguration("/**", corsConfiguration);
        return urlBasedCorsConfigurationSource;
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
                .trailingSlashHandler("/api/**")
                .redirect(HttpStatus.PERMANENT_REDIRECT)
                .build();
    }

    @Bean
    public SecurityFilterChain filterChainAuthentication(HttpSecurity security,
                                                         ClerkAuthenticationFilter authenticationFilter,
                                                         UrlHandlerFilter urlHandlerFilter,
                                                         CsrfTokenRepository csrfTokenRepository,
                                                         CorsConfigurationSource corsConfigurationSource) {
        security
                .formLogin(FormLoginConfigurer::disable)
                .httpBasic(HttpBasicConfigurer::disable)
                .logout(LogoutConfigurer::disable)
                .sessionManagement(management ->
                        management.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .requestCache(RequestCacheConfigurer::disable)
                .oauth2Login(OAuth2LoginConfigurer::disable)
                .addFilterBefore(urlHandlerFilter, SecurityContextHolderFilter.class)
                .addFilterAfter(new HiddenHttpMethodFilter(), UrlHandlerFilter.class)
                .addFilterAfter(authenticationFilter, HiddenHttpMethodFilter.class)
                .csrf(management ->
                        management.csrfTokenRepository(csrfTokenRepository))
                .headers(
                headers -> headers.contentSecurityPolicy(
                        csp -> csp.policyDirectives("default-src 'self'; script-src 'self'; " +
                                "img-src 'self'; frame-ancestors 'none';")
                )).
                authorizeHttpRequests(auth -> auth
                .requestMatchers("/webhooks/**").permitAll()
                .requestMatchers("/api/**").authenticated()
                .anyRequest().denyAll())
                .redirectToHttps(Customizer.withDefaults())
                .cors(cors -> cors.configurationSource(corsConfigurationSource));
        return security.build();

    }


}
