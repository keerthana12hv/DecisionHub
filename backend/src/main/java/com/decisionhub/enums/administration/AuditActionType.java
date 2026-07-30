package com.decisionhub.enums.administration;

public enum AuditActionType {

    LOGIN,
    LOGOUT,
    REGISTER,

    CREATE_COMMUNITY,
    UPDATE_COMMUNITY,
    DELETE_COMMUNITY,

    CREATE_DECISION,
    UPDATE_DECISION,
    DELETE_DECISION,

    CREATE_POLL,
    CAST_VOTE,

    DECISION_FEEDBACK_SUBMITTED, 

    SUPPORT_TICKET_CREATED,         // ✅ Added for Support Module
    SUPPORT_TICKET_STATUS_UPDATED,  // ✅ Added for Support Module

    EXPORT_REPORT,

    ADMIN_ACTION
}