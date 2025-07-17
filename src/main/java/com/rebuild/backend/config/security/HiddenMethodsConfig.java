package com.rebuild.backend.config.security;

import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.filter.HiddenHttpMethodFilter;

@Configuration
public class HiddenMethodsConfig {

    @Bean
    public FilterRegistrationBean<HiddenHttpMethodFilter> hiddenMethodsFilterRegistrationBean() {
        FilterRegistrationBean<HiddenHttpMethodFilter> registrationBean = new FilterRegistrationBean<>();

        registrationBean.setFilter(new HiddenHttpMethodFilter());

        //Matches fo
        registrationBean.addUrlPatterns("/*");

        return registrationBean;

    }
}
