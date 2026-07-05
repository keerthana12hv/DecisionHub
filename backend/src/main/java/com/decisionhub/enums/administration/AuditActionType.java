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

    EXPORT_REPORT,

    ADMIN_ACTION
}