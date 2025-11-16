-- V72__create_role_table.sql
-- Role 테이블 및 시퀀스 생성 + 기본 Role 시드

CREATE SEQUENCE role_seq START WITH 1 INCREMENT BY 50;

CREATE TABLE role (
                      id           BIGINT        NOT NULL PRIMARY KEY,
                      code         VARCHAR(100)  NOT NULL,
                      display_name VARCHAR(100)  NOT NULL,
                      description  VARCHAR(255)  NOT NULL,
                      is_builtin   BOOLEAN       NOT NULL DEFAULT TRUE,
                      created_at   TIMESTAMPTZ   NOT NULL DEFAULT CURRENT_TIMESTAMP,
                      updated_at   TIMESTAMPTZ   NOT NULL DEFAULT CURRENT_TIMESTAMP,
                      CONSTRAINT uq_role_code UNIQUE (code),
                      CONSTRAINT uq_role_display_name UNIQUE (display_name)
);

-- 기본 Role 시드
INSERT INTO role (id, code, display_name, description, is_builtin, created_at, updated_at) VALUES
       (nextval('role_seq'), 'USER', '일반 사용자', '서비스를 이용하는 일반 회원', TRUE, now(), now()),
       (nextval('role_seq'), 'ADMIN_VIEWER', '관리자(조회 전용)', '관리자 화면 조회 전용 권한', TRUE, now(), now()),
       (nextval('role_seq'), 'ADMIN_EDITOR', '관리자(편집 가능)', '관리자 화면 전체 권한 (생성/수정/삭제 포함)', TRUE, now(), now());
