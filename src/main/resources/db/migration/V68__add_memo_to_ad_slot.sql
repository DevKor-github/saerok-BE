-- ad_slot 테이블에 관리자 메모 컬럼 추가
ALTER TABLE ad_slot
    ADD COLUMN IF NOT EXISTS memo TEXT;
