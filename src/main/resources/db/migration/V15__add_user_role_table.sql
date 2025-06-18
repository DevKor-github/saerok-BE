CREATE SEQUENCE user_role_seq START WITH 1 INCREMENT BY 50;

CREATE TABLE user_role (
                           id      BIGINT PRIMARY KEY,
                           user_id BIGINT NOT NULL,
                           role    VARCHAR(30) NOT NULL,
                           CONSTRAINT uq_user_role UNIQUE (user_id, role),
                           CONSTRAINT fk_user_role_user
                               FOREIGN KEY (user_id) REFERENCES users(id)
                                   ON DELETE CASCADE
);
