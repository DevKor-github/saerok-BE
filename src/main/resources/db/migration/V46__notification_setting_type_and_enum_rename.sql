-- 1) notification_setting: 유효하지 않은 그룹 토글 레코드 제거
DELETE FROM notification_setting
WHERE subject = 'COLLECTION' AND action IS NULL;

-- 2) notification_setting: type 컬럼 추가 (임시 nullable)
ALTER TABLE notification_setting ADD COLUMN type VARCHAR(64);

-- 3) subject/action -> type 매핑
-- COLLECTION + LIKE -> LIKED_ON_COLLECTION
UPDATE notification_setting
SET type = 'LIKED_ON_COLLECTION'
WHERE subject = 'COLLECTION' AND action = 'LIKE';

-- COLLECTION + COMMENT -> COMMENTED_ON_COLLECTION
UPDATE notification_setting
SET type = 'COMMENTED_ON_COLLECTION'
WHERE subject = 'COLLECTION' AND action = 'COMMENT';

-- COLLECTION + SUGGEST_BIRD_ID -> SUGGESTED_BIRD_ID_ON_COLLECTION
UPDATE notification_setting
SET type = 'SUGGESTED_BIRD_ID_ON_COLLECTION'
WHERE subject = 'COLLECTION' AND action = 'SUGGEST_BIRD_ID';

-- 4) NOT NULL 강제
ALTER TABLE notification_setting
    ALTER COLUMN type SET NOT NULL;

-- 5) 기존 유니크 제약( user_device_id + subject + action ) 제거 및 ( user_device_id + type )로 재정의
DO $$
    BEGIN
        IF EXISTS (
            SELECT 1
            FROM pg_constraint
            WHERE conname = 'uq_notification_setting_user_device_subject_action'
        ) THEN
            ALTER TABLE notification_setting DROP CONSTRAINT uq_notification_setting_user_device_subject_action;
        END IF;
    END $$;

ALTER TABLE notification_setting
    ADD CONSTRAINT uq_notification_setting_user_device_type UNIQUE (user_device_id, type);

-- 6) subject/action 컬럼 제거
ALTER TABLE notification_setting DROP COLUMN action;
ALTER TABLE notification_setting DROP COLUMN subject;

-- 7) (선택) 정합성 보장: enabled 기본값이 null 이면 false로
UPDATE notification_setting SET enabled = false WHERE enabled IS NULL;

-- === 참고 ===
-- NotificationType 값 변경은 코드 레벨에서 처리(하단 enum 변경).
-- notification 테이블은 현행 코드가 subject/action 기반 저장이므로 이번 마이그레이션에서는 변경하지 않음.
