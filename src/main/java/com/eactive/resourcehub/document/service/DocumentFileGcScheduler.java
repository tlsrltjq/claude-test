package com.eactive.resourcehub.document.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

/**
 * 매일 새벽 2시 파일 GC 정기 실행 스케줄러.
 *
 * {@link DocumentFileGcService}를 별도 빈으로 주입해 호출한다 — 같은 빈 안에서
 * 비트랜잭션 메서드가 {@code @Transactional} 메서드를 self-invocation 하면
 * Spring AOP 프록시를 우회해 트랜잭션 어드바이스가 적용되지 않으므로, 스케줄러를 분리한다.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DocumentFileGcScheduler {

    private final DocumentFileGcService documentFileGcService;

    @Scheduled(cron = "0 0 2 * * *")
    public void scheduledGc() {
        log.info("[GC] 정기 파일 GC 시작");
        int deleted = 0;
        int orphans = 0;
        try {
            deleted = documentFileGcService.runGc();
        } catch (Exception e) {
            log.error("[GC] 정기 파일 GC 실패: {}", e.getMessage(), e);
        }
        try {
            orphans = documentFileGcService.runOrphanScan();
        } catch (Exception e) {
            log.error("[GC] 고아 파일 스캔 실패: {}", e.getMessage(), e);
        }
        log.info("[GC] 정기 파일 GC 완료 — DELETED 문서 {}건, 고아 파일 {}개 처리", deleted, orphans);
    }
}
