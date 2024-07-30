package com.rebuild.backend.config.security.filters;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rebuild.backend.exceptions.rate_limiting_exceptions.IPAddressBlockedException;
import com.rebuild.backend.exceptions.rate_limiting_exceptions.UserBlockedException;
import com.rebuild.backend.model.forms.LoginForm;
import com.rebuild.backend.service.rate_limiting.IPRateLimitingService;
import com.rebuild.backend.service.rate_limiting.UserRateLimitingService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Arrays;
import java.util.Scanner;

@Component
public class RateLimitingFilter extends OncePerRequestFilter implements Ordered {

    private final IPRateLimitingService ipRateLimitingService;

    private final UserRateLimitingService userRateLimitingService;
    private final ObjectMapper mapper;

    @Autowired
    public RateLimitingFilter(IPRateLimitingService ipRateLimitingService,
                              UserRateLimitingService userRateLimitingService,
                              ObjectMapper mapper) {
        this.ipRateLimitingService = ipRateLimitingService;
        this.userRateLimitingService = userRateLimitingService;
        this.mapper = mapper;
    }

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain) throws ServletException, IOException {
        String ipAddress = request.getRemoteAddr();
        blockAllProxyAddresses(request, response, filterChain);
        ipRateLimitingService.registerConnection(ipAddress);
        if (ipRateLimitingService.isAddressBlocked(ipAddress)){
            filterChain.doFilter(request, response);
            throw new IPAddressBlockedException("Due to making too many requests in a short time, " +
                    "you have been temporarily blocked",
                    Duration.from(ipRateLimitingService.getTimeRemaining(ipAddress)));
        }
        LoginForm reqBody = extractRequestBody(request);
        if(reqBody != null) {
            String email = reqBody.email();
            userRateLimitingService.registerConnection(email);
            if (userRateLimitingService.isEmailBlocked(email)){
                filterChain.doFilter(request, response);
                throw new UserBlockedException("Your account has been temporarily suspended because you have made too" +
                        "many attempts to log in", Duration.from(userRateLimitingService.getTimeRemaining(email)));
            }
        }
        filterChain.doFilter(request, response);

    }

    private LoginForm extractRequestBody(HttpServletRequest request) throws IOException {
        StringBuilder builder = new StringBuilder();
        String method = request.getMethod();
        //This already retrieves the body
        InputStream requestStream = request.getInputStream();
        if(method.equals("POST")){
            //Delimit based on network newlines
            Scanner scanner = new Scanner(requestStream, StandardCharsets.UTF_8).useDelimiter("\r\n");
            while(scanner.hasNext()){
                String nextLine = scanner.next();
                builder.append(nextLine);
            }
            String fullBody = builder.toString();
            return mapper.readValue(fullBody, LoginForm.class);
        }
        return null;
    }

    private void blockAllProxyAddresses(@NonNull  HttpServletRequest request,
                                        @NonNull HttpServletResponse response,
                                        @NonNull FilterChain filterChain) {
        String allProxyIps = request.getHeader("X-Forwarded-For");
        //If it is null, then it means the user isn't connecting through any proxies
        if(allProxyIps != null){
            String[] allAddressesSeparate = allProxyIps.split(",");
            Arrays.stream(allAddressesSeparate).forEach((address) ->
                    ipRateLimitingService.registerConnection(address.trim()));
            Arrays.stream(allAddressesSeparate).forEach((address) -> {
                if(ipRateLimitingService.isAddressBlocked(address.trim())){
                    try {
                        //If any of the proxy ip addresses are blocked, block all the others as wll
                        Arrays.stream(allAddressesSeparate).forEach((newAddress) ->
                                ipRateLimitingService.blockIpAddress(newAddress.trim()));
                        filterChain.doFilter(request, response);
                    } catch (IOException | ServletException e) {
                        throw new RuntimeException(e);
                    }
                    throw new IPAddressBlockedException("Due to making too many requests in a short time, " +
                            "you have been temporarily blocked",
                            Duration.from(ipRateLimitingService.getTimeRemaining(address.trim())));
                }
            });
        }
    }

    @Override
    public int getOrder() {
        return 1;
    }
}
