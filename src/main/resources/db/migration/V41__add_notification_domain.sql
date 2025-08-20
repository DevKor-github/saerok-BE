-- 알림 설정 시퀀스 생성
CREATE SEQUENCE notification_setting_seq START WITH 1 INCREMENT BY 50;
-- 알림 시퀀스 생성
CREATE SEQUENCE notification_seq START WITH 1 INCREMENT BY 50;


-- 알림 설정 테이블 생성
CREATE TABLE notification_setting (
    id              BIGINT        NOT NULL PRIMARY KEY,
    user_device_id  BIGINT        NOT NULL,
    type            VARCHAR(50)   NOT NULL,
    enabled         BOOLEAN       NOT NULL DEFAULT true,
    created_at      TIMESTAMPTZ   NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMPTZ   NOT NULL DEFAULT CURRENT_TIMESTAMP,

    -- user_device당 알림 타입별로 고유한 설정
    CONSTRAINT uq_notification_setting_user_device_type UNIQUE (user_device_id, type)
);
-- 알림 테이블 생성
CREATE TABLE notification (
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
ALTER TABLE notification_setting
    ADD CONSTRAINT fk_notification_setting_user_device FOREIGN KEY (user_device_id) REFERENCES user_device(id) ON DELETE CASCADE;

ALTER TABLE notification
    ADD CONSTRAINT fk_notification_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    ADD CONSTRAINT fk_notification_sender FOREIGN KEY (sender_id) REFERENCES users(id) ON DELETE SET NULL;

-- 인덱스 생성
-- 사용자별 알림 조회에 이용 (읽지 않은 알림, 최신순 정렬 등)
CREATE INDEX idx_notification_user ON notification (user_id);
CREATE INDEX idx_notification_user_read ON notification (user_id, is_read);
