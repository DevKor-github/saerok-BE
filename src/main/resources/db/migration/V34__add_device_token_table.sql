CREATE SEQUENCE device_token_seq START WITH 1 INCREMENT BY 50;

-- 디바이스 토큰 테이블 생성
CREATE TABLE device_token (
    id         BIGINT        NOT NULL PRIMARY KEY,
    user_id    BIGINT        NOT NULL,
    device_id  VARCHAR(256)  NOT NULL,
    token      VARCHAR(512)  NOT NULL,
    is_active  BOOLEAN       NOT NULL DEFAULT true,
    created_at TIMESTAMPTZ   NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ   NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT uq_device_token_device UNIQUE (device_id)

);

-- 외래키 제약조건 추가
ALTER TABLE device_token
    ADD CONSTRAINT fk_device_token_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE;

-- 인덱스 생성
-- 사용자별 디바이스 토큰 조회에 이용
CREATE INDEX idx_device_token_user ON device_token(user_id);
CREATE INDEX idx_device_token_user_device ON device_token(user_id, device_id);
-- 전체 공지/업데이트 알림 발송 시 활성 토큰 조회에 이용
CREATE INDEX idx_device_token_user_active ON device_token(user_id, is_active);
-- 토큰 중복 확인 및 검색에 이용
CREATE INDEX idx_device_token_token ON device_token(token);
