-- Flyway migration to add pinned and locked columns to decisions table
-- Version 3 chosen to run on top of version 2 migration

ALTER TABLE decisions ADD COLUMN IF NOT EXISTS pinned BOOLEAN NOT NULL DEFAULT FALSE;
ALTER TABLE decisions ADD COLUMN IF NOT EXISTS locked BOOLEAN NOT NULL DEFAULT FALSE;
