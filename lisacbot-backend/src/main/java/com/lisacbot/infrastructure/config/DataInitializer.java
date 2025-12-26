package com.lisacbot.infrastructure.config;

import com.lisacbot.infrastructure.persistence.JpaUserRepository;
import com.lisacbot.infrastructure.persistence.UserEntity;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * Initializes the database with default data on application startup.
 */
@Component
public class DataInitializer {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    @Autowired
    private JpaUserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Value("${app.admin.username}")
    private String adminUsername;

    @Value("${app.admin.password}")
    private String adminPassword;

    /**
     * Create default admin user if it doesn't exist.
     */
    @PostConstruct
    public void init() {
        // Security warning if using default credentials
        if ("admin".equals(adminUsername) && "admin".equals(adminPassword)) {
            log.warn("========================================================");
            log.warn("  SECURITY WARNING: Using default credentials!");
            log.warn("  Username: admin / Password: admin");
            log.warn("  CHANGE THESE IMMEDIATELY in production!");
            log.warn("  Set ADMIN_USERNAME and ADMIN_PASSWORD env variables");
            log.warn("========================================================");
        }

        if (userRepository.findByUsername(adminUsername).isEmpty()) {
            log.info("Creating default admin user: {}", adminUsername);

            UserEntity admin = new UserEntity(
                    adminUsername,
                    passwordEncoder.encode(adminPassword)
            );

            userRepository.save(admin);
            log.info("Default admin user created successfully");

            // Additional warning after creation
            if ("admin".equals(adminPassword)) {
                log.warn("SECURITY: Default password detected. Please change it using /api/auth/change-password");
            }
        } else {
            log.info("Admin user already exists: {}", adminUsername);
        }
    }
}
