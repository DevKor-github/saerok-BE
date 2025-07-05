CREATE SEQUENCE user_bird_collection_comment_seq START WITH 1 INCREMENT BY 50;

-- 테이블 생성
CREATE TABLE user_bird_collection_comment (
                                           id                      BIGINT      NOT NULL PRIMARY KEY,
                                           user_id                 BIGINT      NOT NULL,
                                           user_bird_collection_id BIGINT      NOT NULL,
                                           content                 TEXT        NOT NULL,
                                           created_at              TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                           updated_at              TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- 외래키 제약조건 추가
ALTER TABLE user_bird_collection_comment
    ADD CONSTRAINT fk_user_bird_collection_comment_user FOREIGN KEY (user_id) REFERENCES users(id),
    ADD CONSTRAINT fk_user_bird_collection_comment_collection FOREIGN KEY (user_bird_collection_id) REFERENCES user_bird_collection(id) ON DELETE CASCADE;

-- 인덱스 생성
-- 특정 컬렉션의 댓글 목록 조회, 특정 사용자가 쓴 댓글 조회에 이용
CREATE INDEX idx_user_bird_collection_comment_user ON user_bird_collection_comment(user_id);
CREATE INDEX idx_user_bird_collection_comment_collection ON user_bird_collection_comment(user_bird_collection_id);
