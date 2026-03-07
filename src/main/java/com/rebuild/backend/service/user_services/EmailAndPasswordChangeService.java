package com.rebuild.backend.service.user_services;

import com.rebuild.backend.model.entities.user_entities.User;
import com.rebuild.backend.model.forms.auth_forms.PasswordResetForm;
import com.rebuild.backend.repository.user_repositories.UserRepository;
import com.sendgrid.Method;
import com.sendgrid.Request;
import com.sendgrid.Response;
import com.sendgrid.SendGrid;
import com.sendgrid.helpers.mail.Mail;
import com.sendgrid.helpers.mail.objects.Email;
import com.sendgrid.helpers.mail.objects.Personalization;
import io.github.cdimascio.dotenv.Dotenv;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
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


    private final PasswordEncoder encoder;

    private final UserRepository userRepository;

    private final SendGrid sendGrid;

    private final SecretKey jwtSigningKey;

    public EmailAndPasswordChangeService(Dotenv dotenv, PasswordEncoder encoder,
                                         UserRepository userRepository, SendGrid sendGrid) {
        this.dotenv = dotenv;
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
        try {
            changingUser.setEmail(newEmail);
        }
        catch (DataIntegrityViolationException e){
            Throwable cause = e.getCause();
            if (cause instanceof ConstraintViolationException violationException){
                if (Objects.equals(violationException.getConstraintName(), "uk_email")){
                    throw new RuntimeException("This email address already exists");
                }
            }
        }
    }


    private String generatePasswordChangeUrl(String email)
    {
        String token = Jwts.builder().setSubject(email).
                setIssuedAt(Date.from(Instant.now())).
                //Expires in 10 minutes
                setExpiration(Date.from(Instant.now().plusSeconds(600))).
                signWith(jwtSigningKey, SignatureAlgorithm.HS256).compact();

        return "https://rerebuild.ca/change_password?token=" + token;
    }


    public ResponseEntity<String> sendPasswordChangeEmail(String email)
    {
        try {
            Mail mailToSend = new Mail();
            mailToSend.setTemplateId(dotenv.get("TWILIO_SENDGRID_TEMPLATE_ID"));
            mailToSend.setFrom(new Email(dotenv.get("TWILIO_EMAIL")));

            //Create the personalization settings
            Personalization personalization = new Personalization();
            personalization.addTo(new Email(email));
            String passwordChangeUrl = generatePasswordChangeUrl(email);
            personalization.addDynamicTemplateData("url", passwordChangeUrl);
            personalization.addDynamicTemplateData("password_reset_link", passwordChangeUrl);

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
        if (token == null)
        {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Missing token");
        }
        if (!resetForm.newPasswordConfirmation().equals(resetForm.newPassword()))
        {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("The 2 passwords don't match");
        }

        Claims tokenClaims = Jwts.parserBuilder().
                setSigningKey(jwtSigningKey).
                build().parseClaimsJws(token).getBody();

        if (tokenClaims.getExpiration().before(new Date())){
            return ResponseEntity.status(401).body("The password reset token has expired, please request a new one");
        }
        String email = tokenClaims.getSubject();

        Optional<User> foundUser = userRepository.findByEmailOrPhoneNumber(email);

        //For security reasons, we do not reveal whether the email address exists within the system
        if (foundUser.isEmpty()){
            return ResponseEntity.status(401).body("Invalid token");
        }

        changePassword(foundUser.get(), resetForm.newPassword());

        return ResponseEntity.ok("Your password has successfully been changed");
    }

}
