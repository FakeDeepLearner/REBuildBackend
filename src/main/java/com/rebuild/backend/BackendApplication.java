package com.rebuild.backend;

import com.rebuild.backend.config.properties.PepperValue;
import com.rebuild.backend.config.properties.RSAKeys;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@EnableConfigurationProperties({RSAKeys.class, PepperValue.class})
@SpringBootApplication
public class BackendApplication {

	public static void main(String[] args) {
		SpringApplication.run(BackendApplication.class, args);
	}

}
