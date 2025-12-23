-- 공지사항 알림 옵션 추가

ALTER TABLE announcement
    ADD COLUMN send_notification BOOLEAN NOT NULL DEFAULT FALSE;

ALTER TABLE announcement
    ADD COLUMN push_title VARCHAR(255);

ALTER TABLE announcement
    ADD COLUMN push_body TEXT;

ALTER TABLE announcement
    ADD COLUMN in_app_body TEXT;
