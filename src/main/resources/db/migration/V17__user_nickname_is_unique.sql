ALTER TABLE users
ADD CONSTRAINT uq_users_nickname UNIQUE (nickname);