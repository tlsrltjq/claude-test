package com.eactive.resourcehub.document.service;

import com.eactive.resourcehub.audit.entity.AuditActionType;
import com.eactive.resourcehub.audit.entity.AuditTargetType;
import com.eactive.resourcehub.common.service.AuditService;
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
    private final AuditService auditService;

    @Transactional
    public void deleteDocument(Long documentId, CustomUserDetails actor, HttpServletRequest request) {
        if (actor.getUser().getRole() != UserRole.ADMIN) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "관리자만 문서를 삭제할 수 있습니다.");
        }

        Document document = documentRepository.findById(documentId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "문서를 찾을 수 없습니다."));

        Long actorId = actor.getUser().getId();
        String title = document.getTitle();

        document.delete(actorId);
        documentRepository.save(document);

        auditService.log(actorId, AuditActionType.DELETE_DOCUMENT, AuditTargetType.DOCUMENT, documentId, "문서 삭제: " + title, request);
        log.info("문서 소프트 삭제 완료 — documentId={}, title={}, adminId={}", documentId, title, actorId);
    }

    /** 본인 문서 삭제 — owner 확인 후 소프트 삭제 */
    @Transactional
    public void deleteOwnDocument(Long documentId, Long userId, HttpServletRequest request) {
        Document document = documentRepository.findByIdForDetail(documentId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "문서를 찾을 수 없습니다."));

        if (!document.getFolder().getOwner().getId().equals(userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "본인 문서만 삭제할 수 있습니다.");
        }

        String title = document.getTitle();
        document.delete(userId);
        documentRepository.save(document);

        auditService.log(userId, AuditActionType.DELETE_DOCUMENT, AuditTargetType.DOCUMENT, documentId, "본인 문서 삭제: " + title, request);
        log.info("본인 문서 소프트 삭제 완료 — documentId={}, title={}, userId={}", documentId, title, userId);
    }

    /** 공용 폴더 문서 삭제 — 최초 업로더 또는 ADMIN만 허용 */
    @Transactional
    public void deletePublicFolderDocument(Long documentId, Long actorId, UserRole actorRole,
                                           HttpServletRequest request) {
        Document document = documentRepository.findById(documentId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "문서를 찾을 수 없습니다."));

        if (actorRole != UserRole.ADMIN) {
            List<DocumentVersion> versions = documentVersionRepository
                    .findByDocumentIdOrderByVersionNoDesc(documentId);
            Long originalUploaderId = versions.isEmpty()
                    ? null
                    : versions.get(versions.size() - 1).getUploadedBy().getId();
            if (!actorId.equals(originalUploaderId)) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "본인이 업로드한 문서만 삭제할 수 있습니다.");
            }
        }

        String title = document.getTitle();
        document.delete(actorId);
        documentRepository.save(document);

        auditService.log(actorId, AuditActionType.DELETE_DOCUMENT, AuditTargetType.DOCUMENT, documentId, "공용 폴더 문서 삭제: " + title, request);
        log.info("공용 폴더 문서 소프트 삭제 완료 — documentId={}, title={}, actorId={}", documentId, title, actorId);
    }
}
