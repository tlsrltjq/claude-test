package com.eactive.resourcehub.permission.repository;

import com.eactive.resourcehub.permission.entity.Permission;
import com.eactive.resourcehub.permission.entity.PermissionTargetType;
import com.eactive.resourcehub.permission.entity.PermissionType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PermissionRepository extends JpaRepository<Permission, Long> {

    List<Permission> findByUserId(Long userId);

    List<Permission> findByUserIdAndTargetType(Long userId, PermissionTargetType targetType);

    Optional<Permission> findByUserIdAndPermissionTypeAndTargetTypeAndTargetId(
            Long userId, PermissionType permissionType,
            PermissionTargetType targetType, Long targetId);

    boolean existsByUserIdAndPermissionTypeAndTargetTypeAndTargetId(
            Long userId, PermissionType permissionType,
            PermissionTargetType targetType, Long targetId);

    void deleteByUserIdAndPermissionTypeAndTargetTypeAndTargetId(
            Long userId, PermissionType permissionType,
            PermissionTargetType targetType, Long targetId);
}
