package com.eactive.resourcehub.document.service;

import com.eactive.resourcehub.document.entity.Document;
import com.eactive.resourcehub.document.entity.DocumentType;
import com.eactive.resourcehub.document.entity.FolderType;
import com.eactive.resourcehub.document.repository.DocumentRepository;
import com.eactive.resourcehub.document.repository.FolderRepository;
import com.eactive.resourcehub.permission.entity.PermissionTargetType;
import com.eactive.resourcehub.permission.entity.PermissionType;
import com.eactive.resourcehub.permission.repository.PermissionRepository;
import com.eactive.resourcehub.user.entity.UserRole;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class SearchService {

    private final DocumentRepository documentRepository;
    private final FolderRepository folderRepository;
    private final PermissionRepository permissionRepository;

    /**
     * 통합 검색. keyword = 제목·파일명, uploaderName·dateFrom·dateTo = Java 후처리.
     * @param folderKind "personal" | "public" | "shared" | "" (전체)
     */
    @Transactional(readOnly = true)
    public List<Document> search(Long userId, UserRole role,
                                 String keyword, DocumentType typeFilter,
                                 String uploaderName, LocalDate dateFrom, LocalDate dateTo,
                                 String folderKind) {
        String kw         = kw(keyword);
        String uploaderKw  = kw(uploaderName);
        // PostgreSQL JDBC는 null LocalDateTime의 타입 추론 불가 → sentinel 사용
        LocalDateTime from = dateFrom != null ? dateFrom.atStartOfDay()    : LocalDateTime.of(1970, 1, 1, 0, 0, 0);
        LocalDateTime to   = dateTo   != null ? dateTo.atTime(LocalTime.MAX) : LocalDateTime.of(9999, 12, 31, 23, 59, 59);

        List<Document> raw;

        if (role == UserRole.ADMIN) {
            raw = documentRepository.searchAll(kw, typeFilter, uploaderKw, from, to);
        } else {
            Map<Long, Document> deduped = new LinkedHashMap<>();

            boolean includePersonal = includeKind(folderKind, "personal");
            boolean includePublic   = includeKind(folderKind, "public");
            boolean includeShared   = includeKind(folderKind, "shared");

            if (includePersonal) {
                documentRepository.searchOwn(userId, kw, typeFilter, uploaderKw, from, to)
                        .forEach(d -> deduped.put(d.getId(), d));
            }
            if (includePublic) {
                folderRepository.findFirstByType(FolderType.SHARED_PUBLIC).ifPresent(pf ->
                    documentRepository.searchInFolders(List.of(pf.getId()), kw, typeFilter, uploaderKw, from, to)
                            .forEach(d -> deduped.putIfAbsent(d.getId(), d)));
            }
            if (includeShared) {
                List<Long> sharedFolderIds = permissionRepository
                        .findByUserIdAndTargetType(userId, PermissionTargetType.FOLDER)
                        .stream()
                        .filter(p -> p.getPermissionType() == PermissionType.FOLDER_ACCESS)
                        .map(p -> p.getTargetId())
                        .toList();
                if (!sharedFolderIds.isEmpty()) {
                    documentRepository.searchInFolders(sharedFolderIds, kw, typeFilter, uploaderKw, from, to)
                            .forEach(d -> deduped.putIfAbsent(d.getId(), d));
                }
            }
            raw = new ArrayList<>(deduped.values());
        }

        // ADMIN folderKind 후처리 (폴더 타입은 DB 쿼리로 내리기 어려우므로 유지)
        if (role == UserRole.ADMIN && folderKind != null && !folderKind.isBlank()) {
            FolderType ft = "public".equals(folderKind) ? FolderType.SHARED_PUBLIC : FolderType.PERSONAL;
            raw = raw.stream().filter(d -> d.getFolder().getType() == ft).toList();
        }

        return raw;
    }

    private static boolean includeKind(String folderKind, String kind) {
        return folderKind == null || folderKind.isBlank() || kind.equals(folderKind);
    }

    private static String kw(String s) {
        return (s != null && !s.isBlank()) ? "%" + s.trim().toLowerCase() + "%" : null;
    }
}
