package com.decisionhub.config;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class DatabaseMigrationHelper {

    private final JdbcTemplate jdbcTemplate;

    @PostConstruct
    public void dropNotificationConstraint() {
        try {
            log.info("Dropping notifications_type_check constraint if it exists...");
            jdbcTemplate.execute("ALTER TABLE notifications DROP CONSTRAINT IF EXISTS notifications_type_check");
            log.info("Successfully dropped notifications_type_check constraint!");
        } catch (Exception e) {
            log.warn("Failed to drop notifications_type_check constraint: {}", e.getMessage());
        }
    }
}
