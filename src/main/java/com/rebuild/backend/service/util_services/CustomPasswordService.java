package com.rebuild.backend.service.util_services;

import com.rebuild.backend.model.entities.users.User;
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

    private final UserService userService;

    @Autowired
    public CustomPasswordService(UserService userService) {
        this.userService = userService;
    }

    @Override
    @Transactional
    public UserDetails updatePassword(UserDetails oldDetails, String newPassword) {
        User updatingUser = userService.findByEmail(oldDetails.getUsername()).orElseThrow(
                () -> new UsernameNotFoundException("User not found")
        );

        updatingUser.setPassword(newPassword);
        return userService.save(updatingUser);
    }
}
