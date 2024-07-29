package com.rebuild.backend.config.security.sessions;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.security.web.session.InvalidSessionStrategy;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class SessionInvalidationCustomStrategy implements InvalidSessionStrategy {
    @Override
    public void onInvalidSessionDetected(HttpServletRequest request,
                                         HttpServletResponse response) throws IOException {
        response.sendError(HttpStatus.REQUEST_TIMEOUT.value(), "Your session has timed out to prolonged inactivity, " +
                "please log in again");
    }
}
