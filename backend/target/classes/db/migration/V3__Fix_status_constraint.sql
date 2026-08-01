-- V3: Re-apply the status check constraint.
-- The constraint is sometimes lost when hibernate ddl-auto=update
-- recreates or alters the decisions table without re-applying it.
-- This migration drops any existing version and re-adds it cleanly.

ALTER TABLE decisions DROP CONSTRAINT IF EXISTS decisions_status_check;

ALTER TABLE decisions
    ADD CONSTRAINT decisions_status_check
    CHECK (status::text = ANY (
        ARRAY['DRAFT'::text, 'ACTIVE'::text, 'OPEN'::text, 'CLOSED'::text, 'ARCHIVED'::text]
    ));
