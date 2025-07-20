CREATE SEQUENCE user_bird_collection_report_seq START WITH 1 INCREMENT BY 50;

-- 테이블 생성
CREATE TABLE user_bird_collection_report (
    id               BIGINT       NOT NULL PRIMARY KEY,
    reporter_id      BIGINT       NOT NULL,
    reported_user_id BIGINT       NOT NULL,
    collection_id    BIGINT       NOT NULL,
    created_at       TIMESTAMPTZ  NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- 외래키 제약조건 추가
ALTER TABLE user_bird_collection_report
    ADD CONSTRAINT fk_user_bird_collection_report_reporter FOREIGN KEY (reporter_id) REFERENCES users(id),
    ADD CONSTRAINT fk_user_bird_collection_report_reported_user FOREIGN KEY (reported_user_id) REFERENCES users(id),
    ADD CONSTRAINT fk_user_bird_collection_report_collection FOREIGN KEY (collection_id) REFERENCES user_bird_collection(id) ON DELETE CASCADE;
