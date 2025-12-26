package com.lisacbot.infrastructure.security;

import com.lisacbot.infrastructure.persistence.JpaUserRepository;
import com.lisacbot.infrastructure.persistence.UserEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;

/**
 * Custom UserDetailsService implementation for loading user from database.
 */
@Service
public class CustomUserDetailsService implements UserDetailsService {

    @Autowired
    private JpaUserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        UserEntity userEntity = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));

        return User.builder()
                .username(userEntity.getUsername())
                .password(userEntity.getPassword())
                .disabled(!userEntity.isEnabled())
                .authorities(new ArrayList<>()) // No roles for simple auth
                .build();
    }
}
