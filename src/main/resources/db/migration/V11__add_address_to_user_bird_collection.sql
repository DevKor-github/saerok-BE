-- user_bird_collection 테이블에 address 컬럼 추가
-- 위/경도를 주소로 변환한 결과를 저장하여 지도 API 사용량을 절약

ALTER TABLE user_bird_collection 
    ADD COLUMN address VARCHAR(512);
