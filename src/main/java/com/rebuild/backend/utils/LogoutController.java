package com.rebuild.backend.utils;

import com.rebuild.backend.service.user_services.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.LogoutHandler;
import org.springframework.stereotype.Component;


@Component
public class LogoutController implements LogoutHandler {

    private final UserService userService;

    @Autowired
    public LogoutController(UserService userService) {
        this.userService = userService;
    }

    @Override
    public void logout(HttpServletRequest request,
                       HttpServletResponse response,
                       Authentication authentication) {
        userService.invalidateAllSessions(authentication.getName());
        //Setting the max age to a negative number instructs the browser to delete this cookie
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
}
