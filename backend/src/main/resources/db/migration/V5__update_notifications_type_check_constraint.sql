ALTER TABLE notifications
DROP CONSTRAINT IF EXISTS notifications_type_check;

ALTER TABLE notifications
    ADD CONSTRAINT notifications_type_check
        CHECK (type IN (
                        'DECISION_PUBLISHED',
                        'DECISION_LOCKED',
                        'DECISION_UNLOCKED',
                        'DECISION_PINNED',
                        'DECISION_UNPINNED',
                        'COMMENT_CREATED',
                        'REPLY_CREATED',
                        'COMMENT_REMOVED',
                        'VOTE_SUBMITTED',
                        'POLL_CLOSED',
                        'FEEDBACK_REMINDER',
                        'MEMBERSHIP_APPROVED',
                        'MEMBERSHIP_REJECTED',
                        'MEMBER_PROMOTED'
            ));