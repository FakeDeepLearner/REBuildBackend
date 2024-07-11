package com.rebuild.backend.config.security.filters;

import com.rebuild.backend.service.rate_limiting.IPRateLimitingService;
import com.rebuild.backend.service.rate_limiting.UserRateLimitingService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class RateLimitingFilter extends OncePerRequestFilter {

    private final IPRateLimitingService ipRateLimitingService;

    private final UserRateLimitingService userRateLimitingService;

    @Autowired
    public RateLimitingFilter(IPRateLimitingService ipRateLimitingService,
                              UserRateLimitingService userRateLimitingService) {
        this.ipRateLimitingService = ipRateLimitingService;
        this.userRateLimitingService = userRateLimitingService;
    }

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain) throws ServletException, IOException {

        filterChain.doFilter(request, response);

    }
}
