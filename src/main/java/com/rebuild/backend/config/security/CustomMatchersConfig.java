package com.rebuild.backend.config.security;

import com.rebuild.backend.config.properties.AppUrlBase;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;

@Configuration
@EnableWebSecurity
public class CustomMatchersConfig {
    private final AppUrlBase urlBase;

    @Autowired
    public CustomMatchersConfig(AppUrlBase urlBase) {
        this.urlBase = urlBase;
    }

    @Bean
    @Order(2)
    public SecurityFilterChain filterChainLoginSignup(HttpSecurity security) throws Exception {
        RequestMatcher loginFail = new AntPathRequestMatcher(urlBase.baseUrl() +
                "/login?error=true", HttpMethod.GET.toString());
        RequestMatcher logoutSuccess = new AntPathRequestMatcher(urlBase.baseUrl() +
                "/login?logout=true", HttpMethod.GET.toString());
        security.authorizeHttpRequests(config ->
                config.requestMatchers(loginFail, logoutSuccess).permitAll());

        return security.build();
    }
}
