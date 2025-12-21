-- 공지사항 도메인 테이블 및 권한 추가

-- 시퀀스
CREATE SEQUENCE announcement_seq START WITH 1 INCREMENT BY 50;
CREATE SEQUENCE announcement_image_seq START WITH 1 INCREMENT BY 50;

-- 공지사항 본문
CREATE TABLE announcement (
    id            BIGINT       NOT NULL PRIMARY KEY,
    admin_user_id BIGINT       NOT NULL,
    title         VARCHAR(255) NOT NULL,
    content       TEXT         NOT NULL,
    status        VARCHAR(32)  NOT NULL,
    scheduled_at  TIMESTAMPTZ,
    published_at  TIMESTAMPTZ,
    created_at    TIMESTAMPTZ  NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at    TIMESTAMPTZ  NOT NULL DEFAULT CURRENT_TIMESTAMP
);

ALTER TABLE announcement
    ADD CONSTRAINT fk_announcement_admin_user
        FOREIGN KEY (admin_user_id) REFERENCES users(id);

CREATE INDEX idx_announcement_status_scheduled_at
    ON announcement (status, scheduled_at);

CREATE INDEX idx_announcement_published_at
    ON announcement (published_at);

-- 공지사항 이미지
CREATE TABLE announcement_image (
    id               BIGINT      NOT NULL PRIMARY KEY,
    announcement_id  BIGINT      NOT NULL,
    object_key       TEXT        NOT NULL,
    content_type     VARCHAR(100) NOT NULL,
    created_at       TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

ALTER TABLE announcement_image
    ADD CONSTRAINT fk_announcement_image_announcement
        FOREIGN KEY (announcement_id) REFERENCES announcement(id) ON DELETE CASCADE;

-- 관리자 권한 추가
INSERT INTO permission (id, key, description, created_at, updated_at)
SELECT nextval('permission_seq'), 'ADMIN_ANNOUNCEMENT_READ', '공지사항 관리 내역 조회', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM permission WHERE key = 'ADMIN_ANNOUNCEMENT_READ');

INSERT INTO permission (id, key, description, created_at, updated_at)
SELECT nextval('permission_seq'), 'ADMIN_ANNOUNCEMENT_WRITE', '공지사항 생성/수정/삭제', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM permission WHERE key = 'ADMIN_ANNOUNCEMENT_WRITE');