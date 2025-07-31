-- V36: user_profile_image 테이블 생성

-- 시퀀스 생성
CREATE SEQUENCE user_profile_image_seq START WITH 1 INCREMENT BY 50;

-- 테이블 생성
CREATE TABLE user_profile_image (
    id           BIGINT       NOT NULL PRIMARY KEY,
    user_id      BIGINT       NOT NULL,
    object_key   VARCHAR(255) NOT NULL,
    content_type VARCHAR(100) NOT NULL,
    created_at   TIMESTAMPTZ  NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at   TIMESTAMPTZ  NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT uq_user_profile_image_user UNIQUE (user_id)
);

-- 외래키 제약조건
ALTER TABLE user_profile_image
    ADD CONSTRAINT fk_user_profile_image_user FOREIGN KEY (user_id) REFERENCES users(id);
