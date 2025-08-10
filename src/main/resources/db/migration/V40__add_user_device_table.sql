CREATE SEQUENCE user_device_seq START WITH 1 INCREMENT BY 50;

-- 디바이스 토큰 테이블 생성
CREATE TABLE user_device (
    id         BIGINT        NOT NULL PRIMARY KEY,
    user_id    BIGINT        NOT NULL,
    device_id  VARCHAR(256)  NOT NULL,
    token      VARCHAR(512)  NOT NULL,
    created_at TIMESTAMPTZ   NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ   NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT uq_user_device_device UNIQUE (user_id, device_id)

);

-- 외래키 제약조건 추가
ALTER TABLE user_device
    ADD CONSTRAINT fk_user_device_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE;

-- 인덱스 생성
-- 사용자별 디바이스 토큰 조회에 이용
CREATE INDEX idx_user_device_user ON user_device(user_id);
-- 토큰 중복 확인 및 검색에 이용
CREATE INDEX idx_user_device_token ON user_device(token);
