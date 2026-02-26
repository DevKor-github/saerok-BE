-- 댓글 테이블에 status, parent_id 컬럼 추가
ALTER TABLE user_bird_collection_comment
    ADD COLUMN status VARCHAR(32) NOT NULL DEFAULT 'ACTIVE',
    ADD COLUMN parent_id BIGINT;

-- 외래키 제약조건 추가
ALTER TABLE user_bird_collection_comment
    ADD CONSTRAINT fk_user_bird_collection_comment_parent FOREIGN KEY (parent_id) REFERENCES user_bird_collection_comment(id) ON DELETE CASCADE;

-- 인덱스 생성
-- 특정 댓글의 대댓글 목록 조회에 이용
CREATE INDEX idx_user_bird_collection_comment_parent ON user_bird_collection_comment(parent_id);
