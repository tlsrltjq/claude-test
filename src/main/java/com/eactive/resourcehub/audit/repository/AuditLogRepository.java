package com.eactive.resourcehub.audit.repository;

import com.eactive.resourcehub.audit.entity.AuditActionType;
import com.eactive.resourcehub.audit.entity.AuditLog;
import com.eactive.resourcehub.audit.entity.AuditTargetType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {

    List<AuditLog> findByUserId(Long userId);

    List<AuditLog> findByTargetTypeAndTargetId(AuditTargetType targetType, Long targetId);

    long countByActionTypeAndCreatedAtAfter(AuditActionType actionType, LocalDateTime from);

    long countByActionType(AuditActionType actionType);

    @Query("SELECT a FROM AuditLog a JOIN FETCH a.user WHERE a.actionType = :type ORDER BY a.createdAt DESC")
    Page<AuditLog> findByActionTypeWithUser(@Param("type") AuditActionType type, Pageable pageable);

    @Query("SELECT a FROM AuditLog a JOIN FETCH a.user WHERE a.actionType = :type AND a.user.id = :userId ORDER BY a.createdAt DESC")
    Page<AuditLog> findByActionTypeAndUserIdWithUser(@Param("type") AuditActionType type,
                                                     @Param("userId") Long userId,
                                                     Pageable pageable);

    @Query("SELECT a.targetId, COUNT(a) FROM AuditLog a WHERE a.actionType = :type GROUP BY a.targetId ORDER BY COUNT(a) DESC")
    List<Object[]> findTopTargetsByActionType(@Param("type") AuditActionType type, Pageable pageable);
}
