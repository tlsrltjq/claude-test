# MVP2 Stage 06 — Deliverables

## SQL
- `V104__create_resume_templates.sql`

## Entity / Repository
- `template/entity/ResumeTemplate.java`
- `template/entity/ResumeTemplateStatus.java`
- `template/repository/ResumeTemplateRepository.java`

## Service
- `template/service/ResumeTemplateService.java`

## Controllers
- `template/controller/AdminResumeTemplateController.java` — `GET/POST /admin/resume-template`
- `template/controller/ResumeTemplateDownloadController.java` — `GET /my/folder/resume-template/download`, `GET /sales/resume-template/download`

## Templates
- `templates/admin/resume-template.html`
- `templates/my/folder.html` 갱신 — "양식 다운로드" 버튼

## Audit
- `AuditActionType` 에 `UPLOAD_RESUME_TEMPLATE`, `DOWNLOAD_RESUME_TEMPLATE` (또는 기존 UPLOAD/DOWNLOAD + target_type=RESUME_TEMPLATE)
