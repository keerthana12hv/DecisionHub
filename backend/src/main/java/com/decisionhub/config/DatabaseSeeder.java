package com.decisionhub.config;

import com.decisionhub.entity.authentication.User;
import com.decisionhub.enums.authentication.PlatformRole;
import com.decisionhub.enums.authentication.UserStatus;
import com.decisionhub.repository.authentication.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DatabaseSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    // This pulls the values you just added to application.properties
    @Value("${app.admin.email}")
    private String adminEmail;

    @Value("${app.admin.password}")
    private String adminPassword;

    public DatabaseSeeder(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) throws Exception {
        
        // Checks if the admin already exists in the DB so we don't duplicate it
        if (!userRepository.existsByEmail(adminEmail)) {
            
            User admin = new User();
            admin.setUsername("SystemAdmin");
            admin.setEmail(adminEmail);
            
            // Hashes the password using your exact security setup
            admin.setPasswordHash(passwordEncoder.encode(adminPassword));
            
            admin.setRole(PlatformRole.ADMIN);
            admin.setStatus(UserStatus.ACTIVE);

            userRepository.save(admin);
            
            System.out.println("\n✅ ======================================");
            System.out.println("✅ Default Admin Seeded Successfully!");
            System.out.println("✅ Email: " + adminEmail);
            System.out.println("✅ ======================================\n");
        }
    }
}