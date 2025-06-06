-- 금칙어 테이블 생성
CREATE TABLE banned_words (
    id          BIGINT      AUTO_INCREMENT PRIMARY KEY,
    word        VARCHAR(50) NOT NULL UNIQUE,
    created_at  TIMESTAMP   DEFAULT CURRENT_TIMESTAMP,
    updated_at  TIMESTAMP   DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    is_active   BOOLEAN     DEFAULT TRUE
);

-- 인덱스 생성 (검색 성능 최적화)
CREATE INDEX idx_banned_words_word ON banned_words(word);
CREATE INDEX idx_banned_words_active ON banned_words(is_active);

-- 초기 데이터 삽입 (서비스명 관련)
INSERT INTO banned_words (word) VALUES 
('새록'),
('saerok'),
('SAEROK');
