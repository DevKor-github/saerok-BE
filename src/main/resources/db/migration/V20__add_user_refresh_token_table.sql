CREATE SEQUENCE user_refresh_token_seq START WITH 1 INCREMENT BY 50;

CREATE TABLE user_refresh_token (
                                    id                   BIGINT PRIMARY KEY,
                                    user_id              BIGINT NOT NULL REFERENCES users(id),
                                    refresh_token_hash   VARCHAR(64) NOT NULL,
                                    user_agent           VARCHAR(255),
                                    ip_address           VARCHAR(45),
                                    issued_at            TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                    expires_at           TIMESTAMPTZ NOT NULL,
                                    revoked_at           TIMESTAMPTZ,

                                    CONSTRAINT uq_user_refresh_token_hash UNIQUE (user_id, refresh_token_hash)
);