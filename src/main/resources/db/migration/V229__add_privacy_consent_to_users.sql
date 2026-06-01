-- 개인정보 동의 일시·버전 추적 (기존 계정은 NULL 허용)
ALTER TABLE users
    ADD COLUMN privacy_consent_at   TIMESTAMP,
    ADD COLUMN privacy_consent_version VARCHAR(10);
