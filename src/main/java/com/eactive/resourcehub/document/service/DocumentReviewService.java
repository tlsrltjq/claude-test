package com.eactive.resourcehub.document.service;

import com.eactive.resourcehub.audit.entity.AuditActionType;
import com.eactive.resourcehub.audit.entity.AuditTargetType;
import com.eactive.resourcehub.common.file.FileStorage;
import com.eactive.resourcehub.common.service.AuditService;
import com.eactive.resourcehub.common.email.EmailSender;
import com.eactive.resourcehub.document.entity.Document;
import com.eactive.resourcehub.document.entity.DocumentReviewStatus;
import com.eactive.resourcehub.document.entity.DocumentVersion;
import com.eactive.resourcehub.document.repository.DocumentRepository;
import com.eactive.resourcehub.document.repository.DocumentVersionRepository;
import com.eactive.resourcehub.user.entity.User;
import com.eactive.resourcehub.user.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class DocumentReviewService {

    private static final int STALE_REVIEW_DAYS = 14;

    private final DocumentVersionRepository documentVersionRepository;
    private final DocumentRepository documentRepository;
    private final UserRepository userRepository;
    private final AuditService auditService;
    private final EmailSender emailSender;
    private final FileStorage fileStorage;

    @Transactional(readOnly = true)
    public List<DocumentVersion> findPendingVersions() {
        return documentVersionRepository.findByReviewStatusWithDetails(DocumentReviewStatus.PENDING_REVIEW);
    }

    @Transactional(readOnly = true)
    public DocumentVersion findVersionForReview(Long documentVersionId) {
        return documentVersionRepository.findByIdForReviewDetail(documentVersionId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "문서 버전을 찾을 수 없습니다."));
    }

    @Transactional
    public void approve(Long documentVersionId, Long reviewerUserId, HttpServletRequest request) {
        DocumentVersion version = documentVersionRepository.findByIdWithDocumentAndFolder(documentVersionId)
                .orElseThrow(() -> new IllegalArgumentException("문서 버전을 찾을 수 없습니다."));

        if (version.getReviewStatus() != DocumentReviewStatus.PENDING_REVIEW) {
            throw new IllegalArgumentException("검토 대기 중인 버전만 승인할 수 있습니다.");
        }

        User reviewer = userRepository.findById(reviewerUserId)
                .orElseThrow(() -> new IllegalArgumentException("관리자를 찾을 수 없습니다."));

        version.approve(reviewer);

        Document document = version.getDocument();
        document.setCurrentVersion(version);

        auditService.log(reviewerUserId, AuditActionType.APPROVE_DOCUMENT,
                AuditTargetType.DOCUMENT_VERSION, documentVersionId, "문서 승인", request);

        try {
            User owner = document.getFolder().getOwner();
            emailSender.sendDocumentApproved(
                    owner.getEmail(), owner.getName(),
                    document.getTitle(), version.getVersionNo());
        } catch (Exception e) {
            log.warn("승인 알림 이메일 발송 실패: {}", e.getMessage());
        }
    }

    @Transactional
    public void reject(Long documentVersionId, String reason, Long reviewerUserId,
                       HttpServletRequest request) {
        if (reason == null || reason.trim().length() < 2) {
            throw new IllegalArgumentException("반려 사유는 최소 2자 이상 입력해야 합니다.");
        }

        DocumentVersion version = documentVersionRepository.findByIdWithDocumentAndFolder(documentVersionId)
                .orElseThrow(() -> new IllegalArgumentException("문서 버전을 찾을 수 없습니다."));

        if (version.getReviewStatus() != DocumentReviewStatus.PENDING_REVIEW) {
            throw new IllegalArgumentException("검토 대기 중인 버전만 반려할 수 있습니다.");
        }

        User reviewer = userRepository.findById(reviewerUserId)
                .orElseThrow(() -> new IllegalArgumentException("관리자를 찾을 수 없습니다."));

        version.reject(reviewer, reason.trim());

        // 반려 즉시 파일 영구 삭제
        purgeVersionFiles(version, "반려");

        auditService.log(reviewerUserId, AuditActionType.REJECT_DOCUMENT,
                AuditTargetType.DOCUMENT_VERSION, documentVersionId, "반려: " + reason.trim(), request);

        try {
            User owner = version.getDocument().getFolder().getOwner();
            emailSender.sendDocumentRejected(
                    owner.getEmail(), owner.getName(),
                    version.getDocument().getTitle(), version.getVersionNo(), reason.trim());
        } catch (Exception e) {
            log.warn("반려 알림 이메일 발송 실패: {}", e.getMessage());
        }
    }

    // ── 2주 이상 방치된 검토 대기 파일 자동 삭제 (매일 03:00) ──────────

    @Scheduled(cron = "0 0 3 * * *")
    @Transactional
    public void purgeStaleReviews() {
        LocalDateTime threshold = LocalDateTime.now().minusDays(STALE_REVIEW_DAYS);
        List<DocumentVersion> stale = documentVersionRepository.findStaleReviewVersions(threshold);
        if (stale.isEmpty()) return;

        log.info("[StaleReview] 검토 {}일 초과 대기 파일 {}건 자동 삭제 시작", STALE_REVIEW_DAYS, stale.size());
        int deleted = 0;
        for (DocumentVersion version : stale) {
            try {
                purgeVersionFiles(version, "검토 " + STALE_REVIEW_DAYS + "일 초과 자동 삭제");
                deleted++;
            } catch (Exception e) {
                log.warn("[StaleReview] 버전 {} 처리 중 오류: {}", version.getId(), e.getMessage());
            }
        }
        log.info("[StaleReview] 자동 삭제 완료 — {}건 처리", deleted);
    }

    // ── 공통: 버전 파일 즉시 삭제 + 경로 무효화 ──────────────────────────

    private void purgeVersionFiles(DocumentVersion version, String reason) {
        deleteIfPresent(version.getStoragePath(), version.getId(), "본문");
        deleteIfPresent(version.getPreviewStoragePath(), version.getId(), "미리보기");
        deleteIfPresent(version.getThumbnailStoragePath(), version.getId(), "썸네일");
        version.clearFiles();
        documentVersionRepository.save(version);
        log.info("[Review] 파일 즉시 삭제 — versionId={}, reason={}", version.getId(), reason);
    }

    private void deleteIfPresent(String path, Long versionId, String label) {
        if (path == null) return;
        try {
            fileStorage.delete(path);
        } catch (IOException | IllegalArgumentException e) {
            log.warn("[Review] {} 파일 삭제 실패 versionId={}: {}", label, versionId, e.getMessage());
        }
    }
}
