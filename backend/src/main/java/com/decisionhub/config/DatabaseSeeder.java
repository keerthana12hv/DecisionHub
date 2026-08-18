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
import org.springframework.core.env.Environment;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DatabaseSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final CategoryRepository categoryRepository;
    private final DecisionRepository decisionRepository;
    private final Environment environment;

    @Value("${app.admin.email}")
    private String adminEmail;

    @Value("${app.admin.password}")
    private String adminPassword;

    public DatabaseSeeder(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            CategoryRepository categoryRepository,
            DecisionRepository decisionRepository,
            Environment environment
    ) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.categoryRepository = categoryRepository;
        this.decisionRepository = decisionRepository;
        this.environment = environment;
    }

    @Override
    public void run(String... args) throws Exception {

        // ============================================================
        // SEED DEFAULT CATEGORIES
        // ============================================================

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

            System.out.println(
                    "Seeded default categories successfully!"
            );
        }

        // ============================================================
        // SEED DEFAULT ADMIN
        // ============================================================

        if (!userRepository.existsByEmail(adminEmail)) {

            User admin = new User();

            admin.setUsername("SystemAdmin");
            admin.setEmail(adminEmail);

            // Password is hashed using the application's configured
            // PasswordEncoder.
            admin.setPasswordHash(
                    passwordEncoder.encode(adminPassword)
            );

            admin.setRole(PlatformRole.ADMIN);
            admin.setStatus(UserStatus.ACTIVE);

            userRepository.save(admin);

            System.out.println("\n======================================");
            System.out.println("Default Admin Seeded Successfully!");
            System.out.println("Email: " + adminEmail);
            System.out.println("======================================\n");
        }

        // ============================================================
        // SEED SAMPLE DECISION - LOCAL DEVELOPMENT ONLY
        // ============================================================

        if (isLocalProfileActive()) {

            try {

                if (decisionRepository.count() == 0) {

                    User adminUser =
                            userRepository
                                    .findByEmail(adminEmail)
                                    .orElse(null);

                    if (adminUser != null) {

                        Decision sample = new Decision();

                        sample.setTitle(
                                "Sample Decision: Adopt new logo"
                        );

                        sample.setDescription(
                                "[Cat:General] Should we adopt the new company logo for the website?"
                        );

                        sample.setCreator(adminUser);

                        sample.setVisibility(
                                DecisionVisibility.PUBLIC
                        );

                        sample.setStatus(
                                DecisionStatus.ACTIVE
                        );

                        sample.setCreatedAt(
                                LocalDateTime.now()
                        );

                        decisionRepository.save(sample);

                        System.out.println(
                                "Seeded a sample decision for local development."
                        );
                    }
                }

            } catch (Exception e) {

                System.out.println(
                        "Could not seed sample decision: "
                                + e.getMessage()
                );
            }
        }
    }

    /**
     * Returns true only when the local Spring profile is active.
     *
     * The sample decision must never be created in production.
     */
    private boolean isLocalProfileActive() {

        for (String profile : environment.getActiveProfiles()) {

            if ("local".equalsIgnoreCase(profile)) {
                return true;
            }
        }

        return false;
    }
}