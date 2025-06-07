-- 시퀀스 생성 (V1 컨벤션 준수)
CREATE SEQUENCE banned_words_seq START WITH 1 INCREMENT BY 1;

-- 금칙어 테이블 생성
CREATE TABLE banned_words (
    id          BIGINT      NOT NULL PRIMARY KEY,
    word        VARCHAR(50) NOT NULL UNIQUE,
    created_at  TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- 초기 데이터 삽입 (서비스명 관련)
INSERT INTO banned_words (word) VALUES 
('새록'),
('saerok'),
('SAEROK');
