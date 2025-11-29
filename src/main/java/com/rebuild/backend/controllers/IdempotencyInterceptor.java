package com.rebuild.backend.controllers;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class IdempotencyInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request,
                             HttpServletResponse response, Object handler) throws Exception {
        //If the method isn't a post method. just proceed with the request normally.
        if (!"POST".equalsIgnoreCase(request.getMethod())) {
            return true;
        }

        return false;
    }
}
