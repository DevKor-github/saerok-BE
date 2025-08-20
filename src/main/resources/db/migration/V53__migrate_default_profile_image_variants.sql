-- 기본 프로필 이미지 개수 변경에 따른 기존 사용자들의 variant 값 재할당

UPDATE users 
SET default_profile_image_variant = (default_profile_image_variant % 2)::smallint
WHERE default_profile_image_variant IS NOT NULL;
