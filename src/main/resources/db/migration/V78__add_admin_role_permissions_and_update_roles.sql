-- 새 관리자 역할 관련 Permission 추가
INSERT INTO permission (id, key, description, created_at, updated_at)
SELECT nextval('permission_seq'), 'ADMIN_ROLE_MY_READ', '로그인한 관리자의 역할/권한 조회', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM permission WHERE key = 'ADMIN_ROLE_MY_READ');

INSERT INTO permission (id, key, description, created_at, updated_at)
SELECT nextval('permission_seq'), 'ADMIN_ROLE_READ', '모든 관리자의 역할 및 권한 조회', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM permission WHERE key = 'ADMIN_ROLE_READ');

INSERT INTO permission (id, key, description, created_at, updated_at)
SELECT nextval('permission_seq'), 'ADMIN_ROLE_WRITE', '관리자 역할 생성/삭제 및 권한 편집, 사용자 역할 부여/회수', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM permission WHERE key = 'ADMIN_ROLE_WRITE');

-- ADMIN_VIEWER → TEAM_MEMBER 이름/설명 변경
UPDATE role
SET code = 'TEAM_MEMBER',
    display_name = '어푸 팀원',
    description = '팀 전용 어드민 화면 조회 권한',
    updated_at = CURRENT_TIMESTAMP
WHERE code = 'ADMIN_VIEWER';

-- ADMIN_EDITOR 는 더 이상 내장 역할이 아니도록 설정
UPDATE role
SET is_builtin = FALSE,
    updated_at = CURRENT_TIMESTAMP
WHERE code = 'ADMIN_EDITOR';

-- TEAM_MEMBER 역할에 ROLE 조회 권한 부여
INSERT INTO role_permission (id, role_id, permission_id, created_at, updated_at)
SELECT nextval('role_permission_seq'), r.id, p.id, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
FROM role r
         JOIN permission p ON p.key = 'ADMIN_ROLE_MY_READ'
WHERE r.code = 'TEAM_MEMBER'
  AND NOT EXISTS (
    SELECT 1
    FROM role_permission rp
    WHERE rp.role_id = r.id
      AND rp.permission_id = p.id
);

INSERT INTO role_permission (id, role_id, permission_id, created_at, updated_at)
SELECT nextval('role_permission_seq'), r.id, p.id, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
FROM role r
         JOIN permission p ON p.key = 'ADMIN_ROLE_READ'
WHERE r.code = 'TEAM_MEMBER'
  AND NOT EXISTS (
    SELECT 1
    FROM role_permission rp
    WHERE rp.role_id = r.id
      AND rp.permission_id = p.id
);