package com.rebuild.backend.service.user_services;

import com.rebuild.backend.model.entities.User;
import com.rebuild.backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service(value = "details")
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Autowired
    public CustomUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }


    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User foundUser = userRepository.findByEmail(username).orElseThrow(
                () -> new UsernameNotFoundException("Email " + username + " not found"));
        return org.springframework.security.core.userdetails.User.builder().username(foundUser.getUsername())
                .password(foundUser.getPassword()).
                roles(foundUser.getAuthorities().toString()).
                accountExpired(!foundUser.isAccountNonExpired()).
                accountLocked(!foundUser.isAccountNonLocked()).
                credentialsExpired(!foundUser.isCredentialsNonExpired()).
                disabled(!foundUser.isEnabled()).
                build();
    }
}
