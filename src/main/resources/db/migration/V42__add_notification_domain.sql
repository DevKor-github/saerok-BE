-- 알림 설정 시퀀스 생성
CREATE SEQUENCE notification_settings_seq START WITH 1 INCREMENT BY 50;
-- 알림 시퀀스 생성
CREATE SEQUENCE notifications_seq START WITH 1 INCREMENT BY 50;


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
-- 알림 테이블 생성
CREATE TABLE notifications (
    id         BIGINT       NOT NULL PRIMARY KEY,
    user_id    BIGINT       NOT NULL,
    title      VARCHAR(255) NOT NULL,
    body       TEXT         NOT NULL,
    type       VARCHAR(50)  NOT NULL,
    related_id BIGINT,                                          -- 컬렉션 id (SYSTEM 메시지의 경우 null)
    deep_link  VARCHAR(500),
    sender_id  BIGINT,
    is_read    BOOLEAN      NOT NULL DEFAULT FALSE,
    created_at TIMESTAMPTZ  NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- 외래키 제약조건 추가
ALTER TABLE notification_settings
    ADD CONSTRAINT fk_notification_settings_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE;
    ADD CONSTRAINT fk_notification_settings_device FOREIGN KEY (device_id) REFERENCES device_token(device_id) ON DELETE CASCADE;

ALTER TABLE notifications
    ADD CONSTRAINT fk_notifications_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    ADD CONSTRAINT fk_notifications_sender FOREIGN KEY (sender_id) REFERENCES users(id) ON DELETE SET NULL;

-- 인덱스 생성
CREATE INDEX idx_notification_settings_user ON notification_settings(user_id);
CREATE INDEX idx_notification_settings_user_device ON notification_settings(user_id, device_id);

-- 사용자별 알림 조회에 이용 (읽지 않은 알림, 최신순 정렬 등)
CREATE INDEX idx_notifications_user ON notifications(user_id);
CREATE INDEX idx_notifications_user_read ON notifications(user_id, is_read);
CREATE INDEX idx_notifications_user_created ON notifications(user_id, created_at);
-- 특정 컬렉션 관련 알림 조회에 이용
CREATE INDEX idx_notifications_type_related ON notifications(type, related_id);
