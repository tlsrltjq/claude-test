-- ============================================================
-- 데모 데이터: 팀 10개 + 직원 100명 (2026-06-08 기준)
-- 비밀번호 공통: Test1234!
-- ============================================================
BEGIN;

-- ─── 1. 팀 ───────────────────────────────────────────────────
INSERT INTO teams (name, description, project_team, created_at, updated_at) VALUES
  ('개발1팀',   '시스템 개발 1팀',     TRUE,  NOW(), NOW()),
  ('개발2팀',   '시스템 개발 2팀',     TRUE,  NOW(), NOW()),
  ('개발3팀',   '시스템 개발 3팀',     TRUE,  NOW(), NOW()),
  ('인프라팀',  '인프라·클라우드팀',   TRUE,  NOW(), NOW()),
  ('QA팀',      '품질보증팀',          TRUE,  NOW(), NOW()),
  ('PM팀',      '프로젝트 관리팀',     TRUE,  NOW(), NOW()),
  ('영업팀',    '영업·제안팀',         TRUE,  NOW(), NOW()),
  ('영업본부',  '영업 총괄 본부',      FALSE, NOW(), NOW()),
  ('경영본부',  '경영 총괄 본부',      FALSE, NOW(), NOW()),
  ('경영지원팀','경영지원·인사팀',     FALSE, NOW(), NOW());

-- ─── 2. 직원 100명 ───────────────────────────────────────────
-- 비밀번호 해시: Test1234!
INSERT INTO users
  (login_id, password, name, email, address, join_date, team_id, position,
   birth_date, phone, role, status, email_verified,
   privacy_consent_at, privacy_consent_version, created_at, updated_at)
VALUES
-- ── 개발1팀 (16명) ──
('minjun.kim@eactive.co.kr',   '$2a$10$m1ejPokJV2BIVL5z7xf4.uE5TwpE4UGTldIHi2UUioWLOQ/AmvJN6','김민준','minjun.kim@eactive.co.kr',   '서울시 강남구 역삼동','2010-05-03',(SELECT id FROM teams WHERE name='개발1팀'),'GENERAL_MANAGER',      '1980-03-12','010-2341-5871','EMPLOYEE','ACTIVE',TRUE,'2023-01-02 09:00:00','1.0',NOW(),NOW()),
('junho.lee@eactive.co.kr',    '$2a$10$m1ejPokJV2BIVL5z7xf4.uE5TwpE4UGTldIHi2UUioWLOQ/AmvJN6','이준호','junho.lee@eactive.co.kr',    '서울시 서초구 방배동','2013-03-04',(SELECT id FROM teams WHERE name='개발1팀'),'DEPUTY_GENERAL_MANAGER','1983-07-21','010-3452-6912','EMPLOYEE','ACTIVE',TRUE,'2023-01-02 09:00:00','1.0',NOW(),NOW()),
('jihun.park@eactive.co.kr',   '$2a$10$m1ejPokJV2BIVL5z7xf4.uE5TwpE4UGTldIHi2UUioWLOQ/AmvJN6','박지훈','jihun.park@eactive.co.kr',   '경기도 성남시 분당구','2016-07-11',(SELECT id FROM teams WHERE name='개발1팀'),'MANAGER',              '1987-11-05','010-4563-7823','EMPLOYEE','ACTIVE',TRUE,'2023-01-02 09:00:00','1.0',NOW(),NOW()),
('jaewon.jung@eactive.co.kr',  '$2a$10$m1ejPokJV2BIVL5z7xf4.uE5TwpE4UGTldIHi2UUioWLOQ/AmvJN6','정재원','jaewon.jung@eactive.co.kr',  '서울시 마포구 합정동','2017-03-06',(SELECT id FROM teams WHERE name='개발1팀'),'MANAGER',              '1988-04-18','010-5674-8134','EMPLOYEE','ACTIVE',TRUE,'2023-01-02 09:00:00','1.0',NOW(),NOW()),
('donghun.kang@eactive.co.kr', '$2a$10$m1ejPokJV2BIVL5z7xf4.uE5TwpE4UGTldIHi2UUioWLOQ/AmvJN6','강동훈','donghun.kang@eactive.co.kr', '서울시 강서구 화곡동','2019-09-02',(SELECT id FROM teams WHERE name='개발1팀'),'ASSISTANT_MANAGER',     '1991-09-30','010-6785-9245','EMPLOYEE','ACTIVE',TRUE,'2023-01-02 09:00:00','1.0',NOW(),NOW()),
('sunghyun.jo@eactive.co.kr',  '$2a$10$m1ejPokJV2BIVL5z7xf4.uE5TwpE4UGTldIHi2UUioWLOQ/AmvJN6','조성현','sunghyun.jo@eactive.co.kr',  '경기도 수원시 영통구','2020-01-06',(SELECT id FROM teams WHERE name='개발1팀'),'ASSISTANT_MANAGER',     '1992-02-14','010-7896-1356','EMPLOYEE','ACTIVE',TRUE,'2023-01-02 09:00:00','1.0',NOW(),NOW()),
('jaeho.yun@eactive.co.kr',    '$2a$10$m1ejPokJV2BIVL5z7xf4.uE5TwpE4UGTldIHi2UUioWLOQ/AmvJN6','윤재호','jaeho.yun@eactive.co.kr',    '서울시 송파구 잠실동','2021-03-02',(SELECT id FROM teams WHERE name='개발1팀'),'ASSISTANT_MANAGER',     '1993-06-22','010-8907-2467','EMPLOYEE','ACTIVE',TRUE,'2023-01-02 09:00:00','1.0',NOW(),NOW()),
('minhyuk.jang@eactive.co.kr', '$2a$10$m1ejPokJV2BIVL5z7xf4.uE5TwpE4UGTldIHi2UUioWLOQ/AmvJN6','장민혁','minhyuk.jang@eactive.co.kr', '인천시 남동구 구월동','2022-07-04',(SELECT id FROM teams WHERE name='개발1팀'),'STAFF',                 '1995-12-08','010-9018-3578','EMPLOYEE','ACTIVE',TRUE,'2023-01-02 09:00:00','1.0',NOW(),NOW()),
('jungho.lim@eactive.co.kr',   '$2a$10$m1ejPokJV2BIVL5z7xf4.uE5TwpE4UGTldIHi2UUioWLOQ/AmvJN6','임정호','jungho.lim@eactive.co.kr',   '경기도 용인시 수지구','2023-03-06',(SELECT id FROM teams WHERE name='개발1팀'),'STAFF',                 '1997-03-17','010-1129-4689','EMPLOYEE','ACTIVE',TRUE,'2023-03-06 09:00:00','1.0',NOW(),NOW()),
('junsu.han@eactive.co.kr',    '$2a$10$m1ejPokJV2BIVL5z7xf4.uE5TwpE4UGTldIHi2UUioWLOQ/AmvJN6','한준서','junsu.han@eactive.co.kr',    '서울시 노원구 공릉동','2023-07-10',(SELECT id FROM teams WHERE name='개발1팀'),'STAFF',                 '1998-08-25','010-2230-5790','EMPLOYEE','ACTIVE',TRUE,'2023-07-10 09:00:00','1.0',NOW(),NOW()),
('seongmin.oh@eactive.co.kr',  '$2a$10$m1ejPokJV2BIVL5z7xf4.uE5TwpE4UGTldIHi2UUioWLOQ/AmvJN6','오성민','seongmin.oh@eactive.co.kr',  '경기도 고양시 일산서구','2024-01-08',(SELECT id FROM teams WHERE name='개발1팀'),'STAFF',                '1999-01-30','010-3341-6801','EMPLOYEE','ACTIVE',TRUE,'2024-01-08 09:00:00','1.0',NOW(),NOW()),
('jaewon.seo@eactive.co.kr',   '$2a$10$m1ejPokJV2BIVL5z7xf4.uE5TwpE4UGTldIHi2UUioWLOQ/AmvJN6','서재원','jaewon.seo@eactive.co.kr',   '서울시 영등포구 당산동','2024-07-01',(SELECT id FROM teams WHERE name='개발1팀'),'STAFF',               '1999-07-11','010-4452-7912','EMPLOYEE','ACTIVE',TRUE,'2024-07-01 09:00:00','1.0',NOW(),NOW()),
('seoyeon.lee@eactive.co.kr',  '$2a$10$m1ejPokJV2BIVL5z7xf4.uE5TwpE4UGTldIHi2UUioWLOQ/AmvJN6','이서연','seoyeon.lee@eactive.co.kr',  '서울시 강남구 논현동','2017-09-04',(SELECT id FROM teams WHERE name='개발1팀'),'MANAGER',               '1989-05-03','010-5563-8023','EMPLOYEE','ACTIVE',TRUE,'2023-01-02 09:00:00','1.0',NOW(),NOW()),
('suhyun.choi@eactive.co.kr',  '$2a$10$m1ejPokJV2BIVL5z7xf4.uE5TwpE4UGTldIHi2UUioWLOQ/AmvJN6','최수현','suhyun.choi@eactive.co.kr',  '경기도 성남시 수정구','2020-07-06',(SELECT id FROM teams WHERE name='개발1팀'),'ASSISTANT_MANAGER',     '1993-10-19','010-6674-9134','EMPLOYEE','ACTIVE',TRUE,'2023-01-02 09:00:00','1.0',NOW(),NOW()),
('jieun.jung@eactive.co.kr',   '$2a$10$m1ejPokJV2BIVL5z7xf4.uE5TwpE4UGTldIHi2UUioWLOQ/AmvJN6','정지은','jieun.jung@eactive.co.kr',   '서울시 동작구 사당동','2022-03-07',(SELECT id FROM teams WHERE name='개발1팀'),'STAFF',                 '1996-04-27','010-7785-1245','EMPLOYEE','ACTIVE',TRUE,'2023-01-02 09:00:00','1.0',NOW(),NOW()),
('suji.han@eactive.co.kr',     '$2a$10$m1ejPokJV2BIVL5z7xf4.uE5TwpE4UGTldIHi2UUioWLOQ/AmvJN6','한수지','suji.han@eactive.co.kr',     '서울시 관악구 봉천동','2023-01-09',(SELECT id FROM teams WHERE name='개발1팀'),'STAFF',                 '1997-09-14','010-8896-2356','EMPLOYEE','ACTIVE',TRUE,'2023-01-09 09:00:00','1.0',NOW(),NOW()),

-- ── 개발2팀 (14명) ──
('donghyun.shin@eactive.co.kr','$2a$10$m1ejPokJV2BIVL5z7xf4.uE5TwpE4UGTldIHi2UUioWLOQ/AmvJN6','신동현','donghyun.shin@eactive.co.kr','서울시 강북구 수유동','2011-03-07',(SELECT id FROM teams WHERE name='개발2팀'),'GENERAL_MANAGER',       '1981-06-09','010-9907-3467','EMPLOYEE','ACTIVE',TRUE,'2023-01-02 09:00:00','1.0',NOW(),NOW()),
('minjun.kwon@eactive.co.kr',  '$2a$10$m1ejPokJV2BIVL5z7xf4.uE5TwpE4UGTldIHi2UUioWLOQ/AmvJN6','권민준','minjun.kwon@eactive.co.kr',  '경기도 안양시 동안구','2014-01-06',(SELECT id FROM teams WHERE name='개발2팀'),'DEPUTY_GENERAL_MANAGER','1984-02-28','010-1018-4578','EMPLOYEE','ACTIVE',TRUE,'2023-01-02 09:00:00','1.0',NOW(),NOW()),
('jungho.hwang@eactive.co.kr', '$2a$10$m1ejPokJV2BIVL5z7xf4.uE5TwpE4UGTldIHi2UUioWLOQ/AmvJN6','황정호','jungho.hwang@eactive.co.kr', '서울시 광진구 구의동','2017-07-03',(SELECT id FROM teams WHERE name='개발2팀'),'MANAGER',               '1988-11-16','010-2129-5689','EMPLOYEE','ACTIVE',TRUE,'2023-01-02 09:00:00','1.0',NOW(),NOW()),
('jaehyun.ahn@eactive.co.kr',  '$2a$10$m1ejPokJV2BIVL5z7xf4.uE5TwpE4UGTldIHi2UUioWLOQ/AmvJN6','안재현','jaehyun.ahn@eactive.co.kr',  '경기도 부천시 원미구','2018-01-08',(SELECT id FROM teams WHERE name='개발2팀'),'MANAGER',               '1989-08-04','010-3230-6790','EMPLOYEE','ACTIVE',TRUE,'2023-01-02 09:00:00','1.0',NOW(),NOW()),
('minsu.song@eactive.co.kr',   '$2a$10$m1ejPokJV2BIVL5z7xf4.uE5TwpE4UGTldIHi2UUioWLOQ/AmvJN6','송민수','minsu.song@eactive.co.kr',   '서울시 성북구 길음동','2020-03-02',(SELECT id FROM teams WHERE name='개발2팀'),'ASSISTANT_MANAGER',     '1992-05-21','010-4341-7801','EMPLOYEE','ACTIVE',TRUE,'2023-01-02 09:00:00','1.0',NOW(),NOW()),
('jaeho.ryu@eactive.co.kr',    '$2a$10$m1ejPokJV2BIVL5z7xf4.uE5TwpE4UGTldIHi2UUioWLOQ/AmvJN6','류재호','jaeho.ryu@eactive.co.kr',    '경기도 화성시 동탄','2021-01-04',(SELECT id FROM teams WHERE name='개발2팀'),'ASSISTANT_MANAGER',     '1993-12-07','010-5452-8912','EMPLOYEE','ACTIVE',TRUE,'2023-01-02 09:00:00','1.0',NOW(),NOW()),
('seongmin.jeon@eactive.co.kr','$2a$10$m1ejPokJV2BIVL5z7xf4.uE5TwpE4UGTldIHi2UUioWLOQ/AmvJN6','전성민','seongmin.jeon@eactive.co.kr','서울시 중랑구 면목동','2022-07-11',(SELECT id FROM teams WHERE name='개발2팀'),'STAFF',                 '1995-07-13','010-6563-9023','EMPLOYEE','ACTIVE',TRUE,'2023-01-02 09:00:00','1.0',NOW(),NOW()),
('jaewon.hong@eactive.co.kr',  '$2a$10$m1ejPokJV2BIVL5z7xf4.uE5TwpE4UGTldIHi2UUioWLOQ/AmvJN6','홍재원','jaewon.hong@eactive.co.kr',  '경기도 시흥시 정왕동','2023-03-06',(SELECT id FROM teams WHERE name='개발2팀'),'STAFF',                 '1997-01-22','010-7674-1134','EMPLOYEE','ACTIVE',TRUE,'2023-03-06 09:00:00','1.0',NOW(),NOW()),
('minjun.go@eactive.co.kr',    '$2a$10$m1ejPokJV2BIVL5z7xf4.uE5TwpE4UGTldIHi2UUioWLOQ/AmvJN6','고민준','minjun.go@eactive.co.kr',    '서울시 강동구 천호동','2023-09-04',(SELECT id FROM teams WHERE name='개발2팀'),'STAFF',                 '1998-05-31','010-8785-2245','EMPLOYEE','ACTIVE',TRUE,'2023-09-04 09:00:00','1.0',NOW(),NOW()),
('junghun.moon@eactive.co.kr', '$2a$10$m1ejPokJV2BIVL5z7xf4.uE5TwpE4UGTldIHi2UUioWLOQ/AmvJN6','문정훈','junghun.moon@eactive.co.kr', '경기도 군포시 산본동','2024-01-02',(SELECT id FROM teams WHERE name='개발2팀'),'STAFF',                 '1999-10-18','010-9896-3356','EMPLOYEE','ACTIVE',TRUE,'2024-01-02 09:00:00','1.0',NOW(),NOW()),
('jihyun.oh@eactive.co.kr',    '$2a$10$m1ejPokJV2BIVL5z7xf4.uE5TwpE4UGTldIHi2UUioWLOQ/AmvJN6','오지현','jihyun.oh@eactive.co.kr',    '서울시 서대문구 홍제동','2015-07-06',(SELECT id FROM teams WHERE name='개발2팀'),'DEPUTY_GENERAL_MANAGER','1986-03-14','010-1007-4467','EMPLOYEE','ACTIVE',TRUE,'2023-01-02 09:00:00','1.0',NOW(),NOW()),
('minji.seo@eactive.co.kr',    '$2a$10$m1ejPokJV2BIVL5z7xf4.uE5TwpE4UGTldIHi2UUioWLOQ/AmvJN6','서민지','minji.seo@eactive.co.kr',    '경기도 의왕시 내손동','2020-09-07',(SELECT id FROM teams WHERE name='개발2팀'),'ASSISTANT_MANAGER',     '1993-08-26','010-2118-5578','EMPLOYEE','ACTIVE',TRUE,'2023-01-02 09:00:00','1.0',NOW(),NOW()),
('suyeon.shin@eactive.co.kr',  '$2a$10$m1ejPokJV2BIVL5z7xf4.uE5TwpE4UGTldIHi2UUioWLOQ/AmvJN6','신수연','suyeon.shin@eactive.co.kr',  '서울시 양천구 목동','2022-01-10',(SELECT id FROM teams WHERE name='개발2팀'),'STAFF',                 '1995-11-09','010-3229-6689','EMPLOYEE','ACTIVE',TRUE,'2023-01-02 09:00:00','1.0',NOW(),NOW()),
('jiwon.kwon@eactive.co.kr',   '$2a$10$m1ejPokJV2BIVL5z7xf4.uE5TwpE4UGTldIHi2UUioWLOQ/AmvJN6','권지원','jiwon.kwon@eactive.co.kr',   '경기도 파주시 운정','2023-07-03',(SELECT id FROM teams WHERE name='개발2팀'),'STAFF',                 '1998-02-17','010-4330-7790','EMPLOYEE','ACTIVE',TRUE,'2023-07-03 09:00:00','1.0',NOW(),NOW()),

-- ── 개발3팀 (12명) ──
('jaeho.yang@eactive.co.kr',   '$2a$10$m1ejPokJV2BIVL5z7xf4.uE5TwpE4UGTldIHi2UUioWLOQ/AmvJN6','양재호','jaeho.yang@eactive.co.kr',   '서울시 은평구 녹번동','2012-01-02',(SELECT id FROM teams WHERE name='개발3팀'),'GENERAL_MANAGER',       '1982-08-23','010-5441-8801','EMPLOYEE','ACTIVE',TRUE,'2023-01-02 09:00:00','1.0',NOW(),NOW()),
('minhyuk.son@eactive.co.kr',  '$2a$10$m1ejPokJV2BIVL5z7xf4.uE5TwpE4UGTldIHi2UUioWLOQ/AmvJN6','손민혁','minhyuk.son@eactive.co.kr',  '경기도 남양주시 별내','2015-03-02',(SELECT id FROM teams WHERE name='개발3팀'),'DEPUTY_GENERAL_MANAGER','1985-04-06','010-6552-9912','EMPLOYEE','ACTIVE',TRUE,'2023-01-02 09:00:00','1.0',NOW(),NOW()),
('sungjun.bae@eactive.co.kr',  '$2a$10$m1ejPokJV2BIVL5z7xf4.uE5TwpE4UGTldIHi2UUioWLOQ/AmvJN6','배성준','sungjun.bae@eactive.co.kr',  '서울시 도봉구 방학동','2018-07-02',(SELECT id FROM teams WHERE name='개발3팀'),'MANAGER',               '1989-12-11','010-7663-1023','EMPLOYEE','ACTIVE',TRUE,'2023-01-02 09:00:00','1.0',NOW(),NOW()),
('jaewon.baek@eactive.co.kr',  '$2a$10$m1ejPokJV2BIVL5z7xf4.uE5TwpE4UGTldIHi2UUioWLOQ/AmvJN6','백재원','jaewon.baek@eactive.co.kr',  '경기도 의정부시 가능동','2021-01-04',(SELECT id FROM teams WHERE name='개발3팀'),'ASSISTANT_MANAGER',    '1993-03-29','010-8774-2134','EMPLOYEE','ACTIVE',TRUE,'2023-01-02 09:00:00','1.0',NOW(),NOW()),
('minjun.heo@eactive.co.kr',   '$2a$10$m1ejPokJV2BIVL5z7xf4.uE5TwpE4UGTldIHi2UUioWLOQ/AmvJN6','허민준','minjun.heo@eactive.co.kr',   '서울시 성동구 금호동','2021-07-05',(SELECT id FROM teams WHERE name='개발3팀'),'ASSISTANT_MANAGER',     '1994-10-15','010-9885-3245','EMPLOYEE','ACTIVE',TRUE,'2023-01-02 09:00:00','1.0',NOW(),NOW()),
('jungho.yu@eactive.co.kr',    '$2a$10$m1ejPokJV2BIVL5z7xf4.uE5TwpE4UGTldIHi2UUioWLOQ/AmvJN6','유정호','jungho.yu@eactive.co.kr',    '경기도 광명시 하안동','2022-09-05',(SELECT id FROM teams WHERE name='개발3팀'),'STAFF',                 '1996-06-03','010-1096-4356','EMPLOYEE','ACTIVE',TRUE,'2023-01-02 09:00:00','1.0',NOW(),NOW()),
('sunghyun.nam@eactive.co.kr', '$2a$10$m1ejPokJV2BIVL5z7xf4.uE5TwpE4UGTldIHi2UUioWLOQ/AmvJN6','남성현','sunghyun.nam@eactive.co.kr', '서울시 구로구 오류동','2023-01-09',(SELECT id FROM teams WHERE name='개발3팀'),'STAFF',                 '1997-11-20','010-2107-5467','EMPLOYEE','ACTIVE',TRUE,'2023-01-09 09:00:00','1.0',NOW(),NOW()),
('taehun.kim2@eactive.co.kr',  '$2a$10$m1ejPokJV2BIVL5z7xf4.uE5TwpE4UGTldIHi2UUioWLOQ/AmvJN6','김태훈','taehun.kim2@eactive.co.kr',  '경기도 평택시 서정동','2023-07-10',(SELECT id FROM teams WHERE name='개발3팀'),'STAFF',                 '1998-04-08','010-3218-6578','EMPLOYEE','ACTIVE',TRUE,'2023-07-10 09:00:00','1.0',NOW(),NOW()),
('mihyun.hwang@eactive.co.kr', '$2a$10$m1ejPokJV2BIVL5z7xf4.uE5TwpE4UGTldIHi2UUioWLOQ/AmvJN6','황미현','mihyun.hwang@eactive.co.kr', '서울시 금천구 시흥동','2018-01-08',(SELECT id FROM teams WHERE name='개발3팀'),'MANAGER',               '1989-07-17','010-4329-7689','EMPLOYEE','ACTIVE',TRUE,'2023-01-02 09:00:00','1.0',NOW(),NOW()),
('jisu.ahn@eactive.co.kr',     '$2a$10$m1ejPokJV2BIVL5z7xf4.uE5TwpE4UGTldIHi2UUioWLOQ/AmvJN6','안지수','jisu.ahn@eactive.co.kr',     '경기도 하남시 미사강변','2020-07-06',(SELECT id FROM teams WHERE name='개발3팀'),'ASSISTANT_MANAGER',    '1992-09-24','010-5430-8790','EMPLOYEE','ACTIVE',TRUE,'2023-01-02 09:00:00','1.0',NOW(),NOW()),
('suyeon.song@eactive.co.kr',  '$2a$10$m1ejPokJV2BIVL5z7xf4.uE5TwpE4UGTldIHi2UUioWLOQ/AmvJN6','송수연','suyeon.song@eactive.co.kr',  '서울시 강남구 대치동','2022-03-07',(SELECT id FROM teams WHERE name='개발3팀'),'STAFF',                 '1995-02-13','010-6541-9801','EMPLOYEE','ACTIVE',TRUE,'2023-01-02 09:00:00','1.0',NOW(),NOW()),
('jiwon.ryu@eactive.co.kr',    '$2a$10$m1ejPokJV2BIVL5z7xf4.uE5TwpE4UGTldIHi2UUioWLOQ/AmvJN6','류지원','jiwon.ryu@eactive.co.kr',    '경기도 용인시 기흥구','2024-01-08',(SELECT id FROM teams WHERE name='개발3팀'),'STAFF',                 '1999-08-02','010-7652-1912','EMPLOYEE','ACTIVE',TRUE,'2024-01-08 09:00:00','1.0',NOW(),NOW()),

-- ── 인프라팀 (10명) ──
('minho.lee@eactive.co.kr',    '$2a$10$m1ejPokJV2BIVL5z7xf4.uE5TwpE4UGTldIHi2UUioWLOQ/AmvJN6','이민호','minho.lee@eactive.co.kr',    '서울시 강남구 삼성동','2014-07-07',(SELECT id FROM teams WHERE name='인프라팀'),'DEPUTY_GENERAL_MANAGER','1984-05-31','010-8763-2023','EMPLOYEE','ACTIVE',TRUE,'2023-01-02 09:00:00','1.0',NOW(),NOW()),
('sungjun.park@eactive.co.kr', '$2a$10$m1ejPokJV2BIVL5z7xf4.uE5TwpE4UGTldIHi2UUioWLOQ/AmvJN6','박성준','sungjun.park@eactive.co.kr', '경기도 성남시 중원구','2017-03-06',(SELECT id FROM teams WHERE name='인프라팀'),'MANAGER',               '1987-09-19','010-9874-3134','EMPLOYEE','ACTIVE',TRUE,'2023-01-02 09:00:00','1.0',NOW(),NOW()),
('jaewon.choi@eactive.co.kr',  '$2a$10$m1ejPokJV2BIVL5z7xf4.uE5TwpE4UGTldIHi2UUioWLOQ/AmvJN6','최재원','jaewon.choi@eactive.co.kr',  '서울시 서초구 잠원동','2018-07-02',(SELECT id FROM teams WHERE name='인프라팀'),'MANAGER',               '1989-04-26','010-1085-4245','EMPLOYEE','ACTIVE',TRUE,'2023-01-02 09:00:00','1.0',NOW(),NOW()),
('minjun.jung@eactive.co.kr',  '$2a$10$m1ejPokJV2BIVL5z7xf4.uE5TwpE4UGTldIHi2UUioWLOQ/AmvJN6','정민준','minjun.jung@eactive.co.kr',  '경기도 안산시 단원구','2020-09-07',(SELECT id FROM teams WHERE name='인프라팀'),'ASSISTANT_MANAGER',     '1992-12-08','010-2196-5356','EMPLOYEE','ACTIVE',TRUE,'2023-01-02 09:00:00','1.0',NOW(),NOW()),
('jaeho.kang@eactive.co.kr',   '$2a$10$m1ejPokJV2BIVL5z7xf4.uE5TwpE4UGTldIHi2UUioWLOQ/AmvJN6','강재호','jaeho.kang@eactive.co.kr',   '서울시 마포구 서교동','2021-07-05',(SELECT id FROM teams WHERE name='인프라팀'),'ASSISTANT_MANAGER',     '1994-07-14','010-3207-6467','EMPLOYEE','ACTIVE',TRUE,'2023-01-02 09:00:00','1.0',NOW(),NOW()),
('minhyuk.jo@eactive.co.kr',   '$2a$10$m1ejPokJV2BIVL5z7xf4.uE5TwpE4UGTldIHi2UUioWLOQ/AmvJN6','조민혁','minhyuk.jo@eactive.co.kr',   '경기도 오산시 원동','2022-07-11',(SELECT id FROM teams WHERE name='인프라팀'),'STAFF',                 '1996-03-28','010-4318-7578','EMPLOYEE','ACTIVE',TRUE,'2023-01-02 09:00:00','1.0',NOW(),NOW()),
('sunghyun.yun@eactive.co.kr', '$2a$10$m1ejPokJV2BIVL5z7xf4.uE5TwpE4UGTldIHi2UUioWLOQ/AmvJN6','윤성현','sunghyun.yun@eactive.co.kr', '서울시 강북구 번동','2023-03-06',(SELECT id FROM teams WHERE name='인프라팀'),'STAFF',                 '1997-10-05','010-5429-8689','EMPLOYEE','ACTIVE',TRUE,'2023-03-06 09:00:00','1.0',NOW(),NOW()),
('mihyun.jeon@eactive.co.kr',  '$2a$10$m1ejPokJV2BIVL5z7xf4.uE5TwpE4UGTldIHi2UUioWLOQ/AmvJN6','전미현','mihyun.jeon@eactive.co.kr',  '경기도 구리시 인창동','2018-03-05',(SELECT id FROM teams WHERE name='인프라팀'),'MANAGER',               '1989-01-15','010-6530-9790','EMPLOYEE','ACTIVE',TRUE,'2023-01-02 09:00:00','1.0',NOW(),NOW()),
('suyeon.hong@eactive.co.kr',  '$2a$10$m1ejPokJV2BIVL5z7xf4.uE5TwpE4UGTldIHi2UUioWLOQ/AmvJN6','홍수연','suyeon.hong@eactive.co.kr',  '서울시 용산구 이태원동','2021-01-04',(SELECT id FROM teams WHERE name='인프라팀'),'ASSISTANT_MANAGER',   '1993-05-22','010-7641-1801','EMPLOYEE','ACTIVE',TRUE,'2023-01-02 09:00:00','1.0',NOW(),NOW()),
('jihyun.go@eactive.co.kr',    '$2a$10$m1ejPokJV2BIVL5z7xf4.uE5TwpE4UGTldIHi2UUioWLOQ/AmvJN6','고지현','jihyun.go@eactive.co.kr',    '경기도 수원시 권선구','2023-09-04',(SELECT id FROM teams WHERE name='인프라팀'),'STAFF',                 '1998-12-01','010-8752-2912','EMPLOYEE','ACTIVE',TRUE,'2023-09-04 09:00:00','1.0',NOW(),NOW()),

-- ── QA팀 (8명) ──
('jaeho.jang@eactive.co.kr',   '$2a$10$m1ejPokJV2BIVL5z7xf4.uE5TwpE4UGTldIHi2UUioWLOQ/AmvJN6','장재호','jaeho.jang@eactive.co.kr',   '서울시 성동구 왕십리','2015-01-05',(SELECT id FROM teams WHERE name='QA팀'),'DEPUTY_GENERAL_MANAGER','1985-07-07','010-9863-3023','EMPLOYEE','ACTIVE',TRUE,'2023-01-02 09:00:00','1.0',NOW(),NOW()),
('minjun.lim@eactive.co.kr',   '$2a$10$m1ejPokJV2BIVL5z7xf4.uE5TwpE4UGTldIHi2UUioWLOQ/AmvJN6','임민준','minjun.lim@eactive.co.kr',   '경기도 김포시 걸포동','2018-09-03',(SELECT id FROM teams WHERE name='QA팀'),'MANAGER',               '1989-03-24','010-1074-4134','EMPLOYEE','ACTIVE',TRUE,'2023-01-02 09:00:00','1.0',NOW(),NOW()),
('suji.moon@eactive.co.kr',    '$2a$10$m1ejPokJV2BIVL5z7xf4.uE5TwpE4UGTldIHi2UUioWLOQ/AmvJN6','문수지','suji.moon@eactive.co.kr',    '서울시 중구 필동','2017-07-03',(SELECT id FROM teams WHERE name='QA팀'),'MANAGER',               '1988-10-11','010-2185-5245','EMPLOYEE','ACTIVE',TRUE,'2023-01-02 09:00:00','1.0',NOW(),NOW()),
('mihyun.yang@eactive.co.kr',  '$2a$10$m1ejPokJV2BIVL5z7xf4.uE5TwpE4UGTldIHi2UUioWLOQ/AmvJN6','양미현','mihyun.yang@eactive.co.kr',  '경기도 부천시 소사구','2020-03-02',(SELECT id FROM teams WHERE name='QA팀'),'ASSISTANT_MANAGER',     '1992-06-30','010-3296-6356','EMPLOYEE','ACTIVE',TRUE,'2023-01-02 09:00:00','1.0',NOW(),NOW()),
('jisu.son@eactive.co.kr',     '$2a$10$m1ejPokJV2BIVL5z7xf4.uE5TwpE4UGTldIHi2UUioWLOQ/AmvJN6','손지수','jisu.son@eactive.co.kr',     '서울시 동대문구 이문동','2021-09-06',(SELECT id FROM teams WHERE name='QA팀'),'ASSISTANT_MANAGER',   '1994-01-18','010-4307-7467','EMPLOYEE','ACTIVE',TRUE,'2023-01-02 09:00:00','1.0',NOW(),NOW()),
('suyeon.bae@eactive.co.kr',   '$2a$10$m1ejPokJV2BIVL5z7xf4.uE5TwpE4UGTldIHi2UUioWLOQ/AmvJN6','배수연','suyeon.bae@eactive.co.kr',   '경기도 이천시 부발읍','2022-07-04',(SELECT id FROM teams WHERE name='QA팀'),'STAFF',                 '1995-09-05','010-5418-8578','EMPLOYEE','ACTIVE',TRUE,'2023-01-02 09:00:00','1.0',NOW(),NOW()),
('jiwon.baek@eactive.co.kr',   '$2a$10$m1ejPokJV2BIVL5z7xf4.uE5TwpE4UGTldIHi2UUioWLOQ/AmvJN6','백지원','jiwon.baek@eactive.co.kr',   '서울시 노원구 상계동','2023-01-09',(SELECT id FROM teams WHERE name='QA팀'),'STAFF',                 '1997-04-23','010-6529-9689','EMPLOYEE','ACTIVE',TRUE,'2023-01-09 09:00:00','1.0',NOW(),NOW()),
('mihyun.heo@eactive.co.kr',   '$2a$10$m1ejPokJV2BIVL5z7xf4.uE5TwpE4UGTldIHi2UUioWLOQ/AmvJN6','허미현','mihyun.heo@eactive.co.kr',   '경기도 성남시 판교','2024-03-04',(SELECT id FROM teams WHERE name='QA팀'),'STAFF',                 '1999-11-10','010-7630-1790','EMPLOYEE','ACTIVE',TRUE,'2024-03-04 09:00:00','1.0',NOW(),NOW()),

-- ── PM팀 (8명) ──
('jaewon.yu@eactive.co.kr',    '$2a$10$m1ejPokJV2BIVL5z7xf4.uE5TwpE4UGTldIHi2UUioWLOQ/AmvJN6','유재원','jaewon.yu@eactive.co.kr',    '서울시 강남구 개포동','2010-09-06',(SELECT id FROM teams WHERE name='PM팀'),'GENERAL_MANAGER',        '1980-11-28','010-8741-2801','EMPLOYEE','ACTIVE',TRUE,'2023-01-02 09:00:00','1.0',NOW(),NOW()),
('jaeho.nam@eactive.co.kr',    '$2a$10$m1ejPokJV2BIVL5z7xf4.uE5TwpE4UGTldIHi2UUioWLOQ/AmvJN6','남재호','jaeho.nam@eactive.co.kr',    '경기도 과천시 별양동','2013-07-01',(SELECT id FROM teams WHERE name='PM팀'),'DEPUTY_GENERAL_MANAGER', '1983-08-15','010-9852-3912','EMPLOYEE','ACTIVE',TRUE,'2023-01-02 09:00:00','1.0',NOW(),NOW()),
('jieun.kim@eactive.co.kr',    '$2a$10$m1ejPokJV2BIVL5z7xf4.uE5TwpE4UGTldIHi2UUioWLOQ/AmvJN6','김지은','jieun.kim@eactive.co.kr',    '서울시 서초구 반포동','2017-09-04',(SELECT id FROM teams WHERE name='PM팀'),'MANAGER',                '1988-06-02','010-1063-4023','EMPLOYEE','ACTIVE',TRUE,'2023-01-02 09:00:00','1.0',NOW(),NOW()),
('suhyun.park@eactive.co.kr',  '$2a$10$m1ejPokJV2BIVL5z7xf4.uE5TwpE4UGTldIHi2UUioWLOQ/AmvJN6','박수현','suhyun.park@eactive.co.kr',  '경기도 성남시 수정구','2018-03-05',(SELECT id FROM teams WHERE name='PM팀'),'MANAGER',                '1989-02-20','010-2174-5134','EMPLOYEE','ACTIVE',TRUE,'2023-01-02 09:00:00','1.0',NOW(),NOW()),
('mihyun.lee@eactive.co.kr',   '$2a$10$m1ejPokJV2BIVL5z7xf4.uE5TwpE4UGTldIHi2UUioWLOQ/AmvJN6','이미현','mihyun.lee@eactive.co.kr',   '서울시 양천구 신정동','2020-07-06',(SELECT id FROM teams WHERE name='PM팀'),'ASSISTANT_MANAGER',      '1992-11-07','010-3285-6245','EMPLOYEE','ACTIVE',TRUE,'2023-01-02 09:00:00','1.0',NOW(),NOW()),
('jisu.choi@eactive.co.kr',    '$2a$10$m1ejPokJV2BIVL5z7xf4.uE5TwpE4UGTldIHi2UUioWLOQ/AmvJN6','최지수','jisu.choi@eactive.co.kr',    '경기도 고양시 덕양구','2021-03-02',(SELECT id FROM teams WHERE name='PM팀'),'ASSISTANT_MANAGER',      '1994-04-14','010-4396-7356','EMPLOYEE','ACTIVE',TRUE,'2023-01-02 09:00:00','1.0',NOW(),NOW()),
('suyeon.jung@eactive.co.kr',  '$2a$10$m1ejPokJV2BIVL5z7xf4.uE5TwpE4UGTldIHi2UUioWLOQ/AmvJN6','정수연','suyeon.jung@eactive.co.kr',  '서울시 관악구 신림동','2022-09-05',(SELECT id FROM teams WHERE name='PM팀'),'STAFF',                  '1996-01-31','010-5407-8467','EMPLOYEE','ACTIVE',TRUE,'2023-01-02 09:00:00','1.0',NOW(),NOW()),
('jiwon.kang@eactive.co.kr',   '$2a$10$m1ejPokJV2BIVL5z7xf4.uE5TwpE4UGTldIHi2UUioWLOQ/AmvJN6','강지원','jiwon.kang@eactive.co.kr',   '경기도 수원시 팔달구','2023-07-10',(SELECT id FROM teams WHERE name='PM팀'),'STAFF',                  '1998-09-19','010-6518-9578','EMPLOYEE','ACTIVE',TRUE,'2023-07-10 09:00:00','1.0',NOW(),NOW()),

-- ── 영업팀 (12명, SALES role) ──
('jinseok.jo@eactive.co.kr',   '$2a$10$m1ejPokJV2BIVL5z7xf4.uE5TwpE4UGTldIHi2UUioWLOQ/AmvJN6','조진석','jinseok.jo@eactive.co.kr',   '서울시 강남구 역삼동','2014-03-03',(SELECT id FROM teams WHERE name='영업팀'),'DEPUTY_GENERAL_MANAGER','1984-09-06','010-7629-1689','SALES','ACTIVE',TRUE,'2023-01-02 09:00:00','1.0',NOW(),NOW()),
('taeyoung.yun@eactive.co.kr', '$2a$10$m1ejPokJV2BIVL5z7xf4.uE5TwpE4UGTldIHi2UUioWLOQ/AmvJN6','윤태영','taeyoung.yun@eactive.co.kr', '경기도 성남시 분당구','2017-07-03',(SELECT id FROM teams WHERE name='영업팀'),'MANAGER',              '1988-03-12','010-8730-2790','SALES','ACTIVE',TRUE,'2023-01-02 09:00:00','1.0',NOW(),NOW()),
('hyunwoo.jang@eactive.co.kr', '$2a$10$m1ejPokJV2BIVL5z7xf4.uE5TwpE4UGTldIHi2UUioWLOQ/AmvJN6','장현우','hyunwoo.jang@eactive.co.kr', '서울시 서초구 서초동','2020-01-06',(SELECT id FROM teams WHERE name='영업팀'),'ASSISTANT_MANAGER',     '1992-07-29','010-9841-3801','SALES','ACTIVE',TRUE,'2023-01-02 09:00:00','1.0',NOW(),NOW()),
('junhyuk.lim@eactive.co.kr',  '$2a$10$m1ejPokJV2BIVL5z7xf4.uE5TwpE4UGTldIHi2UUioWLOQ/AmvJN6','임준혁','junhyuk.lim@eactive.co.kr',  '경기도 용인시 처인구','2021-07-05',(SELECT id FROM teams WHERE name='영업팀'),'ASSISTANT_MANAGER',     '1994-02-16','010-1052-4912','SALES','ACTIVE',TRUE,'2023-01-02 09:00:00','1.0',NOW(),NOW()),
('jinho.han@eactive.co.kr',    '$2a$10$m1ejPokJV2BIVL5z7xf4.uE5TwpE4UGTldIHi2UUioWLOQ/AmvJN6','한진호','jinho.han@eactive.co.kr',    '서울시 마포구 망원동','2022-03-07',(SELECT id FROM teams WHERE name='영업팀'),'STAFF',                 '1995-10-03','010-2163-5023','SALES','ACTIVE',TRUE,'2023-01-02 09:00:00','1.0',NOW(),NOW()),
('junsu.oh@eactive.co.kr',     '$2a$10$m1ejPokJV2BIVL5z7xf4.uE5TwpE4UGTldIHi2UUioWLOQ/AmvJN6','오준서','junsu.oh@eactive.co.kr',     '경기도 김포시 장기동','2023-01-09',(SELECT id FROM teams WHERE name='영업팀'),'STAFF',                 '1997-06-21','010-3274-6134','SALES','ACTIVE',TRUE,'2023-01-09 09:00:00','1.0',NOW(),NOW()),
('hyunmin.seo@eactive.co.kr',  '$2a$10$m1ejPokJV2BIVL5z7xf4.uE5TwpE4UGTldIHi2UUioWLOQ/AmvJN6','서현민','hyunmin.seo@eactive.co.kr',  '서울시 강동구 암사동','2023-07-10',(SELECT id FROM teams WHERE name='영업팀'),'STAFF',                 '1998-01-08','010-4385-7245','SALES','ACTIVE',TRUE,'2023-07-10 09:00:00','1.0',NOW(),NOW()),
('jaehyuk.shin@eactive.co.kr', '$2a$10$m1ejPokJV2BIVL5z7xf4.uE5TwpE4UGTldIHi2UUioWLOQ/AmvJN6','신재혁','jaehyuk.shin@eactive.co.kr', '경기도 파주시 금촌동','2024-01-08',(SELECT id FROM teams WHERE name='영업팀'),'STAFF',                 '1999-08-27','010-5496-8356','SALES','ACTIVE',TRUE,'2024-01-08 09:00:00','1.0',NOW(),NOW()),
('miyeon.jo@eactive.co.kr',    '$2a$10$m1ejPokJV2BIVL5z7xf4.uE5TwpE4UGTldIHi2UUioWLOQ/AmvJN6','조미연','miyeon.jo@eactive.co.kr',    '서울시 강남구 청담동','2017-01-09',(SELECT id FROM teams WHERE name='영업팀'),'MANAGER',               '1988-12-15','010-6507-9467','SALES','ACTIVE',TRUE,'2023-01-02 09:00:00','1.0',NOW(),NOW()),
('sohyun.yun@eactive.co.kr',   '$2a$10$m1ejPokJV2BIVL5z7xf4.uE5TwpE4UGTldIHi2UUioWLOQ/AmvJN6','윤소현','sohyun.yun@eactive.co.kr',   '경기도 성남시 야탑동','2020-09-07',(SELECT id FROM teams WHERE name='영업팀'),'ASSISTANT_MANAGER',     '1993-04-09','010-7618-1578','SALES','ACTIVE',TRUE,'2023-01-02 09:00:00','1.0',NOW(),NOW()),
('sujin.jang@eactive.co.kr',   '$2a$10$m1ejPokJV2BIVL5z7xf4.uE5TwpE4UGTldIHi2UUioWLOQ/AmvJN6','장수진','sujin.jang@eactive.co.kr',   '서울시 송파구 가락동','2022-07-11',(SELECT id FROM teams WHERE name='영업팀'),'STAFF',                 '1996-11-26','010-8729-2689','SALES','ACTIVE',TRUE,'2023-01-02 09:00:00','1.0',NOW(),NOW()),
('jiyun.lim@eactive.co.kr',    '$2a$10$m1ejPokJV2BIVL5z7xf4.uE5TwpE4UGTldIHi2UUioWLOQ/AmvJN6','임지윤','jiyun.lim@eactive.co.kr',    '경기도 광주시 오포읍','2023-09-04',(SELECT id FROM teams WHERE name='영업팀'),'STAFF',                 '1998-07-13','010-9830-3790','SALES','ACTIVE',TRUE,'2023-09-04 09:00:00','1.0',NOW(),NOW()),

-- ── 영업본부 (5명, SALES role, 고직급) ──
('hyukjun.kwon@eactive.co.kr', '$2a$10$m1ejPokJV2BIVL5z7xf4.uE5TwpE4UGTldIHi2UUioWLOQ/AmvJN6','권혁준','hyukjun.kwon@eactive.co.kr', '서울시 강남구 신사동','2007-03-05',(SELECT id FROM teams WHERE name='영업본부'),'DIRECTOR',             '1977-05-20','010-1041-4801','SALES','ACTIVE',TRUE,'2023-01-02 09:00:00','1.0',NOW(),NOW()),
('sungchul.hwang@eactive.co.kr','$2a$10$m1ejPokJV2BIVL5z7xf4.uE5TwpE4UGTldIHi2UUioWLOQ/AmvJN6','황성철','sungchul.hwang@eactive.co.kr','서울시 서초구 양재동','2010-01-04',(SELECT id FROM teams WHERE name='영업본부'),'GENERAL_MANAGER',      '1980-02-07','010-2152-5912','SALES','ACTIVE',TRUE,'2023-01-02 09:00:00','1.0',NOW(),NOW()),
('youngsoo.ahn@eactive.co.kr', '$2a$10$m1ejPokJV2BIVL5z7xf4.uE5TwpE4UGTldIHi2UUioWLOQ/AmvJN6','안영수','youngsoo.ahn@eactive.co.kr', '경기도 성남시 수정구','2013-09-02',(SELECT id FROM teams WHERE name='영업본부'),'DEPUTY_GENERAL_MANAGER','1983-11-14','010-3263-6023','SALES','ACTIVE',TRUE,'2023-01-02 09:00:00','1.0',NOW(),NOW()),
('miyoung.choi@eactive.co.kr', '$2a$10$m1ejPokJV2BIVL5z7xf4.uE5TwpE4UGTldIHi2UUioWLOQ/AmvJN6','최미영','miyoung.choi@eactive.co.kr', '서울시 강남구 도곡동','2011-07-04',(SELECT id FROM teams WHERE name='영업본부'),'GENERAL_MANAGER',      '1981-09-01','010-4374-7134','SALES','ACTIVE',TRUE,'2023-01-02 09:00:00','1.0',NOW(),NOW()),
('dahee.song@eactive.co.kr',   '$2a$10$m1ejPokJV2BIVL5z7xf4.uE5TwpE4UGTldIHi2UUioWLOQ/AmvJN6','송다희','dahee.song@eactive.co.kr',   '서울시 서초구 우면동','2014-09-01',(SELECT id FROM teams WHERE name='영업본부'),'DEPUTY_GENERAL_MANAGER','1984-06-18','010-5485-8245','SALES','ACTIVE',TRUE,'2023-01-02 09:00:00','1.0',NOW(),NOW()),

-- ── 경영본부 (5명, EMPLOYEE role, 임원급) ──
('chulsu.lee@eactive.co.kr',   '$2a$10$m1ejPokJV2BIVL5z7xf4.uE5TwpE4UGTldIHi2UUioWLOQ/AmvJN6','이철수','chulsu.lee@eactive.co.kr',   '서울시 강남구 논현동','2004-03-01',(SELECT id FROM teams WHERE name='경영본부'),'EXECUTIVE_DIRECTOR',   '1974-04-25','010-6596-9356','EMPLOYEE','ACTIVE',TRUE,'2023-01-02 09:00:00','1.0',NOW(),NOW()),
('youngho.park@eactive.co.kr', '$2a$10$m1ejPokJV2BIVL5z7xf4.uE5TwpE4UGTldIHi2UUioWLOQ/AmvJN6','박영호','youngho.park@eactive.co.kr', '경기도 성남시 분당구','2006-07-03',(SELECT id FROM teams WHERE name='경영본부'),'MANAGING_DIRECTOR',    '1976-10-12','010-7607-1467','EMPLOYEE','ACTIVE',TRUE,'2023-01-02 09:00:00','1.0',NOW(),NOW()),
('donghun.jung@eactive.co.kr', '$2a$10$m1ejPokJV2BIVL5z7xf4.uE5TwpE4UGTldIHi2UUioWLOQ/AmvJN6','정동훈','donghun.jung@eactive.co.kr', '서울시 서초구 잠원동','2008-09-01',(SELECT id FROM teams WHERE name='경영본부'),'DIRECTOR',             '1978-07-30','010-8718-2578','EMPLOYEE','ACTIVE',TRUE,'2023-01-02 09:00:00','1.0',NOW(),NOW()),
('mirae.kang@eactive.co.kr',   '$2a$10$m1ejPokJV2BIVL5z7xf4.uE5TwpE4UGTldIHi2UUioWLOQ/AmvJN6','강미래','mirae.kang@eactive.co.kr',   '서울시 강남구 삼성동','2009-03-02',(SELECT id FROM teams WHERE name='경영본부'),'DIRECTOR',             '1979-01-17','010-9829-3689','EMPLOYEE','ACTIVE',TRUE,'2023-01-02 09:00:00','1.0',NOW(),NOW()),
('youngran.jo@eactive.co.kr',  '$2a$10$m1ejPokJV2BIVL5z7xf4.uE5TwpE4UGTldIHi2UUioWLOQ/AmvJN6','조영란','youngran.jo@eactive.co.kr',  '경기도 성남시 수정구','2010-07-05',(SELECT id FROM teams WHERE name='경영본부'),'GENERAL_MANAGER',      '1980-08-04','010-1030-4790','EMPLOYEE','ACTIVE',TRUE,'2023-01-02 09:00:00','1.0',NOW(),NOW()),

-- ── 경영지원팀 (6명) ──
('kyungho.yun@eactive.co.kr',  '$2a$10$m1ejPokJV2BIVL5z7xf4.uE5TwpE4UGTldIHi2UUioWLOQ/AmvJN6','윤경호','kyungho.yun@eactive.co.kr',  '서울시 강남구 수서동','2017-03-06',(SELECT id FROM teams WHERE name='경영지원팀'),'MANAGER',           '1988-05-11','010-2141-5801','EMPLOYEE','ACTIVE',TRUE,'2023-01-02 09:00:00','1.0',NOW(),NOW()),
('suhyun.jang@eactive.co.kr',  '$2a$10$m1ejPokJV2BIVL5z7xf4.uE5TwpE4UGTldIHi2UUioWLOQ/AmvJN6','장수현','suhyun.jang@eactive.co.kr',  '경기도 용인시 수지구','2020-01-06',(SELECT id FROM teams WHERE name='경영지원팀'),'ASSISTANT_MANAGER', '1992-08-28','010-3252-6912','EMPLOYEE','ACTIVE',TRUE,'2023-01-02 09:00:00','1.0',NOW(),NOW()),
('nayoung.lim@eactive.co.kr',  '$2a$10$m1ejPokJV2BIVL5z7xf4.uE5TwpE4UGTldIHi2UUioWLOQ/AmvJN6','임나영','nayoung.lim@eactive.co.kr',  '서울시 서초구 서초동','2021-03-02',(SELECT id FROM teams WHERE name='경영지원팀'),'ASSISTANT_MANAGER', '1994-03-15','010-4363-7023','EMPLOYEE','ACTIVE',TRUE,'2023-01-02 09:00:00','1.0',NOW(),NOW()),
('miso.han@eactive.co.kr',     '$2a$10$m1ejPokJV2BIVL5z7xf4.uE5TwpE4UGTldIHi2UUioWLOQ/AmvJN6','한미소','miso.han@eactive.co.kr',     '경기도 안양시 만안구','2022-07-04',(SELECT id FROM teams WHERE name='경영지원팀'),'STAFF',             '1996-12-22','010-5474-8134','EMPLOYEE','ACTIVE',TRUE,'2023-01-02 09:00:00','1.0',NOW(),NOW()),
('soyeon.oh@eactive.co.kr',    '$2a$10$m1ejPokJV2BIVL5z7xf4.uE5TwpE4UGTldIHi2UUioWLOQ/AmvJN6','오소연','soyeon.oh@eactive.co.kr',    '서울시 강남구 일원동','2023-01-09',(SELECT id FROM teams WHERE name='경영지원팀'),'STAFF',             '1997-07-09','010-6485-9245','EMPLOYEE','ACTIVE',TRUE,'2023-01-09 09:00:00','1.0',NOW(),NOW()),
('jinyoung.seo@eactive.co.kr', '$2a$10$m1ejPokJV2BIVL5z7xf4.uE5TwpE4UGTldIHi2UUioWLOQ/AmvJN6','서진영','jinyoung.seo@eactive.co.kr', '경기도 하남시 풍산동','2024-01-08',(SELECT id FROM teams WHERE name='경영지원팀'),'STAFF',             '1999-04-26','010-7496-1356','EMPLOYEE','ACTIVE',TRUE,'2024-01-08 09:00:00','1.0',NOW(),NOW()),

-- ── 팀 미배정 (4명, 최근 입사자) ──
('dohyun.kim@eactive.co.kr',   '$2a$10$m1ejPokJV2BIVL5z7xf4.uE5TwpE4UGTldIHi2UUioWLOQ/AmvJN6','김도현','dohyun.kim@eactive.co.kr',   '서울시 관악구 신림동','2025-07-01',NULL,'STAFF',                  '2000-03-14','010-8507-2467','EMPLOYEE','ACTIVE',TRUE,'2025-07-01 09:00:00','1.0',NOW(),NOW()),
('sieun.lee@eactive.co.kr',    '$2a$10$m1ejPokJV2BIVL5z7xf4.uE5TwpE4UGTldIHi2UUioWLOQ/AmvJN6','이시은','sieun.lee@eactive.co.kr',    '경기도 성남시 수정구','2025-09-01',NULL,'STAFF',                  '2001-09-30','010-9618-3578','EMPLOYEE','ACTIVE',TRUE,'2025-09-01 09:00:00','1.0',NOW(),NOW()),
('haneul.park@eactive.co.kr',  '$2a$10$m1ejPokJV2BIVL5z7xf4.uE5TwpE4UGTldIHi2UUioWLOQ/AmvJN6','박하늘','haneul.park@eactive.co.kr',  '서울시 마포구 성산동','2026-01-05',NULL,'STAFF',                  '2001-12-18','010-1729-4689','EMPLOYEE','ACTIVE',TRUE,'2026-01-05 09:00:00','1.0',NOW(),NOW()),
('yerin.choi@eactive.co.kr',   '$2a$10$m1ejPokJV2BIVL5z7xf4.uE5TwpE4UGTldIHi2UUioWLOQ/AmvJN6','최예린','yerin.choi@eactive.co.kr',   '경기도 수원시 장안구','2026-03-02',NULL,'STAFF',                  '2002-05-07','010-2830-5790','EMPLOYEE','ACTIVE',TRUE,'2026-03-02 09:00:00','1.0',NOW(),NOW());

-- ─── 3. employee_profiles ───────────────────────────────────────
-- 직급·팀별 job_title, skills, developer_grade, career_months 설정
INSERT INTO employee_profiles
  (user_id, job_title, career_summary, skills, developer_grade, career_months, career_total_days, available_status, created_at, updated_at)
SELECT
  u.id,
  CASE
    WHEN u.team_id = (SELECT id FROM teams WHERE name='개발1팀') AND u.position IN ('GENERAL_MANAGER','DEPUTY_GENERAL_MANAGER') THEN '시니어 백엔드 개발자'
    WHEN u.team_id = (SELECT id FROM teams WHERE name='개발1팀') AND u.position IN ('MANAGER','ASSISTANT_MANAGER') THEN '백엔드 개발자'
    WHEN u.team_id = (SELECT id FROM teams WHERE name='개발1팀') AND u.position = 'STAFF' THEN '주니어 백엔드 개발자'
    WHEN u.team_id = (SELECT id FROM teams WHERE name='개발2팀') AND u.position IN ('GENERAL_MANAGER','DEPUTY_GENERAL_MANAGER') THEN '시니어 풀스택 개발자'
    WHEN u.team_id = (SELECT id FROM teams WHERE name='개발2팀') AND u.position IN ('MANAGER','ASSISTANT_MANAGER') THEN '풀스택 개발자'
    WHEN u.team_id = (SELECT id FROM teams WHERE name='개발2팀') AND u.position = 'STAFF' THEN '주니어 풀스택 개발자'
    WHEN u.team_id = (SELECT id FROM teams WHERE name='개발3팀') AND u.position IN ('GENERAL_MANAGER','DEPUTY_GENERAL_MANAGER') THEN '시니어 프론트엔드 개발자'
    WHEN u.team_id = (SELECT id FROM teams WHERE name='개발3팀') AND u.position IN ('MANAGER','ASSISTANT_MANAGER') THEN '프론트엔드 개발자'
    WHEN u.team_id = (SELECT id FROM teams WHERE name='개발3팀') AND u.position = 'STAFF' THEN '주니어 프론트엔드 개발자'
    WHEN u.team_id = (SELECT id FROM teams WHERE name='인프라팀') AND u.position IN ('DEPUTY_GENERAL_MANAGER','GENERAL_MANAGER') THEN '시니어 클라우드 엔지니어'
    WHEN u.team_id = (SELECT id FROM teams WHERE name='인프라팀') AND u.position IN ('MANAGER','ASSISTANT_MANAGER') THEN 'DevOps 엔지니어'
    WHEN u.team_id = (SELECT id FROM teams WHERE name='인프라팀') AND u.position = 'STAFF' THEN '시스템 엔지니어'
    WHEN u.team_id = (SELECT id FROM teams WHERE name='QA팀') AND u.position IN ('DEPUTY_GENERAL_MANAGER','GENERAL_MANAGER') THEN '시니어 QA 엔지니어'
    WHEN u.team_id = (SELECT id FROM teams WHERE name='QA팀') AND u.position IN ('MANAGER','ASSISTANT_MANAGER') THEN 'QA 엔지니어'
    WHEN u.team_id = (SELECT id FROM teams WHERE name='QA팀') AND u.position = 'STAFF' THEN '주니어 QA 엔지니어'
    WHEN u.team_id = (SELECT id FROM teams WHERE name='PM팀') AND u.position IN ('GENERAL_MANAGER','DEPUTY_GENERAL_MANAGER') THEN '수석 프로젝트 매니저'
    WHEN u.team_id = (SELECT id FROM teams WHERE name='PM팀') AND u.position IN ('MANAGER','ASSISTANT_MANAGER') THEN '프로젝트 매니저'
    WHEN u.team_id = (SELECT id FROM teams WHERE name='PM팀') AND u.position = 'STAFF' THEN 'PMO 담당'
    WHEN u.team_id = (SELECT id FROM teams WHERE name='영업팀') THEN '영업 담당자'
    WHEN u.team_id = (SELECT id FROM teams WHERE name='영업본부') AND u.position IN ('DIRECTOR','GENERAL_MANAGER','DEPUTY_GENERAL_MANAGER') THEN '영업 총괄'
    WHEN u.team_id = (SELECT id FROM teams WHERE name='경영본부') AND u.position IN ('EXECUTIVE_DIRECTOR','MANAGING_DIRECTOR') THEN '임원'
    WHEN u.team_id = (SELECT id FROM teams WHERE name='경영본부') AND u.position = 'DIRECTOR' THEN '이사'
    WHEN u.team_id = (SELECT id FROM teams WHERE name='경영본부') THEN '경영 담당'
    WHEN u.team_id = (SELECT id FROM teams WHERE name='경영지원팀') THEN '경영지원 담당'
    ELSE '개발자'
  END,
  NULL, -- career_summary
  CASE
    WHEN u.team_id IN (SELECT id FROM teams WHERE name IN ('개발1팀','개발2팀','개발3팀')) AND u.position IN ('GENERAL_MANAGER','DEPUTY_GENERAL_MANAGER') THEN 'Java, Spring Boot, React, PostgreSQL, AWS, Docker, Kubernetes, MSA, Git'
    WHEN u.team_id IN (SELECT id FROM teams WHERE name IN ('개발1팀','개발2팀','개발3팀')) AND u.position IN ('MANAGER','ASSISTANT_MANAGER') THEN 'Java, Spring Boot, Vue.js, MySQL, Docker, Git, Jenkins'
    WHEN u.team_id IN (SELECT id FROM teams WHERE name IN ('개발1팀','개발2팀','개발3팀')) AND u.position = 'STAFF' THEN 'Java, Spring Boot, JavaScript, Git'
    WHEN u.team_id = (SELECT id FROM teams WHERE name='인프라팀') AND u.position IN ('DEPUTY_GENERAL_MANAGER','MANAGER') THEN 'AWS, Azure, Kubernetes, Docker, Terraform, Ansible, Linux, CI/CD'
    WHEN u.team_id = (SELECT id FROM teams WHERE name='인프라팀') THEN 'AWS, Linux, Docker, Shell Script, Monitoring'
    WHEN u.team_id = (SELECT id FROM teams WHERE name='QA팀') AND u.position IN ('DEPUTY_GENERAL_MANAGER','MANAGER') THEN 'Selenium, JMeter, TestRail, JIRA, 테스트 자동화, Python, SQL'
    WHEN u.team_id = (SELECT id FROM teams WHERE name='QA팀') THEN 'Selenium, JMeter, JIRA, SQL'
    WHEN u.team_id = (SELECT id FROM teams WHERE name='PM팀') THEN 'MS Project, JIRA, Confluence, PMP, 애자일, PMBOK'
    WHEN u.team_id = (SELECT id FROM teams WHERE name='영업팀') THEN '영업 관리, 제안서 작성, 고객 관리, CRM'
    WHEN u.team_id = (SELECT id FROM teams WHERE name='영업본부') THEN '전략 영업, 대형 고객 관리, 입찰 전략'
    ELSE NULL
  END,
  -- developer_grade (개발·인프라·QA팀만 적용)
  CASE
    WHEN u.team_id IN (SELECT id FROM teams WHERE name IN ('개발1팀','개발2팀','개발3팀','인프라팀','QA팀'))
    THEN
      CASE
        WHEN EXTRACT(YEAR FROM AGE('2026-06-08'::date, u.join_date)) * 12
           + EXTRACT(MONTH FROM AGE('2026-06-08'::date, u.join_date)) >= 144 THEN '특급'
        WHEN EXTRACT(YEAR FROM AGE('2026-06-08'::date, u.join_date)) * 12
           + EXTRACT(MONTH FROM AGE('2026-06-08'::date, u.join_date)) >= 84  THEN '고급'
        WHEN EXTRACT(YEAR FROM AGE('2026-06-08'::date, u.join_date)) * 12
           + EXTRACT(MONTH FROM AGE('2026-06-08'::date, u.join_date)) >= 36  THEN '중급'
        ELSE '초급'
      END
    ELSE NULL
  END,
  -- career_months (join_date 기준)
  (EXTRACT(YEAR FROM AGE('2026-06-08'::date, u.join_date)) * 12
   + EXTRACT(MONTH FROM AGE('2026-06-08'::date, u.join_date)))::INT,
  -- career_total_days
  ('2026-06-08'::date - u.join_date)::INT,
  'AVAILABLE',
  NOW(), NOW()
FROM users u
WHERE u.login_id NOT IN ('admin@eactive.co.kr', 'test@eactive.co.kr');

COMMIT;
