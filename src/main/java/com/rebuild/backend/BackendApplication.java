package com.rebuild.backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;


@SpringBootApplication
@EnableWebSecurity
@EnableMethodSecurity
@EnableJpaRepositories
@EnableTransactionManagement
@EnableWebSocketMessageBroker
public class BackendApplication {

	static void main(String[] args) {
		SpringApplication.run(BackendApplication.class, args);
	}

}
