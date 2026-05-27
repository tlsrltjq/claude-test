package com.eactive.resourcehub.document.service;

import com.eactive.resourcehub.common.security.CustomUserDetails;
import com.eactive.resourcehub.document.entity.*;
import com.eactive.resourcehub.document.repository.DocumentVersionRepository;
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
class DocumentAccessServiceTest {

    @Mock DocumentVersionRepository documentVersionRepository;
    @Mock PermissionRepository permissionRepository;

    @InjectMocks DocumentAccessService service;

    private User ownerUser;
    private User otherUser;
    private User adminUser;
    private User salesUser;
    private Folder personalFolder;
    private Folder publicFolder;

    @BeforeEach
    void setUp() {
        ownerUser = makeUser(1L, UserRole.EMPLOYEE);
        otherUser = makeUser(2L, UserRole.EMPLOYEE);
        adminUser = makeUser(3L, UserRole.ADMIN);
        salesUser = makeUser(4L, UserRole.SALES);

        personalFolder = Folder.create(ownerUser, "개인 폴더");
        publicFolder   = Folder.createPublic(ownerUser, "공용 폴더");
    }

    // ── checkReadAccess ─────────────────────────────────────────

    @Test
    void ADMIN은_개인폴더_접근_허용() {
        assertDoesNotThrow(() ->
                service.checkReadAccess(personalFolder, details(adminUser)));
    }

    @Test
    void SALES는_개인폴더_접근_허용() {
        assertDoesNotThrow(() ->
                service.checkReadAccess(personalFolder, details(salesUser)));
    }

    @Test
    void 본인_폴더는_접근_허용() {
        assertDoesNotThrow(() ->
                service.checkReadAccess(personalFolder, details(ownerUser)));
    }

    @Test
    void 공용_폴더는_EMPLOYEE도_접근_허용() {
        assertDoesNotThrow(() ->
                service.checkReadAccess(publicFolder, details(otherUser)));
    }

    @Test
    void 권한_없는_EMPLOYEE는_타인_개인폴더_접근_거부() {
        when(permissionRepository.existsByUserIdAndPermissionTypeAndTargetTypeAndTargetId(
                eq(2L), eq(PermissionType.FOLDER_ACCESS), eq(PermissionTargetType.FOLDER), any()))
                .thenReturn(false);

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () ->
                service.checkReadAccess(personalFolder, details(otherUser)));
        assertEquals(HttpStatus.FORBIDDEN, ex.getStatusCode());
    }

    @Test
    void 권한_부여된_EMPLOYEE는_타인_개인폴더_접근_허용() {
        when(permissionRepository.existsByUserIdAndPermissionTypeAndTargetTypeAndTargetId(
                eq(2L), eq(PermissionType.FOLDER_ACCESS), eq(PermissionTargetType.FOLDER), any()))
                .thenReturn(true);

        assertDoesNotThrow(() ->
                service.checkReadAccess(personalFolder, details(otherUser)));
    }

    // ── checkReviewStatusAccess ─────────────────────────────────

    @Test
    void ADMIN은_PENDING_REVIEW_버전_접근_허용() {
        DocumentVersion version = pendingVersion(ownerUser);
        assertDoesNotThrow(() ->
                service.checkReviewStatusAccess(version, details(adminUser)));
    }

    @Test
    void SALES는_PENDING_REVIEW_버전_접근_허용() {
        DocumentVersion version = pendingVersion(ownerUser);
        assertDoesNotThrow(() ->
                service.checkReviewStatusAccess(version, details(salesUser)));
    }

    @Test
    void 폴더_소유자는_PENDING_REVIEW_버전_접근_허용() {
        DocumentVersion version = pendingVersion(ownerUser);
        assertDoesNotThrow(() ->
                service.checkReviewStatusAccess(version, details(ownerUser)));
    }

    @Test
    void 업로드한_본인은_PENDING_REVIEW_버전_접근_허용() {
        // 다른 사용자의 폴더에 올린 버전이지만 업로드는 본인
        DocumentVersion version = pendingVersion(otherUser);
        setUploadedBy(version, ownerUser);
        assertDoesNotThrow(() ->
                service.checkReviewStatusAccess(version, details(ownerUser)));
    }

    @Test
    void 권한없는_EMPLOYEE는_PENDING_REVIEW_버전_접근_거부() {
        DocumentVersion version = pendingVersion(ownerUser);
        setUploadedBy(version, ownerUser);

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () ->
                service.checkReviewStatusAccess(version, details(otherUser)));
        assertEquals(HttpStatus.FORBIDDEN, ex.getStatusCode());
    }

    @Test
    void APPROVED_버전은_권한_없는_EMPLOYEE도_접근_허용() {
        DocumentVersion version = approvedVersion(ownerUser);
        assertDoesNotThrow(() ->
                service.checkReviewStatusAccess(version, details(otherUser)));
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

    private DocumentVersion pendingVersion(User folderOwner) {
        Folder folder = Folder.create(folderOwner, "폴더");
        Document doc = Document.create(folder, DocumentType.RESUME, "이력서");
        DocumentVersion version = DocumentVersion.create(
                doc, 1, "이력서.pdf", "uuid.pdf", "/storage/uuid.pdf",
                1024L, "application/pdf", "checksum", folderOwner
        );
        return version;
    }

    private DocumentVersion approvedVersion(User folderOwner) {
        DocumentVersion version = pendingVersion(folderOwner);
        version.autoApprove();
        return version;
    }

    private void setUploadedBy(DocumentVersion version, User user) {
        ReflectionTestUtils.setField(version, "uploadedBy", user);
    }
}
