package com.eactive.resourcehub.document.service;

import com.eactive.resourcehub.audit.service.AuditLogService;
import com.eactive.resourcehub.document.entity.Document;
import com.eactive.resourcehub.document.entity.DocumentReviewStatus;
import com.eactive.resourcehub.document.entity.DocumentVersion;
import com.eactive.resourcehub.document.repository.DocumentRepository;
import com.eactive.resourcehub.document.repository.DocumentVersionRepository;
import com.eactive.resourcehub.user.entity.User;
import com.eactive.resourcehub.user.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DocumentReviewService {

    private final DocumentVersionRepository documentVersionRepository;
    private final DocumentRepository documentRepository;
    private final UserRepository userRepository;
    private final AuditLogService auditLogService;

    @Transactional(readOnly = true)
    public List<DocumentVersion> findPendingVersions() {
        return documentVersionRepository.findByReviewStatusWithDetails(DocumentReviewStatus.PENDING_REVIEW);
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

        auditLogService.logApproveDocument(reviewerUserId, documentVersionId, request);
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

        auditLogService.logRejectDocument(reviewerUserId, documentVersionId, reason.trim(), request);
    }
}
