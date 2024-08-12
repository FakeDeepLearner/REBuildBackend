package com.rebuild.backend.config.security.filters;

import com.rebuild.backend.exceptions.token_exceptions.TokenBlackListedException;
import com.rebuild.backend.model.entities.enums.TokenBlacklistPurpose;
import com.rebuild.backend.service.token_services.TokenBlacklistService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class JwtBlacklistFilter extends OncePerRequestFilter implements Ordered {

    private final TokenBlacklistService blacklistService;

    @Autowired
    public JwtBlacklistFilter( TokenBlacklistService blacklistService) {
        this.blacklistService = blacklistService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain) throws ServletException, IOException {

        String requestURI = request.getRequestURI();
        TokenBlacklistPurpose purpose = getPurposeFromURI(requestURI);
        String token = request.getParameter("token");

        if(token != null && purpose != null){
            if(blacklistService.isTokenBlacklisted(token, purpose)){
                filterChain.doFilter(request, response);
                throw new TokenBlackListedException("This token is blacklisted");
            }
        }
        filterChain.doFilter(request, response);
    }

    private TokenBlacklistPurpose getPurposeFromURI(String URI){
        if (URI.endsWith("/activate")){
            return TokenBlacklistPurpose.ACCOUNT_ACTIVATION;
        }
        if (URI.endsWith("/reset")){
            return TokenBlacklistPurpose.PASSWORD_CHANGE;
        }
        return TokenBlacklistPurpose.EMAIL_CHANGE;

    }

    @Override
    public int getOrder() {
        return 2;
    }
}
