package com.decisionhub.config;

import com.decisionhub.entity.authentication.User;
import com.decisionhub.enums.authentication.PlatformRole;
import com.decisionhub.enums.authentication.UserStatus;
import com.decisionhub.repository.authentication.UserRepository;
import com.decisionhub.entity.community.Category;
import com.decisionhub.repository.community.CategoryRepository;
import com.decisionhub.entity.decision.Decision;
import com.decisionhub.enums.decision.DecisionStatus;
import com.decisionhub.enums.decision.DecisionVisibility;
import com.decisionhub.repository.decision.DecisionRepository;
import java.time.LocalDateTime;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DatabaseSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final CategoryRepository categoryRepository;
    private final DecisionRepository decisionRepository;

    @Value("${app.admin.email}")
    private String adminEmail;

    @Value("${app.admin.password}")
    private String adminPassword;

    public DatabaseSeeder(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            CategoryRepository categoryRepository,
            DecisionRepository decisionRepository) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.categoryRepository = categoryRepository;
        this.decisionRepository = decisionRepository;
    }

    @Override
    public void run(String... args) throws Exception {
        // Seed default categories if none exist
        if (categoryRepository.count() == 0) {
            String[][] defaultCategories = {
                {"General", "general"},
                {"Technology", "technology"},
                {"Business", "business"},
                {"Health", "health"},
                {"Education", "education"}
            };

            for (String[] catData : defaultCategories) {
                Category category = new Category();
                category.setName(catData[0]);
                category.setSlug(catData[1]);
                category.setIsActive(true);
                categoryRepository.save(category);
            }
            System.out.println("✅ Seeded default categories successfully!");
        }
        
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

        // Seed a sample public decision for local development if none exist
        try {
            if (decisionRepository != null && decisionRepository.count() == 0) {
                User adminUser = userRepository.findByEmail(adminEmail).orElse(null);
                if (adminUser != null) {
                    Decision sample = new Decision();
                    sample.setTitle("Sample Decision: Adopt new logo");
                    sample.setDescription("[Cat:General] Should we adopt the new company logo for the website?");
                    sample.setCreator(adminUser);
                    sample.setVisibility(DecisionVisibility.PUBLIC);
                    sample.setStatus(DecisionStatus.ACTIVE);
                    sample.setCreatedAt(LocalDateTime.now());
                    decisionRepository.save(sample);
                    System.out.println("✅ Seeded a sample decision for local development.");
                }
            }
        } catch (Exception e) {
            System.out.println("Could not seed sample decision: " + e.getMessage());
        }
    }
}