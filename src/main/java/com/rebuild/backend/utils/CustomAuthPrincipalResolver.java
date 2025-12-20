package com.rebuild.backend.utils;

import com.rebuild.backend.model.entities.users.User;
import com.rebuild.backend.service.user_services.UserService;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.MethodParameter;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.core.annotation.MergedAnnotations;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.annotation.SecurityAnnotationScanner;
import org.springframework.security.core.annotation.SecurityAnnotationScanners;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.context.SecurityContextHolderStrategy;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.method.annotation.AuthenticationPrincipalArgumentResolver;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import java.lang.annotation.Annotation;
import java.util.Optional;

@Component
public class CustomAuthPrincipalResolver implements HandlerMethodArgumentResolver {

    private final Class<AuthenticationPrincipal> annotationType = AuthenticationPrincipal.class;
    private final SecurityContextHolderStrategy securityContextHolderStrategy = SecurityContextHolder.getContextHolderStrategy();
    private final ExpressionParser parser = new SpelExpressionParser();
    private final SecurityAnnotationScanner<@NonNull AuthenticationPrincipal> scanner = SecurityAnnotationScanners.requireUnique(AuthenticationPrincipal.class);


    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return findMethodAnnotation(parameter) != null;
    }

    @Override
    public @Nullable Object resolveArgument(MethodParameter parameter,
                                            @Nullable ModelAndViewContainer mavContainer,
                                            NativeWebRequest webRequest,
                                            @Nullable WebDataBinderFactory binderFactory) throws Exception {
        Authentication authentication = this.securityContextHolderStrategy.getContext().getAuthentication();
        if (authentication == null) {
            return null;
        } else {
            // The principal is a type of User, since our UserDetailsService returns an actual User type.
            // Even though it is stored as a UserDetails in the authentication flow, its actual type is still User
            Object principal = authentication.getPrincipal();

            if (principal == null) {
                return null;
            }
            AuthenticationPrincipal annotation = this.findMethodAnnotation(parameter);
            Assert.notNull(annotation, "@AuthenticationPrincipal is required. Call supportsParameter first.");

            //principal.getClass is UserDetails.
            // parameter.getParameterType is User.

            /*
             * In the normal implementation of the resolver, a check is made that a type of UserDetails (rhs)
             * is assignable to a type of User (lhs). This check will fail for our purposes.
             * What we need is checking that a type of User (rhs) being assignable to a type of UserDetails (lhs).
             * This will succeed, since UserDetails is a superinterface of User
             */
            if (!ClassUtils.isAssignable(principal.getClass(), parameter.getParameterType())) {
                return null;
            } else {
                //Since the principal is of type User, this means we can just simply return it.
                //It is guaranteed that it is not null by this point.
                return principal;
            }
        }
    }

    private @Nullable AuthenticationPrincipal findMethodAnnotation(MethodParameter parameter) {
        boolean useAnnotationTemplate = false;
        if (useAnnotationTemplate) {
            return this.scanner.scan(parameter.getParameter());
        } else {
            AuthenticationPrincipal annotation = parameter.getParameterAnnotation(this.annotationType);
            if (annotation != null) {
                return annotation;
            } else {
                Annotation[] annotationsToSearch = parameter.getParameterAnnotations();

                for(Annotation toSearch : annotationsToSearch) {
                    annotation = AnnotationUtils.findAnnotation(toSearch.annotationType(), this.annotationType);
                    if (annotation != null) {
                        return MergedAnnotations.from(new Annotation[]{toSearch}).get(this.annotationType).synthesize();
                    }
                }

                return null;
            }
        }
    }
}
