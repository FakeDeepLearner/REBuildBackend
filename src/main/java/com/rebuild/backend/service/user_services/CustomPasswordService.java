package com.rebuild.backend.service.user_services;

import com.rebuild.backend.model.entities.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsPasswordService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service(value = "password_service")
public class CustomPasswordService implements UserDetailsPasswordService {

    private final UserService userService;

    @Autowired
    public CustomPasswordService(UserService userService) {
        this.userService = userService;
    }

    @Override
    public UserDetails updatePassword(UserDetails oldDetails, String newPassword) {
        User updatingUser = userService.findByEmail(oldDetails.getUsername()).orElseThrow(
                () -> new UsernameNotFoundException("User not found")
        );

        updatingUser.setPassword(newPassword);
        return userService.save(updatingUser);
    }
}
