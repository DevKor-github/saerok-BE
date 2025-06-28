CREATE SEQUENCE user_bird_collection_like_seq START WITH 1 INCREMENT BY 50;

-- 테이블 생성
CREATE TABLE user_bird_collection_like (
    id                      BIGINT      NOT NULL PRIMARY KEY,
    user_id                 BIGINT      NOT NULL,
    user_bird_collection_id BIGINT      NOT NULL,
    created_at              TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT uq_user_bird_collection_like_user_collection UNIQUE (user_id, user_bird_collection_id)
);

-- 외래키 제약조건 추가
ALTER TABLE user_bird_collection_like
    ADD CONSTRAINT fk_user_bird_collection_like_user FOREIGN KEY (user_id) REFERENCES users(id),
    ADD CONSTRAINT fk_user_bird_collection_like_collection FOREIGN KEY (user_bird_collection_id) REFERENCES user_bird_collection(id) ON DELETE CASCADE;

-- 인덱스 생성
-- 좋아요 누른 컬렉션 목록 조회, 좋아요 누른 유저 조회 시에 이용
CREATE INDEX idx_user_bird_collection_like_user ON user_bird_collection_like(user_id);
CREATE INDEX idx_user_bird_collection_like_collection ON user_bird_collection_like(user_bird_collection_id);
