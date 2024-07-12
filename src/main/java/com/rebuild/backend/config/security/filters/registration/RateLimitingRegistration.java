package com.rebuild.backend.config.security.filters.registration;

import com.rebuild.backend.config.security.filters.RateLimitingFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RateLimitingRegistration {

    private final RateLimitingFilter rateLimitingFilter;

    @Autowired
    public RateLimitingRegistration(RateLimitingFilter rateLimitingFilter) {
        this.rateLimitingFilter = rateLimitingFilter;
    }

    @Bean
    public FilterRegistrationBean<RateLimitingFilter> registerRateLimiter(){
        FilterRegistrationBean<RateLimitingFilter> registrationBean = new FilterRegistrationBean<>();
        registrationBean.setFilter(rateLimitingFilter);
        registrationBean.addUrlPatterns("/login");
        return registrationBean;
    }

}
