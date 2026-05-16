package com.rebuild.backend.config.aws;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3AsyncClient;
@Configuration
public class S3ClientConfig {


    @Bean
    public S3AsyncClient s3Client(CustomCredentialsProvider customCredentialsProvider) {

        return S3AsyncClient.builder().credentialsProvider(customCredentialsProvider).
                region(Region.of(System.getenv("AWS_REGION"))).build();

    }
}
