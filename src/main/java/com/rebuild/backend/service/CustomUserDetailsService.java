package com.rebuild.backend.service;

import com.rebuild.backend.model.entities.User;
import com.rebuild.backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service(value = "details")
public class CustomUserDetailsService {

    private final UserRepository userRepository;

    @Autowired
    public CustomUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }


    public UserDetails loadUserByEmail(String email){
        User foundUser = userRepository.findByEmail(email).orElseThrow(
                () -> new UsernameNotFoundException("Email " + email + " not found"));
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
