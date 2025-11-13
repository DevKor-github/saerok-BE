-- V67__alter_ad_event_log_device_hash_to_varchar.sql
-- ad_event_log.device_hash 컬럼을 CHAR(64)에서 VARCHAR(64)로 변경

ALTER TABLE ad_event_log
ALTER COLUMN device_hash TYPE varchar(64);
