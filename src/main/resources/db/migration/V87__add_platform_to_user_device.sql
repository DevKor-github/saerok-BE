-- user_device 테이블에 platform 컬럼 추가
-- 기존 데이터는 iOS 앱만 존재했으므로 'IOS'로 설정

ALTER TABLE user_device
    ADD COLUMN platform VARCHAR(16) NOT NULL DEFAULT 'IOS';

-- 기존 unique constraint 삭제 후 platform 포함하여 재생성
-- 같은 user_id + device_id라도 platform이 다르면 별개의 디바이스로 취급
ALTER TABLE user_device
    DROP CONSTRAINT uq_user_device_device;

ALTER TABLE user_device
    ADD CONSTRAINT uq_user_device_user_device_platform UNIQUE (user_id, device_id, platform);
