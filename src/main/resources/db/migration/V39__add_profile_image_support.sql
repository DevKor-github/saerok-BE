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

-- 2) users 테이블에 default_profile_image_variant 칼럼을 추가하여, 기본 프로필 사진 set 중 어떤 것을 줄지 설정값으로 관리
-- 초기값을 일괄 설정하지 않고, 애플리케이션 레벨에서 각 user가 필요할 때 lazy하게 설정해주는 방식을 사용

ALTER TABLE users
    ADD COLUMN default_profile_image_variant SMALLINT;