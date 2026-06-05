package com.eactive.resourcehub.document.service;

import com.eactive.resourcehub.common.file.FileStorage;
import com.eactive.resourcehub.document.entity.Document;
import com.eactive.resourcehub.document.entity.DocumentVersion;
import com.eactive.resourcehub.document.repository.DocumentRepository;
import com.eactive.resourcehub.document.repository.DocumentVersionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 소프트 삭제된 문서의 물리 파일을 주기적으로 정리하는 GC 서비스.
 *
 * 동작 조건: status = 'DELETED' AND deleted_at < now() - retentionDays AND files_purged_at IS NULL
 * 기본 보존 기간: 7일 (RESOURCEHUB_GC_RETENTION_DAYS 환경변수로 조정 가능)
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DocumentFileGcService {

    private final DocumentRepository documentRepository;
    private final DocumentVersionRepository documentVersionRepository;
    private final FileStorage fileStorage;

    @Value("${resourcehub.gc.retention-days:7}")
    private int retentionDays;

    /**
     * GC를 즉시 실행하고 처리한 문서 건수를 반환한다.
     * 정기 실행은 {@link DocumentFileGcScheduler}, 수동 실행은 AdminController, 테스트에서도 호출 가능.
     */
    @Transactional
    public int runGc() {
        LocalDateTime threshold = LocalDateTime.now().minusDays(retentionDays);
        List<Document> candidates = documentRepository.findPurgeCandidates(threshold);

        if (candidates.isEmpty()) {
            log.info("[GC] 정리 대상 없음");
            return 0;
        }

        log.info("[GC] 정리 대상 {}건 발견 (threshold={})", candidates.size(), threshold);

        List<Long> documentIds = candidates.stream().map(Document::getId).toList();
        List<DocumentVersion> versions = documentVersionRepository.findByDocumentIdIn(documentIds);

        List<String> paths = collectPaths(versions);
        int deleted = deleteFiles(paths);

        for (Document doc : candidates) {
            doc.markFilesPurged();
            documentRepository.save(doc);
        }

        log.info("[GC] 파일 {}개 삭제, 문서 {}건 purged 마킹 완료", deleted, candidates.size());
        return candidates.size();
    }

    /** 문서 버전의 모든 경로(본문 + 미리보기)를 수집 */
    private List<String> collectPaths(List<DocumentVersion> versions) {
        List<String> paths = new ArrayList<>();
        for (DocumentVersion v : versions) {
            if (v.getStoragePath() != null) {
                paths.add(v.getStoragePath());
            }
            if (v.getPreviewStoragePath() != null) {
                paths.add(v.getPreviewStoragePath());
            }
        }
        return paths;
    }

    /** 파일을 하나씩 삭제한다. 개별 실패는 경고 로그만 남기고 계속 진행한다. */
    private int deleteFiles(List<String> paths) {
        int count = 0;
        for (String path : paths) {
            try {
                fileStorage.delete(path);
                count++;
                log.debug("[GC] 파일 삭제: {}", path);
            } catch (IOException e) {
                log.warn("[GC] 파일 삭제 실패 (무시하고 계속) — path={}, error={}", path, e.getMessage());
            }
        }
        return count;
    }

    /**
     * 스토리지에 존재하지만 DB에 경로가 없는 고아 파일을 찾아 삭제한다.
     * 업로드 중인 파일을 보호하기 위해 1시간 이상 된 파일만 대상으로 한다.
     * LocalFileStorage만 완전 지원 — S3 등은 빈 목록 반환으로 자동 skip.
     */
    @Transactional(readOnly = true)
    public int runOrphanScan() {
        List<String> fsPaths;
        try {
            Instant cutoff = Instant.now().minusSeconds(3600);
            fsPaths = fileStorage.listAll(cutoff);
        } catch (IOException e) {
            log.warn("[GC] 스토리지 파일 목록 조회 실패: {}", e.getMessage());
            return 0;
        }
        if (fsPaths.isEmpty()) return 0;

        Set<String> knownPaths = new HashSet<>();
        knownPaths.addAll(documentVersionRepository.findAllStoragePaths());
        knownPaths.addAll(documentVersionRepository.findAllPreviewPaths());
        knownPaths.addAll(documentVersionRepository.findAllThumbnailPaths());

        int count = 0;
        for (String path : fsPaths) {
            if (!knownPaths.contains(path)) {
                try {
                    fileStorage.delete(path);
                    count++;
                    log.info("[GC] 고아 파일 삭제: {}", path);
                } catch (IOException e) {
                    log.warn("[GC] 고아 파일 삭제 실패 (무시하고 계속) — path={}, error={}", path, e.getMessage());
                }
            }
        }
        if (count > 0) log.info("[GC] 고아 파일 {}개 삭제 완료", count);
        return count;
    }

    /** GC 실행 시 삭제될 파일 목록을 반환한다. 실제 삭제는 하지 않는다. */
    @Transactional(readOnly = true)
    public List<GcPreviewItem> previewGc() {
        LocalDateTime threshold = LocalDateTime.now().minusDays(retentionDays);
        List<Document> candidates = documentRepository.findPurgeCandidates(threshold);
        if (candidates.isEmpty()) return List.of();

        List<Long> documentIds = candidates.stream().map(Document::getId).toList();
        List<DocumentVersion> allVersions = documentVersionRepository.findByDocumentIdIn(documentIds);

        Map<Long, List<DocumentVersion>> versionByDocId = new HashMap<>();
        for (DocumentVersion v : allVersions) {
            Long docId = v.getDocument().getId();
            versionByDocId.computeIfAbsent(docId, k -> new ArrayList<>()).add(v);
        }

        return candidates.stream().map(doc -> {
            List<DocumentVersion> versions = versionByDocId.getOrDefault(doc.getId(), List.of());
            int fileCount = collectPaths(versions).size();
            String ownerName = doc.getFolder() != null && doc.getFolder().getOwner() != null
                    ? doc.getFolder().getOwner().getName() : "-";
            return new GcPreviewItem(doc.getId(), doc.getTitle(), ownerName, doc.getDeletedAt(),
                    versions.size(), fileCount);
        }).toList();
    }

    public int getRetentionDays() {
        return retentionDays;
    }

    public record GcPreviewItem(
            Long documentId,
            String title,
            String ownerName,
            LocalDateTime deletedAt,
            int versionCount,
            int fileCount
    ) {}
}
