package com.rebuild.backend.config.aws;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.AwsCredentials;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;

@Component
public class CustomCredentialsProvider implements AwsCredentialsProvider {

    private final Dotenv dotenv;

    @Autowired
    public CustomCredentialsProvider(Dotenv dotenv) {
        this.dotenv = dotenv;
    }

    @Override
    public AwsCredentials resolveCredentials() {
        return AwsBasicCredentials.create(dotenv.get("AWS_ACCESS_KEY_ID"),
                dotenv.get("AWS_SECRET_ACCESS_KEY"));
    }
}
