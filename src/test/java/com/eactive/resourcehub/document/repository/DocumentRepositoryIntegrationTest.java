package com.eactive.resourcehub.document.repository;

import com.eactive.resourcehub.common.config.JpaAuditingConfig;
import com.eactive.resourcehub.document.entity.Document;
import com.eactive.resourcehub.document.entity.DocumentType;
import com.eactive.resourcehub.document.entity.DocumentVersion;
import com.eactive.resourcehub.document.entity.Folder;
import com.eactive.resourcehub.user.entity.Position;
import com.eactive.resourcehub.user.entity.User;
import com.eactive.resourcehub.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * DocumentRepository PostgreSQL 통합 테스트.
 * null LocalDateTime 파라미터 sentinel 처리, JPQL 검색 쿼리의 실제 DB 동작을 검증한다.
 * (이 테스트가 통과해야 "could not determine data type of parameter" 류 회귀를 잡을 수 있다.)
 */
@Testcontainers
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
@Import(JpaAuditingConfig.class)
class DocumentRepositoryIntegrationTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:18");

    @Autowired TestEntityManager em;
    @Autowired DocumentRepository documentRepository;
    @Autowired FolderRepository   folderRepository;
    @Autowired UserRepository     userRepository;
    @Autowired DocumentVersionRepository versionRepository;

    private User owner;
    private Folder personalFolder;
    private Document resumeDoc;

    @BeforeEach
    void setUp() {
        owner = userRepository.save(User.create(
                "search@test.com", "encoded", "홍길동",
                "search@test.com", null, Position.STAFF,
                LocalDate.of(1990, 1, 1), "010-1234-5678"));

        personalFolder = folderRepository.save(Folder.create(owner, "개인폴더"));

        resumeDoc = documentRepository.save(
                Document.create(personalFolder, DocumentType.RESUME, "홍길동 이력서"));
        em.flush();
        em.clear();
    }

    // ── searchOwn — 날짜 sentinel 패턴 ───────────────────────────

    @Test
    void searchOwn_날짜_필터_없이_sentinel_값으로_전체_조회() {
        List<Document> result = documentRepository.searchOwn(
                owner.getId(),
                null,
                null,
                null,
                LocalDateTime.of(1970, 1, 1, 0, 0, 0),
                LocalDateTime.of(9999, 12, 31, 23, 59, 59));

        assertThat(result).isNotEmpty();
        assertThat(result).anyMatch(d -> d.getTitle().contains("이력서"));
    }

    @Test
    void searchOwn_키워드_매칭() {
        List<Document> matched    = documentRepository.searchOwn(
                owner.getId(), "%이력서%", null, null,
                LocalDateTime.of(1970, 1, 1, 0, 0, 0),
                LocalDateTime.of(9999, 12, 31, 23, 59, 59));
        List<Document> notMatched = documentRepository.searchOwn(
                owner.getId(), "%자격증%", null, null,
                LocalDateTime.of(1970, 1, 1, 0, 0, 0),
                LocalDateTime.of(9999, 12, 31, 23, 59, 59));

        assertThat(matched).hasSize(1);
        assertThat(notMatched).isEmpty();
    }

    @Test
    void searchOwn_타입_필터() {
        List<Document> withFilter    = documentRepository.searchOwn(
                owner.getId(), null, DocumentType.RESUME, null,
                LocalDateTime.of(1970, 1, 1, 0, 0, 0),
                LocalDateTime.of(9999, 12, 31, 23, 59, 59));
        List<Document> wrongType = documentRepository.searchOwn(
                owner.getId(), null, DocumentType.LICENSE, null,
                LocalDateTime.of(1970, 1, 1, 0, 0, 0),
                LocalDateTime.of(9999, 12, 31, 23, 59, 59));

        assertThat(withFilter).hasSize(1);
        assertThat(wrongType).isEmpty();
    }

    @Test
    void searchOwn_날짜_범위_필터_미래로_설정하면_제외() {
        LocalDateTime tomorrow = LocalDateTime.now().plusDays(1);
        LocalDateTime nextWeek = LocalDateTime.now().plusDays(7);

        List<Document> result = documentRepository.searchOwn(
                owner.getId(), null, null, null, tomorrow, nextWeek);

        assertThat(result).isEmpty();
    }

    @Test
    void searchOwn_날짜_범위_필터_과거로_설정하면_포함() {
        LocalDateTime from = LocalDateTime.of(1970, 1, 1, 0, 0, 0);
        LocalDateTime to   = LocalDateTime.of(9999, 12, 31, 23, 59, 59);

        List<Document> result = documentRepository.searchOwn(
                owner.getId(), null, null, null, from, to);

        assertThat(result).hasSize(1);
    }

    // ── searchAll — ADMIN 전체 조회 ───────────────────────────────

    @Test
    void searchAll_sentinel_날짜로_전체_조회() {
        List<Document> result = documentRepository.searchAll(
                null, null, null,
                LocalDateTime.of(1970, 1, 1, 0, 0, 0),
                LocalDateTime.of(9999, 12, 31, 23, 59, 59));

        assertThat(result).isNotEmpty();
    }

    @Test
    void searchAll_키워드_null이면_전체_반환() {
        List<Document> result = documentRepository.searchAll(
                null, null, null,
                LocalDateTime.of(1970, 1, 1, 0, 0, 0),
                LocalDateTime.of(9999, 12, 31, 23, 59, 59));

        assertThat(result).hasSize(1);
    }

    @Test
    void searchAll_없는_키워드이면_빈_결과() {
        List<Document> result = documentRepository.searchAll(
                "%존재하지않는키워드xyz%", null, null,
                LocalDateTime.of(1970, 1, 1, 0, 0, 0),
                LocalDateTime.of(9999, 12, 31, 23, 59, 59));

        assertThat(result).isEmpty();
    }

    // ── searchInFolders ──────────────────────────────────────────

    @Test
    void searchInFolders_지정된_폴더_내_문서만_반환() {
        List<Document> result = documentRepository.searchInFolders(
                List.of(personalFolder.getId()),
                null, null, null,
                LocalDateTime.of(1970, 1, 1, 0, 0, 0),
                LocalDateTime.of(9999, 12, 31, 23, 59, 59));

        assertThat(result).hasSize(1);
    }

    @Test
    void searchInFolders_폴더_ID_불일치이면_빈_결과() {
        List<Document> result = documentRepository.searchInFolders(
                List.of(99999L),
                null, null, null,
                LocalDateTime.of(1970, 1, 1, 0, 0, 0),
                LocalDateTime.of(9999, 12, 31, 23, 59, 59));

        assertThat(result).isEmpty();
    }

    // ── 타입 필터 IS NULL — PostgreSQL 타입 추론 검증 ─────────────

    @Test
    void typeFilter_null이면_모든_타입_반환() {
        // 다른 타입 문서 추가
        documentRepository.save(Document.create(personalFolder, DocumentType.LICENSE, "정보처리기사"));
        em.flush();
        em.clear();

        List<Document> result = documentRepository.searchOwn(
                owner.getId(), null, null, null,
                LocalDateTime.of(1970, 1, 1, 0, 0, 0),
                LocalDateTime.of(9999, 12, 31, 23, 59, 59));

        assertThat(result).hasSize(2);
    }
}
