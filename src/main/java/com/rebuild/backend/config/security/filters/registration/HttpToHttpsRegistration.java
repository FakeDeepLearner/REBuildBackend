package com.rebuild.backend.config.security.filters.registration;


import com.rebuild.backend.config.security.filters.HttpToHttpsFilter;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class HttpToHttpsRegistration {

    private final HttpToHttpsFilter filter;

    public HttpToHttpsRegistration(HttpToHttpsFilter filter) {
        this.filter = filter;
    }

    @Bean
    public FilterRegistrationBean<HttpToHttpsFilter> registrationBean(){
        FilterRegistrationBean<HttpToHttpsFilter> registrationBean = new FilterRegistrationBean<>();
        registrationBean.setFilter(filter);
        registrationBean.addUrlPatterns("/api/*");
        return registrationBean;
    }
}
