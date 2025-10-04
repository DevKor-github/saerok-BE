CREATE SEQUENCE popular_collection_seq START WITH 1 INCREMENT BY 50;

-- 테이블 생성
CREATE TABLE popular_collection (
    id                      BIGINT      NOT NULL PRIMARY KEY,
    user_bird_collection_id BIGINT      NOT NULL,
    created_at              TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT uq_popular_collection_user_bird_collection UNIQUE (user_bird_collection_id)
);

-- 외래키 제약조건 추가
ALTER TABLE popular_collection
    ADD CONSTRAINT fk_popular_collection_user_bird_collection FOREIGN KEY (user_bird_collection_id) REFERENCES user_bird_collection(id) ON DELETE CASCADE;

-- 인덱스 생성
CREATE INDEX idx_popular_collection_created_at ON popular_collection(created_at);
