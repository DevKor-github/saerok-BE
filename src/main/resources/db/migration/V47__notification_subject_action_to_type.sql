-- V47__notification_subject_action_to_type.sql
-- 목적: notification(subject, action) -> notification(type) 전환
--  - 매핑 규칙:
--      (COLLECTION, LIKE)            -> LIKED_ON_COLLECTION
--      (COLLECTION, COMMENT)         -> COMMENTED_ON_COLLECTION
--      (COLLECTION, SUGGEST_BIRD_ID) -> SUGGESTED_BIRD_ID_ON_COLLECTION

DO $$
    BEGIN
        IF NOT EXISTS (
            SELECT 1
            FROM information_schema.columns
            WHERE table_name = 'notification' AND column_name = 'type'
        ) THEN
            ALTER TABLE notification ADD COLUMN type VARCHAR(64);
        END IF;
    END $$;

UPDATE notification n
SET type = CASE
               WHEN n.subject = 'COLLECTION' AND n.action = 'LIKE'            THEN 'LIKED_ON_COLLECTION'
               WHEN n.subject = 'COLLECTION' AND n.action = 'COMMENT'         THEN 'COMMENTED_ON_COLLECTION'
               WHEN n.subject = 'COLLECTION' AND n.action = 'SUGGEST_BIRD_ID' THEN 'SUGGESTED_BIRD_ID_ON_COLLECTION'
               ELSE NULL
    END;

-- 매핑되지 않은 데이터는 현재 스키마상 존재하지 않는 게 정상이며,
-- 혹시 모를 이질 데이터는 안전하게 제거 (정책에 맞게 수정 가능)
DELETE FROM notification WHERE type IS NULL;

ALTER TABLE notification ALTER COLUMN type SET NOT NULL;

-- 기존 칼럼 제거
ALTER TABLE notification DROP COLUMN IF EXISTS subject;
ALTER TABLE notification DROP COLUMN IF EXISTS action;
