package com.rebuild.backend.service.util_services;

import com.rebuild.backend.model.entities.users.User;
import com.rebuild.backend.repository.user_repositories.UserRepository;
import com.rebuild.backend.service.user_services.UserAuthenticationHelperService;
import com.rebuild.backend.service.user_services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Nonnull;

@Service(value = "details")
@Transactional(readOnly = true)
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Autowired
    public CustomUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }


    @Override
    @Transactional
    public @Nonnull User loadUserByUsername(@Nonnull String username) throws UsernameNotFoundException {
        return userRepository.findByEmailOrPhoneNumber(username).orElseThrow(
                () -> new UsernameNotFoundException("Email " + username + " not found"));
    }
}
