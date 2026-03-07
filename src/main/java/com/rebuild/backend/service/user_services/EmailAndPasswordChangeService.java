package com.rebuild.backend.service.user_services;

import com.rebuild.backend.model.dtos.CredentialValidationDTO;
import com.rebuild.backend.model.entities.user_entities.User;
import com.rebuild.backend.model.forms.auth_forms.EmailChangeConfirmationForm;
import com.rebuild.backend.model.forms.auth_forms.LoginForm;
import com.rebuild.backend.model.forms.auth_forms.PasswordResetForm;
import com.rebuild.backend.repository.user_repositories.UserRepository;
import com.rebuild.backend.service.auth_services.UserAuthenticationHelperService;
import com.sendgrid.Method;
import com.sendgrid.Request;
import com.sendgrid.Response;
import com.sendgrid.SendGrid;
import com.sendgrid.helpers.mail.Mail;
import com.sendgrid.helpers.mail.objects.Email;
import com.sendgrid.helpers.mail.objects.Personalization;
import io.github.cdimascio.dotenv.Dotenv;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import org.hibernate.exception.ConstraintViolationException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.crypto.SecretKey;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.Objects;
import java.util.Optional;

@Service
public class EmailAndPasswordChangeService {

    private final Dotenv dotenv;

    private final UserAuthenticationHelperService authenticationHelperService;

    private final PasswordEncoder encoder;

    private final UserRepository userRepository;

    private final SendGrid sendGrid;

    private final SecretKey jwtSigningKey;

    private final static int EMAIL_EXPIRY_MINUTES = 10;

    public EmailAndPasswordChangeService(Dotenv dotenv, UserAuthenticationHelperService authenticationHelperService,
                                         PasswordEncoder encoder,
                                         UserRepository userRepository, SendGrid sendGrid) {
        this.dotenv = dotenv;
        this.authenticationHelperService = authenticationHelperService;
        this.encoder = encoder;
        this.userRepository = userRepository;
        this.sendGrid = sendGrid;
        this.jwtSigningKey = Keys.hmacShaKeyFor(dotenv.get("JWT_SIGNING_KEY").
                getBytes(StandardCharsets.UTF_8));
    }


    private void changePassword(User changingUser, String newRawPassword){
        String userSalt = changingUser.getSaltValue();
        String pepper = dotenv.get("PEPPER_VALUE");
        String newHashedPassword = encoder.encode(newRawPassword + userSalt + pepper);

        assert newHashedPassword != null;
        changingUser.setPassword(newHashedPassword);
        userRepository.save(changingUser);
    }

    @Transactional
    public void changeEmail(User changingUser, String newEmail){

        changingUser.setEmail(newEmail);
        userRepository.save(changingUser);

    }


    private String generatePasswordChangeUrl(String email)
    {
        String token = Jwts.builder().setSubject(email).
                setIssuedAt(Date.from(Instant.now())).
                //Expires in 10 minutes
                setExpiration(Date.from(Instant.now().plusSeconds(EMAIL_EXPIRY_MINUTES * 60))).
                signWith(jwtSigningKey, SignatureAlgorithm.HS256).compact();

        return "https://rerebuild.ca/change_password?token=" + token;
    }

    private String generateEmailChangeUrl(String oldEmail, String newEmail)
    {
        String token = Jwts.builder().setSubject(oldEmail).
                claim("new_subject", newEmail).
                setIssuedAt(Date.from(Instant.now())).
                setExpiration(Date.from(Instant.now().plusSeconds(EMAIL_EXPIRY_MINUTES * 10))).
                signWith(jwtSigningKey, SignatureAlgorithm.HS256).compact();

        return "https://rerebuild.ca/change_email?token=" + token;
    }

    public ResponseEntity<String> sendEmailChange(String currentEmail, String newEmail)
    {
        try {
            Optional<User> foundUser = userRepository.findByEmailOrPhoneNumber(newEmail);
            if (foundUser.isPresent())
            {
                return ResponseEntity.status(HttpStatus.CONFLICT).body("A user already exists with " +
                        "the same email address as your new email");
            }


            Mail mailToSend = new Mail();
            mailToSend.setTemplateId(dotenv.get("SENDGRID_EMAIL_CHANGE_TEMPLATE_ID"));
            mailToSend.setFrom(new Email(dotenv.get("TWILIO_EMAIL")));

            //Create the personalization settings
            Personalization personalization = new Personalization();
            personalization.addTo(new Email(newEmail));
            String emailChangeUrl = generateEmailChangeUrl(currentEmail, newEmail);
            personalization.addDynamicTemplateData("url", emailChangeUrl);
            personalization.addDynamicTemplateData("email_reset_link", emailChangeUrl);
            personalization.addDynamicTemplateData("email_expiry_minutes", EMAIL_EXPIRY_MINUTES);

            mailToSend.addPersonalization(personalization);

            Request request = new Request();

            request.setMethod(Method.POST);
            request.setEndpoint("/mail/send");
            request.setBaseUri("https://api.sendgrid.com");
            request.setBody(mailToSend.build());

            Response response = sendGrid.api(request);
            if (response.getStatusCode() == 202)
            {
                return ResponseEntity.status(202).body("An email has been sent to the new email address that you entered," +
                        "please follow the instructions in that email");
            }
            return ResponseEntity.status(response.getStatusCode()).body(response.getBody());
        }
        catch (IOException e)
        {
            return ResponseEntity.internalServerError().body("An unexpected error occurred, please try again");
        }
    }

    @Transactional
    public ResponseEntity<String> processEmailChange(String token, EmailChangeConfirmationForm confirmationForm)
    {
        try {
            Claims tokenClaims = Jwts.parserBuilder().
                    setSigningKey(jwtSigningKey).
                    build().parseClaimsJws(token).getBody();

            CredentialValidationDTO validationResult = authenticationHelperService.
                    validateLoginCredentials(new LoginForm(confirmationForm.oldEmail(), confirmationForm.password()));

            if (validationResult == null || !validationResult.canLogin())
            {
                return ResponseEntity.status(404).body("A user with these credentials has not been found");
            }



            String newEmail = tokenClaims.get("new_subject").toString();

            User foundUser = validationResult.foundUser();

            changeEmail(foundUser, newEmail);


            return ResponseEntity.ok("Your email has successfully been changed");
        }
        catch (ExpiredJwtException _)
        {
            return ResponseEntity.status(401).body("The email change token has expired, please request a new one");
        }
        catch (SignatureException _)
        {
            return ResponseEntity.status(401).body("The token is either invalid or has been tampered with");
        }
        catch (IllegalArgumentException _)
        {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Missing or empty token, please try again");
        }
        catch (MalformedJwtException _)
        {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("The token format is malformed, please try again.");
        }



    }


    public ResponseEntity<String> sendPasswordChangeEmail(String email)
    {
        try {
            Mail mailToSend = new Mail();
            mailToSend.setTemplateId(dotenv.get("SENDGRID_PASSWORD_TEMPLATE_ID"));
            mailToSend.setFrom(new Email(dotenv.get("TWILIO_EMAIL")));

            //Create the personalization settings
            Personalization personalization = new Personalization();
            personalization.addTo(new Email(email));
            String passwordChangeUrl = generatePasswordChangeUrl(email);
            personalization.addDynamicTemplateData("url", passwordChangeUrl);
            personalization.addDynamicTemplateData("password_reset_link", passwordChangeUrl);
            personalization.addDynamicTemplateData("email_expiry_minutes", EMAIL_EXPIRY_MINUTES);

            mailToSend.addPersonalization(personalization);

            Request request = new Request();

            request.setMethod(Method.POST);
            request.setEndpoint("/mail/send");
            request.setBaseUri("https://api.sendgrid.com");
            request.setBody(mailToSend.build());

            Response response = sendGrid.api(request);
            if (response.getStatusCode() == 202)
            {
                return ResponseEntity.status(202).body("If there is an email address with " +
                        "this account, it has been sent an email to reset your password");
            }
            return ResponseEntity.status(response.getStatusCode()).body(response.getBody());
        }
        catch (IOException e)
        {
            return ResponseEntity.internalServerError().body("An unexpected error occurred, please try again");
        }
    }


    @Transactional
    public ResponseEntity<String> processPasswordChange(String token, PasswordResetForm resetForm)
    {
        if (!resetForm.newPasswordConfirmation().equals(resetForm.newPassword()))
        {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("The 2 passwords don't match");
        }

        try {
            Claims tokenClaims = Jwts.parserBuilder().
                    setSigningKey(jwtSigningKey).
                    build().parseClaimsJws(token).getBody();

            String email = tokenClaims.getSubject();

            Optional<User> foundUser = userRepository.findByEmailOrPhoneNumber(email);

            //For security reasons, we do not reveal whether the email address exists within the system
            if (foundUser.isEmpty()) {
                return ResponseEntity.status(401).body("Invalid token");
            }

            changePassword(foundUser.get(), resetForm.newPassword());

            return ResponseEntity.ok("Your password has successfully been changed");
        }
        catch (ExpiredJwtException _)
        {
            return ResponseEntity.status(401).body("The password reset token has expired, please request a new one");
        }
        catch (SignatureException _)
        {
            return ResponseEntity.status(401).body("The token is either invalid or has been tampered with");
        }
        catch (IllegalArgumentException _)
        {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Missing or empty token, please try again");
        }
        catch (MalformedJwtException _)
        {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("The token format is malformed, please try again.");
        }
    }

}
