package com.eactive.resourcehub.permission.entity;

import com.eactive.resourcehub.common.entity.BaseEntity;
import com.eactive.resourcehub.user.entity.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "permissions")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Permission extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private PermissionType permissionType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private PermissionTargetType targetType;

    @Column(nullable = false)
    private Long targetId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "granted_by", nullable = false)
    private User grantedBy;

    public static Permission grant(User user, PermissionType permissionType,
                                    PermissionTargetType targetType, Long targetId,
                                    User grantedBy) {
        Permission permission = new Permission();
        permission.user = user;
        permission.permissionType = permissionType;
        permission.targetType = targetType;
        permission.targetId = targetId;
        permission.grantedBy = grantedBy;
        return permission;
    }
}
