package com.rebuild.backend.config.security;

import com.rebuild.backend.exceptions.jwt_exceptions.NoJWTTokenException;
import com.rebuild.backend.service.token_services.JWTTokenService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class JWTVerificationFilter extends OncePerRequestFilter {
    private final JWTTokenService tokenService;

    private final UserDetailsService detailsService;

    @Autowired
    public JWTVerificationFilter(JWTTokenService tokenService,
                                @Qualifier("details") UserDetailsService detailsService) {
        this.tokenService = tokenService;
        this.detailsService = detailsService;
    }


    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain) throws ServletException, IOException {
        String servletPath = request.getServletPath();
        // If we are trying to log in or to refresh our token, just let us through
        if(servletPath.equals("/api/refresh_token") || servletPath.equals("/login")){
            filterChain.doFilter(request, response);
            return;
        }
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        try {
            String accessToken = tokenService.extractTokenFromRequest(request);
            String extractedUsername = tokenService.extractSubject(accessToken);
            //The user is not authenticated yet
            if (extractedUsername != null && SecurityContextHolder.getContext().getAuthentication() != null){
                UserDetails details = detailsService.loadUserByUsername(extractedUsername);
                if (tokenService.isTokenValid(accessToken, details)){
                    UsernamePasswordAuthenticationToken newToken = new UsernamePasswordAuthenticationToken(
                            details, null, details.getAuthorities()
                    );
                    newToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(newToken);
                }
            }
            filterChain.doFilter(request, response);
        }
        catch (NoJWTTokenException e){
            filterChain.doFilter(request, response);
        }

    }

}
