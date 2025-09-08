package com.rebuild.backend.config.security;



import com.rebuild.backend.utils.LogoutController;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;

import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.oauth2.client.OAuth2LoginConfigurer;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AnonymousAuthenticationFilter;
import org.springframework.security.web.authentication.RememberMeServices;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.authentication.rememberme.RememberMeAuthenticationFilter;


@Configuration
@EnableWebSecurity
public class SecureAuthConfig {

    private final LogoutController logoutController;

    @Autowired
    public SecureAuthConfig(LogoutController logoutController) {
        this.logoutController = logoutController;
    }

    @Bean
    @Order(3)
    public SecurityFilterChain filterChainAuthentication(HttpSecurity security,
                                                         RememberMeServices rememberMeServices,
                                                         RememberMeAuthenticationFilter rememberMeAuthenticationFilter,
                                                         ClientRegistrationRepository registrationRepository) throws Exception {
        security.
                authorizeHttpRequests(config -> config.
                        requestMatchers(HttpMethod.GET, "/home/**").authenticated().
                        requestMatchers(HttpMethod.POST, "/home/**").authenticated().
                        requestMatchers(HttpMethod.PUT,  "/home/**").authenticated().
                        requestMatchers(HttpMethod.DELETE,  "/home/**").authenticated().
                        requestMatchers(HttpMethod.PATCH,  "/home/**").authenticated())
                //Form login is disabled, because it expects everything to be done in one endpoint.
                //However, our standard (non-OAuth) login flow has 2 separate endpoints that need to be called.
                .formLogin(AbstractHttpConfigurer::disable)
                .oauth2Login(config ->
                        config.clientRegistrationRepository(registrationRepository).
                                loginPage("/login"))
                .logout(config -> config.
                    logoutUrl("/logout").
                    logoutSuccessUrl("/login?logout=true").
                    addLogoutHandler(logoutController).deleteCookies("JSESSIONID").permitAll())
                .rememberMe(rememberMe ->
                        rememberMe.rememberMeServices(rememberMeServices).useSecureCookie(true).
                                rememberMeCookieName("REMEMBERED_USER_COOKIE"))
                .addFilterAfter(rememberMeAuthenticationFilter, AnonymousAuthenticationFilter.class);
        return security.build();


    }

    @Bean
    public AuthenticationEntryPoint unauthorizedAuthEntry(){
        return (request, response, authException) -> {
            //401 = Unauthorized
            response.setStatus(401);
            response.setHeader("WWW-Authenticate",
                    "Bearer error=\"invalid_token\", " +
                            "error_description=\"Your token has either expired, its credentials do not match," +
                            "or its claims do not match\"");
            response.getWriter().write(authException.getMessage());

        };
    }
}
