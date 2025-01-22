package com.rebuild.backend.exception_handlers;

import com.rebuild.backend.utils.ExceptionBodyBuilder;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;

@Aspect
@Component
public class HandlerAspect {

    private final ExceptionBodyBuilder bodyBuilder;

    @Autowired
    public HandlerAspect(ExceptionBodyBuilder bodyBuilder) {
        this.bodyBuilder = bodyBuilder;
    }

    @Pointcut("@annotation(com.rebuild.backend.utils.HandleException)")
    public void handledMethodTypes(){}

    @AfterThrowing(pointcut = "handledMethodTypes()", throwing = "exception")
    public Map<String, String> handleException(RuntimeException exception){
        return bodyBuilder.buildBody(exception);
    }
}
