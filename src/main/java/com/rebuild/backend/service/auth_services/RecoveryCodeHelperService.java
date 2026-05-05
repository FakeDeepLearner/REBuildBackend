package com.rebuild.backend.service.auth_services;

import com.rebuild.backend.model.dtos.RecoveryCodesDTO;
import com.rebuild.backend.model.entities.user_entities.MFARecoveryCodeEntity;
import com.rebuild.backend.model.entities.user_entities.User;
import com.rebuild.backend.model.responses.RecoveryCodeVerificationResponse;
import com.rebuild.backend.repository.user_repositories.RecoveryCodeRepository;
import com.rebuild.backend.repository.user_repositories.UserRepository;
import com.rebuild.backend.model.entities.user_entities.RecoveryCode;
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



    public List<RecoveryCode> generateCodes()
    {
        return Stream.generate(RecoveryCode::create).
                limit(NUM_RECOVERY_CODES_GENERATED).toList();
    }

    public RecoveryCodesDTO getHashedAndDisplayedCodes(List<RecoveryCode> rawCodes)
    {
        List<String> displayedCodes = rawCodes.stream().map(RecoveryCode::getDisplayValue).toList();

        List<String> hashedCodes = rawCodes.stream().map(RecoveryCode::hashedValue).toList();

        return new RecoveryCodesDTO(hashedCodes, displayedCodes);
    }

    @Transactional
    public void associateCodesWithUser(User user, List<String> codes)
    {
        List<MFARecoveryCodeEntity> derivedCodes = codes.stream()
                .map(code -> {
                    MFARecoveryCodeEntity newCode =
                            new MFARecoveryCodeEntity(code);
                    newCode.setUser(user);
                    return newCode;
                }).toList();

        user.setRecoveryCodes(derivedCodes);
    }


    @Transactional
    public List<String> regenerateRecoveryCodesFor(User user)
    {
        List<RecoveryCode> rawCodes = generateCodes();

        RecoveryCodesDTO recoveryCodesDTO = getHashedAndDisplayedCodes(rawCodes);

        associateCodesWithUser(user, recoveryCodesDTO.hashedCodes());
        userRepository.save(user);

        return recoveryCodesDTO.displayedCodes();
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
