package com.lisacbot.domain.service;

import com.lisacbot.infrastructure.persistence.JpaUserRepository;
import com.lisacbot.infrastructure.persistence.UserEntity;
import com.lisacbot.infrastructure.security.JwtTokenProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

/**
 * Service for handling user authentication.
 */
@Service
public class AuthenticationService {

    private static final Logger log = LoggerFactory.getLogger(AuthenticationService.class);

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Autowired
    private JpaUserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    /**
     * Authenticate user and generate JWT token.
     *
     * @param username the username
     * @param password the password
     * @return the JWT token
     * @throws AuthenticationException if authentication fails
     */
    public String authenticateAndGenerateToken(String username, String password) {
        log.info("Attempting authentication for user: {}", username);

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(username, password)
        );

        String token = jwtTokenProvider.generateToken(authentication);
        log.info("Authentication successful for user: {}", username);

        return token;
    }

    /**
     * Change user password.
     *
     * @param username the username
     * @param currentPassword the current password
     * @param newPassword the new password
     * @throws AuthenticationException if current password is incorrect
     * @throws IllegalArgumentException if user not found
     */
    public void changePassword(String username, String currentPassword, String newPassword) {
        log.info("Password change request for user: {}", username);

        // Verify current password
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(username, currentPassword)
        );

        // Load user and update password
        UserEntity user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + username));

        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        log.info("Password changed successfully for user: {}", username);
    }
}
