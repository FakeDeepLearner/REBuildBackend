package com.rebuild.backend.controllers;

import com.rebuild.backend.exceptions.EmailDoesNotExistException;
import com.rebuild.backend.model.entities.User;
import com.rebuild.backend.model.forms.PasswordResetForm;
import com.rebuild.backend.service.ResetTokenService;
import com.rebuild.backend.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;

@RestController
public class PasswordResetController {

    private final UserService userService;

    private final ResetTokenService tokenService;

    @Autowired
    public PasswordResetController(UserService userService, ResetTokenService tokenService) {
        this.userService = userService;
        this.tokenService = tokenService;
    }

    @PostMapping("/api/reset_password")
    public ResponseEntity<?> submitChangeForm(@Valid @RequestBody PasswordResetForm resetForm){
        Optional<User> foundUser = userService.findByEmail(resetForm.enteredEmail());
        if (foundUser.isEmpty()){
            throw new EmailDoesNotExistException("A user hasn't been found with the provided email address.");
        }
        return ResponseEntity.ok("ok");
    }
}
