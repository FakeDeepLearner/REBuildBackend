package com.rebuild.backend.config.properties;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.beanvalidation.MethodValidationPostProcessor;

@Configuration
public class ValidationsConfig {

    @Bean(name = "processor")
    public MethodValidationPostProcessor processor(){
        return new MethodValidationPostProcessor();
    }
}
