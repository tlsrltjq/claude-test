package com.eactive.resourcehub.document.service;

import com.eactive.resourcehub.common.security.CustomUserDetails;
import com.eactive.resourcehub.document.entity.Folder;
import com.eactive.resourcehub.document.repository.FolderRepository;
import com.eactive.resourcehub.permission.entity.PermissionTargetType;
import com.eactive.resourcehub.permission.entity.PermissionType;
import com.eactive.resourcehub.permission.repository.PermissionRepository;
import com.eactive.resourcehub.user.entity.Position;
import com.eactive.resourcehub.user.entity.User;
import com.eactive.resourcehub.user.entity.UserRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FolderAccessServiceTest {

    @Mock FolderRepository folderRepository;
    @Mock PermissionRepository permissionRepository;

    @InjectMocks FolderAccessService service;

    private User ownerUser;
    private User otherEmployee;
    private User adminUser;
    private User salesUser;
    private Folder personalFolder;
    private Folder publicFolder;

    @BeforeEach
    void setUp() {
        ownerUser     = makeUser(1L, UserRole.EMPLOYEE);
        otherEmployee = makeUser(2L, UserRole.EMPLOYEE);
        adminUser     = makeUser(3L, UserRole.ADMIN);
        salesUser     = makeUser(4L, UserRole.SALES);

        personalFolder = Folder.create(ownerUser, "개인 폴더");
        publicFolder   = Folder.createPublic(ownerUser, "공용 폴더");
        ReflectionTestUtils.setField(personalFolder, "id", 10L);
        ReflectionTestUtils.setField(publicFolder,   "id", 20L);
    }

    // ── checkReadAccess ─────────────────────────────────────────

    @Test
    void ADMIN_read_접근_허용() {
        assertDoesNotThrow(() -> service.checkReadAccess(personalFolder, details(adminUser)));
    }

    @Test
    void SALES_read_접근_허용() {
        assertDoesNotThrow(() -> service.checkReadAccess(personalFolder, details(salesUser)));
    }

    @Test
    void 본인_폴더_read_허용() {
        assertDoesNotThrow(() -> service.checkReadAccess(personalFolder, details(ownerUser)));
    }

    @Test
    void 공용폴더_EMPLOYEE_read_허용() {
        assertDoesNotThrow(() -> service.checkReadAccess(publicFolder, details(otherEmployee)));
    }

    @Test
    void 권한_없는_EMPLOYEE_타인_개인폴더_read_거부() {
        when(permissionRepository.existsByUserIdAndPermissionTypeAndTargetTypeAndTargetId(
                eq(2L), eq(PermissionType.FOLDER_ACCESS), eq(PermissionTargetType.FOLDER), eq(10L)))
                .thenReturn(false);

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> service.checkReadAccess(personalFolder, details(otherEmployee)));
        assertEquals(HttpStatus.FORBIDDEN, ex.getStatusCode());
    }

    @Test
    void FOLDER_ACCESS_권한_부여된_EMPLOYEE_read_허용() {
        when(permissionRepository.existsByUserIdAndPermissionTypeAndTargetTypeAndTargetId(
                eq(2L), eq(PermissionType.FOLDER_ACCESS), eq(PermissionTargetType.FOLDER), eq(10L)))
                .thenReturn(true);

        assertDoesNotThrow(() -> service.checkReadAccess(personalFolder, details(otherEmployee)));
    }

    // ── checkWriteAccess ────────────────────────────────────────

    @Test
    void ADMIN_write_접근_허용() {
        assertDoesNotThrow(() -> service.checkWriteAccess(personalFolder, details(adminUser)));
    }

    @Test
    void 본인_폴더_write_허용() {
        assertDoesNotThrow(() -> service.checkWriteAccess(personalFolder, details(ownerUser)));
    }

    @Test
    void SALES_타인_폴더_write_거부() {
        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> service.checkWriteAccess(personalFolder, details(salesUser)));
        assertEquals(HttpStatus.FORBIDDEN, ex.getStatusCode());
    }

    @Test
    void 권한_있어도_EMPLOYEE_타인_폴더_write_거부() {
        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> service.checkWriteAccess(personalFolder, details(otherEmployee)));
        assertEquals(HttpStatus.FORBIDDEN, ex.getStatusCode());
    }

    // ── hasReadAccess (boolean) ─────────────────────────────────

    @Test
    void hasReadAccess_폴더없으면_false() {
        when(folderRepository.findByIdWithOwner(99L)).thenReturn(java.util.Optional.empty());
        assertFalse(service.hasReadAccess(99L, details(adminUser)));
    }

    @Test
    void hasReadAccess_권한있으면_true() {
        when(folderRepository.findByIdWithOwner(10L))
                .thenReturn(java.util.Optional.of(personalFolder));
        assertTrue(service.hasReadAccess(10L, details(ownerUser)));
    }

    @Test
    void hasReadAccess_권한없으면_false() {
        when(folderRepository.findByIdWithOwner(10L))
                .thenReturn(java.util.Optional.of(personalFolder));
        when(permissionRepository.existsByUserIdAndPermissionTypeAndTargetTypeAndTargetId(
                eq(2L), any(), any(), any())).thenReturn(false);
        assertFalse(service.hasReadAccess(10L, details(otherEmployee)));
    }

    // ── 헬퍼 ────────────────────────────────────────────────────

    private User makeUser(long id, UserRole role) {
        User user = User.create(
                "user" + id, "encoded", "사용자" + id,
                "user" + id + "@eactive.co.kr", null, Position.STAFF,
                LocalDate.of(1990, 1, 1), "010-0000-0000"
        );
        ReflectionTestUtils.setField(user, "id", id);
        if (role != UserRole.EMPLOYEE) {
            ReflectionTestUtils.setField(user, "role", role);
        }
        return user;
    }

    private CustomUserDetails details(User user) {
        return new CustomUserDetails(user);
    }
}
