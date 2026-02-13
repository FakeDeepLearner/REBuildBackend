package com.rebuild.backend.service.auth_services;

import com.rebuild.backend.model.entities.user_entities.MFARecoveryCodeEntity;
import com.rebuild.backend.model.entities.user_entities.User;
import com.rebuild.backend.model.responses.RecoveryCodeVerificationResponse;
import com.rebuild.backend.repository.user_repositories.RecoveryCodeRepository;
import com.rebuild.backend.repository.user_repositories.UserRepository;
import com.rebuild.backend.utils.util_entities.RecoveryCode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

@Service
public class RecoveryCodeHelperService {

    private static final int NUM_RECOVERY_CODES_GENERATED = 8;

    private final UserRepository userRepository;

    private final RecoveryCodeRepository recoveryCodeRepository;

    public RecoveryCodeHelperService(UserRepository userRepository,
                                     RecoveryCodeRepository recoveryCodeRepository) {
        this.userRepository = userRepository;
        this.recoveryCodeRepository = recoveryCodeRepository;
    }

    List<String> generateCodesForDisplay()
    {
        return Stream.generate(RecoveryCode::create).
                map(RecoveryCode::getDisplayValue).
                limit(NUM_RECOVERY_CODES_GENERATED).toList();
    }


    @Transactional
    void associateCodesWithUser(User user, List<String> codes)
    {
        List<MFARecoveryCodeEntity> derivedCodes = codes.stream()
                .map(code -> {
                    RecoveryCode codeRepr = RecoveryCode.fromInput(code);
                    MFARecoveryCodeEntity newCode =
                            new MFARecoveryCodeEntity(Objects.
                                    requireNonNull(codeRepr.hashedValue()));
                    newCode.setUser(user);
                    return newCode;
                }).toList();

        user.setRecoveryCodes(derivedCodes);
    }


    @Transactional
    public List<String> regenerateRecoveryCodesFor(User user)
    {
        List<String> newCodes = generateCodesForDisplay();

        associateCodesWithUser(user, newCodes);
        userRepository.save(user);

        return newCodes;
    }


    @Transactional
    public RecoveryCodeVerificationResponse verifyRecoveryCode(String emailOrPhone, String enteredCode)
    {
        User foundUser = userRepository.findByEmailOrPhoneWithRecoveryCodes(emailOrPhone).orElse(null);
        RecoveryCode inputCodeRepresentation = RecoveryCode.fromInput(enteredCode);
        assert foundUser != null;



        List<MFARecoveryCodeEntity> validCodes = foundUser.getRecoveryCodes().
                stream().filter(code -> !code.isUsed()).toList();

        if (validCodes.isEmpty())
        {
            List<String> newlyGeneratedCodes = regenerateRecoveryCodesFor(foundUser);
            return new RecoveryCodeVerificationResponse(false, true,
                    newlyGeneratedCodes);
        }


        for (MFARecoveryCodeEntity recoveryCode : validCodes)
        {
            if (inputCodeRepresentation.hashedValueMatches(recoveryCode.getHashedCode()))
            {
                recoveryCode.setUsed(true);
                recoveryCodeRepository.save(recoveryCode);
                return new RecoveryCodeVerificationResponse(true, false, Collections.emptyList());
            }
        }

        return new RecoveryCodeVerificationResponse(false, false, Collections.emptyList());
    }
}
