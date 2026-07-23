package com.rebuild.backend.config.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.*;
import org.springframework.security.config.annotation.web.configurers.oauth2.client.OAuth2LoginConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.context.SecurityContextHolderFilter;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.csrf.CsrfTokenRepository;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.time.Duration;
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
        corsConfiguration.setMaxAge(Duration.ofHours(1));

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

    private void commonChain(HttpSecurity security) {
        security
                .formLogin(FormLoginConfigurer::disable)
                .httpBasic(HttpBasicConfigurer::disable)
                .logout(LogoutConfigurer::disable)
                .requestCache(RequestCacheConfigurer::disable)
                .oauth2Login(OAuth2LoginConfigurer::disable)
                .sessionManagement(management ->
                        management.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .redirectToHttps(Customizer.withDefaults());
    }

    @Bean
    public SecurityFilterChain webhooksFilterChain(HttpSecurity security)  {
        commonChain(security);
        security.
                securityMatcher("/webhooks/**")
                .csrf(CsrfConfigurer::disable)
                .authorizeHttpRequests(auth ->
                        auth.anyRequest().permitAll());
        return security.build();
    }

    @Bean
    public SecurityFilterChain apiRequestsFilterChain(HttpSecurity security,
                                                      ClerkAuthenticationFilter authenticationFilter,
                                                      CsrfTokenRepository csrfTokenRepository,
                                                      CorsConfigurationSource corsConfigurationSource) {
        commonChain(security);
        
        security
                .securityMatcher("/api/**")
                .addFilterAfter(authenticationFilter, SecurityContextHolderFilter.class)
                .csrf(management ->
                        management.csrfTokenRepository(csrfTokenRepository))
                .headers(
                headers -> headers.contentSecurityPolicy(
                        csp -> csp.policyDirectives("default-src 'self'; " +
                                "script-src 'self'; " +
                                "img-src 'self'; frame-ancestors 'none';")
                )).
                authorizeHttpRequests(auth -> auth
                .anyRequest().authenticated())
                .cors(cors -> cors.configurationSource(corsConfigurationSource));
        return security.build();

    }


}
