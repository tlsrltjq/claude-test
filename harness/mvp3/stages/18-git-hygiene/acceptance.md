# 18-git-hygiene 합격 기준

## 필수 (FAIL 없어야 함)

- [ ] `.gitignore`에 `.env` 패턴 존재
- [ ] `.gitignore`에 `storage/` 패턴 존재
- [ ] `.gitignore`에 `build/` 패턴 존재
- [ ] `.env` 파일이 git-tracked 상태가 아님
- [ ] `docker-compose.override.yml`이 git-tracked 상태가 아님
- [ ] 최근 50 커밋 diff에 하드코딩 비밀번호·시크릿 패턴 없음

## 허용 기준 (WARN)

- [ ] main에 merge된 feature/* 브랜치가 남아있음 (WARN — 삭제 권장, FAIL 아님)
- [ ] 작업 폴더에 미커밋 변경사항 있음 (개발 중이라면 허용)
- [ ] 현재 브랜치가 main이 아님 (작업 브랜치라면 허용)
