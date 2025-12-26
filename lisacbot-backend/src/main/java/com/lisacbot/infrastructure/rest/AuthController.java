package com.lisacbot.infrastructure.rest;

import com.lisacbot.domain.service.AuthenticationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * REST controller for authentication endpoints.
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private static final Logger log = LoggerFactory.getLogger(AuthController.class);

    @Autowired
    private AuthenticationService authenticationService;

    /**
     * Login endpoint to authenticate user and return JWT token.
     *
     * @param loginRequest containing username and password
     * @return JWT token and username
     */
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> loginRequest) {
        try {
            String username = loginRequest.get("username");
            String password = loginRequest.get("password");

            if (username == null || password == null) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "Username and password are required"));
            }

            String token = authenticationService.authenticateAndGenerateToken(username, password);

            return ResponseEntity.ok(Map.of(
                    "token", token,
                    "username", username
            ));
        } catch (AuthenticationException e) {
            log.warn("Authentication failed: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Invalid username or password"));
        } catch (Exception e) {
            log.error("Login error", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "An error occurred during login"));
        }
    }

    /**
     * Change password endpoint.
     *
     * @param request containing currentPassword and newPassword
     * @param authentication the authenticated user
     * @return success or error message
     */
    @PostMapping("/change-password")
    public ResponseEntity<?> changePassword(
            @RequestBody Map<String, String> request,
            Authentication authentication
    ) {
        try {
            String currentPassword = request.get("currentPassword");
            String newPassword = request.get("newPassword");

            if (currentPassword == null || newPassword == null) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "Current password and new password are required"));
            }

            if (newPassword.length() < 4) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "New password must be at least 4 characters"));
            }

            String username = authentication.getName();
            authenticationService.changePassword(username, currentPassword, newPassword);

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Password changed successfully"
            ));
        } catch (AuthenticationException e) {
            log.warn("Password change failed - invalid current password: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Current password is incorrect"));
        } catch (Exception e) {
            log.error("Password change error", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "An error occurred while changing password"));
        }
    }
}
