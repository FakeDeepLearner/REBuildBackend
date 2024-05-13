package com.rebuild.backend.config.security;


import org.springframework.boot.context.properties.ConfigurationProperties;

import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;

@ConfigurationProperties(prefix = "rsa")
public record RSAProperties(RSAPublicKey publicKey, RSAPrivateKey privateKey) {

}
