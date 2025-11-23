package com.rebuild.backend.config.other;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.web.filter.UrlHandlerFilter;

@Configuration
public class UrlHandlingConfig {

    //This will redirect to the original endpoint if there is a trailing slash at the end of the request's URL.
    @Bean
    public UrlHandlerFilter handlerFilter()
    {

        return UrlHandlerFilter
                .trailingSlashHandler("/api/**", "/home/**").redirect(HttpStatus.PERMANENT_REDIRECT)
                .build();
    }
}

