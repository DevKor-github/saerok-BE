-- V70__create_permission_table.sql
-- Permission 테이블 및 시퀀스 생성 + 기본 Permission 시드

CREATE SEQUENCE permission_seq START WITH 1 INCREMENT BY 50;

CREATE TABLE permission (
                            id          BIGINT       NOT NULL PRIMARY KEY,
                            key         VARCHAR(100) NOT NULL,
                            description VARCHAR(255) NOT NULL,
                            created_at  TIMESTAMPTZ  NOT NULL DEFAULT CURRENT_TIMESTAMP,
                            updated_at  TIMESTAMPTZ  NOT NULL DEFAULT CURRENT_TIMESTAMP,
                            CONSTRAINT uq_permission_key UNIQUE (key)
);

/* ───────────────────────────────────────────────────────────────────────────
   기본 Permission 시드
      - Java enum PermissionKey 와 1:1로 대응
   ─────────────────────────────────────────────────────────────────────────── */
INSERT INTO permission (id, key, description, created_at, updated_at) VALUES
      (nextval('permission_seq'), 'ADMIN_REPORT_READ', '신고된 콘텐츠 내용 조회', now(), now()),
      (nextval('permission_seq'), 'ADMIN_REPORT_WRITE', '신고된 콘텐츠에 대한 조치 수행', now(), now()),
      (nextval('permission_seq'), 'ADMIN_AUDIT_READ', '관리자 활동 로그 조회', now(), now()),
      (nextval('permission_seq'), 'ADMIN_STAT_READ', '서비스 통계 조회', now(), now()),
      (nextval('permission_seq'), 'ADMIN_STAT_WRITE', '서비스 통계 수동 집계 실행', now(), now()),
      (nextval('permission_seq'), 'ADMIN_AD_READ', '광고/광고 위치/광고 스케줄 조회', now(), now()),
      (nextval('permission_seq'), 'ADMIN_AD_WRITE', '광고 및 광고 스케줄 생성/수정/삭제', now(), now()),
      (nextval('permission_seq'), 'ADMIN_SLOT_WRITE', '광고 위치 생성/수정/삭제', now(), now());
