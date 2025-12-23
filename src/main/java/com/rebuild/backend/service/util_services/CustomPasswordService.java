package com.rebuild.backend.service.util_services;

import com.rebuild.backend.model.entities.users.User;
import com.rebuild.backend.repository.user_repositories.UserRepository;
import com.rebuild.backend.service.user_services.UserService;
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
    public UserDetails updatePassword(UserDetails oldDetails, String newPassword) {
        User updatingUser = userRepository.findByEmail(oldDetails.getUsername()).orElseThrow(
                () -> new UsernameNotFoundException("User not found")
        );

        updatingUser.setPassword(newPassword);
        return userRepository.save(updatingUser);
    }
}
