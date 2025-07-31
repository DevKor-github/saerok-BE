-- 기존 사용자들에게 기본 프로필 이미지 추가

INSERT INTO user_profile_image (user_id, object_key, content_type, created_at, updated_at)
SELECT 
    u.id as user_id,
    CASE 
        WHEN u.id % 6 = 0 THEN 'profile-images/default/default-1.png'
        WHEN u.id % 6 = 1 THEN 'profile-images/default/default-2.png'
        WHEN u.id % 6 = 2 THEN 'profile-images/default/default-3.png'
        WHEN u.id % 6 = 3 THEN 'profile-images/default/default-4.png'
        WHEN u.id % 6 = 4 THEN 'profile-images/default/default-5.png'
        ELSE 'profile-images/default/default-6.png'
    END as object_key,
    'image/png' as content_type,
    NOW() as created_at,
    NOW() as updated_at
FROM users u
WHERE u.id NOT IN (
    SELECT upi.user_id 
    FROM user_profile_image upi 
    WHERE upi.user_id IS NOT NULL
);
