package com.rebuild.backend.service.auth_services;

import com.rebuild.backend.model.dtos.RecoveryCodesDTO;
import com.rebuild.backend.model.entities.user_entities.MFARecoveryCodeEntity;
import com.rebuild.backend.model.entities.user_entities.TemporaryMFACredentials;
import com.rebuild.backend.model.entities.user_entities.User;
import com.rebuild.backend.utils.exceptions.UserAuthException;
import com.rebuild.backend.repository.user_repositories.RecoveryCodeRepository;
import com.rebuild.backend.repository.user_repositories.TemporaryMFACredentialsRepository;
import com.rebuild.backend.repository.user_repositories.UserRepository;
import com.rebuild.backend.model.entities.util_entitites.RecoveryCode;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Service
public class RecoveryCodeHelperService {

    private final UserRepository userRepository;

    private final RecoveryCodeRepository recoveryCodeRepository;

    private final TemporaryMFACredentialsRepository temporaryMFACredentialsRepository;

    public RecoveryCodeHelperService(UserRepository userRepository,
                                     RecoveryCodeRepository recoveryCodeRepository,
                                     TemporaryMFACredentialsRepository temporaryMFACredentialsRepository) {
        this.userRepository = userRepository;
        this.recoveryCodeRepository = recoveryCodeRepository;
        this.temporaryMFACredentialsRepository = temporaryMFACredentialsRepository;
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
        List<RecoveryCode> rawCodes = RecoveryCode.createCodes();

        RecoveryCodesDTO recoveryCodesDTO = getHashedAndDisplayedCodes(rawCodes);

        Optional<TemporaryMFACredentials> foundCredentials = temporaryMFACredentialsRepository.
                findByEmailAndExpiryTimeAfter(user.getEmail(), Instant.now());
        foundCredentials.ifPresent(temporaryMFACredentialsRepository::delete);

        TemporaryMFACredentials temporaryCodes = new TemporaryMFACredentials(user.getEmail(),
                recoveryCodesDTO.hashedCodes(), Instant.now().plus(Duration.ofMinutes(10)));
        temporaryMFACredentialsRepository.save(temporaryCodes);
        userRepository.save(user);

        return recoveryCodesDTO.displayedCodes();
    }


    @Transactional
    public User associateGeneratedCodes(User user, boolean confirmation)
    {
        if (!confirmation)
        {
            throw new UserAuthException(HttpStatus.BAD_REQUEST, "Please confirm that you have saved the " +
                    "codes somewhere safe, and that you will not be able to see them again.");
        }
        Optional<TemporaryMFACredentials> foundCredentials = temporaryMFACredentialsRepository.
                findByEmailAndExpiryTimeAfter(user.getEmail(), Instant.now());
        if (foundCredentials.isPresent())
        {
            TemporaryMFACredentials actualCredentials = foundCredentials.get();
            associateCodesWithUser(user, actualCredentials.getTemporaryCodes());
            temporaryMFACredentialsRepository.delete(actualCredentials);
            return userRepository.save(user);
        }
        // If there is no such credential found in this case, we have to indicate to
        // the user that the codes they generated are no longer valid
        else
        {
           throw new UserAuthException(HttpStatus.UNPROCESSABLE_CONTENT,
                   "The codes that you generated have expired, please regenerate the codes and try again.");
        }
    }


    @Transactional
    public boolean verifyRecoveryCode(String emailOrPhone, String enteredCode)
    {
        User foundUser = userRepository.findByEmailWithRecoveryCodes(emailOrPhone).orElse(null);
        RecoveryCode inputCodeRepresentation = RecoveryCode.fromInput(enteredCode);
        assert foundUser != null;

        List<MFARecoveryCodeEntity> validCodes = foundUser.getRecoveryCodes().
                stream().filter(code -> !code.isUsed()).toList();

        if (validCodes.isEmpty())
        {
            return false;
        }


        for (MFARecoveryCodeEntity recoveryCode : validCodes)
        {
            if (inputCodeRepresentation.hashedValueMatches(recoveryCode.getHashedCode()))
            {
                recoveryCode.setUsed(true);
                recoveryCodeRepository.save(recoveryCode);
                return true;
            }
        }

        return false;
    }
}
