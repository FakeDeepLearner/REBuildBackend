package com.rebuild.backend.service.auth_services;

import com.rebuild.backend.model.entities.user_entities.TemporaryMFASecret;
import com.rebuild.backend.model.entities.user_entities.User;
import com.rebuild.backend.model.exceptions.UserAuthException;
import com.rebuild.backend.model.forms.auth_forms.*;
import com.rebuild.backend.model.responses.MFAEnrolmentResponse;
import com.rebuild.backend.repository.user_repositories.TemporaryMFASecretRepository;
import com.rebuild.backend.repository.user_repositories.UserRepository;
import com.rebuild.backend.service.user_services.UserService;
import com.warrenstrange.googleauth.GoogleAuthenticator;
import com.warrenstrange.googleauth.GoogleAuthenticatorConfig;
import org.apache.commons.codec.binary.Base32;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.util.List;
import java.util.Optional;

@Service
public class TOTPCodeService {

    private static final int TOTP_CODE_NUM_DIGITS = 6;

    private static final String ISSUER_NAME = "rerebuild.ca";

    private final UserRepository userRepository;

    private final RecoveryCodeHelperService recoveryCodeHelperService;

    private final TemporaryMFASecretRepository temporaryMFASecretRepository;

    private final UserService userService;

    public TOTPCodeService(UserRepository userRepository,
                           RecoveryCodeHelperService recoveryCodeHelperService,
                           TemporaryMFASecretRepository temporaryMFASecretRepository, UserService userService) {
        this.userRepository = userRepository;
        this.recoveryCodeHelperService = recoveryCodeHelperService;
        this.temporaryMFASecretRepository = temporaryMFASecretRepository;
        this.userService = userService;
    }

    private String generateRandomSecret()
    {
        SecureRandom random = new SecureRandom();

        byte[] bytes = new byte[16];

        random.nextBytes(bytes);

        return new Base32().encodeToString(bytes);
    }


    @Transactional
    public MFAEnrolmentResponse startMFAEnrolment(SignupInitializationForm signupInitializationForm)
    {

        List<String> codes = recoveryCodeHelperService.generateCodesForDisplay();

        String rawSecret = generateRandomSecret();

        String userEmail = signupInitializationForm.email();


        Optional<TemporaryMFASecret> existingSecret = temporaryMFASecretRepository.findByEmail(userEmail);

        existingSecret.ifPresent(temporaryMFASecretRepository::delete);

        TemporaryMFASecret newSecret = new TemporaryMFASecret(userEmail, rawSecret);

        temporaryMFASecretRepository.save(newSecret);

        String generatedURL = String.format("otpauth://totp/%s:%s?secret=%s&issuer=%s&digits=%s",
                ISSUER_NAME, userEmail, rawSecret, ISSUER_NAME, TOTP_CODE_NUM_DIGITS);

        return new MFAEnrolmentResponse(generatedURL, codes);

    }

    private boolean userOtpMatches(String userSecret, String enteredOtp)
    {
        GoogleAuthenticatorConfig authConfig =
                new GoogleAuthenticatorConfig.GoogleAuthenticatorConfigBuilder().setSecretBits(128).build();

        GoogleAuthenticator authenticator = new GoogleAuthenticator(authConfig);

        return authenticator.authorize(userSecret, Integer.parseInt(enteredOtp));
    }

    public boolean otpMatches(LoginFinalizationForm form, String enteredOtp)
    {
        User foundUser = userRepository.findByEmailOrPhoneNumber(form.emailOrPhone()).orElse(null);

        assert foundUser != null;

        return userOtpMatches(foundUser.getMfaSecretValue(), enteredOtp);
    }


    public User enrolUserInMFA(SignupFinalizationForm finalizationForm)
    {

        if (!finalizationForm.codesUnretrievableConfirmation())
        {
            throw new UserAuthException(HttpStatus.BAD_REQUEST, "Please confirm that you will not be able to retrieve " +
                    "the codes later, and that you have saved them somewhere");

        }

        Optional<TemporaryMFASecret> foundSecret = temporaryMFASecretRepository.findByEmail(finalizationForm.email());

        //There should be a found secret here.
        if (foundSecret.isPresent()){
            TemporaryMFASecret mfaSecret = foundSecret.get();
            if (!userOtpMatches(mfaSecret.getSecret(), finalizationForm.enteredOTP()))
            {
                throw new UserAuthException(HttpStatus.BAD_REQUEST, "The code that you have entered is incorrect");
            }
            // The moment we know that the user has entered the code correctly, we create the user record,
            // associate this secret and the generated recovery codes with it, and then delete this temporary saved secret.

            User newUser = userService.createNewUser(finalizationForm, mfaSecret.getSecret());
            newUser.setMfaSecretValue(mfaSecret.getSecret());
            recoveryCodeHelperService.associateCodesWithUser(newUser, finalizationForm.recoveryCodes());
            temporaryMFASecretRepository.delete(mfaSecret);
            return userRepository.save(newUser);
        }
        //If there is not found secret, we return null for now. We will figure out how to deal with this later.
        return null;


    }

}
