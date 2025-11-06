-- users.signup_completed_at 추가 및 전환 트리거 (psql)

ALTER TABLE users
    ADD COLUMN IF NOT EXISTS signup_completed_at TIMESTAMPTZ;

-- COMPLETED 상태로 생성되거나, 다른 상태 → COMPLETED 로 전환되는 순간 1회 세팅
CREATE OR REPLACE FUNCTION trg_set_signup_completed_at()
RETURNS TRIGGER
LANGUAGE plpgsql
AS $$
BEGIN
    IF TG_OP = 'INSERT' THEN
        IF NEW.signup_status = 'COMPLETED' AND NEW.signup_completed_at IS NULL THEN
            NEW.signup_completed_at := NOW();
END IF;
RETURN NEW;
ELSIF TG_OP = 'UPDATE' THEN
        IF NEW.signup_status = 'COMPLETED'
           AND (OLD.signup_status IS DISTINCT FROM 'COMPLETED')
           AND NEW.signup_completed_at IS NULL THEN
            NEW.signup_completed_at := NOW();
END IF;
RETURN NEW;
END IF;
RETURN NEW;
END;
$$;

DROP TRIGGER IF EXISTS set_signup_completed_at_on_users_insert ON users;
CREATE TRIGGER set_signup_completed_at_on_users_insert
    BEFORE INSERT ON users
    FOR EACH ROW
    EXECUTE FUNCTION trg_set_signup_completed_at();

DROP TRIGGER IF EXISTS set_signup_completed_at_on_users_update ON users;
CREATE TRIGGER set_signup_completed_at_on_users_update
    BEFORE UPDATE OF signup_status ON users
    FOR EACH ROW
    EXECUTE FUNCTION trg_set_signup_completed_at();

-- 조회 성능 인덱스
CREATE INDEX IF NOT EXISTS idx_users_signup_completed_at ON users (signup_completed_at);
CREATE INDEX IF NOT EXISTS idx_users_deleted_at          ON users (deleted_at);