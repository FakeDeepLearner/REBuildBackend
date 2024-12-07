package com.rebuild.backend.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rebuild.backend.config.properties.AppUrlBase;
import com.rebuild.backend.model.entities.enums.TokenBlacklistPurpose;
import com.rebuild.backend.model.forms.auth_forms.LoginForm;
import com.rebuild.backend.model.forms.dtos.jwt_tokens_dto.AccessAndRefreshTokensDTO;
import com.rebuild.backend.service.token_services.JWTTokenService;
import com.rebuild.backend.service.token_services.TokenBlacklistService;
import com.rebuild.backend.service.user_services.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.LogoutHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

@Component
public class LogoutController implements LogoutHandler {
    private final TokenBlacklistService tokenBlacklistService;

    private final UserService userService;

    private final ObjectMapper objectMapper;

    private final JWTTokenService tokenService;

    private final AppUrlBase base;

    @Autowired
    public LogoutController(TokenBlacklistService tokenBlacklistService,
                            UserService userService,
                            ObjectMapper objectMapper, JWTTokenService tokenService, AppUrlBase base) {
        this.tokenBlacklistService = tokenBlacklistService;
        this.userService = userService;
        this.objectMapper = objectMapper;
        this.tokenService = tokenService;
        this.base = base;
    }

    @Override
    public void logout(HttpServletRequest request,
                       HttpServletResponse response,
                       Authentication authentication) {
        try {
            AccessAndRefreshTokensDTO requestBody = extractRequestBody(request);
            if (requestBody != null) {
                tokenBlacklistService.blacklistTokenFor(requestBody.access_token(),
                        TokenBlacklistPurpose.AUTHENTICATION);
                tokenBlacklistService.blacklistTokenFor(requestBody.refresh_token(),
                        TokenBlacklistPurpose.AUTHENTICATION);
            }
            userService.invalidateAllSessions(authentication.getName());
            //Setting the max age to a negative number instructs the browser to delete it
            ResponseCookie deleteRefreshCookie = ResponseCookie.from("refreshToken", "").
                    httpOnly(true).
                    secure(true).
                    sameSite("Strict").
                    maxAge(-1).
                    path("/").
                    build();
            response.setHeader(HttpHeaders.SET_COOKIE, deleteRefreshCookie.toString());
            //We do not need to perform redirection here, the logout configuration already does it for us.
            response.setStatus(HttpServletResponse.SC_SEE_OTHER);
        }
        catch (IOException e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }

    }

    private AccessAndRefreshTokensDTO extractRequestBody(HttpServletRequest request) throws IOException {
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
            return objectMapper.readValue(fullBody, AccessAndRefreshTokensDTO.class);
        }
        return null;
    }
}
