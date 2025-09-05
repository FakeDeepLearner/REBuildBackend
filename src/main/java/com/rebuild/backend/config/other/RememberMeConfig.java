package com.rebuild.backend.config.other;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.RememberMeAuthenticationProvider;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.RememberMeServices;
import org.springframework.security.web.authentication.rememberme.*;

import javax.sql.DataSource;


// Pretty much this entire class is taken from here, with some modifications:
// https://docs.spring.io/spring-security/reference/servlet/authentication/rememberme.html
@Configuration
public class RememberMeConfig {

    @Bean
    public PersistentTokenRepository persistentTokenRepository(DataSource dataSource) {
        JdbcTokenRepositoryImpl tokenRepository = new JdbcTokenRepositoryImpl();
        tokenRepository.setDataSource(dataSource);

        return tokenRepository;
    }

    @Bean
    public RememberMeServices rememberMeServices(UserDetailsService userDetailsService,
                                                 PersistentTokenRepository persistentTokenRepository) {
        PersistentTokenBasedRememberMeServices services = new PersistentTokenBasedRememberMeServices(
                System.getenv("REMEMBER_ME_KEY"),
                userDetailsService, persistentTokenRepository);
        services.setTokenValiditySeconds(86400 * 7);
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
    public RememberMeAuthenticationProvider rememberMeAuthenticationProvider(){
        return new RememberMeAuthenticationProvider(System.getenv("REMEMBER_ME_KEY"));

    }


}
