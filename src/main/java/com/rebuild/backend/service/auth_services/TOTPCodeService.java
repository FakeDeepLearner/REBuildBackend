package com.rebuild.backend.service.auth_services;

import com.rebuild.backend.model.entities.user_entities.MFARecoveryCodeEntity;
import com.rebuild.backend.model.entities.user_entities.SecretStatus;
import com.rebuild.backend.model.entities.user_entities.TOTPSecret;
import com.rebuild.backend.model.entities.user_entities.User;
import com.rebuild.backend.model.forms.auth_forms.LoginForm;
import com.rebuild.backend.model.forms.auth_forms.MFAEnrolmentForm;
import com.rebuild.backend.model.responses.MFAEnrolmentResponse;
import com.rebuild.backend.repository.user_repositories.RecoveryCodeRepository;
import com.rebuild.backend.repository.user_repositories.TOTPSecretRepository;
import com.rebuild.backend.repository.user_repositories.UserRepository;
import com.rebuild.backend.utils.util_entities.RecoveryCode;
import com.warrenstrange.googleauth.GoogleAuthenticator;
import com.warrenstrange.googleauth.GoogleAuthenticatorConfig;
import org.apache.commons.codec.binary.Base32;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

@Service
public class TOTPCodeService {


    private static final int NUM_RECOVERY_CODES_GENERATED = 8;

    private static final int TOTP_CODE_NUM_DIGITS = 6;

    private static final String ISSUER_NAME = "rerebuild.ca";

    private final PasswordEncoder encoder;

    private final TOTPSecretRepository totpSecretRepository;

    private final UserRepository userRepository;

    private final RecoveryCodeHelperService recoveryCodeHelperService;

    public TOTPCodeService(TOTPSecretRepository totpSecretRepository,
                           UserRepository userRepository,
                           RecoveryCodeHelperService recoveryCodeHelperService) {
        this.totpSecretRepository = totpSecretRepository;
        this.userRepository = userRepository;
        this.recoveryCodeHelperService = recoveryCodeHelperService;
        this.encoder = new BCryptPasswordEncoder();
    }

    private String generateRandomSecret()
    {
        SecureRandom random = new SecureRandom();

        byte[] bytes = new byte[16];

        random.nextBytes(bytes);

        return new Base32().encodeToString(bytes);
    }


    @Transactional
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

    @Transactional
    public boolean otpMatches(LoginForm loginForm, String enteredOtp)
    {
        User foundUser = userRepository.findByEmailOrPhoneNumber(loginForm.emailOrPhone()).orElse(null);

        assert foundUser != null;

        return userOtpMatches(foundUser, enteredOtp);
    }

    @Transactional
    public ResponseEntity<String> enrolUserInMFA(User enrollingUser, MFAEnrolmentForm enrolmentForm)
    {

        if (!enrolmentForm.codesUnretrievableConfirmation())
        {
            return ResponseEntity.badRequest().body("Please confirm that you will not be able to retrieve " +
                    "the codes later, and that you have saved them somewhere");
        }


        if (!userOtpMatches(enrollingUser, enrolmentForm.enteredOTP()))
        {
            return ResponseEntity.badRequest().body("The code that you have entered is incorrect");
        }

        enrollingUser.setEnrolledInMFA(true);
        enrollingUser.getTotpSecret().setStatus(SecretStatus.CONFIRMED);


        recoveryCodeHelperService.associateCodesWithUser(enrollingUser, enrolmentForm.recoveryCodes());

        userRepository.save(enrollingUser);

        return ResponseEntity.ok("Enrolment in MFA is successful");

    }

}
