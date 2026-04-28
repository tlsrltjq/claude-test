package com.eactive.resourcehub.audit.service;

import com.eactive.resourcehub.audit.entity.AuditActionType;
import com.eactive.resourcehub.audit.entity.AuditLog;
import com.eactive.resourcehub.audit.entity.AuditTargetType;
import com.eactive.resourcehub.audit.repository.AuditLogRepository;
import com.eactive.resourcehub.user.entity.User;
import com.eactive.resourcehub.user.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuditLogService {

    private final AuditLogRepository auditLogRepository;
    private final UserRepository userRepository;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void logView(Long userId, Long documentVersionId, HttpServletRequest request) {
        record(userId, AuditActionType.VIEW, AuditTargetType.DOCUMENT_VERSION,
                documentVersionId, null, request);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void logDownload(Long userId, Long documentVersionId, String reason,
                            HttpServletRequest request) {
        record(userId, AuditActionType.DOWNLOAD, AuditTargetType.DOCUMENT_VERSION,
                documentVersionId, reason, request);
    }

    private void record(Long userId, AuditActionType action, AuditTargetType targetType,
                        Long targetId, String reason, HttpServletRequest request) {
        try {
            User user = userRepository.findById(userId).orElse(null);
            if (user == null) return;
            String ip = extractIp(request);
            String ua = request != null ? request.getHeader("User-Agent") : null;
            auditLogRepository.save(AuditLog.record(user, action, targetType, targetId, reason, ip, ua));
        } catch (Exception e) {
            log.warn("감사 로그 기록 실패: {}", e.getMessage());
        }
    }

    private String extractIp(HttpServletRequest request) {
        if (request == null) return null;
        String xff = request.getHeader("X-Forwarded-For");
        return (xff != null && !xff.isBlank()) ? xff.split(",")[0].trim() : request.getRemoteAddr();
    }
}
