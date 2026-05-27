-- V216에서 SMALLINT로 정의된 allocation_rate를 JPA 엔티티(int)와 일치하도록 INTEGER로 변경
ALTER TABLE project_assignments ALTER COLUMN allocation_rate TYPE INTEGER;
