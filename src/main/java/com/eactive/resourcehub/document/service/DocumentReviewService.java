package com.eactive.resourcehub.document.service;

import com.eactive.resourcehub.audit.entity.AuditActionType;
import com.eactive.resourcehub.audit.entity.AuditTargetType;
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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class DocumentReviewService {

    private final DocumentVersionRepository documentVersionRepository;
    private final DocumentRepository documentRepository;
    private final UserRepository userRepository;
    private final AuditService auditService;
    private final EmailSender emailSender;

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

        // current_version_id를 이 버전으로 갱신
        Document document = version.getDocument();
        document.setCurrentVersion(version);

        auditService.log(reviewerUserId, AuditActionType.APPROVE_DOCUMENT,
                AuditTargetType.DOCUMENT_VERSION, documentVersionId, "문서 승인", request);

        // 이메일 알림 — 실패해도 트랜잭션 영향 없음
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
        // current_version_id 변경 없음

        auditService.log(reviewerUserId, AuditActionType.REJECT_DOCUMENT,
                AuditTargetType.DOCUMENT_VERSION, documentVersionId, "반려: " + reason.trim(), request);

        // 이메일 알림 — 실패해도 트랜잭션 영향 없음
        try {
            User owner = version.getDocument().getFolder().getOwner();
            emailSender.sendDocumentRejected(
                    owner.getEmail(), owner.getName(),
                    version.getDocument().getTitle(), version.getVersionNo(), reason.trim());
        } catch (Exception e) {
            log.warn("반려 알림 이메일 발송 실패: {}", e.getMessage());
        }
    }
}
