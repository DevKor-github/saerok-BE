ALTER TABLE users DROP COLUMN name;

INSERT INTO users (id, nickname, created_at, updated_at)
VALUES (
           999,
           'dummy.saerokuser',
           now(),
           now()
       );
