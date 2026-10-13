package com.servicesphere.audit;

import com.servicesphere.audit.dto.AuditLogDto;
import com.servicesphere.common.PagedResponse;
import com.servicesphere.user.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuditService {

    private final AuditLogRepository auditLogRepository;

    public AuditService(AuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }

    @Transactional
    public void recordEvent(User actor, String action, String entityType, String entityId, String details, String ipAddress) {
        SystemAuditLog log = new SystemAuditLog(actor, action, entityType, entityId, details, ipAddress);
        auditLogRepository.save(log);
    }

    @Transactional(readOnly = true)
    public PagedResponse<AuditLogDto> getAuditLogs(int page, int size) {
        Page<SystemAuditLog> logs = auditLogRepository.findAllByOrderByCreatedAtDesc(PageRequest.of(page, size));
        return PagedResponse.of(logs.map(AuditLogDto::fromEntity));
    }
}
