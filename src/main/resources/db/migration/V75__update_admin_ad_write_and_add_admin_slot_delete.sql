-- 1) ADMIN_AD_WRITE 설명 업데이트
UPDATE permission
SET description = '광고, 광고 위치, 광고 스케줄 생성/수정/삭제 (단, 광고 위치 삭제는 불가)',
    updated_at = CURRENT_TIMESTAMP
WHERE key = 'ADMIN_AD_WRITE';

-- 2) ADMIN_SLOT_WRITE → ADMIN_SLOT_DELETE로 키/설명 변경
UPDATE permission
SET key = 'ADMIN_SLOT_DELETE',
    description = '광고 위치 삭제',
    updated_at = CURRENT_TIMESTAMP
WHERE key = 'ADMIN_SLOT_WRITE';