ALTER TABLE social_auth
    ADD COLUMN refresh_token_ciphertext BYTEA,
    ADD COLUMN refresh_token_key BYTEA,
    ADD COLUMN refresh_token_iv BYTEA,
    ADD COLUMN refresh_token_tag BYTEA;