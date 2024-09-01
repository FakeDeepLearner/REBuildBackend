package com.rebuild.backend.controllers.token_controllers;

import com.rebuild.backend.model.entities.enums.TokenType;
import com.rebuild.backend.model.forms.dto_forms.ActivationTokenExpiredDTO;
import com.rebuild.backend.model.forms.dto_forms.EmailChangeDTO;
import com.rebuild.backend.model.forms.dto_forms.ResetTokenExpiredDTO;
import com.rebuild.backend.service.token_services.JWTTokenService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.temporal.ChronoUnit;

@RestController
@RequestMapping(value = "/api/request_new_token", method = RequestMethod.GET)
public class NewTokenController {

    private final JWTTokenService tokenService;

    @Autowired
    public NewTokenController(JWTTokenService tokenService) {
        this.tokenService = tokenService;
    }

    @GetMapping("/reset")
    public void sendResetToken(@RequestBody ResetTokenExpiredDTO expiredDTO){
        long timeCount = 10L;
        ChronoUnit timeUnit = ChronoUnit.MINUTES;
        String newToken = tokenService.generateTokenForPasswordReset(
                expiredDTO.failedEmailFor(), timeCount, timeUnit,
                TokenType.CHANGE_PASSWORD.typeName
        );
        tokenService.sendProperEmail(newToken, timeCount, timeUnit);
    }

    @GetMapping("/activation")
    public void sendActivationToken(@RequestBody ActivationTokenExpiredDTO expiredDTO){
        long timeCount = 10L;
        ChronoUnit timeUnit = ChronoUnit.MINUTES;
        String newToken = tokenService.generateTokenForAccountActivation(expiredDTO.email(), timeCount,
                timeUnit, TokenType.ACTIVATE_ACCOUNT.typeName,
                expiredDTO.remembered(), expiredDTO.password()
        );
        tokenService.sendProperEmail(newToken, timeCount, timeUnit);

    }

    @GetMapping("/email")
    public void sendEmailChangeToken(@RequestBody EmailChangeDTO changeDTO){
        long timeCount = 10L;
        ChronoUnit timeUnit = ChronoUnit.MINUTES;
        String newToken = tokenService.generateTokenForEmailChange(
                changeDTO.oldEmail(), changeDTO.newEmail(),
                timeCount, timeUnit, TokenType.CHANGE_EMAIL.typeName
        );
        tokenService.sendProperEmail(newToken, timeCount, timeUnit);
    }
}
