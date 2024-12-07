package com.rebuild.backend.config.security.filters;

import com.rebuild.backend.exceptions.unauthorized_exceptions.AccountInactivityException;
import com.rebuild.backend.exceptions.unauthorized_exceptions.AccountIsLockedException;
import com.rebuild.backend.exceptions.unauthorized_exceptions.AccountNotActivatedException;
import com.rebuild.backend.service.user_services.CustomUserDetailsService;
import com.rebuild.backend.service.token_services.JWTTokenService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.Ordered;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class JWTVerificationFilter extends OncePerRequestFilter implements Ordered {
    private final JWTTokenService tokenService;
    private final CustomUserDetailsService detailsService;

    @Autowired
    public JWTVerificationFilter(JWTTokenService tokenService,
                                 CustomUserDetailsService detailsService) {
        this.tokenService = tokenService;
        this.detailsService = detailsService;
    }


    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain) throws ServletException, IOException {
        String servletPath = request.getServletPath();
        /*
          Normally, the filter applies to all paths that start with "/api", but we need to be able
          to update our token without requiring one, so we let the refresh token endpoint true by processing
          the other filters and returning immediately
         */
        if(servletPath.equals("/api/refresh_token")){
            filterChain.doFilter(request, response);
            return;
        }
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);

            String accessToken = tokenService.extractTokenFromRequest(request);
            String subject = tokenService.extractSubject(accessToken);
            //The user is not authenticated yet
            if (subject != null && SecurityContextHolder.getContext().getAuthentication() != null){
                UserDetails details = detailsService.loadUserByUsername(subject);
                checkUserDetails(details);
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

    private void checkUserDetails(UserDetails details){
        if(!details.isEnabled()){
            throw new AccountNotActivatedException("Your account has not been activated yet, please activate it");
        }
        if(!details.isAccountNonExpired()){
            throw new AccountInactivityException("Your account has been blocked due to prolonged inactivity, " +
                    "please unlock it before continuing");
        }
        if(!details.isAccountNonLocked()){
            throw new AccountIsLockedException("Your account has been locked due to suspicious activity, please unlock it");
        }
    }

    @Override
    public int getOrder() {
        return 3;
    }
}
