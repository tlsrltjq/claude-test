package com.eactive.resourcehub.document.service;

import com.eactive.resourcehub.document.entity.Document;
import com.eactive.resourcehub.document.entity.DocumentType;
import com.eactive.resourcehub.document.entity.Folder;
import com.eactive.resourcehub.document.entity.FolderType;
import com.eactive.resourcehub.user.entity.Position;
import com.eactive.resourcehub.user.entity.User;
import java.time.LocalDate;
import com.eactive.resourcehub.document.repository.DocumentRepository;
import com.eactive.resourcehub.document.repository.FolderRepository;
import com.eactive.resourcehub.permission.entity.Permission;
import com.eactive.resourcehub.permission.entity.PermissionTargetType;
import com.eactive.resourcehub.permission.entity.PermissionType;
import com.eactive.resourcehub.permission.repository.PermissionRepository;
import com.eactive.resourcehub.user.entity.UserRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * SearchService 단위 테스트.
 * 역할별 라우팅, folderKind 필터, 날짜 sentinel 변환, 키워드·업로더 kw 변환을 검증한다.
 */
@ExtendWith(MockitoExtension.class)
class SearchServiceTest {

    @Mock DocumentRepository documentRepository;
    @Mock FolderRepository   folderRepository;
    @Mock PermissionRepository permissionRepository;

    @InjectMocks SearchService searchService;

    private Document personalDoc;
    private Document publicDoc;
    private Folder   publicFolder;

    @BeforeEach
    void setUp() {
        User dummyOwner = User.create("owner@test.com", "encoded", "홍길동",
                "owner@test.com", null, Position.STAFF,
                LocalDate.of(1990, 1, 1), "010-0000-0000");
        ReflectionTestUtils.setField(dummyOwner, "id", 99L);

        // 개인 폴더 문서
        Folder personalFolder = Folder.create(dummyOwner, "개인폴더");
        ReflectionTestUtils.setField(personalFolder, "id", 10L);
        personalDoc = Document.create(personalFolder, DocumentType.RESUME, "이력서");
        ReflectionTestUtils.setField(personalDoc, "id", 1L);

        // 공용 폴더 문서
        publicFolder = Folder.createPublic(dummyOwner, "공용폴더");
        ReflectionTestUtils.setField(publicFolder, "id", 20L);
        publicDoc = Document.create(publicFolder, DocumentType.RESUME, "공용이력서");
        ReflectionTestUtils.setField(publicDoc, "id", 2L);
    }

    // ── ADMIN 라우팅 ─────────────────────────────────────────────

    @Test
    void ADMIN은_searchAll_호출() {
        when(documentRepository.searchAll(any(), any(), any(), any(), any()))
                .thenReturn(List.of(personalDoc));

        List<Document> result = searchService.search(
                1L, UserRole.ADMIN, null, null, null, null, null, null);

        assertEquals(1, result.size());
        verify(documentRepository).searchAll(any(), any(), any(), any(), any());
        verify(documentRepository, never()).searchOwn(any(), any(), any(), any(), any(), any());
    }

    @Test
    void ADMIN_folderKind_public_이면_PERSONAL_폴더_결과_필터아웃() {
        // personalDoc은 PERSONAL 폴더 — folderKind="public" 이면 제외돼야 함
        when(documentRepository.searchAll(any(), any(), any(), any(), any()))
                .thenReturn(List.of(personalDoc));

        List<Document> result = searchService.search(
                1L, UserRole.ADMIN, null, null, null, null, null, "public");

        assertTrue(result.isEmpty(), "PERSONAL 폴더 문서는 folderKind=public 필터에서 제외");
    }

    @Test
    void ADMIN_folderKind_personal_이면_PUBLIC_폴더_결과_필터아웃() {
        when(documentRepository.searchAll(any(), any(), any(), any(), any()))
                .thenReturn(List.of(publicDoc));

        List<Document> result = searchService.search(
                1L, UserRole.ADMIN, null, null, null, null, null, "personal");

        assertTrue(result.isEmpty(), "PUBLIC 폴더 문서는 folderKind=personal 필터에서 제외");
    }

    // ── EMPLOYEE 라우팅 ──────────────────────────────────────────

    @Test
    void EMPLOYEE_folderKind_없으면_personal과_public_모두_조회() {
        when(documentRepository.searchOwn(any(), any(), any(), any(), any(), any()))
                .thenReturn(List.of(personalDoc));
        when(folderRepository.findFirstByType(FolderType.SHARED_PUBLIC))
                .thenReturn(Optional.of(publicFolder));
        when(documentRepository.searchInFolders(any(), any(), any(), any(), any(), any()))
                .thenReturn(List.of(publicDoc));
        when(permissionRepository.findByUserIdAndTargetType(any(), any()))
                .thenReturn(List.of());

        List<Document> result = searchService.search(
                99L, UserRole.EMPLOYEE, null, null, null, null, null, null);

        assertEquals(2, result.size());
        verify(documentRepository).searchOwn(any(), any(), any(), any(), any(), any());
        verify(documentRepository, atLeastOnce()).searchInFolders(any(), any(), any(), any(), any(), any());
    }

    @Test
    void EMPLOYEE_folderKind_personal이면_searchOwn만_호출() {
        when(documentRepository.searchOwn(any(), any(), any(), any(), any(), any()))
                .thenReturn(List.of(personalDoc));

        searchService.search(99L, UserRole.EMPLOYEE, null, null, null, null, null, "personal");

        verify(documentRepository).searchOwn(any(), any(), any(), any(), any(), any());
        verify(documentRepository, never()).searchInFolders(any(), any(), any(), any(), any(), any());
        verify(folderRepository, never()).findFirstByType(any());
    }

    @Test
    void EMPLOYEE_공유_폴더_있으면_searchInFolders_두_번_호출() {
        Permission sharedPerm = mock(Permission.class);
        when(sharedPerm.getPermissionType()).thenReturn(PermissionType.FOLDER_ACCESS);
        when(sharedPerm.getTargetId()).thenReturn(30L);
        when(documentRepository.searchOwn(any(), any(), any(), any(), any(), any()))
                .thenReturn(List.of());
        when(folderRepository.findFirstByType(FolderType.SHARED_PUBLIC))
                .thenReturn(Optional.of(publicFolder));
        when(documentRepository.searchInFolders(any(), any(), any(), any(), any(), any()))
                .thenReturn(List.of());
        when(permissionRepository.findByUserIdAndTargetType(any(), eq(PermissionTargetType.FOLDER)))
                .thenReturn(List.of(sharedPerm));

        searchService.search(99L, UserRole.EMPLOYEE, null, null, null, null, null, null);

        // public 폴더 + shared 폴더 = 2번
        verify(documentRepository, times(2)).searchInFolders(any(), any(), any(), any(), any(), any());
    }

    @Test
    void EMPLOYEE_공유_권한_없으면_searchInFolders_한_번만_public으로_호출() {
        when(documentRepository.searchOwn(any(), any(), any(), any(), any(), any()))
                .thenReturn(List.of());
        when(folderRepository.findFirstByType(FolderType.SHARED_PUBLIC))
                .thenReturn(Optional.of(publicFolder));
        when(documentRepository.searchInFolders(any(), any(), any(), any(), any(), any()))
                .thenReturn(List.of());
        when(permissionRepository.findByUserIdAndTargetType(any(), any()))
                .thenReturn(List.of());

        searchService.search(99L, UserRole.EMPLOYEE, null, null, null, null, null, null);

        verify(documentRepository, times(1)).searchInFolders(any(), any(), any(), any(), any(), any());
    }

    // ── 날짜 sentinel 변환 ───────────────────────────────────────

    @Test
    void dateFrom_null이면_1970_sentinel_전달() {
        ArgumentCaptor<LocalDateTime> fromCaptor = ArgumentCaptor.forClass(LocalDateTime.class);
        when(documentRepository.searchAll(any(), any(), any(), fromCaptor.capture(), any()))
                .thenReturn(List.of());

        searchService.search(1L, UserRole.ADMIN, null, null, null, null, null, null);

        LocalDateTime from = fromCaptor.getValue();
        assertEquals(LocalDateTime.of(1970, 1, 1, 0, 0, 0), from,
                "날짜 없을 때 from은 1970-01-01T00:00:00 sentinel");
    }

    @Test
    void dateTo_null이면_9999_sentinel_전달() {
        ArgumentCaptor<LocalDateTime> toCaptor = ArgumentCaptor.forClass(LocalDateTime.class);
        when(documentRepository.searchAll(any(), any(), any(), any(), toCaptor.capture()))
                .thenReturn(List.of());

        searchService.search(1L, UserRole.ADMIN, null, null, null, null, null, null);

        LocalDateTime to = toCaptor.getValue();
        assertEquals(LocalDateTime.of(9999, 12, 31, 23, 59, 59), to,
                "날짜 없을 때 to는 9999-12-31T23:59:59 sentinel");
    }

    @Test
    void dateFrom_지정하면_해당날짜_자정으로_변환() {
        ArgumentCaptor<LocalDateTime> fromCaptor = ArgumentCaptor.forClass(LocalDateTime.class);
        when(documentRepository.searchAll(any(), any(), any(), fromCaptor.capture(), any()))
                .thenReturn(List.of());

        LocalDate from = LocalDate.of(2024, 3, 15);
        searchService.search(1L, UserRole.ADMIN, null, null, null, from, null, null);

        assertEquals(from.atStartOfDay(), fromCaptor.getValue());
    }

    @Test
    void dateTo_지정하면_해당날짜_23시59분_59초로_변환() {
        ArgumentCaptor<LocalDateTime> toCaptor = ArgumentCaptor.forClass(LocalDateTime.class);
        when(documentRepository.searchAll(any(), any(), any(), any(), toCaptor.capture()))
                .thenReturn(List.of());

        LocalDate to = LocalDate.of(2024, 3, 15);
        searchService.search(1L, UserRole.ADMIN, null, null, null, null, to, null);

        assertEquals(to.atTime(LocalTime.MAX), toCaptor.getValue());
    }

    // ── 키워드·업로더 kw 변환 ─────────────────────────────────────

    @Test
    void 키워드_있으면_소문자_퍼센트_와일드카드로_변환() {
        ArgumentCaptor<String> kwCaptor = ArgumentCaptor.forClass(String.class);
        when(documentRepository.searchAll(kwCaptor.capture(), any(), any(), any(), any()))
                .thenReturn(List.of());

        searchService.search(1L, UserRole.ADMIN, "이력서", null, null, null, null, null);

        assertEquals("%이력서%", kwCaptor.getValue());
    }

    @Test
    void 키워드_null이면_null_전달() {
        ArgumentCaptor<String> kwCaptor = ArgumentCaptor.forClass(String.class);
        when(documentRepository.searchAll(kwCaptor.capture(), any(), any(), any(), any()))
                .thenReturn(List.of());

        searchService.search(1L, UserRole.ADMIN, null, null, null, null, null, null);

        assertNull(kwCaptor.getValue());
    }

    @Test
    void 키워드_공백만이면_null_전달() {
        ArgumentCaptor<String> kwCaptor = ArgumentCaptor.forClass(String.class);
        when(documentRepository.searchAll(kwCaptor.capture(), any(), any(), any(), any()))
                .thenReturn(List.of());

        searchService.search(1L, UserRole.ADMIN, "   ", null, null, null, null, null);

        assertNull(kwCaptor.getValue());
    }

    @Test
    void 업로더명_대문자_입력이면_소문자_와일드카드로_변환() {
        ArgumentCaptor<String> uploaderCaptor = ArgumentCaptor.forClass(String.class);
        when(documentRepository.searchAll(any(), any(), uploaderCaptor.capture(), any(), any()))
                .thenReturn(List.of());

        searchService.search(1L, UserRole.ADMIN, null, null, "HONG", null, null, null);

        assertEquals("%hong%", uploaderCaptor.getValue());
    }

    // ── 중복 제거 ────────────────────────────────────────────────

    @Test
    void EMPLOYEE_같은_문서가_여러_폴더에서_조회돼도_중복_제거() {
        when(documentRepository.searchOwn(any(), any(), any(), any(), any(), any()))
                .thenReturn(List.of(personalDoc));
        when(folderRepository.findFirstByType(FolderType.SHARED_PUBLIC))
                .thenReturn(Optional.of(publicFolder));
        // personalDoc(id=1)이 공용 폴더에서도 동일 ID로 반환
        when(documentRepository.searchInFolders(any(), any(), any(), any(), any(), any()))
                .thenReturn(List.of(personalDoc));
        when(permissionRepository.findByUserIdAndTargetType(any(), any()))
                .thenReturn(List.of());

        List<Document> result = searchService.search(
                99L, UserRole.EMPLOYEE, null, null, null, null, null, null);

        assertEquals(1, result.size(), "같은 id 문서는 한 번만 포함");
    }
}
