-- 1) 새 컬럼 추가
ALTER TABLE notification_setting ADD COLUMN subject VARCHAR(50);
ALTER TABLE notification_setting ADD COLUMN action  VARCHAR(50);

-- 2) 데이터 마이그레이션 (SYSTEM은 삭제)
UPDATE notification_setting
SET subject = 'COLLECTION',
    action = CASE type
                 WHEN 'LIKE'               THEN 'LIKE'
                 WHEN 'COMMENT'            THEN 'COMMENT'
                 WHEN 'BIRD_ID_SUGGESTION' THEN 'SUGGEST_BIRD_ID'
                 WHEN 'SYSTEM'             THEN NULL
        END;

DELETE FROM notification_setting WHERE type = 'SYSTEM';

-- 3) NOT NULL 제약
ALTER TABLE notification_setting ALTER COLUMN subject SET NOT NULL;

-- 4) 기존 유니크 제약 제거 및 신규 유니크 제약 추가
ALTER TABLE notification_setting DROP CONSTRAINT IF EXISTS uq_notification_setting_user_device_type;
ALTER TABLE notification_setting
    ADD CONSTRAINT uq_notification_setting_device_subject_action
        UNIQUE (user_device_id, subject, action);

-- 5) 기존 type 컬럼 제거
ALTER TABLE notification_setting DROP COLUMN type;

-- 6) 그룹(Subject) 토글 로우 보강: 각 디바이스에 (subject='COLLECTION', action=NULL) 없으면 생성
INSERT INTO notification_setting (id, user_device_id, subject, action, enabled, created_at, updated_at)
SELECT nextval('notification_setting_seq'), ud.id, 'COLLECTION', NULL, TRUE, now(), now()
FROM user_device ud
WHERE NOT EXISTS (
    SELECT 1 FROM notification_setting ns
    WHERE ns.user_device_id = ud.id
      AND ns.subject = 'COLLECTION'
      AND ns.action IS NULL
);
