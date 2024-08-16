package com.rebuild.backend.config.security.filters.registration;

import com.rebuild.backend.config.security.filters.JWTVerificationFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class JwtVerificationRegistration {

    private final JWTVerificationFilter verificationFilter;

    @Autowired
    public JwtVerificationRegistration(JWTVerificationFilter verificationFilter) {
        this.verificationFilter = verificationFilter;
    }

    @Bean
    public FilterRegistrationBean<JWTVerificationFilter> registrationBean(){
        FilterRegistrationBean<JWTVerificationFilter> registrationBean = new FilterRegistrationBean<>();

        registrationBean.setFilter(verificationFilter);

        registrationBean.addUrlPatterns("/api/*");

        return registrationBean;
    }

}
