package com.rebuild.backend.utils;

import com.rebuild.backend.exceptions.unauthorized_exceptions.NotAuthenticatedException;
import lombok.NonNull;
import org.springframework.core.MethodParameter;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import java.security.Principal;

@Component
public class AuthenticationPrincipalNullChecker implements HandlerMethodArgumentResolver {

    //If the parameter has the AuthenticationPrincipal annotation, our resolver will handle it
    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return parameter.hasParameterAnnotation(AuthenticationPrincipal.class);
    }

    @Override
    public UserDetails resolveArgument(@NonNull MethodParameter parameter,
                                       ModelAndViewContainer mavContainer,
                                       NativeWebRequest webRequest,
                                       WebDataBinderFactory binderFactory)  {
        Principal nativePrincipal = webRequest.getUserPrincipal();
        if(nativePrincipal == null) {
            throw new NotAuthenticatedException("You are not authenticated!");
        }
        return (UserDetails) nativePrincipal;
    }
}
