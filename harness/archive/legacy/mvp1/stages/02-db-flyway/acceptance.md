# Stage 02 — Acceptance

## 자동 검증 (verify.sh)
- [ ] `V1__create_base_tables.sql` 존재
- [ ] SQL 파일에 8개 테이블 `CREATE TABLE` 존재
- [ ] `BaseEntity.java` 존재 + `@MappedSuperclass` 포함
- [ ] 8개 도메인 Entity 클래스 모두 존재
- [ ] 8개 Repository 모두 존재
- [ ] enum 13개 존재
- [ ] `JpaAuditingConfig.java` 존재 + `@EnableJpaAuditing` 포함

## 수동 검증
- [ ] `docker compose up -d postgres` 후 애플리케이션 부팅 시 Flyway가 V1 적용 → `flyway_schema_history`에 V1 row 존재
- [ ] DBeaver 등으로 8개 테이블 + 인덱스/FK/unique 제약 확인
- [ ] Entity ↔ 테이블 매핑 일치 (컬럼명/타입/null 여부)
- [ ] `created_at`, `updated_at`이 자동으로 채워지는지 (테스트 insert로 확인)
- [ ] enum 컬럼이 VARCHAR로 저장되는지 (`@Enumerated(EnumType.STRING)`)
