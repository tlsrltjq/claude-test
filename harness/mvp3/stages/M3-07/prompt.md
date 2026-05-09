# MVP3 M3-07 — Prompt

작업: 전 사원 공용 폴더 추가.

요구사항:

1. Flyway `V203__add_folders_type_and_public.sql`
   ```sql
   -- 컬럼 추가 (기본값 PERSONAL)
   ALTER TABLE folders ADD COLUMN type VARCHAR(20) NOT NULL DEFAULT 'PERSONAL';
   ALTER TABLE folders ADD CONSTRAINT folders_type_check CHECK (type IN ('PERSONAL','SHARED_PUBLIC'));

   -- 기본 공용 폴더 1행 시드 (owner_user_id 는 NULL — FK 가 NULL 허용해야 함, 또는 시스템 admin id)
   -- folders.owner_user_id 가 NOT NULL 이면 ALTER 후 시드
   ALTER TABLE folders ALTER COLUMN owner_user_id DROP NOT NULL;
   INSERT INTO folders (owner_user_id, folder_name, type)
   VALUES (NULL, '전 사원 공용 폴더', 'SHARED_PUBLIC');
   ```
   - 만약 owner_user_id 가 PK 또는 unique 인덱스의 일부라면 별도 처리 필요 — 코드/스키마 확인 후 보정

2. Entity
   - `Folder` 에 `FolderType type` 추가 + 기본값 PERSONAL
   - `FolderType` enum 신설 (PERSONAL, SHARED_PUBLIC)

3. 컨트롤러 — `shared/controller/PublicFolderController`
   - `GET /shared/folders/public` — 공용 폴더 문서 목록 (모두 read)
   - `POST /shared/folders/public/upload` — 파일 업로드 (모두 write)
   - `DELETE /shared/folders/public/documents/{id}` 또는 `POST .../delete` — 본인 업로드 또는 ADMIN
   - 다운로드는 기존 `/documents/{versionId}/download` 재사용 (권한 분기에 SHARED_PUBLIC 케이스 추가)

4. 권한 분기 갱신
   - `DocumentAccessService` / `FolderAccessService`:
     - 폴더 type=SHARED_PUBLIC 이면 모든 로그인 사용자 read OK
     - 업로드: 모든 로그인 사용자 OK (EMPLOYEE/SALES/ADMIN)
     - 삭제: ADMIN OR (uploader == currentUser)
   - 업로드된 document.uploader (DocumentVersion.uploaded_by) 가 비교 기준

5. 화면
   - `templates/shared/public-folder.html` — 카드/리스트 뷰
     - 업로드 버튼
     - 카드별: 보기/다운로드/삭제(권한 있을 때)
     - 업로드 사용자 / 업로드 일시 표시
   - 헤더 메뉴 — 모든 로그인 사용자에게 "공용 폴더" 링크 노출

6. SecurityConfig
   - `/shared/**` 는 로그인 필요 (이미 anyRequest().authenticated() 일 가능성 — 명시적 매처 추가도 OK)

7. NOT-DOING
   - 폴더 안 카테고리 분류 (가이드/이력서 양식 등) — 단순 평면 목록부터
   - 통합 검색 (M3-08)

검증:
- 부팅 → V203 적용, 공용 폴더 1행 자동 시드
- 일반 EMPLOYEE 가 /shared/folders/public 접속 → 카드 보임
- 업로드 OK
- 본인이 올린 카드에 삭제 버튼 노출, 클릭 → 삭제
- 다른 사람 카드에 삭제 버튼 미노출 (ADMIN 제외)
- ADMIN 은 모든 카드 삭제 가능
- 다운로드는 모든 로그인 사용자
- 비로그인 시 redirect to /login
