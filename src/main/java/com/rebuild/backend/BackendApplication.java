package com.rebuild.backend;

import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.EnableJdbcJobRepository;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;


@SpringBootApplication
@EnableCaching
@EnableWebSecurity
@EnableMethodSecurity
@EnableScheduling
@EnableJpaRepositories
@EnableJpaAuditing
@EnableBatchProcessing
@EnableJdbcJobRepository
@EnableRabbit
@EnableTransactionManagement
@EnableWebSocketMessageBroker
public class BackendApplication {

	static void main(String[] args) {
		SpringApplication.run(BackendApplication.class, args);
	}

}
