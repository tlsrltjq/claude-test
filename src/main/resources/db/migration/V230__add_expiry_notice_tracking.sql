-- 문서 만료 알림을 "임박 1회 + 만료 1회"로 고정하기 위한 발송 이력 컬럼.
-- expiry_warn_sent_at   : 만료 30일 전 임박 알림 발송 시각 (NULL = 미발송)
-- expired_notice_sent_at: 만료 알림 발송 시각 (NULL = 미발송)
ALTER TABLE documents ADD COLUMN expiry_warn_sent_at    TIMESTAMP;
ALTER TABLE documents ADD COLUMN expired_notice_sent_at TIMESTAMP;

-- 백필: 배포 시점에 이미 임박 구간(만료 30일 이내)에 들어왔거나 만료된 문서는
-- 구버전에서 이미 매일 알림을 받아왔으므로 '발송함'으로 처리해 대량 재발송을 방지한다.
UPDATE documents
   SET expiry_warn_sent_at = now()
 WHERE status = 'ACTIVE'
   AND expires_at IS NOT NULL
   AND expires_at <= CURRENT_DATE + INTERVAL '30 days';

UPDATE documents
   SET expired_notice_sent_at = now()
 WHERE status = 'ACTIVE'
   AND expires_at IS NOT NULL
   AND expires_at < CURRENT_DATE;
