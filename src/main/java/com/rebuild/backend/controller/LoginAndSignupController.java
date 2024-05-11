package com.rebuild.backend.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class LoginAndSignupController {

    @GetMapping("/login")
    public void loginPage(@RequestParam(required = false) boolean error,
                          @RequestParam(required = false) boolean logout){

    }



}
