package com.rebuild.backend.config.other;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.multipart.MultipartFile;

@Configuration
public class DotenvConfig {
    @Bean
    public Dotenv dotenv()
    {
        return Dotenv.load();
    }
}
