# Stage 06 — Acceptance

## 자동 검증 (verify.sh)
- [ ] `DocumentAccessService` 존재
- [ ] `AuditLogService` 존재 + VIEW/DOWNLOAD 메서드 존재
- [ ] `/documents/{documentVersionId}/preview` 매핑
- [ ] `/documents/{documentVersionId}/download` 매핑
- [ ] `/documents/{documentVersionId}/download/reason` 매핑
- [ ] AuditActionType enum에 VIEW, DOWNLOAD 존재

## 수동 검증
- [ ] PDF 업로드 → 상세 화면에서 iframe으로 미리보기 표시
- [ ] 이미지(jpg/png) 업로드 → img 태그로 미리보기 표시
- [ ] DOCX + preview PDF 업로드 → preview PDF가 미리보기로 표시
- [ ] preview PDF 없는 DOCX는 "미리보기 불가" 안내
- [ ] 미리보기 시 audit_logs에 VIEW 행 기록 (user_id, target_id=docVersionId, IP, UA)
- [ ] 다운로드 버튼 → 사유 입력 화면으로 이동
- [ ] 빈 사유로 제출 → 거부
- [ ] 사유 입력 후 다운로드 → 원본 파일명으로 내려옴 (`Content-Disposition: attachment`)
- [ ] 다운로드 시 audit_logs에 DOWNLOAD 행 기록 (reason 포함)
- [ ] 일반 사용자가 다른 사람 문서의 preview/download URL에 직접 접근 → 403/거부
- [ ] 관리자 계정은 모든 직원 문서 미리보기/다운로드 가능
- [ ] `./storage/uploads/...` 경로를 브라우저로 직접 접근 시 404/거부 (정적 노출 안 됨)

## NOT-DOING 확인
- [ ] 팀장/개별 권한 화면 없음
- [ ] 썸네일 컬럼/이미지 없음
- [ ] DOCX/HWP 자동 PDF 변환 코드 없음
