-- USER Role 제거 및 TEAM_MEMBER Role 정보 갱신

-- USER Role 삭제 (user_role 등 FK 는 ON DELETE CASCADE)
DELETE FROM role WHERE code = 'USER';

-- TEAM_MEMBER Role 표시 이름 및 설명 갱신
UPDATE role
SET display_name = '기본 조회',
    description = '새록 어드민에 접속해 기본 운영 기능을 조회할 수 있습니다',
    updated_at = CURRENT_TIMESTAMP
WHERE code = 'TEAM_MEMBER';
