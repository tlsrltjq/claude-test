package com.eactive.resourcehub.document.service;

import com.eactive.resourcehub.audit.service.AuditLogService;
import com.eactive.resourcehub.common.file.FileStorage;
import com.eactive.resourcehub.common.security.CustomUserDetails;
import com.eactive.resourcehub.document.entity.Document;
import com.eactive.resourcehub.document.entity.DocumentVersion;
import com.eactive.resourcehub.document.repository.DocumentRepository;
import com.eactive.resourcehub.document.repository.DocumentVersionRepository;
import com.eactive.resourcehub.user.entity.UserRole;
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
public class DocumentDeleteService {

    private final DocumentRepository documentRepository;
    private final DocumentVersionRepository documentVersionRepository;
    private final FileStorage fileStorage;
    private final AuditLogService auditLogService;

    @Transactional
    public void deleteDocument(Long documentId, CustomUserDetails actor, HttpServletRequest request) {
        if (actor.getUser().getRole() != UserRole.ADMIN) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "관리자만 문서를 삭제할 수 있습니다.");
        }

        Document document = documentRepository.findById(documentId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "문서를 찾을 수 없습니다."));

        String title = document.getTitle();
        List<DocumentVersion> versions = documentVersionRepository.findByDocumentIdOrderByVersionNoDesc(documentId);

        // 1. circular FK 해제 (documents.current_version_id → document_versions.id)
        document.setCurrentVersion(null);
        documentRepository.save(document);

        // 2. 각 버전의 디스크 파일 삭제
        for (DocumentVersion v : versions) {
            deleteFileSilently(v.getStoragePath());
            deleteFileSilently(v.getPreviewStoragePath());
            deleteFileSilently(v.getThumbnailStoragePath());
        }

        // 3. 버전 행 삭제 → 문서 행 삭제 (document_tags는 ON DELETE CASCADE로 자동 처리)
        documentVersionRepository.deleteAll(versions);
        documentRepository.delete(document);

        auditLogService.logDeleteDocument(actor.getUser().getId(), documentId,
                "문서 삭제: " + title, request);

        log.info("문서 삭제 완료 — documentId={}, title={}, adminId={}",
                documentId, title, actor.getUser().getId());
    }

    private void deleteFileSilently(String storagePath) {
        if (storagePath == null) return;
        try {
            fileStorage.delete(storagePath);
        } catch (Exception e) {
            log.warn("파일 삭제 실패 — path={}, error={}", storagePath, e.getMessage());
        }
    }
}
