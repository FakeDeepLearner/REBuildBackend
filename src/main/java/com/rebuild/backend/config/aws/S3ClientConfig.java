package com.rebuild.backend.config.aws;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3AsyncClient;
import software.amazon.awssdk.services.s3.S3Client;

import java.util.concurrent.CompletableFuture;

@Configuration
public class S3ClientConfig {


    @Bean
    public S3AsyncClient s3Client(Dotenv dotenv, CustomCredentialsProvider customCredentialsProvider) {

        return S3AsyncClient.builder().credentialsProvider(customCredentialsProvider).
                region(Region.of(dotenv.get("AWS_S3_REGION"))).build();

    }
}
