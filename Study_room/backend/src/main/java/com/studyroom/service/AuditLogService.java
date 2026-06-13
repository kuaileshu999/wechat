package com.studyroom.service;

import com.studyroom.entity.AuditLog;
import com.studyroom.repository.AuditLogRepository;
import com.studyroom.repository.SysUserRepository;
import com.studyroom.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AuditLogService {

    private final AuditLogRepository auditLogRepository;
    private final SysUserRepository userRepository;

    public void log(String entityType, Long entityId, String action, String content) {
        AuditLog log = new AuditLog();
        log.setEntityType(entityType);
        log.setEntityId(entityId);
        log.setAction(action);
        log.setContent(content);
        try {
            Long userId = SecurityUtils.getCurrentUserId();
            log.setOperatorId(userId);
            userRepository.findById(userId).ifPresent(u -> log.setOperatorName(
                    u.getRealName() != null ? u.getRealName() : u.getUsername()));
        } catch (Exception ignored) {
            log.setOperatorName("system");
        }
        auditLogRepository.save(log);
    }

    public List<AuditLog> getLogs(String entityType, Long entityId) {
        return auditLogRepository.findByEntityTypeAndEntityIdOrderByCreatedAtDesc(entityType, entityId);
    }
}
