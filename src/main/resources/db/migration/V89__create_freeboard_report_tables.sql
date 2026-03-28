-- 자유게시판 게시글 신고
CREATE SEQUENCE free_board_post_report_seq START WITH 1 INCREMENT BY 50;

CREATE TABLE free_board_post_report (
    id              BIGINT       NOT NULL DEFAULT nextval('free_board_post_report_seq'),
    reporter_id     BIGINT       NOT NULL,
    reported_user_id BIGINT      NOT NULL,
    free_board_post_id BIGINT    NOT NULL,
    created_at      TIMESTAMPTZ  NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT pk_free_board_post_report PRIMARY KEY (id),
    CONSTRAINT fk_free_board_post_report_reporter FOREIGN KEY (reporter_id) REFERENCES users (id),
    CONSTRAINT fk_free_board_post_report_reported_user FOREIGN KEY (reported_user_id) REFERENCES users (id),
    CONSTRAINT fk_free_board_post_report_post FOREIGN KEY (free_board_post_id) REFERENCES free_board_post (id) ON DELETE CASCADE
);

CREATE INDEX idx_free_board_post_report_post_id ON free_board_post_report (free_board_post_id);

-- 자유게시판 댓글 신고
CREATE SEQUENCE free_board_post_comment_report_seq START WITH 1 INCREMENT BY 50;

CREATE TABLE free_board_post_comment_report (
    id              BIGINT       NOT NULL DEFAULT nextval('free_board_post_comment_report_seq'),
    reporter_id     BIGINT       NOT NULL,
    reported_user_id BIGINT      NOT NULL,
    comment_id      BIGINT       NOT NULL,
    comment_content TEXT         NOT NULL,
    created_at      TIMESTAMPTZ  NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT pk_free_board_post_comment_report PRIMARY KEY (id),
    CONSTRAINT fk_free_board_post_comment_report_reporter FOREIGN KEY (reporter_id) REFERENCES users (id),
    CONSTRAINT fk_free_board_post_comment_report_reported_user FOREIGN KEY (reported_user_id) REFERENCES users (id),
    CONSTRAINT fk_free_board_post_comment_report_comment FOREIGN KEY (comment_id) REFERENCES free_board_post_comment (id) ON DELETE CASCADE
);

CREATE INDEX idx_free_board_post_comment_report_comment_id ON free_board_post_comment_report (comment_id);
