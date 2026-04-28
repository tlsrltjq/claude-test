package com.eactive.resourcehub.audit.repository;

import com.eactive.resourcehub.audit.entity.AuditActionType;
import com.eactive.resourcehub.audit.entity.AuditLog;
import com.eactive.resourcehub.audit.entity.AuditTargetType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {

    List<AuditLog> findByUserId(Long userId);

    List<AuditLog> findByTargetTypeAndTargetId(AuditTargetType targetType, Long targetId);

    Page<AuditLog> findByActionType(AuditActionType actionType, Pageable pageable);

    List<AuditLog> findByUserIdAndCreatedAtBetween(Long userId,
                                                    LocalDateTime from,
                                                    LocalDateTime to);
}
