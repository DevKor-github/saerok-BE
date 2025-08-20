-- 인앱 알림(Notification)은 title을 보관하지 않도록 컬럼 제거
-- 기존 body 데이터는 그대로 유지됩니다.
ALTER TABLE notification
    DROP COLUMN IF EXISTS title;
