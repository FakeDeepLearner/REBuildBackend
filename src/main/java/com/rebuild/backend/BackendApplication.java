package com.rebuild.backend;

import com.rebuild.backend.config.properties.HTTPContentType;
import com.rebuild.backend.config.properties.MailAppCredentials;
import com.rebuild.backend.config.properties.PepperValue;
import com.rebuild.backend.config.properties.RSAKeys;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cache.annotation.EnableCaching;

@EnableConfigurationProperties({RSAKeys.class, PepperValue.class,
		HTTPContentType.class, MailAppCredentials.class})
@SpringBootApplication
@EnableCaching
public class BackendApplication {

	public static void main(String[] args) {
		SpringApplication.run(BackendApplication.class, args);
	}

}
