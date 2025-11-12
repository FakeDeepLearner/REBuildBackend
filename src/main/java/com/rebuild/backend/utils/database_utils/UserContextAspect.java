package com.rebuild.backend.utils.database_utils;

import com.rebuild.backend.model.entities.users.User;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Component;

import java.lang.annotation.Annotation;

@Component
@Aspect
public class UserContextAspect {

    @Pointcut(value = "within(com.rebuild.backend.controllers..*)")
    public void contextPointcut(){}

    @Before("contextPointcut()")
    public void fillUserContext(JoinPoint joinPoint){
        Object[] args = joinPoint.getArgs();
        MethodSignature methodSignature = (MethodSignature) joinPoint.getSignature();
        Annotation[][] paramAnnotations = methodSignature.getMethod().getParameterAnnotations();

        for (int i = 0; i < args.length; i++) {
            for (Annotation ann : paramAnnotations[i]) {
                if (ann instanceof AuthenticationPrincipal && args[i] instanceof User u) {
                    UserContext.set(u.getId());
                    return;
                }
            }
        }
    }


    @After("contextPointcut()")
    public void clearContext()
    {
        UserContext.clear();
    }
}
