-- V209: 시드 사용자별 학위·자격증 문서 메타 채우기 + 누락 문서 추가
-- 기존 문서는 degree_type / cert_type_meta / issued_date 만 UPDATE
-- 없는 문서는 documents + document_versions INSERT 후 current_version_id 연결

-- ──────────────────────────────────────────────
-- 1. 기존 문서 메타 UPDATE
-- ──────────────────────────────────────────────

-- 이서연 (doc 46) - 정보처리기사
UPDATE documents SET cert_type_meta='ENGINEER', issued_date='2021-06-05', title='정보처리기사' WHERE id=46;

-- 강동현 (doc 54) - 정보처리기사
UPDATE documents SET cert_type_meta='ENGINEER', issued_date='2019-05-11', title='정보처리기사' WHERE id=54;

-- 임준혁 (doc 59) - 전문학사 졸업증명서
UPDATE documents SET degree_type='ASSOCIATE', issued_date='2020-02-20', title='졸업증명서 (전문학사)' WHERE id=59;

-- 오하은 (doc 63) - 석사 졸업증명서
UPDATE documents SET degree_type='MASTER', issued_date='2005-02-20', title='졸업증명서 (석사)' WHERE id=63;

-- 신재원 (doc 65) - 정보처리기사
UPDATE documents SET cert_type_meta='ENGINEER', issued_date='2005-05-15', title='정보처리기사' WHERE id=65;

-- 한소율 (doc 66) - 석사 졸업증명서
UPDATE documents SET degree_type='MASTER', issued_date='2007-02-20', title='졸업증명서 (석사)' WHERE id=66;

-- 문성준 (doc 70) - 정보처리기사
UPDATE documents SET cert_type_meta='ENGINEER', issued_date='2014-11-22', title='정보처리기사' WHERE id=70;

-- 배지우 (doc 71) - 정보처리기사
UPDATE documents SET cert_type_meta='ENGINEER', issued_date='2022-11-19', title='정보처리기사' WHERE id=71;

-- 양준서 (doc 84) - 학사 졸업증명서
UPDATE documents SET degree_type='BACHELOR', issued_date='2015-02-20', title='졸업증명서 (학사)' WHERE id=84;

-- 양준서 (doc 85) - 정보처리기사
UPDATE documents SET cert_type_meta='ENGINEER', issued_date='2017-05-27', title='정보처리기사' WHERE id=85;

-- 허다은 (doc 87) - 학사 졸업증명서
UPDATE documents SET degree_type='BACHELOR', issued_date='2018-02-20', title='졸업증명서 (학사)' WHERE id=87;

-- 남도현 (doc 90) - 학사 졸업증명서
UPDATE documents SET degree_type='BACHELOR', issued_date='2013-02-20', title='졸업증명서 (학사)' WHERE id=90;

-- ──────────────────────────────────────────────
-- 2. 누락 문서 INSERT (CTE: document → version → current_version_id)
-- ──────────────────────────────────────────────
-- 형식: folder_id, document_type, title, degree_type/cert_type_meta, issued_date, uploaded_by(=owner user_id)

-- 관리자 (folder=45, user=1) — 석사
WITH d AS (INSERT INTO documents (folder_id, document_type, title, status, degree_type, issued_date, created_at, updated_at)
           VALUES (45,'GRADUATION_CERTIFICATE','졸업증명서 (석사)','ACTIVE','MASTER','1998-02-20',NOW(),NOW()) RETURNING id),
     v AS (INSERT INTO document_versions (document_id, version_no, original_file_name, stored_file_name, storage_path, file_size, content_type, uploaded_by, review_status, created_at, updated_at)
           SELECT d.id,1,'졸업증명서.pdf',gen_random_uuid()::text||'.pdf','seed/'||gen_random_uuid()::text||'.pdf',443,'application/pdf',1,'APPROVED',NOW(),NOW() FROM d RETURNING id, document_id)
UPDATE documents SET current_version_id=v.id FROM v WHERE documents.id=v.document_id;

-- 테스트영업 (folder=46, user=2) — 학사
WITH d AS (INSERT INTO documents (folder_id, document_type, title, status, degree_type, issued_date, created_at, updated_at)
           VALUES (46,'GRADUATION_CERTIFICATE','졸업증명서 (학사)','ACTIVE','BACHELOR','2013-02-20',NOW(),NOW()) RETURNING id),
     v AS (INSERT INTO document_versions (document_id, version_no, original_file_name, stored_file_name, storage_path, file_size, content_type, uploaded_by, review_status, created_at, updated_at)
           SELECT d.id,1,'졸업증명서.pdf',gen_random_uuid()::text||'.pdf','seed/'||gen_random_uuid()::text||'.pdf',443,'application/pdf',2,'APPROVED',NOW(),NOW() FROM d RETURNING id, document_id)
UPDATE documents SET current_version_id=v.id FROM v WHERE documents.id=v.document_id;

-- 테스트영업 (folder=46, user=2) — 정보처리기사
WITH d AS (INSERT INTO documents (folder_id, document_type, title, status, cert_type_meta, issued_date, created_at, updated_at)
           VALUES (46,'LICENSE','정보처리기사','ACTIVE','ENGINEER','2014-05-24',NOW(),NOW()) RETURNING id),
     v AS (INSERT INTO document_versions (document_id, version_no, original_file_name, stored_file_name, storage_path, file_size, content_type, uploaded_by, review_status, created_at, updated_at)
           SELECT d.id,1,'정보처리기사.pdf',gen_random_uuid()::text||'.pdf','seed/'||gen_random_uuid()::text||'.pdf',443,'application/pdf',2,'APPROVED',NOW(),NOW() FROM d RETURNING id, document_id)
UPDATE documents SET current_version_id=v.id FROM v WHERE documents.id=v.document_id;

-- 김민준 (folder=47, user=18) — 석사
WITH d AS (INSERT INTO documents (folder_id, document_type, title, status, degree_type, issued_date, created_at, updated_at)
           VALUES (47,'GRADUATION_CERTIFICATE','졸업증명서 (석사)','ACTIVE','MASTER','2013-02-20',NOW(),NOW()) RETURNING id),
     v AS (INSERT INTO document_versions (document_id, version_no, original_file_name, stored_file_name, storage_path, file_size, content_type, uploaded_by, review_status, created_at, updated_at)
           SELECT d.id,1,'졸업증명서.pdf',gen_random_uuid()::text||'.pdf','seed/'||gen_random_uuid()::text||'.pdf',443,'application/pdf',18,'APPROVED',NOW(),NOW() FROM d RETURNING id, document_id)
UPDATE documents SET current_version_id=v.id FROM v WHERE documents.id=v.document_id;

-- 김민준 (folder=47, user=18) — 정보처리기사
WITH d AS (INSERT INTO documents (folder_id, document_type, title, status, cert_type_meta, issued_date, created_at, updated_at)
           VALUES (47,'LICENSE','정보처리기사','ACTIVE','ENGINEER','2015-11-21',NOW(),NOW()) RETURNING id),
     v AS (INSERT INTO document_versions (document_id, version_no, original_file_name, stored_file_name, storage_path, file_size, content_type, uploaded_by, review_status, created_at, updated_at)
           SELECT d.id,1,'정보처리기사.pdf',gen_random_uuid()::text||'.pdf','seed/'||gen_random_uuid()::text||'.pdf',443,'application/pdf',18,'APPROVED',NOW(),NOW() FROM d RETURNING id, document_id)
UPDATE documents SET current_version_id=v.id FROM v WHERE documents.id=v.document_id;

-- 이서연 (folder=48, user=19) — 학사 (license는 기존 doc 46 업데이트로 처리)
WITH d AS (INSERT INTO documents (folder_id, document_type, title, status, degree_type, issued_date, created_at, updated_at)
           VALUES (48,'GRADUATION_CERTIFICATE','졸업증명서 (학사)','ACTIVE','BACHELOR','2017-02-20',NOW(),NOW()) RETURNING id),
     v AS (INSERT INTO document_versions (document_id, version_no, original_file_name, stored_file_name, storage_path, file_size, content_type, uploaded_by, review_status, created_at, updated_at)
           SELECT d.id,1,'졸업증명서.pdf',gen_random_uuid()::text||'.pdf','seed/'||gen_random_uuid()::text||'.pdf',443,'application/pdf',19,'APPROVED',NOW(),NOW() FROM d RETURNING id, document_id)
UPDATE documents SET current_version_id=v.id FROM v WHERE documents.id=v.document_id;

-- 박지훈 (folder=49, user=20) — 학사
WITH d AS (INSERT INTO documents (folder_id, document_type, title, status, degree_type, issued_date, created_at, updated_at)
           VALUES (49,'GRADUATION_CERTIFICATE','졸업증명서 (학사)','ACTIVE','BACHELOR','2021-02-20',NOW(),NOW()) RETURNING id),
     v AS (INSERT INTO document_versions (document_id, version_no, original_file_name, stored_file_name, storage_path, file_size, content_type, uploaded_by, review_status, created_at, updated_at)
           SELECT d.id,1,'졸업증명서.pdf',gen_random_uuid()::text||'.pdf','seed/'||gen_random_uuid()::text||'.pdf',443,'application/pdf',20,'APPROVED',NOW(),NOW() FROM d RETURNING id, document_id)
UPDATE documents SET current_version_id=v.id FROM v WHERE documents.id=v.document_id;

-- 박지훈 (folder=49, user=20) — 정보처리기사
WITH d AS (INSERT INTO documents (folder_id, document_type, title, status, cert_type_meta, issued_date, created_at, updated_at)
           VALUES (49,'LICENSE','정보처리기사','ACTIVE','ENGINEER','2023-06-10',NOW(),NOW()) RETURNING id),
     v AS (INSERT INTO document_versions (document_id, version_no, original_file_name, stored_file_name, storage_path, file_size, content_type, uploaded_by, review_status, created_at, updated_at)
           SELECT d.id,1,'정보처리기사.pdf',gen_random_uuid()::text||'.pdf','seed/'||gen_random_uuid()::text||'.pdf',443,'application/pdf',20,'APPROVED',NOW(),NOW() FROM d RETURNING id, document_id)
UPDATE documents SET current_version_id=v.id FROM v WHERE documents.id=v.document_id;

-- 최수빈 (folder=50, user=21) — 학사
WITH d AS (INSERT INTO documents (folder_id, document_type, title, status, degree_type, issued_date, created_at, updated_at)
           VALUES (50,'GRADUATION_CERTIFICATE','졸업증명서 (학사)','ACTIVE','BACHELOR','2022-02-20',NOW(),NOW()) RETURNING id),
     v AS (INSERT INTO document_versions (document_id, version_no, original_file_name, stored_file_name, storage_path, file_size, content_type, uploaded_by, review_status, created_at, updated_at)
           SELECT d.id,1,'졸업증명서.pdf',gen_random_uuid()::text||'.pdf','seed/'||gen_random_uuid()::text||'.pdf',443,'application/pdf',21,'APPROVED',NOW(),NOW() FROM d RETURNING id, document_id)
UPDATE documents SET current_version_id=v.id FROM v WHERE documents.id=v.document_id;

-- 정민서 (folder=51, user=22) — 석사
WITH d AS (INSERT INTO documents (folder_id, document_type, title, status, degree_type, issued_date, created_at, updated_at)
           VALUES (51,'GRADUATION_CERTIFICATE','졸업증명서 (석사)','ACTIVE','MASTER','2009-02-20',NOW(),NOW()) RETURNING id),
     v AS (INSERT INTO document_versions (document_id, version_no, original_file_name, stored_file_name, storage_path, file_size, content_type, uploaded_by, review_status, created_at, updated_at)
           SELECT d.id,1,'졸업증명서.pdf',gen_random_uuid()::text||'.pdf','seed/'||gen_random_uuid()::text||'.pdf',443,'application/pdf',22,'APPROVED',NOW(),NOW() FROM d RETURNING id, document_id)
UPDATE documents SET current_version_id=v.id FROM v WHERE documents.id=v.document_id;

-- 정민서 (folder=51, user=22) — 정보처리기사
WITH d AS (INSERT INTO documents (folder_id, document_type, title, status, cert_type_meta, issued_date, created_at, updated_at)
           VALUES (51,'LICENSE','정보처리기사','ACTIVE','ENGINEER','2007-05-19',NOW(),NOW()) RETURNING id),
     v AS (INSERT INTO document_versions (document_id, version_no, original_file_name, stored_file_name, storage_path, file_size, content_type, uploaded_by, review_status, created_at, updated_at)
           SELECT d.id,1,'정보처리기사.pdf',gen_random_uuid()::text||'.pdf','seed/'||gen_random_uuid()::text||'.pdf',443,'application/pdf',22,'APPROVED',NOW(),NOW() FROM d RETURNING id, document_id)
UPDATE documents SET current_version_id=v.id FROM v WHERE documents.id=v.document_id;

-- 강동현 (folder=52, user=23) — 학사 (license는 기존 doc 54)
WITH d AS (INSERT INTO documents (folder_id, document_type, title, status, degree_type, issued_date, created_at, updated_at)
           VALUES (52,'GRADUATION_CERTIFICATE','졸업증명서 (학사)','ACTIVE','BACHELOR','2014-02-20',NOW(),NOW()) RETURNING id),
     v AS (INSERT INTO document_versions (document_id, version_no, original_file_name, stored_file_name, storage_path, file_size, content_type, uploaded_by, review_status, created_at, updated_at)
           SELECT d.id,1,'졸업증명서.pdf',gen_random_uuid()::text||'.pdf','seed/'||gen_random_uuid()::text||'.pdf',443,'application/pdf',23,'APPROVED',NOW(),NOW() FROM d RETURNING id, document_id)
UPDATE documents SET current_version_id=v.id FROM v WHERE documents.id=v.document_id;

-- 윤지아 (folder=53, user=24) — 학사
WITH d AS (INSERT INTO documents (folder_id, document_type, title, status, degree_type, issued_date, created_at, updated_at)
           VALUES (53,'GRADUATION_CERTIFICATE','졸업증명서 (학사)','ACTIVE','BACHELOR','2018-02-20',NOW(),NOW()) RETURNING id),
     v AS (INSERT INTO document_versions (document_id, version_no, original_file_name, stored_file_name, storage_path, file_size, content_type, uploaded_by, review_status, created_at, updated_at)
           SELECT d.id,1,'졸업증명서.pdf',gen_random_uuid()::text||'.pdf','seed/'||gen_random_uuid()::text||'.pdf',443,'application/pdf',24,'APPROVED',NOW(),NOW() FROM d RETURNING id, document_id)
UPDATE documents SET current_version_id=v.id FROM v WHERE documents.id=v.document_id;

-- 윤지아 (folder=53, user=24) — 정보처리기사
WITH d AS (INSERT INTO documents (folder_id, document_type, title, status, cert_type_meta, issued_date, created_at, updated_at)
           VALUES (53,'LICENSE','정보처리기사','ACTIVE','ENGINEER','2022-05-28',NOW(),NOW()) RETURNING id),
     v AS (INSERT INTO document_versions (document_id, version_no, original_file_name, stored_file_name, storage_path, file_size, content_type, uploaded_by, review_status, created_at, updated_at)
           SELECT d.id,1,'정보처리기사.pdf',gen_random_uuid()::text||'.pdf','seed/'||gen_random_uuid()::text||'.pdf',443,'application/pdf',24,'APPROVED',NOW(),NOW() FROM d RETURNING id, document_id)
UPDATE documents SET current_version_id=v.id FROM v WHERE documents.id=v.document_id;

-- 오하은 (folder=55, user=26) — 정보처리기사 (degree는 기존 doc 63)
WITH d AS (INSERT INTO documents (folder_id, document_type, title, status, cert_type_meta, issued_date, created_at, updated_at)
           VALUES (55,'LICENSE','정보처리기사','ACTIVE','ENGINEER','2006-05-20',NOW(),NOW()) RETURNING id),
     v AS (INSERT INTO document_versions (document_id, version_no, original_file_name, stored_file_name, storage_path, file_size, content_type, uploaded_by, review_status, created_at, updated_at)
           SELECT d.id,1,'정보처리기사.pdf',gen_random_uuid()::text||'.pdf','seed/'||gen_random_uuid()::text||'.pdf',443,'application/pdf',26,'APPROVED',NOW(),NOW() FROM d RETURNING id, document_id)
UPDATE documents SET current_version_id=v.id FROM v WHERE documents.id=v.document_id;

-- 신재원 (folder=56, user=27) — 박사 (license는 기존 doc 65)
WITH d AS (INSERT INTO documents (folder_id, document_type, title, status, degree_type, issued_date, created_at, updated_at)
           VALUES (56,'GRADUATION_CERTIFICATE','졸업증명서 (박사)','ACTIVE','DOCTORATE','2007-02-20',NOW(),NOW()) RETURNING id),
     v AS (INSERT INTO document_versions (document_id, version_no, original_file_name, stored_file_name, storage_path, file_size, content_type, uploaded_by, review_status, created_at, updated_at)
           SELECT d.id,1,'졸업증명서.pdf',gen_random_uuid()::text||'.pdf','seed/'||gen_random_uuid()::text||'.pdf',443,'application/pdf',27,'APPROVED',NOW(),NOW() FROM d RETURNING id, document_id)
UPDATE documents SET current_version_id=v.id FROM v WHERE documents.id=v.document_id;

-- 문성준 (folder=58, user=29) — 석사 (license는 기존 doc 70)
WITH d AS (INSERT INTO documents (folder_id, document_type, title, status, degree_type, issued_date, created_at, updated_at)
           VALUES (58,'GRADUATION_CERTIFICATE','졸업증명서 (석사)','ACTIVE','MASTER','2012-02-20',NOW(),NOW()) RETURNING id),
     v AS (INSERT INTO document_versions (document_id, version_no, original_file_name, stored_file_name, storage_path, file_size, content_type, uploaded_by, review_status, created_at, updated_at)
           SELECT d.id,1,'졸업증명서.pdf',gen_random_uuid()::text||'.pdf','seed/'||gen_random_uuid()::text||'.pdf',443,'application/pdf',29,'APPROVED',NOW(),NOW() FROM d RETURNING id, document_id)
UPDATE documents SET current_version_id=v.id FROM v WHERE documents.id=v.document_id;

-- 배지우 (folder=59, user=30) — 학사 (license는 기존 doc 71)
WITH d AS (INSERT INTO documents (folder_id, document_type, title, status, degree_type, issued_date, created_at, updated_at)
           VALUES (59,'GRADUATION_CERTIFICATE','졸업증명서 (학사)','ACTIVE','BACHELOR','2016-02-20',NOW(),NOW()) RETURNING id),
     v AS (INSERT INTO document_versions (document_id, version_no, original_file_name, stored_file_name, storage_path, file_size, content_type, uploaded_by, review_status, created_at, updated_at)
           SELECT d.id,1,'졸업증명서.pdf',gen_random_uuid()::text||'.pdf','seed/'||gen_random_uuid()::text||'.pdf',443,'application/pdf',30,'APPROVED',NOW(),NOW() FROM d RETURNING id, document_id)
UPDATE documents SET current_version_id=v.id FROM v WHERE documents.id=v.document_id;

-- 송하린 (folder=60, user=31) — 석사
WITH d AS (INSERT INTO documents (folder_id, document_type, title, status, degree_type, issued_date, created_at, updated_at)
           VALUES (60,'GRADUATION_CERTIFICATE','졸업증명서 (석사)','ACTIVE','MASTER','2007-02-20',NOW(),NOW()) RETURNING id),
     v AS (INSERT INTO document_versions (document_id, version_no, original_file_name, stored_file_name, storage_path, file_size, content_type, uploaded_by, review_status, created_at, updated_at)
           SELECT d.id,1,'졸업증명서.pdf',gen_random_uuid()::text||'.pdf','seed/'||gen_random_uuid()::text||'.pdf',443,'application/pdf',31,'APPROVED',NOW(),NOW() FROM d RETURNING id, document_id)
UPDATE documents SET current_version_id=v.id FROM v WHERE documents.id=v.document_id;

-- 전민재 (folder=61, user=32) — 석사
WITH d AS (INSERT INTO documents (folder_id, document_type, title, status, degree_type, issued_date, created_at, updated_at)
           VALUES (61,'GRADUATION_CERTIFICATE','졸업증명서 (석사)','ACTIVE','MASTER','2011-02-20',NOW(),NOW()) RETURNING id),
     v AS (INSERT INTO document_versions (document_id, version_no, original_file_name, stored_file_name, storage_path, file_size, content_type, uploaded_by, review_status, created_at, updated_at)
           SELECT d.id,1,'졸업증명서.pdf',gen_random_uuid()::text||'.pdf','seed/'||gen_random_uuid()::text||'.pdf',443,'application/pdf',32,'APPROVED',NOW(),NOW() FROM d RETURNING id, document_id)
UPDATE documents SET current_version_id=v.id FROM v WHERE documents.id=v.document_id;

-- 전민재 (folder=61, user=32) — 정보처리기사
WITH d AS (INSERT INTO documents (folder_id, document_type, title, status, cert_type_meta, issued_date, created_at, updated_at)
           VALUES (61,'LICENSE','정보처리기사','ACTIVE','ENGINEER','2012-11-24',NOW(),NOW()) RETURNING id),
     v AS (INSERT INTO document_versions (document_id, version_no, original_file_name, stored_file_name, storage_path, file_size, content_type, uploaded_by, review_status, created_at, updated_at)
           SELECT d.id,1,'정보처리기사.pdf',gen_random_uuid()::text||'.pdf','seed/'||gen_random_uuid()::text||'.pdf',443,'application/pdf',32,'APPROVED',NOW(),NOW() FROM d RETURNING id, document_id)
UPDATE documents SET current_version_id=v.id FROM v WHERE documents.id=v.document_id;

-- 조수아 (folder=62, user=33) — 학사
WITH d AS (INSERT INTO documents (folder_id, document_type, title, status, degree_type, issued_date, created_at, updated_at)
           VALUES (62,'GRADUATION_CERTIFICATE','졸업증명서 (학사)','ACTIVE','BACHELOR','2019-02-20',NOW(),NOW()) RETURNING id),
     v AS (INSERT INTO document_versions (document_id, version_no, original_file_name, stored_file_name, storage_path, file_size, content_type, uploaded_by, review_status, created_at, updated_at)
           SELECT d.id,1,'졸업증명서.pdf',gen_random_uuid()::text||'.pdf','seed/'||gen_random_uuid()::text||'.pdf',443,'application/pdf',33,'APPROVED',NOW(),NOW() FROM d RETURNING id, document_id)
UPDATE documents SET current_version_id=v.id FROM v WHERE documents.id=v.document_id;

-- 류지현 (folder=63, user=34) — 학사
WITH d AS (INSERT INTO documents (folder_id, document_type, title, status, degree_type, issued_date, created_at, updated_at)
           VALUES (63,'GRADUATION_CERTIFICATE','졸업증명서 (학사)','ACTIVE','BACHELOR','2022-02-20',NOW(),NOW()) RETURNING id),
     v AS (INSERT INTO document_versions (document_id, version_no, original_file_name, stored_file_name, storage_path, file_size, content_type, uploaded_by, review_status, created_at, updated_at)
           SELECT d.id,1,'졸업증명서.pdf',gen_random_uuid()::text||'.pdf','seed/'||gen_random_uuid()::text||'.pdf',443,'application/pdf',34,'APPROVED',NOW(),NOW() FROM d RETURNING id, document_id)
UPDATE documents SET current_version_id=v.id FROM v WHERE documents.id=v.document_id;

-- 남도현 (folder=66, user=37) — 정보처리기사 (degree는 기존 doc 90)
WITH d AS (INSERT INTO documents (folder_id, document_type, title, status, cert_type_meta, issued_date, created_at, updated_at)
           VALUES (66,'LICENSE','정보처리기사','ACTIVE','ENGINEER','2015-05-23',NOW(),NOW()) RETURNING id),
     v AS (INSERT INTO document_versions (document_id, version_no, original_file_name, stored_file_name, storage_path, file_size, content_type, uploaded_by, review_status, created_at, updated_at)
           SELECT d.id,1,'정보처리기사.pdf',gen_random_uuid()::text||'.pdf','seed/'||gen_random_uuid()::text||'.pdf',443,'application/pdf',37,'APPROVED',NOW(),NOW() FROM d RETURNING id, document_id)
UPDATE documents SET current_version_id=v.id FROM v WHERE documents.id=v.document_id;
