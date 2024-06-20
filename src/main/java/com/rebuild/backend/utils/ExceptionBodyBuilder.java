package com.rebuild.backend.utils;

import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class ExceptionBodyBuilder{

    public <T extends RuntimeException> Map<String, String> buildBody(T exception){
        Map<String, String> body = new HashMap<>();
        body.put("error_message", exception.getMessage());
        return body;
    }
}
