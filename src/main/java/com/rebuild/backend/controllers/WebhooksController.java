package com.rebuild.backend.controllers;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController("/webhooks")
public class WebhooksController {

    @PostMapping("/update")
    public void updateUser(HttpServletRequest request) {

    }
}
