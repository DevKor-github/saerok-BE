INSERT INTO users (id, nickname, email, signup_status, joined_at, created_at, updated_at)
VALUES (
           99999,
           'dummy.saerokuser',
           'fake-email@saerok.com',
           'COMPLETED',
           now(),
           now(),
           now()
       ) ON CONFLICT (id) DO NOTHING;

INSERT INTO user_role(id, user_id, role)
VALUES (
            99999,
            99999,
            'USER'
       ) ON CONFLICT (id) DO NOTHING;

-- 더미 유저 기본 프로필 이미지 추가
INSERT INTO user_profile_image (id, user_id, object_key, content_type, created_at, updated_at)
VALUES (
    99999,
    99999,
    'profile-images/default/default.png',
    'image/png',
    now(),
    now()
) ON CONFLICT (id) DO NOTHING;