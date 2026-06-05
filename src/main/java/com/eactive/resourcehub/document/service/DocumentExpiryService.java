package com.eactive.resourcehub.document.service;

import com.eactive.resourcehub.common.email.EmailSender;
import com.eactive.resourcehub.document.entity.Document;
import com.eactive.resourcehub.document.repository.DocumentRepository;
import com.eactive.resourcehub.user.entity.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class DocumentExpiryService {

    private static final int EXPIRY_WARN_DAYS = 30;

    private final DocumentRepository documentRepository;
    private final EmailSender emailSender;

    @Transactional(readOnly = true)
    public List<Document> findExpired() {
        return documentRepository.findExpired(LocalDate.now());
    }

    @Transactional(readOnly = true)
    public List<Document> findExpiringSoon() {
        LocalDate today = LocalDate.now();
        return documentRepository.findExpiringSoon(today, today.plusDays(EXPIRY_WARN_DAYS));
    }

    // 만료 알림은 문서당 "임박 1회 + 만료 1회"만 발송한다.
    // 발송에 성공한 문서는 발송 시각을 기록해 다음 실행부터 대상에서 제외한다.
    @Scheduled(cron = "0 0 9 * * *")
    @Transactional
    public void sendExpiryNotifications() {
        log.info("[만료 알림] 일일 체크 시작");

        LocalDate today = LocalDate.now();

        List<Document> expired = documentRepository.findExpiredNeedingNotice(today);
        int expiredSent = 0;
        for (Document doc : expired) {
            try {
                User owner = doc.getFolder().getOwner();
                emailSender.sendDocumentExpired(
                        owner.getEmail(), owner.getName(),
                        doc.getTitle(), doc.getExpiresAt());
                doc.markExpiredNoticeSent();
                expiredSent++;
            } catch (Exception e) {
                log.warn("만료 알림 이메일 실패 docId={}: {}", doc.getId(), e.getMessage());
            }
        }

        List<Document> expiringSoon =
                documentRepository.findExpiringSoonNeedingWarn(today, today.plusDays(EXPIRY_WARN_DAYS));
        int warnSent = 0;
        for (Document doc : expiringSoon) {
            try {
                User owner = doc.getFolder().getOwner();
                emailSender.sendDocumentExpiringSoon(
                        owner.getEmail(), owner.getName(),
                        doc.getTitle(), doc.getExpiresAt(), EXPIRY_WARN_DAYS);
                doc.markExpiryWarnSent();
                warnSent++;
            } catch (Exception e) {
                log.warn("임박 알림 이메일 실패 docId={}: {}", doc.getId(), e.getMessage());
            }
        }

        log.info("[만료 알림] 완료 — 만료 {}건, 임박 {}건", expiredSent, warnSent);
    }
}
