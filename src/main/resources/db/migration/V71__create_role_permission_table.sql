-- V71__create_role_permission_table.sql
-- Role - Permission 매핑 테이블 및 시퀀스 생성 + 기본 매핑 시드

CREATE SEQUENCE role_permission_seq START WITH 1 INCREMENT BY 50;

CREATE TABLE role_permission (
                                 id            BIGINT       NOT NULL PRIMARY KEY,
                                 role          VARCHAR(100) NOT NULL,
                                 permission_id BIGINT       NOT NULL,
                                 created_at    TIMESTAMPTZ  NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                 updated_at    TIMESTAMPTZ  NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                 CONSTRAINT fk_role_permission_permission
                                     FOREIGN KEY (permission_id) REFERENCES permission (id),
                                 CONSTRAINT uq_role_permission UNIQUE (role, permission_id)
);

/* ───────────────────────────────────────────────────────────────────────────
   기본 Role - Permission 매핑 시드
      - role 컬럼: UserRoleType enum 의 name()
      - permission.key: PermissionKey enum 및 permission 테이블의 key 컬럼과 1:1
   ─────────────────────────────────────────────────────────────────────────── */

-- ADMIN_VIEWER 기본 권한
INSERT INTO role_permission (id, role, permission_id, created_at, updated_at)
SELECT nextval('role_permission_seq'), 'ADMIN_VIEWER', p.id, now(), now()
FROM permission p
WHERE p.key IN (
                'ADMIN_REPORT_READ',
                'ADMIN_AUDIT_READ',
                'ADMIN_STAT_READ',
                'ADMIN_AD_READ'
    );

-- ADMIN_EDITOR 기본 권한
INSERT INTO role_permission (id, role, permission_id, created_at, updated_at)
SELECT nextval('role_permission_seq'), 'ADMIN_EDITOR', p.id, now(), now()
FROM permission p
WHERE p.key IN (
                'ADMIN_REPORT_READ',
                'ADMIN_REPORT_WRITE',
                'ADMIN_AUDIT_READ',
                'ADMIN_STAT_READ',
                'ADMIN_STAT_WRITE',
                'ADMIN_AD_READ',
                'ADMIN_AD_WRITE',
                'ADMIN_SLOT_WRITE'
    );
