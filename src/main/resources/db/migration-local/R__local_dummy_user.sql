-- 로컬 더미 유저 생성 (없을 경우에만)
INSERT INTO users (id, nickname, email, signup_status, joined_at, created_at, updated_at)
VALUES (
           99999,
           'dummy.saerokuser',
           'fake-email@saerok.com',
           'COMPLETED',
           now(),
           now(),
           now()
       )
ON CONFLICT (id) DO NOTHING;

-- 이미 존재하는 로컬 더미 유저의 joined_at 값이 NULL이면 채우기
UPDATE users
SET joined_at = now()
WHERE id = 99999
  AND joined_at IS NULL;

-- 로컬 더미 유저 역할 생성 (없을 경우에만)
INSERT INTO user_role(id, user_id, role_id)
VALUES (
           99999,
           99999,
           1 -- USER
       )
ON CONFLICT (id) DO NOTHING;