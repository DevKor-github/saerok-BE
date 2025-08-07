-- bird_id_suggestion 테이블에 type 컬럼 추가 및 제약조건 수정

-- type 컬럼 추가 (기본값은 SUGGEST)
ALTER TABLE bird_id_suggestion 
ADD COLUMN type VARCHAR(20) NOT NULL DEFAULT 'SUGGEST';

-- 기존 unique 제약조건 삭제
ALTER TABLE bird_id_suggestion 
DROP CONSTRAINT uq_bird_id_suggestion_user_collection_bird;

-- 새로운 unique 제약조건 추가 (type 포함)
ALTER TABLE bird_id_suggestion 
ADD CONSTRAINT uq_bird_id_suggestion_user_collection_bird_type 
UNIQUE (user_id, user_bird_collection_id, bird_id, type);

-- 인덱스 추가 (성능 최적화)
CREATE INDEX idx_bird_id_suggestion_collection_bird_type ON bird_id_suggestion(user_bird_collection_id, bird_id, type);
CREATE INDEX idx_bird_id_suggestion_type ON bird_id_suggestion(type);

-- 타입 값 체크 제약조건 추가
ALTER TABLE bird_id_suggestion 
ADD CONSTRAINT chk_bird_id_suggestion_type 
CHECK (type IN ('SUGGEST', 'AGREE', 'DISAGREE'));
