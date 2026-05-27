package com.eactive.resourcehub.document.service;

import com.eactive.resourcehub.document.entity.Document;
import com.eactive.resourcehub.document.entity.DocumentStatus;
import com.eactive.resourcehub.document.entity.DocumentVersion;
import com.eactive.resourcehub.document.entity.Folder;
import com.eactive.resourcehub.document.entity.FolderType;
import com.eactive.resourcehub.document.repository.DocumentRepository;
import com.eactive.resourcehub.document.repository.DocumentVersionRepository;
import com.eactive.resourcehub.document.repository.FolderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class MyFolderService {

    private final FolderRepository folderRepository;
    private final DocumentRepository documentRepository;
    private final DocumentVersionRepository documentVersionRepository;

    @Transactional(readOnly = true)
    public Optional<Folder> findPersonalFolder(Long userId) {
        return folderRepository.findByOwnerIdAndType(userId, FolderType.PERSONAL);
    }

    @Transactional(readOnly = true)
    public List<Document> findActiveDocuments(Long folderId) {
        return documentRepository.findByFolderIdAndStatus(folderId, DocumentStatus.ACTIVE);
    }

    @Transactional(readOnly = true)
    public Map<Long, DocumentVersion> findLatestVersionMap(List<Document> documents) {
        Map<Long, DocumentVersion> map = new HashMap<>();
        for (Document doc : documents) {
            documentVersionRepository.findFirstByDocumentIdOrderByVersionNoDesc(doc.getId())
                    .ifPresent(v -> map.put(doc.getId(), v));
        }
        return map;
    }

    @Transactional(readOnly = true)
    public Document findDocumentDetail(Long documentId) {
        return documentRepository.findByIdForDetail(documentId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "문서를 찾을 수 없습니다."));
    }

    @Transactional(readOnly = true)
    public Document findDocumentForUpdate(Long documentId) {
        return documentRepository.findByIdForDetail(documentId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "문서를 찾을 수 없습니다."));
    }

    @Transactional(readOnly = true)
    public List<DocumentVersion> findDocumentVersions(Long documentId) {
        return documentVersionRepository.findByDocumentIdOrderByVersionNoDesc(documentId);
    }

    @Transactional
    public void updateExpiry(Long documentId, Long ownerId, LocalDate expiresAt) {
        Document document = findDocumentForUpdate(documentId);
        if (!document.getFolder().getOwner().getId().equals(ownerId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }
        document.updateExpiresAt(expiresAt);
        documentRepository.save(document);
    }
}
