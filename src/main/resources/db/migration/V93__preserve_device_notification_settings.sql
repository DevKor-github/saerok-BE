ALTER TABLE user_device
    ALTER COLUMN token DROP NOT NULL;

-- 과거 데이터에 빈 token이 있다면 비활성 상태로 정규화한다.
UPDATE user_device
SET token = NULL,
    updated_at = now()
WHERE token IS NOT NULL
  AND btrim(token) = '';

-- partial unique index 생성 전에 중복 token을 안전하게 정리한다.
-- 가장 최근에 갱신된 row 하나만 활성 상태로 유지하고 나머지는 설정과 함께 보존한다.
WITH ranked_tokens AS (
    SELECT id,
           row_number() OVER (
               PARTITION BY token
               ORDER BY updated_at DESC, id DESC
           ) AS row_rank
    FROM user_device
    WHERE token IS NOT NULL
)
UPDATE user_device ud
SET token = NULL,
    updated_at = now()
FROM ranked_tokens ranked
WHERE ud.id = ranked.id
  AND ranked.row_rank > 1;

DROP INDEX IF EXISTS idx_user_device_token;

CREATE UNIQUE INDEX uq_user_device_active_token
    ON user_device(token)
    WHERE token IS NOT NULL;

ALTER TABLE user_device
    ADD CONSTRAINT ck_user_device_token_not_blank
        CHECK (token IS NULL OR btrim(token) <> '');
