package com.eactive.resourcehub.audit.entity;

import com.eactive.resourcehub.common.entity.BaseEntity;
import com.eactive.resourcehub.user.entity.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "audit_logs")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AuditLog extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = true)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private AuditActionType actionType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private AuditTargetType targetType;

    @Column(nullable = false)
    private Long targetId;

    @Column(length = 500)
    private String reason;

    @Column(length = 50)
    private String ipAddress;

    @Column(length = 500)
    private String userAgent;

    public static AuditLog record(User user, AuditActionType actionType,
                                   AuditTargetType targetType, Long targetId,
                                   String reason, String ipAddress, String userAgent) {
        AuditLog log = new AuditLog();
        log.user = user;
        log.actionType = actionType;
        log.targetType = targetType;
        log.targetId = targetId;
        log.reason = reason;
        log.ipAddress = ipAddress;
        log.userAgent = userAgent;
        return log;
    }
}
