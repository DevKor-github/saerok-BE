-- V1__init_schema.sql

/* ───────────────────────────────────────────────────────────────────────────
   0. PostGIS 확장 (geometry(Point,4326) 사용을 위해 필수)
─────────────────────────────────────────────────────────────────────────── */
CREATE EXTENSION IF NOT EXISTS postgis;

/* ───────────────────────────────────────────────────────────────────────────
   1. 시퀀스 정의 (Hibernate 기본 INCREMENT BY 50 유지)
─────────────────────────────────────────────────────────────────────────── */
CREATE SEQUENCE bird_seq                       START WITH 1 INCREMENT BY 50;
CREATE SEQUENCE bird_habitat_seq               START WITH 1 INCREMENT BY 50;
CREATE SEQUENCE bird_image_seq                 START WITH 1 INCREMENT BY 50;
CREATE SEQUENCE bird_residency_seq             START WITH 1 INCREMENT BY 50;
CREATE SEQUENCE rarity_type_seq                START WITH 1 INCREMENT BY 50;
CREATE SEQUENCE residency_type_seq             START WITH 1 INCREMENT BY 50;
CREATE SEQUENCE users_seq                      START WITH 1 INCREMENT BY 50;
CREATE SEQUENCE user_bird_collection_seq       START WITH 1 INCREMENT BY 50;
CREATE SEQUENCE user_bird_collection_image_seq START WITH 1 INCREMENT BY 50;
CREATE SEQUENCE user_bird_bookmark_seq         START WITH 1 INCREMENT BY 50;
CREATE SEQUENCE user_auth_seq                  START WITH 1 INCREMENT BY 50;
CREATE SEQUENCE social_auth_seq                START WITH 1 INCREMENT BY 50;

/* ───────────────────────────────────────────────────────────────────────────
   2. ENUM 타입 정의
─────────────────────────────────────────────────────────────────────────── */
CREATE TYPE gender_enum         AS ENUM ('MALE','FEMALE','OTHER');
CREATE TYPE rarity_code_enum    AS ENUM ('COMMON','UNSPECIFIED','RARE');
CREATE TYPE residency_code_enum AS ENUM ('RESIDENT','SUMMER','WINTER','PASSAGE','VAGRANT');
CREATE TYPE access_level_enum   AS ENUM ('PUBLIC','PRIVATE');
CREATE TYPE habitat_type_enum   AS ENUM (
  'MUDFLAT','FARMLAND','FOREST','MARINE','RESIDENTIAL',
  'PLAINS_FOREST','RIVER_LAKE','ARTIFICIAL','CAVE','WETLAND','OTHERS'
);

/* ───────────────────────────────────────────────────────────────────────────
   3. 테이블 생성
─────────────────────────────────────────────────────────────────────────── */

CREATE TABLE bird (
                      id                        BIGINT        NOT NULL PRIMARY KEY,
                      created_at                TIMESTAMPTZ   NOT NULL DEFAULT CURRENT_TIMESTAMP,
                      updated_at                TIMESTAMPTZ   NOT NULL DEFAULT CURRENT_TIMESTAMP,
                      body_length_cm            DOUBLE PRECISION,
                      scientific_year           INTEGER,
                      description_is_ai_generated BOOLEAN,
                      class_eng                 VARCHAR(100)  NOT NULL,
                      class_kor                 VARCHAR(100)  NOT NULL,
                      order_eng                 VARCHAR(100)  NOT NULL,
                      order_kor                 VARCHAR(100)  NOT NULL,
                      family_eng                VARCHAR(100)  NOT NULL,
                      family_kor                VARCHAR(100)  NOT NULL,
                      genus_eng                 VARCHAR(100)  NOT NULL,
                      genus_kor                 VARCHAR(100)  NOT NULL,
                      species_eng               VARCHAR(100)  NOT NULL,
                      species_kor               VARCHAR(100)  NOT NULL,
                      korean_name               VARCHAR(50)   NOT NULL,
                      scientific_name           VARCHAR(100)  NOT NULL,
                      scientific_author         VARCHAR(255),
                      phylum_eng                VARCHAR(100)  NOT NULL,
                      phylum_kor                VARCHAR(100)  NOT NULL,
                      nibr_url                  TEXT,
                      description               TEXT,
                      description_source        TEXT
);

CREATE TABLE rarity_type (
                             id       BIGINT             NOT NULL PRIMARY KEY,
                             priority INTEGER            NOT NULL,
                             code     rarity_code_enum   NOT NULL
);

CREATE TABLE residency_type (
                                id            BIGINT                NOT NULL PRIMARY KEY,
                                month_bitmask INTEGER               NOT NULL,  -- 12비트 월 마스크 (1월=LSB, 12월=MSB)
                                code          residency_code_enum   NOT NULL,
                                CONSTRAINT ck_residency_type_month_bitmask
                                    CHECK (month_bitmask BETWEEN 0 AND 4095)
);

CREATE TABLE bird_habitat (
                              id           BIGINT               NOT NULL PRIMARY KEY,
                              bird_id      BIGINT               NOT NULL,
                              habitat_type habitat_type_enum
);

CREATE TABLE bird_image (
                            id           BIGINT        NOT NULL PRIMARY KEY,
                            bird_id      BIGINT        NOT NULL,
                            created_at   TIMESTAMPTZ   NOT NULL DEFAULT CURRENT_TIMESTAMP,
                            updated_at   TIMESTAMPTZ   NOT NULL DEFAULT CURRENT_TIMESTAMP,
                            is_thumb     BOOLEAN       NOT NULL,
                            order_index  INTEGER       NOT NULL,
                            original_url TEXT          NOT NULL,
                            s3_url       TEXT          NOT NULL
);

CREATE TABLE bird_residency (
                                id                 BIGINT        NOT NULL PRIMARY KEY,
                                bird_id            BIGINT        NOT NULL,
                                rarity_type_id     BIGINT        NOT NULL,
                                residency_type_id  BIGINT        NOT NULL,
                                month_bitmask      INTEGER,
                                created_at         TIMESTAMPTZ   NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                updated_at         TIMESTAMPTZ   NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                CONSTRAINT ck_bird_residency_month_bitmask
                                    CHECK (
                                        month_bitmask IS NULL
                                            OR month_bitmask BETWEEN 0 AND 4095   -- 12비트 범위
                                        )
);

CREATE TABLE users (
                       id          BIGINT        NOT NULL PRIMARY KEY,
                       created_at  TIMESTAMPTZ   NOT NULL DEFAULT CURRENT_TIMESTAMP,
                       updated_at  TIMESTAMPTZ   NOT NULL DEFAULT CURRENT_TIMESTAMP,
                       deleted_at  TIMESTAMPTZ,
                       name        VARCHAR(50)   NOT NULL,
                       nickname    VARCHAR(50)   NOT NULL,
                       email       VARCHAR(320) UNIQUE,
                       phone       VARCHAR(20) UNIQUE,
                       gender      gender_enum,
                       birth_date  DATE
);

CREATE TABLE user_bird_collection (
                                      id               BIGINT             NOT NULL PRIMARY KEY,
                                      user_id          BIGINT             NOT NULL,
                                      bird_id          BIGINT,
                                      created_at       TIMESTAMPTZ        NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                      updated_at       TIMESTAMPTZ        NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                      discovered_date  DATE               NOT NULL,
                                      access_level     access_level_enum  NOT NULL,
                                      temp_bird_name   VARCHAR(50),
                                      location_alias   VARCHAR(100),
                                      note             VARCHAR(200),
                                      location         GEOMETRY(Point,4326) NOT NULL
);

CREATE TABLE user_bird_collection_image (
                                            id                      BIGINT        NOT NULL PRIMARY KEY,
                                            user_bird_collection_id BIGINT        NOT NULL,
                                            created_at              TIMESTAMPTZ   NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                            updated_at              TIMESTAMPTZ   NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                            order_index             INTEGER       NOT NULL,
                                            s3_url                  TEXT          NOT NULL
);

CREATE TABLE user_bird_bookmark (
                                    id          BIGINT        NOT NULL PRIMARY KEY,
                                    created_at  TIMESTAMPTZ   NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                    user_id     BIGINT        NOT NULL,
                                    bird_id     BIGINT        NOT NULL,
                                    CONSTRAINT uq_user_bird_bookmark_user_bird UNIQUE(user_id, bird_id)
);

CREATE TABLE user_auth (
                           id             BIGINT        NOT NULL PRIMARY KEY,
                           created_at     TIMESTAMPTZ   NOT NULL DEFAULT CURRENT_TIMESTAMP,
                           updated_at     TIMESTAMPTZ   NOT NULL DEFAULT CURRENT_TIMESTAMP,
                           user_id        BIGINT        NOT NULL UNIQUE,
                           login_id       VARCHAR(255)  NOT NULL UNIQUE,
                           password       VARCHAR(255)  NOT NULL,
                           last_login_at  TIMESTAMPTZ
);

CREATE TABLE social_auth (
                             id                   BIGINT        NOT NULL PRIMARY KEY,
                             created_at           TIMESTAMPTZ   NOT NULL DEFAULT CURRENT_TIMESTAMP,
                             user_id              BIGINT        NOT NULL,
                             provider             VARCHAR(255)  NOT NULL,
                             provider_user_id     VARCHAR(255)  NOT NULL,
                             last_login_at        TIMESTAMPTZ,
                             CONSTRAINT uq_social_auth_provider_user UNIQUE(provider, provider_user_id)
);


/* ───────────────────────────────────────────────────────────────────────────
   4. 외래키(FK) 및 인덱스
─────────────────────────────────────────────────────────────────────────── */

ALTER TABLE bird_habitat
    ADD CONSTRAINT fk_bird_habitat_bird FOREIGN KEY (bird_id) REFERENCES bird(id);

ALTER TABLE bird_image
    ADD CONSTRAINT fk_bird_image_bird FOREIGN KEY (bird_id) REFERENCES bird(id);

ALTER TABLE bird_residency
    ADD CONSTRAINT fk_bird_residency_bird      FOREIGN KEY (bird_id)        REFERENCES bird(id),
  ADD CONSTRAINT fk_bird_residency_rarity    FOREIGN KEY (rarity_type_id) REFERENCES rarity_type(id),
  ADD CONSTRAINT fk_bird_residency_residency FOREIGN KEY (residency_type_id) REFERENCES residency_type(id);

ALTER TABLE user_bird_collection
    ADD CONSTRAINT fk_collection_user FOREIGN KEY (user_id) REFERENCES users(id),
  ADD CONSTRAINT fk_collection_bird FOREIGN KEY (bird_id) REFERENCES bird(id);

ALTER TABLE user_bird_collection_image
    ADD CONSTRAINT fk_collection_image_collection FOREIGN KEY (user_bird_collection_id)
        REFERENCES user_bird_collection(id);

ALTER TABLE user_bird_bookmark
    ADD CONSTRAINT fk_user_bird_bookmark_user FOREIGN KEY (user_id) REFERENCES users(id),
  ADD CONSTRAINT fk_user_bird_bookmark_bird FOREIGN KEY (bird_id) REFERENCES bird(id);

ALTER TABLE user_auth
    ADD CONSTRAINT fk_user_auth_user FOREIGN KEY (user_id) REFERENCES users(id);

ALTER TABLE social_auth
    ADD CONSTRAINT fk_social_auth_user FOREIGN KEY (user_id) REFERENCES users(id);

CREATE INDEX idx_bird_image_bird ON bird_image(bird_id);
CREATE INDEX idx_collection_user ON user_bird_collection(user_id);
CREATE INDEX idx_social_auth_user ON social_auth(user_id);

/* 공간 인덱스 (GiST) */
CREATE INDEX idx_user_collection_location
    ON user_bird_collection USING GIST (location);
