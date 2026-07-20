-- Flyway migration to add voting fields and update decisions table constraints
-- Version 2 chosen to run successfully on top of version 1 baseline

ALTER TABLE decisions ADD COLUMN IF NOT EXISTS voting_type VARCHAR(50) NOT NULL DEFAULT 'RATING_BASED';
ALTER TABLE decisions ADD COLUMN IF NOT EXISTS voting_end_time TIMESTAMP;

-- Drop and recreate status constraint to include ACTIVE
ALTER TABLE decisions DROP CONSTRAINT IF EXISTS decisions_status_check;
ALTER TABLE decisions ADD CONSTRAINT decisions_status_check CHECK (status::text = ANY (ARRAY['DRAFT'::text, 'ACTIVE'::text, 'OPEN'::text, 'CLOSED'::text, 'ARCHIVED'::text]));

-- Drop and recreate visibility constraint to include COMMUNITY
ALTER TABLE decisions DROP CONSTRAINT IF EXISTS decisions_visibility_check;
ALTER TABLE decisions ADD CONSTRAINT decisions_visibility_check CHECK (visibility::text = ANY (ARRAY['PUBLIC'::text, 'COMMUNITY'::text, 'PRIVATE'::text]));
