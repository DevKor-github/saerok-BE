-- 시퀀스 생성 (Hibernate SEQUENCE, INCREMENT BY 50 컨벤션)
CREATE SEQUENCE daily_stat_seq START WITH 1 INCREMENT BY 50;
CREATE SEQUENCE bird_id_request_history_seq START WITH 1 INCREMENT BY 50;

-- 일일 통계 스냅샷 테이블 (모든 값을 payload(jsonb)에 유연하게 저장)
CREATE TABLE daily_stat (
                            id         BIGINT      NOT NULL PRIMARY KEY,
                            metric     VARCHAR(64) NOT NULL,
                            date       DATE        NOT NULL,
                            payload    JSONB       NOT NULL DEFAULT '{}'::jsonb,
                            created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
                            CONSTRAINT uk_daily_stat_metric_date UNIQUE (metric, date)
);

CREATE INDEX idx_daily_stat_metric_date ON daily_stat(metric, date);

-- 동정 요청 상태 이력 (pending/해결 경로 구분; ADOPT만 집계 대상)
CREATE TABLE bird_id_request_history (
                                         id                  BIGINT      NOT NULL PRIMARY KEY,
                                         collection_id       BIGINT,
                                         started_at          TIMESTAMPTZ NOT NULL,
                                         resolved_at         TIMESTAMPTZ,
                                         resolution_seconds  BIGINT,
                                         resolution_kind     VARCHAR(16), -- ADOPT | EDIT (NULL이면 미해결 상태)
                                         created_at          TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- 외래키 및 인덱스
ALTER TABLE bird_id_request_history
    ADD CONSTRAINT fk_bird_id_req_hist_collection
        FOREIGN KEY (collection_id) REFERENCES user_bird_collection(id) ON DELETE SET NULL;

CREATE INDEX idx_bird_id_req_hist_collection  ON bird_id_request_history(collection_id);
CREATE INDEX idx_bird_id_req_hist_started_at  ON bird_id_request_history(started_at);
CREATE INDEX idx_bird_id_req_hist_resolved_at ON bird_id_request_history(resolved_at);
