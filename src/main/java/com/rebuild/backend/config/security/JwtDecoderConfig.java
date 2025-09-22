package com.rebuild.backend.config.security;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;

import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class JwtDecoderConfig {

    @Bean(name = "rsaKey")
    public RSAPublicKey publicKey(Dotenv dotenv) throws NoSuchAlgorithmException, InvalidKeySpecException {
        String encodedBase64Key = dotenv.get("JWT_DECODER_PUBLIC_KEY");
        byte[] decodedBase64Key = Base64.getDecoder().decode(encodedBase64Key);
        KeyFactory rsaFactory = KeyFactory.getInstance("RSA");

        X509EncodedKeySpec keySpec = new X509EncodedKeySpec(decodedBase64Key);

        PublicKey rsaKey = rsaFactory.generatePublic(keySpec);

        return (RSAPublicKey) rsaKey;
    }

    @Bean
    public JwtDecoder decoder(RSAPublicKey rsaKey) {
        return NimbusJwtDecoder.withPublicKey(rsaKey).build();
    }



}
