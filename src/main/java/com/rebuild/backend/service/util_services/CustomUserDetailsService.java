package com.rebuild.backend.service.util_services;

import com.rebuild.backend.model.entities.users.User;
import com.rebuild.backend.repository.UserRepository;
import com.rebuild.backend.service.user_services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service(value = "details")
@Transactional(readOnly = true)
public class CustomUserDetailsService implements UserDetailsService {

    private final UserService userService;

    @Autowired
    public CustomUserDetailsService(UserService userService) {
        this.userService = userService;
    }


    @Override
    @Transactional
    public User loadUserByUsername(String username) throws UsernameNotFoundException {
        return userService.findByEmailOrPhone(username).orElseThrow(
                () -> new UsernameNotFoundException("Email " + username + " not found"));
    }
}
