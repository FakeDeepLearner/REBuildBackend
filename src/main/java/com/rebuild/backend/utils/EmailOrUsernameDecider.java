package com.rebuild.backend.utils;

import com.rebuild.backend.service.CustomUserDetailsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class EmailOrUsernameDecider {

    private final CustomUserDetailsService userDetailsService;

    @Autowired
    public EmailOrUsernameDecider(CustomUserDetailsService userDetailsService) {
        this.userDetailsService = userDetailsService;
    }


    public boolean isInputEmail(String input){
        String emailRegex = "^[a-zA-Z\\d._%+-]+@[a-zA-Z\\d.-]+\\.com$";

        Matcher matcher = Pattern.compile(emailRegex).matcher(input);

        return matcher.matches();
    }

    public UserDetails createProperUserDetails(String input){
        if(isInputEmail(input)){
            return userDetailsService.loadUserByEmail(input);
        }
        return userDetailsService.loadUserByUsername(input);
    }
}
