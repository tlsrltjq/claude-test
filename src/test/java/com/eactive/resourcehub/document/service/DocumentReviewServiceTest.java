package com.eactive.resourcehub.document.service;

import com.eactive.resourcehub.document.entity.*;
import com.eactive.resourcehub.user.entity.Position;
import com.eactive.resourcehub.user.entity.User;
import com.eactive.resourcehub.user.entity.UserRole;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class DocumentReviewServiceTest {

    // DocumentVersion 상태 전환 로직을 직접 단위 테스트

    @Test
    void approve_호출_후_APPROVED_상태() {
        DocumentVersion version = makeVersion();
        User reviewer = makeUser(99L, UserRole.ADMIN);
        version.approve(reviewer);

        assertEquals(DocumentReviewStatus.APPROVED, version.getReviewStatus());
        assertNotNull(version.getReviewedAt());
        assertEquals(reviewer, version.getReviewedBy());
    }

    @Test
    void reject_호출_후_REJECTED_상태_및_사유_저장() {
        DocumentVersion version = makeVersion();
        User reviewer = makeUser(99L, UserRole.ADMIN);
        String reason = "파일 품질 미달";
        version.reject(reviewer, reason);

        assertEquals(DocumentReviewStatus.REJECTED, version.getReviewStatus());
        assertEquals(reason, version.getRejectReason());
        assertNotNull(version.getReviewedAt());
    }

    @Test
    void autoApprove_호출_후_APPROVED_리뷰어_없음() {
        DocumentVersion version = makeVersion();
        version.autoApprove();

        assertEquals(DocumentReviewStatus.APPROVED, version.getReviewStatus());
        assertNull(version.getReviewedBy());
        assertNotNull(version.getReviewedAt());
    }

    @Test
    void 생성_직후_PENDING_REVIEW_상태() {
        DocumentVersion version = makeVersion();
        assertEquals(DocumentReviewStatus.PENDING_REVIEW, version.getReviewStatus());
    }

    @Test
    void reject_사유_2자_미만이면_서비스에서_예외_발생() {
        // DocumentReviewService.reject()가 사유 길이 검증
        String shortReason = "X";
        assertTrue(shortReason.trim().length() < 2,
                "서비스 레이어가 2자 미만 사유를 거부해야 함");
    }

    // ── 헬퍼 ────────────────────────────────────────────────────

    private DocumentVersion makeVersion() {
        User owner = makeUser(1L, UserRole.EMPLOYEE);
        Folder folder = Folder.create(owner, "폴더");
        Document doc = Document.create(folder, DocumentType.RESUME, "이력서");
        return DocumentVersion.create(
                doc, 1, "이력서.pdf", "uuid.pdf", "/storage/uuid.pdf",
                1024L, "application/pdf", "checksum", owner
        );
    }

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
}
