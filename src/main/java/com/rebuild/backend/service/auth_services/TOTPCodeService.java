package com.rebuild.backend.service.auth_services;

import com.rebuild.backend.model.entities.user_entities.SecretStatus;
import com.rebuild.backend.model.entities.user_entities.TOTPSecret;
import com.rebuild.backend.model.entities.user_entities.User;
import com.rebuild.backend.model.exceptions.UserAuthException;
import com.rebuild.backend.model.forms.auth_forms.LoginForm;
import com.rebuild.backend.model.forms.auth_forms.MFAEnrolmentForm;
import com.rebuild.backend.model.responses.MFAEnrolmentResponse;
import com.rebuild.backend.repository.user_repositories.TOTPSecretRepository;
import com.rebuild.backend.repository.user_repositories.UserRepository;
import com.warrenstrange.googleauth.GoogleAuthenticator;
import com.warrenstrange.googleauth.GoogleAuthenticatorConfig;
import org.apache.commons.codec.binary.Base32;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.util.List;

@Service
public class TOTPCodeService {

    private static final int TOTP_CODE_NUM_DIGITS = 6;

    private static final String ISSUER_NAME = "rerebuild.ca";

    private final PasswordEncoder encoder;

    private final TOTPSecretRepository totpSecretRepository;

    private final UserRepository userRepository;

    private final RecoveryCodeHelperService recoveryCodeHelperService;

    public TOTPCodeService(TOTPSecretRepository totpSecretRepository,
                           UserRepository userRepository,
                           RecoveryCodeHelperService recoveryCodeHelperService,
                           PasswordEncoder encoder) {
        this.totpSecretRepository = totpSecretRepository;
        this.userRepository = userRepository;
        this.recoveryCodeHelperService = recoveryCodeHelperService;
        this.encoder = encoder;
    }

    private String generateRandomSecret()
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

        List<String> codes = recoveryCodeHelperService.generateCodesForDisplay();

        String rawSecret = generateRandomSecret();

        TOTPSecret newSecret = new TOTPSecret(SecretStatus.PENDING, rawSecret);
        newSecret.setUser(requestingUser);
        requestingUser.setTotpSecret(newSecret);

        totpSecretRepository.save(newSecret);

        String generatedURL = String.format("otpauth://totp/%s:%s?secret=%s&issuer=%s&digits=%s",
                ISSUER_NAME, requestingUser.getEmail(), rawSecret, ISSUER_NAME, TOTP_CODE_NUM_DIGITS);

        return new MFAEnrolmentResponse(generatedURL, codes);

    }

    private boolean userOtpMatches(User enteringUser, String enteredOtp)
    {
        String userSecret = enteringUser.getTotpSecret().getActualSecret();
        GoogleAuthenticatorConfig authConfig =
                new GoogleAuthenticatorConfig.GoogleAuthenticatorConfigBuilder().setSecretBits(128).build();

        GoogleAuthenticator authenticator = new GoogleAuthenticator(authConfig);

        return authenticator.authorize(userSecret, Integer.parseInt(enteredOtp));
    }

    public boolean otpMatches(LoginForm loginForm, String enteredOtp)
    {
        User foundUser = userRepository.findByEmailOrPhoneNumber(loginForm.emailOrPhone()).orElse(null);

        assert foundUser != null;

        return userOtpMatches(foundUser, enteredOtp);
    }

    public User enrolUserInMFA(User enrollingUser, MFAEnrolmentForm enrolmentForm)
    {

        if (!enrolmentForm.codesUnretrievableConfirmation())
        {
            throw new UserAuthException(HttpStatus.BAD_REQUEST, "Please confirm that you will not be able to retrieve " +
                    "the codes later, and that you have saved them somewhere");

        }


        if (!userOtpMatches(enrollingUser, enrolmentForm.enteredOTP()))
        {
            throw new UserAuthException(HttpStatus.BAD_REQUEST, "The code that you have entered is incorrect");
        }

        enrollingUser.setEnrolledInMFA(true);
        enrollingUser.getTotpSecret().setStatus(SecretStatus.CONFIRMED);


        recoveryCodeHelperService.associateCodesWithUser(enrollingUser, enrolmentForm.recoveryCodes());

        return userRepository.save(enrollingUser);

    }

}
