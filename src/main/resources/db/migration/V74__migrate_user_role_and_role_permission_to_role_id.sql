-- user_role / role_permission 을 role 문자열이 아닌 role_id(FK) 기반으로 마이그레이션

-- 1) 컬럼 추가
ALTER TABLE role_permission
    ADD COLUMN role_id BIGINT;

ALTER TABLE user_role
    ADD COLUMN role_id BIGINT;

-- 2) 기존 role 문자열 → role.id 로 매핑
UPDATE role_permission rp
SET role_id = r.id
FROM role r
WHERE r.code = rp.role;

UPDATE user_role ur
SET role_id = r.id
FROM role r
WHERE r.code = ur.role;

-- 3) 매핑 실패 여부 방어적인 체크
DO $$
    BEGIN
        IF EXISTS (SELECT 1 FROM role_permission WHERE role_id IS NULL) THEN
            RAISE EXCEPTION 'Found role_permission rows with unknown role code';
        END IF;

        IF EXISTS (SELECT 1 FROM user_role WHERE role_id IS NULL) THEN
            RAISE EXCEPTION 'Found user_role rows with unknown role code';
        END IF;
    END $$;

-- 4) FK, NOT NULL, UNIQUE 제약 변경
ALTER TABLE role_permission
    ADD CONSTRAINT fk_role_permission_role
        FOREIGN KEY (role_id) REFERENCES role (id)
            ON DELETE CASCADE;

ALTER TABLE user_role
    ADD CONSTRAINT fk_user_role_role
        FOREIGN KEY (role_id) REFERENCES role (id)
            ON DELETE CASCADE;

ALTER TABLE role_permission
    DROP CONSTRAINT IF EXISTS uq_role_permission;

ALTER TABLE user_role
    DROP CONSTRAINT IF EXISTS uq_user_role;

ALTER TABLE role_permission
    ALTER COLUMN role_id SET NOT NULL;

ALTER TABLE user_role
    ALTER COLUMN role_id SET NOT NULL;

-- 5) 기존 role 문자열 컬럼 제거
ALTER TABLE role_permission
    DROP COLUMN role;

ALTER TABLE user_role
    DROP COLUMN role;

-- 6) 새로운 UNIQUE 제약 추가
ALTER TABLE role_permission
    ADD CONSTRAINT uq_role_permission UNIQUE (role_id, permission_id);

ALTER TABLE user_role
    ADD CONSTRAINT uq_user_role UNIQUE (user_id, role_id);
