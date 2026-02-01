package com.rebuild.backend.service.user_services;

import com.rebuild.backend.model.entities.user_entities.CaptchaVerificationRecord;
import com.rebuild.backend.model.entities.user_entities.SecretStatus;
import com.rebuild.backend.model.entities.user_entities.TOTPSecret;
import com.rebuild.backend.model.entities.user_entities.User;
import com.rebuild.backend.model.forms.auth_forms.LoginForm;
import com.rebuild.backend.model.forms.auth_forms.SignupForm;
import com.rebuild.backend.model.forms.dtos.CredentialValidationDTO;
import com.rebuild.backend.model.responses.MFAEnrolmentResponse;
import com.rebuild.backend.model.responses.PasswordFeedbackResponse;
import com.rebuild.backend.repository.user_repositories.CaptchaVerificationRepository;
import com.rebuild.backend.repository.user_repositories.TOTPSecretRepository;
import com.rebuild.backend.repository.user_repositories.UserRepository;
import com.rebuild.backend.service.util_services.CustomPasswordService;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.BucketConfiguration;
import io.github.bucket4j.distributed.proxy.ProxyManager;
import io.github.cdimascio.dotenv.Dotenv;
import org.apache.commons.codec.binary.Base32;
import org.springframework.http.HttpStatus;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

@Service
public class UserAuthenticationHelperService {

    private static final char[] ALPHABET =
            "ABCDEFGHJKLMNPQRSTUVWXYZ23456789".toCharArray();

    private static final int NUM_CODES_GENERATED = 8;

    private static final int RECOVERY_CODE_LENGTH = 16;

    private final UserRepository userRepository;

    private final Dotenv dotenv;

    private final CaptchaVerificationRepository verificationRepository;

    private final PasswordEncoder encoder;

    private final CustomPasswordService passwordService;

    private final ProxyManager<String> proxyManager;

    private final BucketConfiguration bucketConfiguration;

    private final TOTPSecretRepository totpSecretRepository;


    public UserAuthenticationHelperService(UserRepository userRepository,
                                           Dotenv dotenv,
                                           CaptchaVerificationRepository verificationRepository,
                                           CustomPasswordService passwordService,
                                           ProxyManager<String> proxyManager,
                                           BucketConfiguration bucketConfiguration,
                                           TOTPSecretRepository totpSecretRepository) {
        this.userRepository = userRepository;
        this.dotenv = dotenv;
        this.verificationRepository = verificationRepository;
        this.totpSecretRepository = totpSecretRepository;
        this.encoder = new BCryptPasswordEncoder();
        this.passwordService = passwordService;
        this.proxyManager = proxyManager;
        this.bucketConfiguration = bucketConfiguration;
    }


    public boolean captchaFailed(String userResponse, String userIp)
    {
        String urlToPost = "https://www.google.com/recaptcha/api/siteverify";

        Map<String, String> body = new HashMap<>();
        body.put("secret", dotenv.get("GOOGLE_CAPTCHA_SECRET_KEY"));
        body.put("response", userResponse);
        body.put("remoteip", userIp);

        RequestEntity<Map<String, String>> request = RequestEntity.post(urlToPost).body(body);

        ResponseEntity<Map> response = new RestTemplate().exchange(request, Map.class);

        Map<String, String> result = response.getBody();

        if(result == null){
            return true;
        }
        String successString = result.get("success");
        String timestampString = result.get("challenge_ts");

        boolean success = Boolean.parseBoolean(successString);

        CaptchaVerificationRecord newRecord = new CaptchaVerificationRecord(userIp,  Instant.now(), success);
        verificationRepository.save(newRecord);

        return !success;
    }


    public CredentialValidationDTO validateLoginCredentials(LoginForm form) {
        String formField = form.emailOrPhone();

        User foundUser = userRepository.findByEmailOrPhoneNumber(formField).orElse(null);

        if (foundUser == null) {
            return null;
        }


        String userSalt = foundUser.getSaltValue();
        String pepper = dotenv.get("PEPPER_VALUE");

        return new CredentialValidationDTO(encoder.matches(form.password() + userSalt + pepper,
                foundUser.getPassword()), foundUser.getEmail(), foundUser.isEnrolledInMFA());

    }


    public ResponseEntity<String> doPreliminaryPasswordChecks(SignupForm signupForm) throws IOException, InterruptedException {
        //Do preliminary checks. If any of them fail, abort the signup immediately
        if (!signupForm.password().equals(signupForm.repeatedPassword())){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Passwords do not match");
        }

        if (signupForm.password().length() < 10)
        {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).
                    body("Password is too short, please ensure it has a length of at least 10");
        }

        if (!signupForm.forcePassword() && passwordService.passwordFoundInDataBreach(signupForm.password()))
        {
            return ResponseEntity.badRequest().body("The password you entered was found in a data breach." +
                    "We strongly recommend that you choose a different one.");
        }

        PasswordFeedbackResponse feedbackResponse = passwordService.evaluateUserPassword(signupForm);
        int score = feedbackResponse.score();

        if (!signupForm.forcePassword() && score <= 2)
        {

            StringBuilder builder = new StringBuilder();
            for (String suggestion : feedbackResponse.suggestions())
            {
                builder.append(suggestion);
                builder.append("\n");
            }

            return ResponseEntity.badRequest().body("This password is not recommended due to " +
                    "the following reason:\n " + feedbackResponse.warning() + "\n" +
                    "We recommend the following:\n" + builder);
        }

        return null;
    }

    public Bucket returnUserBucket(String loginEmail){
        //The lambda is to get around the fact that building
        // with supplying a bucket configuration directly is deprecated, thank god for lambdas
        return proxyManager.builder().build(loginEmail, () -> bucketConfiguration);
    }


    public String generateRandomRecoveryCode()
    {
        SecureRandom random = new SecureRandom();

        char[] characters = new char[RECOVERY_CODE_LENGTH];

        for (int i = 0; i < RECOVERY_CODE_LENGTH; i++)
        {
            characters[i] = ALPHABET[random.nextInt(ALPHABET.length)];
        }

        return String.format(
                "%s-%s-%s-%s",
                new String(characters, 0, 4),
                new String(characters, 4, 4),
                new String(characters, 8, 4),
                new String(characters, 12, 4)
        );

    }

    public String generateRandomSecret()
    {
        SecureRandom random = new SecureRandom();

        byte[] bytes = new byte[16];

        random.nextBytes(bytes);

        return new Base32().encodeToString(bytes);
    }

    public MFAEnrolmentResponse startMFAEnrolment(User requestingUser, String enteredPassword)
    {
        if (!encoder.matches(enteredPassword, requestingUser.getPassword()))
        {
            return null;
        }

        List<String> codes = Stream.generate(this::generateRandomRecoveryCode).
                limit(NUM_CODES_GENERATED).toList();

        String rawSecret = generateRandomSecret();

        TOTPSecret newSecret = new TOTPSecret(SecretStatus.PENDING, rawSecret);
        newSecret.setUser(requestingUser);
        requestingUser.setTotpSecret(newSecret);

        totpSecretRepository.save(newSecret);


        String generatedURL = String.format("otpauth://totp/%s?secret=%s&issuer=%s&digits=%s",
                requestingUser.getEmail(), rawSecret, "rerebuild.ca", 6);

        return new MFAEnrolmentResponse(generatedURL, codes);

    }

}
