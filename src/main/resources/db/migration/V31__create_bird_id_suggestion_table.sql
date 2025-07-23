-- 시퀀스 생성
CREATE SEQUENCE bird_id_suggestion_seq START WITH 1 INCREMENT BY 50;

-- 테이블 생성
CREATE TABLE bird_id_suggestion (
                                    id                      BIGINT       NOT NULL PRIMARY KEY,
                                    user_id                 BIGINT       NOT NULL,
                                    user_bird_collection_id BIGINT       NOT NULL,
                                    bird_id                 BIGINT       NOT NULL,
                                    created_at              TIMESTAMPTZ  NOT NULL DEFAULT CURRENT_TIMESTAMP,

                                    CONSTRAINT uq_bird_id_suggestion_user_collection_bird UNIQUE (user_id, user_bird_collection_id, bird_id)
);

-- 외래키 제약조건 추가
ALTER TABLE bird_id_suggestion
    ADD CONSTRAINT fk_bird_id_suggestion_user FOREIGN KEY (user_id) REFERENCES users(id),
    ADD CONSTRAINT fk_bird_id_suggestion_collection FOREIGN KEY (user_bird_collection_id) REFERENCES user_bird_collection(id) ON DELETE CASCADE,
    ADD CONSTRAINT fk_bird_id_suggestion_bird FOREIGN KEY (bird_id) REFERENCES bird(id);
