package com.eactive.resourcehub.document.service;

import com.eactive.resourcehub.document.entity.Document;
import com.eactive.resourcehub.document.entity.DocumentType;
import com.eactive.resourcehub.document.repository.DocumentRepository;
import com.eactive.resourcehub.permission.entity.PermissionTargetType;
import com.eactive.resourcehub.permission.entity.PermissionType;
import com.eactive.resourcehub.permission.repository.PermissionRepository;
import com.eactive.resourcehub.user.entity.UserRole;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class SearchService {

    private final DocumentRepository documentRepository;
    private final PermissionRepository permissionRepository;

    @Transactional(readOnly = true)
    public List<Document> search(Long userId, UserRole role,
                                  String keyword, DocumentType typeFilter, String tagName) {
        String kw = (keyword != null && !keyword.isBlank())
                ? "%" + keyword.trim().toLowerCase() + "%" : null;
        String tn = (tagName != null && !tagName.isBlank())
                ? tagName.trim().toLowerCase() : null;

        if (role == UserRole.ADMIN) {
            return documentRepository.searchAll(kw, typeFilter, tn);
        }

        // 본인 문서
        List<Document> results = new ArrayList<>(
                documentRepository.searchOwn(userId, kw, typeFilter, tn));

        // 공유 폴더 문서 (FOLDER_ACCESS 권한 있는 폴더)
        List<Long> sharedFolderIds = permissionRepository
                .findByUserIdAndTargetType(userId, PermissionTargetType.FOLDER)
                .stream()
                .filter(p -> p.getPermissionType() == PermissionType.FOLDER_ACCESS)
                .map(p -> p.getTargetId())
                .toList();

        if (!sharedFolderIds.isEmpty()) {
            List<Document> sharedDocs = documentRepository.searchInFolders(
                    sharedFolderIds, kw, typeFilter, tn);
            // 중복 제거 (본인 문서와 겹칠 수 있음)
            Map<Long, Document> deduped = new LinkedHashMap<>();
            results.forEach(d -> deduped.put(d.getId(), d));
            sharedDocs.forEach(d -> deduped.putIfAbsent(d.getId(), d));
            return new ArrayList<>(deduped.values());
        }

        return results;
    }
}
