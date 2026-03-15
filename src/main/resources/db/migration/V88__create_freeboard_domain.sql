-- 자유게시판 게시글 시퀀스
CREATE SEQUENCE free_board_post_seq START WITH 1 INCREMENT BY 50;

-- 자유게시판 게시글 테이블
CREATE TABLE free_board_post (
    id         BIGINT      NOT NULL PRIMARY KEY,
    user_id    BIGINT      NOT NULL,
    content    TEXT        NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

ALTER TABLE free_board_post
    ADD CONSTRAINT fk_free_board_post_user FOREIGN KEY (user_id) REFERENCES users(id);

CREATE INDEX idx_free_board_post_created_at ON free_board_post(created_at DESC);

-- 자유게시판 댓글 시퀀스
CREATE SEQUENCE free_board_post_comment_seq START WITH 1 INCREMENT BY 50;

-- 자유게시판 댓글 테이블
CREATE TABLE free_board_post_comment (
    id                 BIGINT      NOT NULL PRIMARY KEY,
    user_id            BIGINT      NOT NULL,
    free_board_post_id BIGINT      NOT NULL,
    content            TEXT        NOT NULL,
    status             VARCHAR(32) NOT NULL DEFAULT 'ACTIVE',
    parent_id          BIGINT,
    created_at         TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at         TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

ALTER TABLE free_board_post_comment
    ADD CONSTRAINT fk_free_board_post_comment_user   FOREIGN KEY (user_id)            REFERENCES users(id),
    ADD CONSTRAINT fk_free_board_post_comment_post   FOREIGN KEY (free_board_post_id) REFERENCES free_board_post(id) ON DELETE CASCADE,
    ADD CONSTRAINT fk_free_board_post_comment_parent FOREIGN KEY (parent_id)          REFERENCES free_board_post_comment(id) ON DELETE CASCADE;

CREATE INDEX idx_free_board_post_comment_post   ON free_board_post_comment(free_board_post_id);
CREATE INDEX idx_free_board_post_comment_parent ON free_board_post_comment(parent_id);

