-- 기존 DB의 '열린 동정 요청'을 이력 테이블로 백필한다.
-- 조건:
--   - user_bird_collection.bird_id_requested_at IS NOT NULL
--   - 아직 확정되지 않음: user_bird_collection.bird_id IS NULL
--   - 이미 열린 이력이 있으면 중복 삽입 금지

INSERT INTO bird_id_request_history (
    id,
    collection_id,
    started_at,
    resolved_at,
    resolution_seconds,
    resolution_kind,
    created_at
)
SELECT
    nextval('bird_id_request_history_seq')                         AS id,
    c.id                                                           AS collection_id,
    c.bird_id_suggestion_requested_at                              AS started_at,
    NULL                                                           AS resolved_at,
    NULL                                                           AS resolution_seconds,
    NULL                                                           AS resolution_kind,     -- 열린 상태이므로 NULL
    COALESCE(c.bird_id_suggestion_requested_at, CURRENT_TIMESTAMP) AS created_at
FROM user_bird_collection c
WHERE
    c.bird_id_suggestion_requested_at IS NOT NULL
  AND c.bird_id IS NULL
  AND NOT EXISTS (   -- 이미 열린(history.resolved_at IS NULL) 행이 있으면 스킵
    SELECT 1
    FROM bird_id_request_history h
    WHERE h.collection_id = c.id
      AND h.resolved_at IS NULL
);
