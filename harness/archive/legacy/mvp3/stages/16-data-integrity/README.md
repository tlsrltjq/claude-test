# M3-16: 데이터 무결성 + 런타임 안전성

## 목적

이 스테이지는 실제 운영 중 발생한 버그 세 가지의 재발을 자동으로 탐지한다.

| 버그 | 증상 | 검사 항목 |
|------|------|----------|
| 중복 개인 폴더 | `findByOwnerIdAndType` → 500 | DB `(owner_user_id, type)` 중복 검사 + 코드 Optional 사용 검증 |
| `Collectors.toMap` 머지 함수 누락 | `Duplicate key N` → `/sales/profiles` 500 | 소스 내 2인수 `toMap` 정적 탐지 |
| `Secure` 쿠키 + HTTP 조합 | 로그인 무한 리다이렉트 | `application-prod.yml` vs `docker-compose.yml` 오버라이드 확인 |

## 실행

```bash
# DB 연결 있는 경우
bash harness/mvp3/stages/16-data-integrity/verify.sh

# DB 비밀번호 명시
POSTGRES_PASSWORD=mypass bash harness/mvp3/stages/16-data-integrity/verify.sh
```

## 합격 기준

`acceptance.md` 참조.
