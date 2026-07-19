-- Flyway migration to add voting_type and voting_end_time to decisions table
-- Maintain PostgreSQL compatibility with default fallback values for existing rows

ALTER TABLE decisions ADD COLUMN voting_type VARCHAR(50) NOT NULL DEFAULT 'RATING_BASED';
ALTER TABLE decisions ADD COLUMN voting_end_time TIMESTAMP;
