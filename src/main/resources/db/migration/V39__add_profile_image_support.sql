-- 프로필 사진 기능을 지원하기 위해 user_profile_image 테이블 추가 및 users 테이블 칼럼 추가

-- 1) user_profile_image 테이블은 사용자가 직접 올린 프로필 사진을 관리

CREATE SEQUENCE user_profile_image_seq START WITH 1 INCREMENT BY 50;

CREATE TABLE user_profile_image (
    id           BIGINT       NOT NULL PRIMARY KEY,
    user_id      BIGINT       NOT NULL,
    object_key   VARCHAR(255) NOT NULL,
    content_type VARCHAR(100) NOT NULL,
    created_at   TIMESTAMPTZ  NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT uq_user_profile_image_user UNIQUE (user_id)
);

ALTER TABLE user_profile_image
    ADD CONSTRAINT fk_user_profile_image_user FOREIGN KEY (user_id) REFERENCES users(id);

-- 2) users.default_profile_image_variant 컬럼 추가
ALTER TABLE users
    ADD COLUMN default_profile_image_variant SMALLINT;

-- 2-1) 기존 유저의 default_profile_image_variant를 결정적으로 초기화 (기본 프사 개수 6개 기준으로 0~5)
UPDATE users
SET default_profile_image_variant = (id % 6)::smallint
WHERE default_profile_image_variant IS NULL;
