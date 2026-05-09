package com.rebuild.backend.config.other;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.RememberMeAuthenticationProvider;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.RememberMeServices;
import org.springframework.security.web.authentication.rememberme.*;



// Pretty much this entire class is taken from here, with some modifications:
// https://docs.spring.io/spring-security/reference/servlet/authentication/rememberme.html
@Configuration
@SuppressWarnings("removal")
public class RememberMeConfig {

    private static final int SECONDS_PER_DAY = 86400;

    //The tokens will be valid for 7 days
    private static final int TOKEN_VALIDITY_SECONDS = 7 * SECONDS_PER_DAY;

    @Bean
    public PersistentTokenRepository persistentTokenRepository(JdbcTemplate jdbcTemplate) {
        JdbcTokenRepositoryImpl tokenRepository = new JdbcTokenRepositoryImpl();
        tokenRepository.setJdbcTemplate(jdbcTemplate);

        return tokenRepository;
    }

    @Bean
    public RememberMeServices rememberMeServices(UserDetailsService userDetailsService,
                                                 PersistentTokenRepository persistentTokenRepository) {
        PersistentTokenBasedRememberMeServices services = new PersistentTokenBasedRememberMeServices(
                System.getenv("REMEMBER_ME_KEY"),
                userDetailsService, persistentTokenRepository);
        services.setTokenValiditySeconds(TOKEN_VALIDITY_SECONDS);
        services.setParameter("remember-me");

        return services;
    }

    @Bean
    public RememberMeAuthenticationFilter rememberMeAuthenticationFilter(RememberMeServices rememberMeServices,
                                                                         AuthenticationManager authenticationManager) {

        return new RememberMeAuthenticationFilter(authenticationManager,
                rememberMeServices);
    }


    @Bean
    public RememberMeAuthenticationProvider rememberMeAuthenticationProvider() {
        return new RememberMeAuthenticationProvider(System.getenv("REMEMBER_ME_KEY"));

    }


}
