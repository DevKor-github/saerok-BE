-- 기존 user_refresh_token 레코드 기반으로 user_activity_ping 백필
-- 트리거(V63)로 신규 발급분은 자동 기록되므로, 여기서는 과거 레코드 중
-- 동일 (user_id, occurred_at=issued_at, source='refresh_token') 가 없는 것만 적재

INSERT INTO user_activity_ping (id, user_id, occurred_at, source)
SELECT
    nextval('user_activity_ping_seq') AS id,
    t.user_id,
    t.issued_at        AS occurred_at,
    'refresh_token'    AS source
FROM user_refresh_token t
         LEFT JOIN user_activity_ping p
                   ON p.user_id = t.user_id
                       AND p.occurred_at = t.issued_at
                       AND p.source = 'refresh_token'
WHERE t.issued_at IS NOT NULL
  AND p.id IS NULL;