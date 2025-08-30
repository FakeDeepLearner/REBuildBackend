package com.rebuild.backend.utils;

import com.rebuild.backend.model.entities.users.User;
import lombok.NonNull;
import org.springframework.core.MethodParameter;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import java.security.Principal;

@Component
public class AuthenticationPrincipalResolver implements HandlerMethodArgumentResolver {

    //If the parameter has the AuthenticationPrincipal annotation,
    // and actually is a UserDetails,our resolver will handle it
    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return parameter.hasParameterAnnotation(AuthenticationPrincipal.class) &&
                parameter.getParameterType().isAssignableFrom(UserDetails.class);
    }

    @Override
    public UserDetails resolveArgument(@NonNull MethodParameter parameter,
                                       ModelAndViewContainer mavContainer,
                                       @NonNull NativeWebRequest webRequest,
                                       WebDataBinderFactory binderFactory)  {
        Authentication currentAuthentication = SecurityContextHolder.getContext().getAuthentication();
        if(currentAuthentication == null || currentAuthentication.getPrincipal() == null) {
            throw new RuntimeException("You are not authenticated!");
        }

        return (User) currentAuthentication.getPrincipal();
    }
}
