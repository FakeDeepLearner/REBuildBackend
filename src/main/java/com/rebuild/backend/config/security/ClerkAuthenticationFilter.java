package com.rebuild.backend.config.security;

import com.clerk.backend_api.helpers.security.AuthenticateRequest;
import com.clerk.backend_api.helpers.security.models.AuthenticateRequestOptions;
import com.clerk.backend_api.helpers.security.models.RequestState;
import com.rebuild.backend.model.entities.user_entities.User;
import com.rebuild.backend.repository.user_repositories.UserRepository;
import io.jsonwebtoken.security.Jwks;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken;
import org.springframework.stereotype.Controller;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.*;

@Configuration
public class ClerkAuthenticationFilter extends OncePerRequestFilter {

    private final UserRepository userRepository;

    @Autowired
    public ClerkAuthenticationFilter(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    //Anything in the webhook controller is exempt from this filter
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String uri =  request.getRequestURI();

        return uri.startsWith("/webhooks/");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String clerkId = getClerkId(request);

        if (clerkId == null) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("Authentication failed");
            filterChain.doFilter(request, response);
        }
        else {
            User foundUser = userRepository.findByClerkId(clerkId).orElseThrow();

            PreAuthenticatedAuthenticationToken authenticationToken =
                    new PreAuthenticatedAuthenticationToken(foundUser,
                            foundUser.getClerkId(), null);
            SecurityContextHolder.getContext().setAuthentication(authenticationToken);
            filterChain.doFilter(request, response);
        }

    }

    private String getClerkId(HttpServletRequest request)
    {
        Map<String, List<String>> headers = new HashMap<>();

        request.getHeaderNames().asIterator().forEachRemaining(
                headerName ->
                {
                    headers.put(headerName, Collections.singletonList(request.getHeader(headerName)));
                }
        );

        RequestState state = AuthenticateRequest.authenticateRequest(headers, AuthenticateRequestOptions.
                secretKey(System.getenv("CLERK_SECRET_KEY"))
                .authorizedParty("https://rerebuild.ca").build());

        if (state.isSignedIn())
        {
            return state.claims().get().getSubject();
        }
        return null;
    }

}
