package com.rebuild.backend.service.user_services;

import com.rebuild.backend.model.dtos.CredentialValidationDTO;
import com.rebuild.backend.model.entities.user_entities.User;
import com.rebuild.backend.model.exceptions.UserAuthException;
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
                setExpiration(Date.from(Instant.now().plusSeconds(EMAIL_EXPIRY_MINUTES * 60))).
                signWith(jwtSigningKey, SignatureAlgorithm.HS256).compact();

        return "https://rerebuild.ca/change_email?token=" + token;
    }

    private Claims extractTokenClaims(String token)
    {
        try{
            return Jwts.parserBuilder().
                    setSigningKey(jwtSigningKey).build()
                    .parseClaimsJws(token).getBody();
        }
        catch (ExpiredJwtException _)
        {
            throw new UserAuthException(HttpStatus.UNAUTHORIZED, "The password reset token has expired, please request a new one");
        }
        catch (SignatureException _)
        {
            throw new UserAuthException(HttpStatus.UNAUTHORIZED, "The token is either invalid or has been tampered with");
        }
        catch (IllegalArgumentException _)
        {
            throw new UserAuthException(HttpStatus.BAD_REQUEST, "Missing or empty token");
        }
        catch (MalformedJwtException _)
        {
            throw new UserAuthException(HttpStatus.BAD_REQUEST, "The token is malformed");
        }

    }

    public boolean sendEmailChange(String currentEmail, String newEmail)
    {
        try {
            Optional<User> foundUser = userRepository.findByEmailOrPhoneNumber(newEmail);
            if (foundUser.isPresent())
            {
                throw new UserAuthException(HttpStatus.CONFLICT, "A user already exists with " +
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
            return finalizeEmailSending(mailToSend, personalization);
        }
        catch (IOException e)
        {
            throw new UserAuthException(HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected error occurred");
        }
    }

    @Transactional
    public boolean processEmailChange(String token, EmailChangeConfirmationForm confirmationForm)
    {

        Claims tokenClaims = extractTokenClaims(token);

        CredentialValidationDTO validationResult = authenticationHelperService.
                validateLoginCredentials(new LoginForm(confirmationForm.oldEmail(), confirmationForm.password()));

        if (validationResult == null || !validationResult.canLogin())
        {
            throw new UserAuthException(HttpStatus.NOT_FOUND, "Invalid credentials");
        }

        String newEmail = tokenClaims.get("new_subject").toString();
        User foundUser = validationResult.foundUser();
        changeEmail(foundUser, newEmail);
        return true;

    }


    public boolean sendPasswordChangeEmail(String email)
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
            return finalizeEmailSending(mailToSend, personalization);
        }
        catch (IOException e)
        {
            throw new UserAuthException(HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected error occurred");
        }
    }

    private boolean finalizeEmailSending(Mail mailToSend, Personalization personalization) throws IOException {
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
            return true;
        }
        throw new UserAuthException(HttpStatus.valueOf(response.getStatusCode()), response.getBody());
    }


    @Transactional
    public boolean processPasswordChange(String token, PasswordResetForm resetForm)
    {
        if (!resetForm.newPasswordConfirmation().equals(resetForm.newPassword()))
        {
            throw new UserAuthException(HttpStatus.BAD_REQUEST, "The passwords do not match");
        }

        Claims tokenClaims = extractTokenClaims(token);
        String email = tokenClaims.getSubject();

        Optional<User> foundUser = userRepository.findByEmailOrPhoneNumber(email);

        //For security reasons, we do not reveal whether the email address exists within the system
        if (foundUser.isEmpty()) {
            throw new UserAuthException(HttpStatus.UNAUTHORIZED, "Invalid token");
        }

        changePassword(foundUser.get(), resetForm.newPassword());
        return true;
    }

}
