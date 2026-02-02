package com.rebuild.backend.service.user_services;

import com.rebuild.backend.model.entities.user_entities.MFARecoveryCode;
import com.rebuild.backend.model.entities.user_entities.SecretStatus;
import com.rebuild.backend.model.entities.user_entities.TOTPSecret;
import com.rebuild.backend.model.entities.user_entities.User;
import com.rebuild.backend.model.forms.auth_forms.LoginForm;
import com.rebuild.backend.model.forms.auth_forms.MFAEnrolmentForm;
import com.rebuild.backend.model.responses.MFAEnrolmentResponse;
import com.rebuild.backend.repository.user_repositories.RecoveryCodeRepository;
import com.rebuild.backend.repository.user_repositories.TOTPSecretRepository;
import com.rebuild.backend.repository.user_repositories.UserRepository;
import com.warrenstrange.googleauth.GoogleAuthenticator;
import com.warrenstrange.googleauth.GoogleAuthenticatorConfig;
import org.apache.commons.codec.binary.Base32;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.util.List;
import java.util.Objects;
import java.util.regex.Pattern;
import java.util.stream.Stream;

@Service
public class TOTPCodeService {

    private static final char[] ALPHABET =
            "ABCDEFGHJKLMNPQRSTUVWXYZ23456789".toCharArray();

    private static final int NUM_CODES_GENERATED = 8;

    private static final int RECOVERY_CODE_LENGTH = 16;

    private final PasswordEncoder encoder;

    private final TOTPSecretRepository totpSecretRepository;

    private final UserRepository userRepository;

    private final RecoveryCodeRepository recoveryCodeRepository;

    public TOTPCodeService(TOTPSecretRepository totpSecretRepository,
                           UserRepository userRepository,
                           RecoveryCodeRepository recoveryCodeRepository) {
        this.totpSecretRepository = totpSecretRepository;
        this.userRepository = userRepository;
        this.recoveryCodeRepository = recoveryCodeRepository;
        this.encoder = new BCryptPasswordEncoder();
    }

    private String generateRandomRecoveryCode()
    {
        SecureRandom random = new SecureRandom();

        char[] characters = new char[RECOVERY_CODE_LENGTH];

        for (int i = 0; i < RECOVERY_CODE_LENGTH; i++)
        {
            characters[i] = ALPHABET[random.nextInt(ALPHABET.length)];
        }

        return new String(characters);

    }

    private String generateRandomSecret()
    {
        SecureRandom random = new SecureRandom();

        byte[] bytes = new byte[16];

        random.nextBytes(bytes);

        return new Base32().encodeToString(bytes);
    }

    private List<String> generateCodesForDisplay()
    {
        return Stream.generate(this::generateRandomRecoveryCode).
                map(generatedCode -> String.format(
                        "%s-%s-%s-%s",
                        generatedCode.substring(0, 4),
                        generatedCode.substring(4, 8),
                        generatedCode.substring(8, 12),
                        generatedCode.substring(12, 16)
                )).
                limit(NUM_CODES_GENERATED).toList();
    }

    private void associateCodesWithUser(User user, List<String> codes)
    {
        List<MFARecoveryCode> derivedCodes = codes.stream()
                .map(code -> {
                    MFARecoveryCode newCode =
                            new MFARecoveryCode(Objects.
                                    requireNonNull(encoder.encode(code.replace("-", ""))));
                    newCode.setUser(user);
                    return newCode;
                }).toList();

        user.setRecoveryCodes(derivedCodes);
    }

    public MFAEnrolmentResponse startMFAEnrolment(User requestingUser, String enteredPassword)
    {
        if (!encoder.matches(enteredPassword, requestingUser.getPassword()))
        {
            return null;
        }

        List<String> codes = generateCodesForDisplay();

        String rawSecret = generateRandomSecret();

        TOTPSecret newSecret = new TOTPSecret(SecretStatus.PENDING, rawSecret);
        newSecret.setUser(requestingUser);
        requestingUser.setTotpSecret(newSecret);

        totpSecretRepository.save(newSecret);

        String generatedURL = String.format("otpauth://totp/%s:%s?secret=%s&issuer=%s&digits=%s",
                "rerebuild.ca", requestingUser.getEmail(), rawSecret, "rerebuild.ca", 6);

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


        associateCodesWithUser(enrollingUser, enrolmentForm.recoveryCodes());

        userRepository.save(enrollingUser);

        return ResponseEntity.ok("Enrolment in MFA is successful");

    }


    public boolean verifyRecoveryCode(String emailOrPhone, String enteredCode)
    {
        User foundUser = userRepository.findByEmailOrPhoneWithRecoveryCodes(emailOrPhone).orElse(null);

        assert foundUser != null;

        String recoveryCodeRegex = "^[a-zA-Z0-9]{4}-?[a-zA-Z0-9]{4}-?[a-zA-Z0-9]{4}-?[a-zA-Z0-9]{4}$";

        Pattern regexPattern = Pattern.compile(recoveryCodeRegex);

        if(!regexPattern.matcher(enteredCode).matches())
        {
            return false;
        }

        String normalizedCode = enteredCode.replace("-", "").toUpperCase();

        List<MFARecoveryCode> validCodes = foundUser.getRecoveryCodes().
                stream().filter(code -> !code.isUsed()).toList();


        for (MFARecoveryCode recoveryCode : validCodes)
        {
            if (encoder.matches(normalizedCode, recoveryCode.getHashedCode()))
            {
                recoveryCode.setUsed(true);
                recoveryCodeRepository.save(recoveryCode);
                return true;
            }
        }

        return false;
    }


    public List<String> regenerateRecoveryCodesFor(User user)
    {
        List<String> newCodes = generateCodesForDisplay();

        associateCodesWithUser(user, newCodes);

        return newCodes;
    }
}
