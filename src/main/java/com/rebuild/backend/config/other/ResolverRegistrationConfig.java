package com.rebuild.backend.config.other;

import com.rebuild.backend.utils.CustomAuthPrincipalResolver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.web.method.annotation.AuthenticationPrincipalArgumentResolver;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

@Configuration
public class ResolverRegistrationConfig implements WebMvcConfigurer {

    private final CustomAuthPrincipalResolver customAuthPrincipalResolver;

    @Autowired
    public ResolverRegistrationConfig(CustomAuthPrincipalResolver customAuthPrincipalResolver) {
        this.customAuthPrincipalResolver = customAuthPrincipalResolver;
    }

    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
        resolvers.add(customAuthPrincipalResolver);
    }
}
