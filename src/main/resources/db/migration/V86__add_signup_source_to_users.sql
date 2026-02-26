-- 회원가입 경로 추적을 위한 컬럼 추가
ALTER TABLE users
    ADD COLUMN signup_source VARCHAR(30);
