package com.eactive.resourcehub.common.service;

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
public class AuditService {

    private final AuditLogRepository auditLogRepository;
    private final UserRepository userRepository;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void log(Long actorUserId, AuditActionType action,
                    AuditTargetType targetType, Long targetId,
                    String reason, HttpServletRequest request) {
        try {
            User actor = userRepository.findById(actorUserId).orElse(null);
            if (actor == null) {
                log.warn("감사 로그 기록 실패 — 사용자 없음: userId={}", actorUserId);
                return;
            }
            String ip = getClientIp(request);
            String ua = request != null ? request.getHeader("User-Agent") : null;
            AuditLog entry = AuditLog.record(actor, action, targetType, targetId, reason, ip, ua);
            auditLogRepository.save(entry);
        } catch (Exception e) {
            log.warn("감사 로그 기록 실패: {}", e.getMessage());
        }
    }

    private String getClientIp(HttpServletRequest request) {
        if (request == null) return null;
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isBlank()) {
            return xForwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
