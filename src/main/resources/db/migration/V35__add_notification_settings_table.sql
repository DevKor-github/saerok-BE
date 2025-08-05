-- 알림 설정 시퀀스 생성
CREATE SEQUENCE notification_settings_seq START WITH 1 INCREMENT BY 50;

-- 알림 설정 테이블 생성
CREATE TABLE notification_settings (
    id                         BIGINT        NOT NULL PRIMARY KEY,
    user_id                    BIGINT        NOT NULL,
    device_id                  VARCHAR(256)  NOT NULL,
    like_enabled               BOOLEAN       NOT NULL DEFAULT true,
    comment_enabled            BOOLEAN       NOT NULL DEFAULT true,
    bird_id_suggestion_enabled BOOLEAN       NOT NULL DEFAULT true,
    system_enabled             BOOLEAN       NOT NULL DEFAULT true,
    created_at                 TIMESTAMPTZ   NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at                 TIMESTAMPTZ   NOT NULL DEFAULT CURRENT_TIMESTAMP,

    -- 사용자당 디바이스별로 고유한 설정
    CONSTRAINT uq_notification_settings_user_device UNIQUE (user_id, device_id)
);

-- 외래키 제약조건 추가
ALTER TABLE notification_settings
    ADD CONSTRAINT fk_notification_settings_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE;

ALTER TABLE notification_settings
    ADD CONSTRAINT fk_notification_settings_device FOREIGN KEY (device_id) REFERENCES device_token(device_id) ON DELETE CASCADE;

-- 인덱스 생성
CREATE INDEX idx_notification_settings_user ON notification_settings(user_id);
CREATE INDEX idx_notification_settings_user_device ON notification_settings(user_id, device_id);
