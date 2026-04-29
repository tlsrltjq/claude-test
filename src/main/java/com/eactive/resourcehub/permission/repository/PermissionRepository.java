package com.eactive.resourcehub.permission.repository;

import com.eactive.resourcehub.permission.entity.Permission;
import com.eactive.resourcehub.permission.entity.PermissionTargetType;
import com.eactive.resourcehub.permission.entity.PermissionType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface PermissionRepository extends JpaRepository<Permission, Long> {

    List<Permission> findByUserId(Long userId);

    List<Permission> findByUserIdAndTargetType(Long userId, PermissionTargetType targetType);

    @Query("SELECT p FROM Permission p JOIN FETCH p.grantedBy WHERE p.user.id = :userId AND p.targetType = :targetType")
    List<Permission> findByUserIdAndTargetTypeWithGrantedBy(
            @Param("userId") Long userId, @Param("targetType") PermissionTargetType targetType);

    Optional<Permission> findByUserIdAndPermissionTypeAndTargetTypeAndTargetId(
            Long userId, PermissionType permissionType,
            PermissionTargetType targetType, Long targetId);

    boolean existsByUserIdAndPermissionTypeAndTargetTypeAndTargetId(
            Long userId, PermissionType permissionType,
            PermissionTargetType targetType, Long targetId);

    void deleteByUserIdAndPermissionTypeAndTargetTypeAndTargetId(
            Long userId, PermissionType permissionType,
            PermissionTargetType targetType, Long targetId);

    @Query("SELECT p FROM Permission p JOIN FETCH p.user u LEFT JOIN FETCH u.team "
         + "WHERE p.targetId = :folderId AND p.permissionType = :type AND p.targetType = :targetType")
    List<Permission> findByFolderIdWithUser(
            @Param("folderId") Long folderId,
            @Param("type") PermissionType type,
            @Param("targetType") PermissionTargetType targetType);
}
