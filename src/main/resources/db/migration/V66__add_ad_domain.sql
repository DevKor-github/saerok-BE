-- V66__add_ad_domain.sql
-- 배너 광고 도메인 테이블 및 시퀀스 추가

-- 시퀀스 생성 (관례상 INCREMENT BY 50)
CREATE SEQUENCE ad_seq              START WITH 1 INCREMENT BY 50;
CREATE SEQUENCE ad_slot_seq         START WITH 1 INCREMENT BY 50;
CREATE SEQUENCE ad_placement_seq    START WITH 1 INCREMENT BY 50;
CREATE SEQUENCE ad_event_log_seq    START WITH 1 INCREMENT BY 50;

-- 광고 기본 정보
CREATE TABLE ad (
                    id           BIGINT      NOT NULL PRIMARY KEY,
                    name         VARCHAR(255) NOT NULL,
                    memo         TEXT,
                    object_key   TEXT        NOT NULL,
                    content_type VARCHAR(100) NOT NULL,
                    target_url   TEXT        NOT NULL,
                    created_at   TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
                    updated_at   TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- 광고 슬롯
CREATE TABLE ad_slot (
                         id             BIGINT      NOT NULL PRIMARY KEY,
                         name           VARCHAR(100) NOT NULL,
                         fallback_ratio DOUBLE PRECISION NOT NULL DEFAULT 0.0,
                         ttl_seconds    INTEGER     NOT NULL,
                         created_at     TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
                         updated_at     TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
                         CONSTRAINT uq_ad_slot_name UNIQUE (name)
);

-- 광고 배치
CREATE TABLE ad_placement (
                              id         BIGINT      NOT NULL PRIMARY KEY,
                              ad_id      BIGINT      NOT NULL,
                              slot_id    BIGINT      NOT NULL,
                              start_date DATE        NOT NULL,
                              end_date   DATE        NOT NULL,
                              weight     SMALLINT    NOT NULL,
                              enabled    BOOLEAN     NOT NULL DEFAULT TRUE,
                              created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
                              updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

ALTER TABLE ad_placement
    ADD CONSTRAINT fk_ad_placement_ad
        FOREIGN KEY (ad_id) REFERENCES ad(id) ON DELETE CASCADE;

ALTER TABLE ad_placement
    ADD CONSTRAINT fk_ad_placement_slot
        FOREIGN KEY (slot_id) REFERENCES ad_slot(id) ON DELETE CASCADE;

-- 광고 이벤트 로그
CREATE TABLE ad_event_log (
                              id          BIGINT      NOT NULL PRIMARY KEY,
                              ad_id       BIGINT      NOT NULL,
                              slot_name   VARCHAR(100) NOT NULL,
                              event_type  VARCHAR(16) NOT NULL,
                              device_hash CHAR(64)    NOT NULL,
                              created_at  TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

ALTER TABLE ad_event_log
    ADD CONSTRAINT fk_ad_event_log_ad
        FOREIGN KEY (ad_id) REFERENCES ad(id) ON DELETE CASCADE;

CREATE INDEX idx_ad_event_log_ad_type_created_at
    ON ad_event_log (ad_id, event_type, created_at);

CREATE INDEX idx_ad_event_log_device_hash
    ON ad_event_log (device_hash);
