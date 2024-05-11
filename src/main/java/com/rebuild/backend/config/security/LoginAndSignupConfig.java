package com.rebuild.backend.config.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;

@Configuration
@EnableWebSecurity
public class LoginAndSignupConfig {

    @Bean
    public SecurityFilterChain filterChainLoginSignup(HttpSecurity security) throws Exception {
        RequestMatcher loginFail = new AntPathRequestMatcher("/login?error=true", HttpMethod.GET.toString());
        RequestMatcher logoutSuccess = new AntPathRequestMatcher("/login?logout=true", HttpMethod.GET.toString());
        security.authorizeHttpRequests(config ->
                config.requestMatchers(HttpMethod.GET, "/signup").permitAll().
                        requestMatchers(loginFail, logoutSuccess).authenticated()).

                formLogin(login -> login.
                        loginPage("/login").
                                permitAll().
                        failureForwardUrl("/login?error=true")).

                logout(logout -> logout.
                        logoutUrl("/logout").
                        logoutSuccessUrl("/login?logout=true").
                        invalidateHttpSession(true).permitAll());

        return security.build();
    }
}
