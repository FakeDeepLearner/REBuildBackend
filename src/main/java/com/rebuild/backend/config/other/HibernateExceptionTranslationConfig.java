package com.rebuild.backend.config.other;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.dao.annotation.PersistenceExceptionTranslationPostProcessor;

@Configuration
public class HibernateExceptionTranslationConfig {

    @Bean
    public PersistenceExceptionTranslationPostProcessor hibernateTranslation(){
        return new PersistenceExceptionTranslationPostProcessor();
    }

}
