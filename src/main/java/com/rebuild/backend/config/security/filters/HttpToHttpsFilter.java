package com.rebuild.backend.config.security.filters;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class HttpToHttpsFilter extends OncePerRequestFilter implements Ordered {
    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain) throws ServletException, IOException {

        if(request.getScheme().equals("http")){
            //The status code 301 (Moved Permanently) is the standard for redirection to https
            response.setStatus(HttpServletResponse.SC_MOVED_PERMANENTLY);
            //Replace "http" with "https", and then redirect
            String redirectLocation = request.getRequestURL().replace(0, 4, "https").toString();
            response.setHeader("Location", redirectLocation);

        }
        filterChain.doFilter(request, response);
    }

    @Override
    public int getOrder() {
        return 4;
    }
}
