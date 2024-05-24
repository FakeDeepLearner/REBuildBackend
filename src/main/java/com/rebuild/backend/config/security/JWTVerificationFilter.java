package com.rebuild.backend.config.security;

import com.rebuild.backend.exceptions.NoJWTTokenException;
import com.rebuild.backend.service.JWTTokenService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
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

        try {
            String jwtToken = extractJWTToken(request);
            String extractedUsername = tokenService.extractUsername(jwtToken);
            //The user is not authenticated yet
            if (extractedUsername != null && SecurityContextHolder.getContext().getAuthentication() != null){
                UserDetails details = detailsService.loadUserByUsername(extractedUsername);
                if (tokenService.isTokenValid(jwtToken, details)){
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

    private String extractJWTToken(HttpServletRequest request){
        String auth = request.getHeader("Authorization");
        if (auth != null && auth.startsWith("Bearer")){
            return auth.substring(7);
        }
        else{
            throw new NoJWTTokenException("No JWT token is in the request");
        }
    }




}
