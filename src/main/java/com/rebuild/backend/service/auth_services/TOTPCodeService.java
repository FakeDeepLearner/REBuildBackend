package com.rebuild.backend.service.auth_services;

import com.rebuild.backend.model.dtos.RecoveryCodesDTO;
import com.rebuild.backend.model.entities.util_entitites.RecoveryCode;
import com.rebuild.backend.model.entities.user_entities.TemporaryMFACredentials;
import com.rebuild.backend.model.entities.user_entities.User;
import com.rebuild.backend.utils.exceptions.UserAuthException;
import com.rebuild.backend.model.forms.auth_forms.*;
import com.rebuild.backend.model.responses.MFAEnrolmentResponse;
import com.rebuild.backend.repository.user_repositories.TemporaryMFACredentialsRepository;
import com.rebuild.backend.repository.user_repositories.UserRepository;
import com.rebuild.backend.service.user_services.UserService;
import com.warrenstrange.googleauth.GoogleAuthenticator;
import com.warrenstrange.googleauth.GoogleAuthenticatorConfig;
import org.apache.commons.codec.binary.Base32;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Service
public class TOTPCodeService {
    private static final int SECRET_BYTES_LENGTH = 16;

    private static final int TOTP_CODE_NUM_DIGITS = 6;

    private static final String ISSUER_NAME = "rerebuild.ca";

    private final UserRepository userRepository;

    private final RecoveryCodeHelperService recoveryCodeHelperService;

    private final TemporaryMFACredentialsRepository temporaryMFACredentialsRepository;

    private final UserService userService;

    public TOTPCodeService(UserRepository userRepository,
                           RecoveryCodeHelperService recoveryCodeHelperService,
                           TemporaryMFACredentialsRepository temporaryMFACredentialsRepository, UserService userService) {
        this.userRepository = userRepository;
        this.recoveryCodeHelperService = recoveryCodeHelperService;
        this.temporaryMFACredentialsRepository = temporaryMFACredentialsRepository;
        this.userService = userService;
    }

    private String generateRandomSecret()
    {
        SecureRandom random = new SecureRandom();

        byte[] bytes = new byte[SECRET_BYTES_LENGTH];

        random.nextBytes(bytes);

        return new Base32().encodeToString(bytes);
    }


    @Transactional
    public MFAEnrolmentResponse startMFAEnrolment(SignupInitializationForm signupInitializationForm)
    {

        List<RecoveryCode> rawCodes = RecoveryCode.createCodes();

        RecoveryCodesDTO recoveryCodesDTO = recoveryCodeHelperService.getHashedAndDisplayedCodes(rawCodes);

        //This secret serves as the setup key and the secret at the same time
        String rawSecret = generateRandomSecret();

        String userEmail = signupInitializationForm.email();


        //If we found a non-expired secret, delete it.
        Optional<TemporaryMFACredentials> existingSecret = temporaryMFACredentialsRepository.
                findByEmailAndExpiryTimeAfter(userEmail, Instant.now());

        existingSecret.ifPresent(temporaryMFACredentialsRepository::delete);

        TemporaryMFACredentials newSecret = new TemporaryMFACredentials(userEmail,
                recoveryCodesDTO.hashedCodes(),
                Instant.now().plus(Duration.ofMinutes(10)));
        newSecret.setSecret(rawSecret);

        temporaryMFACredentialsRepository.save(newSecret);

        String generatedURL = String.format("otpauth://totp/%s:%s?secret=%s&issuer=%s&digits=%s",
                ISSUER_NAME, userEmail, rawSecret, ISSUER_NAME, TOTP_CODE_NUM_DIGITS);

        return new MFAEnrolmentResponse(generatedURL, recoveryCodesDTO.displayedCodes(), rawSecret);

    }

    private boolean userOtpMatches(String userSecret, String enteredOtp)
    {
        GoogleAuthenticatorConfig authConfig =
                new GoogleAuthenticatorConfig.GoogleAuthenticatorConfigBuilder().
                        setSecretBits(SECRET_BYTES_LENGTH * 8).setCodeDigits(TOTP_CODE_NUM_DIGITS).build();

        GoogleAuthenticator authenticator = new GoogleAuthenticator(authConfig);

        return authenticator.authorize(userSecret, Integer.parseInt(enteredOtp));
    }

    @Transactional
    public boolean otpMatches(LoginFinalizationForm form)
    {
        User foundUser = userRepository.findByEmail(form.emailOrPhone()).orElse(null);

        assert foundUser != null;

        return userOtpMatches(foundUser.getMfaSecretValue(), form.enteredCode());
    }


    @Transactional
    public User enrolUserInMFA(SignupFinalizationForm finalizationForm)
    {

        if (!finalizationForm.codesUnretrievableConfirmation())
        {
            throw new UserAuthException(HttpStatus.BAD_REQUEST,
                    "Please confirm that you will not be able to retrieve " +
                    "the codes later, and that you have saved them somewhere");

        }

        Optional<TemporaryMFACredentials> foundSecret = temporaryMFACredentialsRepository.
                findByEmailAndExpiryTimeAfter(finalizationForm.email(), Instant.now());

        //There should be a found secret here.
        if (foundSecret.isPresent()){
            TemporaryMFACredentials mfaSecret = foundSecret.get();
            if (!userOtpMatches(mfaSecret.getSecret(), finalizationForm.enteredOTP()))
            {
                throw new UserAuthException(HttpStatus.BAD_REQUEST, "The code that you have entered is incorrect");
            }
            // The moment we know that the user has entered the code correctly, we create the user record,
            // associate this secret and the generated recovery codes with it, and then delete this temporary saved secret.

            User newUser = userService.createNewUser(finalizationForm, mfaSecret.getSecret());
            recoveryCodeHelperService.associateCodesWithUser(newUser, mfaSecret.getTemporaryCodes());
            temporaryMFACredentialsRepository.delete(mfaSecret);
            return userRepository.save(newUser);
        }
        //If a secret is not found, raise an exception as it is not possible to process this request.
        //This means that this is either an adversary trying to skip the previous steps, or it is
        //a regular user, but their temporary credential no longer exists
        else
        {
            throw new UserAuthException(HttpStatus.UNPROCESSABLE_CONTENT, "This signup attempt is either expired," +
                    "or is trying to bypass MFA. Please try signing up again");
        }


    }

}
