package com.rebuild.backend.service.util_services;

import com.rebuild.backend.model.entities.user_entities.User;

import com.rebuild.backend.repository.user_repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsPasswordService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service(value = "password_service")
@Transactional(readOnly = true)
public class CustomPasswordService implements UserDetailsPasswordService {

    private final UserRepository userRepository;

    @Autowired
    public CustomPasswordService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    @Transactional
    public UserDetails updatePassword(UserDetails user, String newPassword) {
        User updatingUser = userRepository.findByEmail(user.getUsername()).orElseThrow(
                () -> new UsernameNotFoundException("User not found")
        );

        updatingUser.setPassword(newPassword);
        return userRepository.save(updatingUser);
    }
}
