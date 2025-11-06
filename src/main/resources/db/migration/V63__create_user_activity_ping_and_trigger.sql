-- 리프레시 토큰 발급시 활동 핑 기록 (psql)

-- 프로젝트 관례에 맞춰 별도 시퀀스 생성 (예: user_refresh_token_seq와 동일 스타일)
CREATE SEQUENCE user_activity_ping_seq START WITH 1 INCREMENT BY 50;

CREATE TABLE IF NOT EXISTS user_activity_ping (
                                                  id          BIGINT       NOT NULL PRIMARY KEY,
                                                  user_id     BIGINT       NOT NULL,
                                                  occurred_at TIMESTAMPTZ  NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                                  source      TEXT         NOT NULL DEFAULT 'refresh_token'
);

CREATE INDEX IF NOT EXISTS idx_uap_occurred_at ON user_activity_ping (occurred_at);
CREATE INDEX IF NOT EXISTS idx_uap_user_time   ON user_activity_ping (user_id, occurred_at);

-- 기존 user_refresh_token INSERT마다 핑 적재
-- (스키마는 기존 마이그레이션과 동일: issued_at, user_id 등)
-- 시퀀스 관례를 따르므로, 트리거에서 id를 nextval(...)로 명시 삽입
CREATE OR REPLACE FUNCTION trg_uap_on_refresh_token_insert()
RETURNS TRIGGER
LANGUAGE plpgsql
AS $$
BEGIN
INSERT INTO user_activity_ping (id, user_id, occurred_at, source)
VALUES (nextval('user_activity_ping_seq'),
        NEW.user_id,
        COALESCE(NEW.issued_at, NOW()),
        'refresh_token');
RETURN NEW;
END;
$$;

DROP TRIGGER IF EXISTS uap_after_insert_on_user_refresh_token ON user_refresh_token;
CREATE TRIGGER uap_after_insert_on_user_refresh_token
    AFTER INSERT ON user_refresh_token
    FOR EACH ROW
    EXECUTE FUNCTION trg_uap_on_refresh_token_insert();
