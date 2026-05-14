# MVP2 Stage 06 — Prompt

MVP2 6단계 작업을 진행해줘.

이전 단계까지: 권한 / 다운로드 / 회원가입 필드 / 영업부 표 + 검색.

이번 단계 목표는 **양식 이력서 기능**을 추가하는 것이야.

요구사항:

1. Flyway 마이그레이션 `V104__create_resume_templates.sql`
   ```sql
   CREATE TABLE resume_templates (
       id BIGSERIAL PRIMARY KEY,
       file_name VARCHAR(255) NOT NULL,        -- 원본 파일명
       stored_file_name VARCHAR(255) NOT NULL, -- UUID 파일명
       storage_path VARCHAR(500) NOT NULL,
       content_type VARCHAR(100) NOT NULL,
       file_size BIGINT NOT NULL,
       checksum VARCHAR(128),
       status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',  -- ACTIVE | ARCHIVED
       uploaded_by BIGINT NOT NULL REFERENCES users(id),
       created_at TIMESTAMPTZ NOT NULL DEFAULT now()
   );
   CREATE INDEX idx_resume_templates_status ON resume_templates(status);
   ```
   - 단일 active 행만 유지. 새로 업로드 시 기존 ACTIVE → ARCHIVED.

2. Entity / Repository
   - `template/entity/ResumeTemplate.java` — 위 컬럼
   - `template/entity/ResumeTemplateStatus.java` — ACTIVE / ARCHIVED
   - `template/repository/ResumeTemplateRepository.java` — `findByStatus`, `findFirstByStatusOrderByCreatedAtDesc`

3. 서비스 — `template/service/ResumeTemplateService.java`
   - `upload(MultipartFile, currentAdminOrSales)` — 5단계 업로드 정책 따름(허용 확장자, 20MB), UUID 파일명, 디스크 저장.
   - 업로드 시 기존 ACTIVE를 ARCHIVED 로 일괄 변경(Transactional).
   - `getActive()` — 현재 ACTIVE Template
   - `download(currentUser)` — 권한 체크 (모든 로그인 사용자 OK), InputStreamResource 반환.

4. 컨트롤러
   - `template/controller/AdminResumeTemplateController` (또는 sales도 가능):
     - `GET /admin/resume-template` — 현재 active 정보 + 업로드 폼
     - `POST /admin/resume-template` — 업로드
   - `template/controller/MyResumeTemplateController` (또는 MyFolderController에 추가):
     - `GET /my/folder/resume-template/download` — 직원 본인이 다운로드
   - SALES도 `/sales/resume-template/download` 로 받을 수 있게.

5. 화면
   - `templates/admin/resume-template.html` — 현재 active 파일명/업로드일/uploaded_by + 새 파일 업로드 폼
   - `templates/my/folder.html` — 상단에 "양식 이력서 다운로드" 버튼 (active 있을 때만)
   - 인력 프로필 표(04)의 이력서 칸은 **그대로 유지** — 직원이 RESUME 유형으로 재업로드하면 이미 04 표에 반영됨

6. 권한
   - 업로드: ADMIN 또는 SALES (PDF: "관리자/영업부가 양식 이력서 파일 업로드")
   - 다운로드: 로그인 사용자 모두 (직원은 본인 폴더에서, 영업부/관리자는 어디서나)
   - 양식 자체 삭제는 1차에서 제외 (새 업로드로 archive)

7. audit_logs
   - 업로드 시 `UPLOAD_RESUME_TEMPLATE` (또는 UPLOAD)
   - 다운로드 시 `DOWNLOAD_RESUME_TEMPLATE` (또는 DOWNLOAD with target_type=RESUME_TEMPLATE)

8. NOT-DOING
   - 양식 이력서 다중 버전 관리 (단일 active만)
   - 양식 자체 삭제 화면

검증:
- ADMIN 또는 SALES 가 `/admin/resume-template` 에서 PDF 업로드 → V104 적용 후 row 1개 ACTIVE
- 같은 화면에서 또 업로드 → 기존 row ARCHIVED, 새 row ACTIVE
- EMPLOYEE 가 `/my/folder` → "양식 다운로드" 버튼 동작
- 직원이 받은 파일을 작성 후 RESUME 유형으로 본인 폴더에 업로드 → 영업부 표 이력서 칸에 보기/다운로드 노출
- audit_logs 기록 확인
