package com.rebuild.backend.config.security;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.RememberMeAuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.core.userdetails.UserDetailsPasswordService;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class AuthenticationManagerConfig {

    @Bean
    public AuthenticationManager authManager(@Qualifier("details") UserDetailsService detailsService,
                                             @Qualifier("password_service")
                                             UserDetailsPasswordService passwordService,
                                             @Qualifier("peppered") PasswordEncoder passwordEncoder,
                                             RememberMeAuthenticationProvider rememberMeAuthenticationProvider) {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider(detailsService);
        provider.setPasswordEncoder(passwordEncoder);
        provider.setUserDetailsPasswordService(passwordService);
        return new ProviderManager(provider, rememberMeAuthenticationProvider);
    }

}
