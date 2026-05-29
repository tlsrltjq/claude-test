package com.eactive.resourcehub.permission.service;

import com.eactive.resourcehub.common.service.AuditService;
import com.eactive.resourcehub.document.entity.Folder;
import com.eactive.resourcehub.document.entity.FolderType;
import com.eactive.resourcehub.document.repository.FolderRepository;
import com.eactive.resourcehub.permission.entity.Permission;
import com.eactive.resourcehub.permission.entity.PermissionTargetType;
import com.eactive.resourcehub.permission.entity.PermissionType;
import com.eactive.resourcehub.permission.repository.PermissionRepository;
import com.eactive.resourcehub.user.entity.Position;
import com.eactive.resourcehub.user.entity.User;
import com.eactive.resourcehub.user.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FolderPermissionServiceTest {

    @Mock PermissionRepository permissionRepository;
    @Mock UserRepository       userRepository;
    @Mock FolderRepository     folderRepository;
    @Mock AuditService         auditService;
    @Mock HttpServletRequest   httpRequest;

    @InjectMocks FolderPermissionService service;

    private User   targetUser;
    private User   actor;
    private Folder folder;

    @BeforeEach
    void setUp() {
        targetUser = User.create("target@test.com", "encoded", "대상",
                "target@test.com", null, Position.STAFF,
                LocalDate.of(1990, 1, 1), "010-0000-0000");
        ReflectionTestUtils.setField(targetUser, "id", 10L);

        actor = User.create("admin@test.com", "encoded", "관리자",
                "admin@test.com", null, Position.REPRESENTATIVE,
                LocalDate.of(1970, 1, 1), "010-0000-0000");
        ReflectionTestUtils.setField(actor, "id", 1L);

        folder = Folder.create(actor, "공유폴더");
        ReflectionTestUtils.setField(folder, "id", 20L);
    }

    // ── grant ────────────────────────────────────────────────────

    @Test
    void grant_이미_권한_있으면_예외() {
        when(permissionRepository.existsByUserIdAndPermissionTypeAndTargetTypeAndTargetId(
                10L, PermissionType.FOLDER_ACCESS, PermissionTargetType.FOLDER, 20L))
                .thenReturn(true);

        assertThrows(IllegalArgumentException.class,
                () -> service.grant(10L, 20L, 1L, httpRequest));
        verify(permissionRepository, never()).save(any());
    }

    @Test
    void grant_사용자_없으면_예외() {
        when(permissionRepository.existsByUserIdAndPermissionTypeAndTargetTypeAndTargetId(
                anyLong(), any(), any(), anyLong())).thenReturn(false);
        when(userRepository.findById(10L)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class,
                () -> service.grant(10L, 20L, 1L, httpRequest));
    }

    @Test
    void grant_폴더_없으면_예외() {
        when(permissionRepository.existsByUserIdAndPermissionTypeAndTargetTypeAndTargetId(
                anyLong(), any(), any(), anyLong())).thenReturn(false);
        when(userRepository.findById(10L)).thenReturn(Optional.of(targetUser));
        when(userRepository.findById(1L)).thenReturn(Optional.of(actor));
        when(folderRepository.findById(20L)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class,
                () -> service.grant(10L, 20L, 1L, httpRequest));
    }

    @Test
    void grant_성공이면_권한_저장_감사로그_기록() {
        when(permissionRepository.existsByUserIdAndPermissionTypeAndTargetTypeAndTargetId(
                anyLong(), any(), any(), anyLong())).thenReturn(false);
        when(userRepository.findById(10L)).thenReturn(Optional.of(targetUser));
        when(userRepository.findById(1L)).thenReturn(Optional.of(actor));
        when(folderRepository.findById(20L)).thenReturn(Optional.of(folder));
        Permission saved = Permission.grant(targetUser, PermissionType.FOLDER_ACCESS,
                PermissionTargetType.FOLDER, 20L, actor);
        ReflectionTestUtils.setField(saved, "id", 99L);
        when(permissionRepository.save(any())).thenReturn(saved);

        service.grant(10L, 20L, 1L, httpRequest);

        verify(permissionRepository).save(any(Permission.class));
        verify(auditService).log(eq(1L), any(), any(), anyLong(), anyString(), eq(httpRequest));
    }

    // ── revoke ───────────────────────────────────────────────────

    @Test
    void revoke_권한_없으면_예외() {
        when(permissionRepository.existsByUserIdAndPermissionTypeAndTargetTypeAndTargetId(
                anyLong(), any(), any(), anyLong())).thenReturn(false);

        assertThrows(IllegalArgumentException.class,
                () -> service.revoke(10L, 20L, 1L, httpRequest));
    }

    @Test
    void revoke_성공이면_삭제_감사로그_기록() {
        when(permissionRepository.existsByUserIdAndPermissionTypeAndTargetTypeAndTargetId(
                anyLong(), any(), any(), anyLong())).thenReturn(true);

        service.revoke(10L, 20L, 1L, httpRequest);

        verify(permissionRepository).deleteByUserIdAndPermissionTypeAndTargetTypeAndTargetId(
                10L, PermissionType.FOLDER_ACCESS, PermissionTargetType.FOLDER, 20L);
        verify(auditService).log(eq(1L), any(), any(), anyLong(), anyString(), eq(httpRequest));
    }

    // ── findGrantableFolders ──────────────────────────────────────

    @Test
    void findGrantableFolders_본인_폴더_제외() {
        Folder ownFolder = Folder.create(targetUser, "내폴더");
        ReflectionTestUtils.setField(ownFolder, "id", 30L);

        when(permissionRepository.findByUserIdAndTargetType(10L, PermissionTargetType.FOLDER))
                .thenReturn(List.of());
        when(folderRepository.findAllWithOwner()).thenReturn(List.of(ownFolder, folder));

        List<Folder> result = service.findGrantableFolders(10L);

        assertTrue(result.stream().noneMatch(f -> f.getOwner().getId().equals(10L)));
    }

    @Test
    void findGrantableFolders_이미_권한_있는_폴더_제외() {
        Permission existing = Permission.grant(targetUser, PermissionType.FOLDER_ACCESS,
                PermissionTargetType.FOLDER, 20L, actor);

        when(permissionRepository.findByUserIdAndTargetType(10L, PermissionTargetType.FOLDER))
                .thenReturn(List.of(existing));
        when(folderRepository.findAllWithOwner()).thenReturn(List.of(folder));

        List<Folder> result = service.findGrantableFolders(10L);

        assertTrue(result.isEmpty());
    }
}
