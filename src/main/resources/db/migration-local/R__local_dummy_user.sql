INSERT INTO users (id, nickname, email, signup_status, created_at, updated_at)
VALUES (
           99999,
           'dummy.saerokuser',
           'fake-email@saerok.com',
           'COMPLETED',
           now(),
           now()
       );

INSERT INTO user_role(id, user_id, role)
VALUES (
            99999,
            99999,
            'USER'
       )