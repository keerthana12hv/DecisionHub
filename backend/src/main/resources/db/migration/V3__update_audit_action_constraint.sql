ALTER TABLE audit_logs
DROP CONSTRAINT IF EXISTS audit_logs_action_check;

ALTER TABLE audit_logs
ADD CONSTRAINT audit_logs_action_check
CHECK (
    action IN (
        'LOGIN',
        'LOGOUT',
        'REGISTER',
        'CREATE_COMMUNITY',
        'UPDATE_COMMUNITY',
        'DELETE_COMMUNITY',
        'CREATE_DECISION',
        'UPDATE_DECISION',
        'DELETE_DECISION',
        'CREATE_POLL',
        'CAST_VOTE',
        'EXPORT_REPORT',
        'ADMIN_ACTION',
        'SUPPORT_TICKET_CREATED',
        'SUPPORT_TICKET_STATUS_UPDATED',
        'DECISION_FEEDBACK_SUBMITTED'
    )
);