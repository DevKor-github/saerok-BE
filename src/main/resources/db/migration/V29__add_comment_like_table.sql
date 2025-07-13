CREATE SEQUENCE user_bird_collection_comment_like_seq START WITH 1 INCREMENT BY 50;

-- 테이블 생성
CREATE TABLE user_bird_collection_comment_like (
       id                              BIGINT      NOT NULL PRIMARY KEY,
       user_id                         BIGINT      NOT NULL,
       user_bird_collection_comment_id BIGINT      NOT NULL,
       created_at                      TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

       CONSTRAINT uq_user_bird_collection_comment_like_user_comment UNIQUE (user_id, user_bird_collection_comment_id)
);

-- 외래키 제약조건 추가
ALTER TABLE user_bird_collection_comment_like
    ADD CONSTRAINT fk_user_bird_collection_comment_like_user FOREIGN KEY (user_id) REFERENCES users(id),
    ADD CONSTRAINT fk_user_bird_collection_comment_like_comment FOREIGN KEY (user_bird_collection_comment_id) REFERENCES user_bird_collection_comment(id) ON DELETE CASCADE;

-- 인덱스 생성
-- 댓글별 좋아요 수 count 시에 이용
CREATE INDEX idx_user_bird_collection_comment_like_comment ON user_bird_collection_comment_like(user_bird_collection_comment_id);