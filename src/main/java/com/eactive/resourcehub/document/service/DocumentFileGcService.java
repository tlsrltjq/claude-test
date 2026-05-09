package com.eactive.resourcehub.document.service;

import com.eactive.resourcehub.common.file.FileStorage;
import com.eactive.resourcehub.document.entity.Document;
import com.eactive.resourcehub.document.entity.DocumentVersion;
import com.eactive.resourcehub.document.repository.DocumentRepository;
import com.eactive.resourcehub.document.repository.DocumentVersionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

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

    /** 매일 새벽 2시 자동 실행 */
    @Scheduled(cron = "0 0 2 * * *")
    public void scheduledGc() {
        log.info("[GC] 정기 파일 GC 시작 (보존 기간 {}일)", retentionDays);
        int count = runGc();
        log.info("[GC] 정기 파일 GC 완료 — {}건 처리", count);
    }

    /**
     * GC를 즉시 실행하고 처리한 문서 건수를 반환한다.
     * 관리자 수동 실행 또는 테스트에서도 호출 가능.
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

    public int getRetentionDays() {
        return retentionDays;
    }
}
