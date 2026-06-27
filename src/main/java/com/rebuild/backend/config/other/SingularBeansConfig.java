package com.rebuild.backend.config.other;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.dao.annotation.PersistenceExceptionTranslationPostProcessor;
import org.springframework.http.HttpStatus;
import org.springframework.validation.beanvalidation.MethodValidationPostProcessor;
import org.springframework.web.filter.UrlHandlerFilter;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;

@Configuration
public class SingularBeansConfig {

    @Bean
    public PersistenceExceptionTranslationPostProcessor hibernateTranslation(){
        return new PersistenceExceptionTranslationPostProcessor();
    }

    @Bean(name = "processor")
    public MethodValidationPostProcessor processor(){
        return new MethodValidationPostProcessor();
    }

    @Bean
    public S3Client s3Client()
    {
        return S3Client.builder().credentialsProvider(() ->
                AwsBasicCredentials.create(System.getenv("AWS_ACCESS_KEY_ID"),
                System.getenv("AWS_SECRET_ACCESS_KEY"))).
                region(Region.CA_CENTRAL_1).build();
    }

}
