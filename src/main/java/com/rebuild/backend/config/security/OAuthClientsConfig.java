package com.rebuild.backend.config.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.registration.InMemoryClientRegistrationRepository;
import org.springframework.security.oauth2.core.AuthorizationGrantType;

@Configuration
public class OAuthClientsConfig {

    @Value("${GITHUB_CLIENT_ID}")
    private String githubClientId;

    @Value("${GITHUB_CLIENT_SECRET}")
    private String githubClientSecret;

    @Value("${GOOGLE_CLIENT_ID}")
    private String googleClientId;

    @Value("${GOOGLE_CLIENT_SECRET}")
    private String googleClientSecret;

    @Bean
    public ClientRegistrationRepository clientRegistrationRepository()
    {
        ClientRegistration githubClient = ClientRegistration.withRegistrationId("github").
                clientId(githubClientId).
                clientSecret(githubClientSecret).
                scope("read:user", "user:email").
                clientName("GitHub").
                authorizationUri("https://github.com/login/oauth/authorize").
                tokenUri("https://github.com/login/oauth/access_token").
                userInfoUri("https://api.github.com/user").
                redirectUri("http://localhost:8080/login/oauth2/code/github").
                authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE).
                build();

        ClientRegistration googleClient = ClientRegistration.withRegistrationId("google").
                clientId(googleClientId).
                clientSecret(googleClientSecret).
                clientName("Google").
                scope("openid", "profile", "email").
                authorizationUri("https://accounts.google.com/o/oauth2/auth").
                tokenUri("https://oauth2.googleapis.com/token").
                userInfoUri("https://openidconnect.googleapis.com/v1/userinfo").
                redirectUri("http://localhost:8080/login/oauth2/code/google").
                authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE).
                build();


        return new InMemoryClientRegistrationRepository(githubClient, googleClient);
    }
}
