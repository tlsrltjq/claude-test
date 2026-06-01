package com.eactive.resourcehub.user.service;

import com.eactive.resourcehub.document.entity.Document;
import com.eactive.resourcehub.document.entity.DocumentStatus;
import com.eactive.resourcehub.document.entity.DocumentType;
import com.eactive.resourcehub.document.entity.Folder;
import com.eactive.resourcehub.document.entity.FolderType;
import com.eactive.resourcehub.document.repository.DocumentRepository;
import com.eactive.resourcehub.document.repository.FolderRepository;
import com.eactive.resourcehub.user.entity.Position;
import com.eactive.resourcehub.user.entity.User;
import com.eactive.resourcehub.user.entity.UserRole;
import com.eactive.resourcehub.user.entity.UserStatus;
import com.eactive.resourcehub.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SalesMemberServiceTest {

    @Mock UserRepository     userRepository;
    @Mock FolderRepository   folderRepository;
    @Mock DocumentRepository documentRepository;

    @InjectMocks SalesMemberService service;

    private User user;
    private Folder folder;

    @BeforeEach
    void setUp() {
        user = User.create("test@e.com", "pw", "홍길동", "test@e.com",
                null, Position.STAFF, LocalDate.of(1990, 1, 1), "010-1111-2222");
        user.verifyEmail();
        ReflectionTestUtils.setField(user, "id", 1L);

        folder = Folder.create(user, "홍길동 개인 폴더");
        ReflectionTestUtils.setField(folder, "id", 10L);
    }

    // ── helpers ────────────────────────────────────────────────────

    private Document gradDoc(String degreeType, LocalDate issuedDate, LocalDate expiresAt) {
        Document d = Document.create(folder, DocumentType.GRADUATION_CERTIFICATE, "졸업증명서");
        d.updateDegreeType(degreeType);
        d.updateIssuedDate(issuedDate);
        if (expiresAt != null) d.updateExpiresAt(expiresAt);
        return d;
    }

    private Document licenseDoc(String certTypeMeta, LocalDate issuedDate, LocalDate expiresAt) {
        Document d = Document.create(folder, DocumentType.LICENSE, "정보처리기사");
        d.updateCertTypeMeta(certTypeMeta);
        d.updateIssuedDate(issuedDate);
        if (expiresAt != null) d.updateExpiresAt(expiresAt);
        return d;
    }

    // ── findActiveMembers ──────────────────────────────────────────

    @Test
    void findActiveMembers_정렬없으면_position_기본정렬() {
        when(userRepository.findActiveMembersFiltered(
                eq(UserStatus.ACTIVE), eq(UserRole.ADMIN), isNull(), isNull()))
                .thenReturn(List.of(user));

        List<User> result = service.findActiveMembers(null, null, null, null);

        assertThat(result).containsExactly(user);
    }

    @Test
    void findActiveMembers_이름_오름차순_정렬() {
        User userA = User.create("a@e.com", "pw", "가나다", "a@e.com",
                null, Position.STAFF, LocalDate.of(1990, 1, 1), "");
        User userB = User.create("b@e.com", "pw", "마바사", "b@e.com",
                null, Position.STAFF, LocalDate.of(1990, 1, 1), "");
        when(userRepository.findActiveMembersFiltered(any(), any(), any(), any()))
                .thenReturn(List.of(userB, userA));

        List<User> result = service.findActiveMembers(null, null, "name", "asc");

        assertThat(result).extracting(User::getName).containsExactly("가나다", "마바사");
    }

    @Test
    void findActiveMembers_이름_내림차순_정렬() {
        User userA = User.create("a@e.com", "pw", "가나다", "a@e.com",
                null, Position.STAFF, LocalDate.of(1990, 1, 1), "");
        User userB = User.create("b@e.com", "pw", "마바사", "b@e.com",
                null, Position.STAFF, LocalDate.of(1990, 1, 1), "");
        when(userRepository.findActiveMembersFiltered(any(), any(), any(), any()))
                .thenReturn(List.of(userA, userB));

        List<User> result = service.findActiveMembers(null, null, "name", "desc");

        assertThat(result).extracting(User::getName).containsExactly("마바사", "가나다");
    }

    @Test
    void findActiveMembers_키워드_있으면_like_패턴_전달() {
        when(userRepository.findActiveMembersFiltered(
                eq(UserStatus.ACTIVE), eq(UserRole.ADMIN), eq("%홍길동%"), isNull()))
                .thenReturn(List.of(user));

        List<User> result = service.findActiveMembers("홍길동", null, null, null);

        assertThat(result).containsExactly(user);
    }

    // ── findMemberById ─────────────────────────────────────────────

    @Test
    void findMemberById_존재하면_반환() {
        when(userRepository.findByIdWithTeam(1L)).thenReturn(Optional.of(user));

        assertThat(service.findMemberById(1L)).isEqualTo(user);
    }

    @Test
    void findMemberById_없으면_404_예외() {
        when(userRepository.findByIdWithTeam(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.findMemberById(99L))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("404");
    }

    // ── findMemberDocuments ────────────────────────────────────────

    @Test
    void findMemberDocuments_폴더_있으면_문서_반환() {
        Document doc = Document.create(folder, DocumentType.RESUME, "이력서");
        when(folderRepository.findByOwnerIdAndType(1L, FolderType.PERSONAL))
                .thenReturn(Optional.of(folder));
        when(documentRepository.findByFolderIdAndStatusWithVersion(10L, DocumentStatus.ACTIVE))
                .thenReturn(List.of(doc));

        assertThat(service.findMemberDocuments(1L)).containsExactly(doc);
    }

    @Test
    void findMemberDocuments_폴더_없으면_빈_리스트() {
        when(folderRepository.findByOwnerIdAndType(1L, FolderType.PERSONAL))
                .thenReturn(Optional.empty());

        assertThat(service.findMemberDocuments(1L)).isEmpty();
    }

    // ── getMemberAutofillData — 만료 필터링 핵심 ────────────────────

    @Test
    void autofill_폴더_없으면_빈_Map() {
        when(folderRepository.findByOwnerIdAndType(1L, FolderType.PERSONAL))
                .thenReturn(Optional.empty());

        assertThat(service.getMemberAutofillData(1L)).isEmpty();
    }

    @Test
    void autofill_유효한_졸업증명서_degree_gradDate_반환() {
        Document doc = gradDoc("BACHELOR", LocalDate.of(2020, 2, 14), null);
        when(folderRepository.findByOwnerIdAndType(1L, FolderType.PERSONAL))
                .thenReturn(Optional.of(folder));
        when(documentRepository.findByFolderIdAndDocumentType(10L, DocumentType.GRADUATION_CERTIFICATE))
                .thenReturn(List.of(doc));
        when(documentRepository.findByFolderIdAndDocumentType(10L, DocumentType.LICENSE))
                .thenReturn(List.of());

        Map<String, String> result = service.getMemberAutofillData(1L);

        assertThat(result).containsEntry("degree", "BACHELOR")
                          .containsEntry("gradDate", "2020-02-14");
    }

    @Test
    void autofill_만료된_졸업증명서는_제외() {
        Document expired = gradDoc("BACHELOR", LocalDate.of(2020, 2, 14),
                LocalDate.now().minusDays(1));
        when(folderRepository.findByOwnerIdAndType(1L, FolderType.PERSONAL))
                .thenReturn(Optional.of(folder));
        when(documentRepository.findByFolderIdAndDocumentType(10L, DocumentType.GRADUATION_CERTIFICATE))
                .thenReturn(List.of(expired));
        when(documentRepository.findByFolderIdAndDocumentType(10L, DocumentType.LICENSE))
                .thenReturn(List.of());

        Map<String, String> result = service.getMemberAutofillData(1L);

        assertThat(result).doesNotContainKey("degree");
    }

    @Test
    void autofill_만료_미래_졸업증명서는_포함() {
        Document valid = gradDoc("MASTER", LocalDate.of(2022, 8, 20),
                LocalDate.now().plusDays(30));
        when(folderRepository.findByOwnerIdAndType(1L, FolderType.PERSONAL))
                .thenReturn(Optional.of(folder));
        when(documentRepository.findByFolderIdAndDocumentType(10L, DocumentType.GRADUATION_CERTIFICATE))
                .thenReturn(List.of(valid));
        when(documentRepository.findByFolderIdAndDocumentType(10L, DocumentType.LICENSE))
                .thenReturn(List.of());

        Map<String, String> result = service.getMemberAutofillData(1L);

        assertThat(result).containsEntry("degree", "MASTER");
    }

    @Test
    void autofill_여러_졸업증명서_중_최고_학위_선택() {
        Document bachelor = gradDoc("BACHELOR", LocalDate.of(2018, 2, 14), null);
        Document master   = gradDoc("MASTER",   LocalDate.of(2020, 8, 31), null);
        when(folderRepository.findByOwnerIdAndType(1L, FolderType.PERSONAL))
                .thenReturn(Optional.of(folder));
        when(documentRepository.findByFolderIdAndDocumentType(10L, DocumentType.GRADUATION_CERTIFICATE))
                .thenReturn(List.of(bachelor, master));
        when(documentRepository.findByFolderIdAndDocumentType(10L, DocumentType.LICENSE))
                .thenReturn(List.of());

        Map<String, String> result = service.getMemberAutofillData(1L);

        assertThat(result).containsEntry("degree", "MASTER")
                          .containsEntry("gradDate", "2020-08-31");
    }

    @Test
    void autofill_만료된_것_제외_후_남은_최고_학위_선택() {
        Document expiredMaster = gradDoc("MASTER", LocalDate.of(2020, 8, 31),
                LocalDate.now().minusDays(1));
        Document validBachelor = gradDoc("BACHELOR", LocalDate.of(2018, 2, 14), null);
        when(folderRepository.findByOwnerIdAndType(1L, FolderType.PERSONAL))
                .thenReturn(Optional.of(folder));
        when(documentRepository.findByFolderIdAndDocumentType(10L, DocumentType.GRADUATION_CERTIFICATE))
                .thenReturn(List.of(expiredMaster, validBachelor));
        when(documentRepository.findByFolderIdAndDocumentType(10L, DocumentType.LICENSE))
                .thenReturn(List.of());

        Map<String, String> result = service.getMemberAutofillData(1L);

        // 만료된 MASTER 제외 → 유효한 BACHELOR 선택
        assertThat(result).containsEntry("degree", "BACHELOR");
    }

    @Test
    void autofill_졸업증명서_degreeType_없으면_제외() {
        Document noType = gradDoc(null, LocalDate.of(2020, 2, 14), null);
        when(folderRepository.findByOwnerIdAndType(1L, FolderType.PERSONAL))
                .thenReturn(Optional.of(folder));
        when(documentRepository.findByFolderIdAndDocumentType(10L, DocumentType.GRADUATION_CERTIFICATE))
                .thenReturn(List.of(noType));
        when(documentRepository.findByFolderIdAndDocumentType(10L, DocumentType.LICENSE))
                .thenReturn(List.of());

        assertThat(service.getMemberAutofillData(1L)).doesNotContainKey("degree");
    }

    @Test
    void autofill_졸업증명서_issuedDate_없으면_제외() {
        Document noDate = gradDoc("BACHELOR", null, null);
        when(folderRepository.findByOwnerIdAndType(1L, FolderType.PERSONAL))
                .thenReturn(Optional.of(folder));
        when(documentRepository.findByFolderIdAndDocumentType(10L, DocumentType.GRADUATION_CERTIFICATE))
                .thenReturn(List.of(noDate));
        when(documentRepository.findByFolderIdAndDocumentType(10L, DocumentType.LICENSE))
                .thenReturn(List.of());

        assertThat(service.getMemberAutofillData(1L)).doesNotContainKey("degree");
    }

    @Test
    void autofill_유효한_자격증_certType_certDate_반환() {
        Document lic = licenseDoc("ENGINEER", LocalDate.of(2021, 6, 1), null);
        when(folderRepository.findByOwnerIdAndType(1L, FolderType.PERSONAL))
                .thenReturn(Optional.of(folder));
        when(documentRepository.findByFolderIdAndDocumentType(10L, DocumentType.GRADUATION_CERTIFICATE))
                .thenReturn(List.of());
        when(documentRepository.findByFolderIdAndDocumentType(10L, DocumentType.LICENSE))
                .thenReturn(List.of(lic));

        Map<String, String> result = service.getMemberAutofillData(1L);

        assertThat(result).containsEntry("certType", "ENGINEER")
                          .containsEntry("certDate", "2021-06-01");
    }

    @Test
    void autofill_만료된_자격증은_제외() {
        Document expired = licenseDoc("ENGINEER", LocalDate.of(2021, 6, 1),
                LocalDate.now().minusDays(1));
        when(folderRepository.findByOwnerIdAndType(1L, FolderType.PERSONAL))
                .thenReturn(Optional.of(folder));
        when(documentRepository.findByFolderIdAndDocumentType(10L, DocumentType.GRADUATION_CERTIFICATE))
                .thenReturn(List.of());
        when(documentRepository.findByFolderIdAndDocumentType(10L, DocumentType.LICENSE))
                .thenReturn(List.of(expired));

        assertThat(service.getMemberAutofillData(1L)).doesNotContainKey("certType");
    }

    @Test
    void autofill_졸업증명서와_자격증_동시_반환() {
        Document grad = gradDoc("BACHELOR", LocalDate.of(2018, 2, 14), null);
        Document lic  = licenseDoc("ENGINEER", LocalDate.of(2021, 6, 1), null);
        when(folderRepository.findByOwnerIdAndType(1L, FolderType.PERSONAL))
                .thenReturn(Optional.of(folder));
        when(documentRepository.findByFolderIdAndDocumentType(10L, DocumentType.GRADUATION_CERTIFICATE))
                .thenReturn(List.of(grad));
        when(documentRepository.findByFolderIdAndDocumentType(10L, DocumentType.LICENSE))
                .thenReturn(List.of(lic));

        Map<String, String> result = service.getMemberAutofillData(1L);

        assertThat(result).containsKeys("degree", "gradDate", "certType", "certDate");
    }

    // ── findActiveMembersForCalculator ─────────────────────────────

    @Test
    void findActiveMembersForCalculator_팀없는_사용자_이름순_정렬() {
        User a = User.create("a@e.com", "pw", "나길동", "a@e.com",
                null, Position.STAFF, LocalDate.of(1990, 1, 1), "");
        User b = User.create("b@e.com", "pw", "가길동", "b@e.com",
                null, Position.STAFF, LocalDate.of(1990, 1, 1), "");
        when(userRepository.findActiveMembersFiltered(
                eq(UserStatus.ACTIVE), eq(UserRole.ADMIN), isNull(), isNull()))
                .thenReturn(List.of(a, b));

        List<User> result = service.findActiveMembersForCalculator();

        assertThat(result).extracting(User::getName).containsExactly("가길동", "나길동");
    }
}
