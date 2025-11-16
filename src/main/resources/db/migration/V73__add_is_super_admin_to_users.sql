-- users.is_super_admin 플래그 추가
-- - SuperAdmin은 별도 Role 이 아니라 User 속성으로 관리

ALTER TABLE users
    ADD COLUMN IF NOT EXISTS is_super_admin BOOLEAN NOT NULL DEFAULT FALSE;
