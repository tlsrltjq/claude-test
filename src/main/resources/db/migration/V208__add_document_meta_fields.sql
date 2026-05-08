-- MVP: 문서에 발급일·학위종류·자격증종류 메타데이터 추가
-- 경력계산기 자동 채우기 기능에서 사용
ALTER TABLE documents
    ADD COLUMN issued_date    DATE,
    ADD COLUMN degree_type    VARCHAR(50),
    ADD COLUMN cert_type_meta VARCHAR(50);
