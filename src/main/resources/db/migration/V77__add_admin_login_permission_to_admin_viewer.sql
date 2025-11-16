-- ADMIN_VIEWER role ↔ ADMIN_LOGIN permission 매핑 추가 (중복 방지)
INSERT INTO role_permission (id, role_id, permission_id, created_at, updated_at)
SELECT
    (SELECT COALESCE(MAX(id), 0) + 1 FROM role_permission) AS id,
    r.id AS role_id,
    p.id AS permission_id,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
FROM role r
         JOIN permission p ON p.key = 'ADMIN_LOGIN'
WHERE r.code = 'ADMIN_VIEWER'
  AND NOT EXISTS (
    SELECT 1
    FROM role_permission rp
    WHERE rp.role_id = r.id
      AND rp.permission_id = p.id
);