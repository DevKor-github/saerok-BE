-- 관리자 감사 로그 테이블 및 시퀀스
CREATE SEQUENCE admin_audit_log_seq START WITH 1 INCREMENT BY 50;

CREATE TABLE admin_audit_log (
                                 id BIGINT NOT NULL PRIMARY KEY,
                                 admin_user_id BIGINT NOT NULL,
                                 action VARCHAR(50) NOT NULL,
                                 target_type VARCHAR(50) NOT NULL,
                                 target_id BIGINT,
                                 report_id BIGINT,
                                 metadata JSONB,
                                 created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

ALTER TABLE admin_audit_log
    ADD CONSTRAINT fk_admin_audit_log_admin_user
        FOREIGN KEY (admin_user_id) REFERENCES users(id);

CREATE INDEX idx_admin_audit_log_admin ON admin_audit_log(admin_user_id, created_at);
CREATE INDEX idx_admin_audit_log_action ON admin_audit_log(action);
CREATE INDEX idx_admin_audit_log_target ON admin_audit_log(target_type, target_id);
