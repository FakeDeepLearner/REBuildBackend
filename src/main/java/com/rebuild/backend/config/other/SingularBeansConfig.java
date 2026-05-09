package com.rebuild.backend.config.other;

import com.cloudinary.Cloudinary;
import com.sendgrid.SendGrid;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.dao.annotation.PersistenceExceptionTranslationPostProcessor;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.validation.beanvalidation.MethodValidationPostProcessor;
import org.springframework.web.filter.UrlHandlerFilter;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

@Configuration
public class SingularBeansConfig {
    @Bean
    public SendGrid sendGrid()
    {
        return new SendGrid(System.getenv("TWILIO_SENDGRID_KEY"));
    }

    @Bean
    public Cloudinary cloudinary() {
        return new Cloudinary(System.getenv("CLOUDINARY_URL"));
    }

    @Bean
    public PersistenceExceptionTranslationPostProcessor hibernateTranslation(){
        return new PersistenceExceptionTranslationPostProcessor();
    }

    //This will redirect to the original endpoint if there is a trailing slash at the end of the request's URL.
    @Bean
    public UrlHandlerFilter handlerFilter()
    {
        return UrlHandlerFilter
                .trailingSlashHandler("/api/**", "/home/**").redirect(HttpStatus.PERMANENT_REDIRECT)
                .build();
    }


    @Bean(name = "processor")
    public MethodValidationPostProcessor processor(){
        return new MethodValidationPostProcessor();
    }

    @Bean(name = "executor")
    public Executor virtualThreadExecutor()
    {
        return Executors.newVirtualThreadPerTaskExecutor();
    }

    @Bean(name = "encoder")
    public PasswordEncoder encoder()
    {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public JdbcTemplate jdbcTemplate() {
        return new JdbcTemplate();
    }

}
