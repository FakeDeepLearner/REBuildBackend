package com.rebuild.backend.config.other;

import lombok.NonNull;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class CORSConfig {

    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(@NonNull CorsRegistry registry) {
                //NOTE: This is normally invalid, but we will change this once we have an actual frontend.
                registry.addMapping("/**").
                        allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH", "HEAD").
                        allowCredentials(true).maxAge(3600).allowedOrigins("*").allowedHeaders("*");
            }
        };
    }
}
