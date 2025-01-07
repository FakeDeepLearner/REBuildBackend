package com.rebuild.backend.service.token_services;

import com.rebuild.backend.model.entities.users.RememberMeToken;
import com.rebuild.backend.model.entities.users.User;
import com.rebuild.backend.repository.RememberMeTokenRepository;
import com.rebuild.backend.repository.UserRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class RememberMeService {

    private final UserRepository userRepository;

    private final RememberMeTokenRepository rememberMeTokenRepository;

    public RememberMeService(UserRepository userRepository, RememberMeTokenRepository rememberMeTokenRepository) {
        this.userRepository = userRepository;
        this.rememberMeTokenRepository = rememberMeTokenRepository;
    }

    private void deleteExpiredTokens(LocalDateTime cutoff) {
        List<RememberMeToken> tokensToDelete = rememberMeTokenRepository.findByExpiryTimeBefore(cutoff);

        tokensToDelete.forEach(token -> {
            User tokenUser = token.getUser();
            tokenUser.setAssociatedRememberMeToken(null);
            //Here, we do not need to also delete the token,
            // because the orphan removal on the user-token relation deletes the object automatically
            userRepository.save(tokenUser);
        });
    }

    @Scheduled(cron = "@midnight")
    public void deleteExpiredTokensTask(){
        deleteExpiredTokens(LocalDateTime.now());
    }
}
