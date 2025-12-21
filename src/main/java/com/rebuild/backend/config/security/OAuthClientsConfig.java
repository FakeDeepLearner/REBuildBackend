package com.rebuild.backend.config.security;

import com.sun.security.auth.UserPrincipal;
import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.oauth2.client.CommonOAuth2Provider;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserService;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.registration.InMemoryClientRegistrationRepository;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;

import java.util.Arrays;

@Configuration
public class OAuthClientsConfig {

    @Bean
    public ClientRegistrationRepository clientRegistrationRepository(Dotenv dotenv)
    {

        ClientRegistration github = CommonOAuth2Provider.GITHUB.getBuilder("github")
                .clientId(dotenv.get("GITHUB_CLIENT_ID"))
                .clientSecret(dotenv.get("GITHUB_CLIENT_SECRET"))
                .scope(Arrays.asList("read:user", "user:email"))
                .build();

        ClientRegistration google = CommonOAuth2Provider.GOOGLE.getBuilder("google").
                clientId(dotenv.get("GOOGLE_CLIENT_ID"))
                .clientSecret(dotenv.get("GOOGLE_CLIENT_SECRET"))
                .build();



        return new InMemoryClientRegistrationRepository(github, google);
    }
}
