package com.rebuild.backend.config.security;



import com.rebuild.backend.config.properties.AppUrlBase;
import com.rebuild.backend.utils.LogoutController;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;

import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.RememberMeServices;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.authentication.rememberme.RememberMeAuthenticationFilter;


@Configuration
@EnableWebSecurity
public class SecureAuthConfig {

    private final AppUrlBase urlBase;

    private final LogoutController logoutController;

    @Autowired
    public SecureAuthConfig(AppUrlBase urlBase, LogoutController logoutController) {
        this.urlBase = urlBase;
        this.logoutController = logoutController;
    }

    @Bean
    @Order(3)
    public SecurityFilterChain filterChainAuthentication(HttpSecurity security,
                                                         RememberMeServices rememberMeServices,
                                                         RememberMeAuthenticationFilter rememberMeAuthenticationFilter) throws Exception {
        security.
                authorizeHttpRequests(config -> config.
                        requestMatchers(HttpMethod.GET, urlBase.baseUrl() + "/home/**").authenticated().
                        requestMatchers(HttpMethod.POST, urlBase.baseUrl() + "/home/**").authenticated().
                        requestMatchers(HttpMethod.PUT, urlBase.baseUrl() + "/home/**").authenticated().
                        requestMatchers(HttpMethod.DELETE, urlBase.baseUrl() + "/home/**").authenticated().
                        requestMatchers(HttpMethod.PATCH, urlBase.baseUrl() + "/home/**").authenticated())
                .formLogin(login -> login.loginPage("/login").
                                permitAll())
                .oauth2Login(login -> login.loginPage("/login")
                        .permitAll())
                .logout(config -> config.
                    logoutUrl(urlBase.baseUrl() + "/logout").
                    logoutSuccessUrl(urlBase.baseUrl() + "/login?logout=true").
                    addLogoutHandler(logoutController).deleteCookies("JSESSIONID").permitAll())
                .rememberMe(rememberMe ->
                        rememberMe.rememberMeServices(rememberMeServices))
                .addFilterAfter(rememberMeAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
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
