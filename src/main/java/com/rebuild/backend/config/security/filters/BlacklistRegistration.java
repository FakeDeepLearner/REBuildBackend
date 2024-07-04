package com.rebuild.backend.config.security.filters;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class BlacklistRegistration {

    private final JwtBlacklistFilter blacklistFilter;

    @Autowired
    public BlacklistRegistration(JwtBlacklistFilter blacklistFilter) {
        this.blacklistFilter = blacklistFilter;
    }

    @Bean
    public FilterRegistrationBean<JwtBlacklistFilter> registerBlacklist(){
        FilterRegistrationBean<JwtBlacklistFilter> registrationBean = new FilterRegistrationBean<>();
        registrationBean.addUrlPatterns("/api/activate", "/api/change_email", "/api/reset");
        registrationBean.setFilter(blacklistFilter);
        return registrationBean;
    }
}
