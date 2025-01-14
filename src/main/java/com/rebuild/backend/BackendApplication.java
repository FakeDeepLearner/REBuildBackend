package com.rebuild.backend;

import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.transaction.annotation.EnableTransactionManagement;


@SpringBootApplication
@EnableCaching
@EnableWebSecurity
@EnableMethodSecurity
@ConfigurationPropertiesScan
@EnableScheduling
@EnableJpaRepositories
@EnableJpaAuditing
@EnableBatchProcessing
@EnableRabbit
@EnableTransactionManagement
public class BackendApplication {

	public static void main(String[] args) {
		SpringApplication.run(BackendApplication.class, args);
	}

}
