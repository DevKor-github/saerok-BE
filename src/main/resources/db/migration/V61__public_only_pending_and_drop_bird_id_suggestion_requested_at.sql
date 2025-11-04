-- 1) PRIVATE 컬렉션(그리고 bird IS NULL)에 열려 있는(미해결) 동정 요청 기록 삭제
DELETE FROM bird_id_request_history h
    USING user_bird_collection c
WHERE h.collection_id = c.id
  AND h.resolved_at IS NULL
  AND c.access_level = 'PRIVATE'
  AND c.bird_id IS NULL;

-- 2) PUBLIC 이면서 bird IS NULL인데, 아직 열린 동정 요청 기록이 없는 컬렉션에 대해 오픈 히스토리 백필
--    started_at은 기존 컬럼이 남아있다면 우선 사용, 없으면 updated_at으로 보정
INSERT INTO bird_id_request_history (id, collection_id, started_at, created_at)
SELECT nextval('bird_id_request_history_seq'),
       c.id,
       COALESCE(c.bird_id_suggestion_requested_at, c.updated_at),
       COALESCE(c.bird_id_suggestion_requested_at, c.updated_at)
FROM user_bird_collection c
         LEFT JOIN LATERAL (
    SELECT 1
    FROM bird_id_request_history h
    WHERE h.collection_id = c.id
      AND h.resolved_at IS NULL
        LIMIT 1
) open ON true
WHERE open IS NULL
  AND c.access_level = 'PUBLIC'
  AND c.bird_id IS NULL;

-- 3) 더 이상 사용하지 않는 컬럼 제거
ALTER TABLE user_bird_collection
DROP COLUMN IF EXISTS bird_id_suggestion_requested_at;
