package com.decisionhub.service.impl.audit;

import com.decisionhub.entity.administration.AuditLog;
import com.decisionhub.entity.authentication.User;
import com.decisionhub.enums.administration.AuditActionType;
import com.decisionhub.repository.decision.AuditLogRepository;
import com.decisionhub.service.interfaces.audit.AuditService;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class AuditServiceImpl implements AuditService {

    private final AuditLogRepository auditLogRepository;

    public AuditServiceImpl(AuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }

    @Override
    @Transactional
    public void log(User user, String action, String tableName, Long entityId, String oldValue, String newValue, String ipAddress, String userAgent) {
        AuditActionType actionType = AuditActionType.ADMIN_ACTION;
        if (action != null) {
            switch (action) {
                case "OPTION_CREATED":
                case "FACTOR_CREATED":
                case "SCORE_CREATED":
                    actionType = AuditActionType.CREATE_DECISION;
                    break;
                case "OPTION_UPDATED":
                case "FACTOR_UPDATED":
                case "SCORE_UPDATED":
                    actionType = AuditActionType.UPDATE_DECISION;
                    break;
                case "OPTION_DELETED":
                case "FACTOR_DELETED":
                case "SCORE_DELETED":
                    actionType = AuditActionType.DELETE_DECISION;
                    break;
            }
        }

        AuditLog log = new AuditLog();
        log.setAction(actionType);
        log.setPerformedBy(user);
        log.setPerformedAt(LocalDateTime.now());
        auditLogRepository.save(log);
    }
}
