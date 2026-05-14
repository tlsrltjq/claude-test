# Stage 03 — 회원가입 / 이메일 인증 / 로그인

## 목적
회사 이메일로만 가입 가능한 회원가입 흐름과 6자리 코드 이메일 인증, 그리고 Spring Security 세션 기반 로그인/로그아웃을 구현한다.

## 진입 조건
- 02-db-flyway verified

## 핵심 산출물 요약
- `email_verification_tokens` 테이블 (V2)
- 회원가입/인증 코드/관리자 승인 대기 흐름
- Spring Security 세션 인증 (JWT 미사용)
- 기본 관리자 계정 자동 생성

## 절대 하지 말 것
- JWT
- Remember-me
- 팀별 접근 권한 / 파일 업로드 / 미리보기 / 관리자 승인 처리 (4단계 이후)
