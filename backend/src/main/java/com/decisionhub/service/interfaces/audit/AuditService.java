package com.decisionhub.service.interfaces.audit;

import com.decisionhub.entity.authentication.User;

public interface AuditService {
    void log(User user, String action, String tableName, Long entityId, String oldValue, String newValue, String ipAddress, String userAgent);
}
